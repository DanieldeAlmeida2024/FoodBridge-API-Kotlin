# Incidente de primeira publicação API Oracle — 13/11/2026

**FB-OCI-INC-001 · status: serviço recuperado, release nova não homologada, auto-deploy bloqueado.**

## Linha do tempo com evidência

- [Workflow #43430701660](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43430701660), `workflow_dispatch`: CI Kotlin, teste PostgreSQL e geração do JAR passaram; preflight Oracle passou. O script transferiu JAR, gerou backup e tentou reiniciar `foodbridge.service` às 13:08 UTC. O endpoint em loopback `127.0.0.1:8080` recusou conexões repetidamente. O script restaurou o JAR anterior no disco, tentou restart, mas não obteve HTTP no timeout do rollback; job falhou.
- [Workflow #43432008862](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43432008862), segundo `workflow_dispatch`: CI passou, mas o preflight expirou após 20 segundos sem corpo HTTP. **Nenhum JAR foi transferido nessa segunda tentativa**.
- [Auditoria read-only #43434109746](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43434109746), 13:30 UTC: `foodbridge.service=active`, `HTTP_8080=200` (~10s), PostgreSQL pronto e Nginx/AnimeDex HTTP 200. `MemAvailable` ~207 MiB e disk 62%; backup `foodbridge-20261113T130852Z-38a0918de9f2.jar` presente.
- [Auditoria complementar read-only #43434923053](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43434923053), 13:35 UTC: `MemTotal=510616 KiB` (~499 MiB), `MemAvailable=207712 KiB` (~203 MiB), `SwapTotal=3064824 KiB` (~2,9 GiB), `SwapFree=2266048 KiB` (~2,16 GiB). Aproximadamente **780 MiB de swap utilizados**. `KERNEL_OOM_SIGNALS=0` na janela consultada; não é evidência definitiva contra pressão de memória.
- O processo ativo atual iniciou às **13:11:12 UTC**, no período de rollback. `CURRENT_JAR_SHA256` = `LATEST_BACKUP_SHA256` = `8b61592f7f0c783307ed2a0e3799f6cf39066bb77c83923ba18202da5d2083fa`. **Rollback do artefato confirmado por checksum**; serviço saudável posteriormente.
- `JOURNAL_ERRORS_BY_CATEGORY=NONE_MATCHED` nos padrões consultados. Ausência de padrões não significa ausência de falhas em outros logs. A causa exata do startup malsucedido **não foi provada**.

## Análise

A instância tem apenas aproximadamente 512 MB de RAM física e executa PostgreSQL, Java/Spring Boot 4, Nginx e Docker/Puppeteer; swap já estava muito utilizada. Esta é uma hipótese forte para startup lento e indisponibilidade no restart, mas precisamos de métricas de pico/telemetria detalhada para demonstrar causalidade.

O `systemd` mostra `TimeoutStartUSec=1min30s` e `TimeoutStopUSec=1min30s`, mas o script de atualização usa sua própria janela de 30 verificações com sleeps. O dashboard de saúde é uma **consulta funcional pesada** (~8–10s). O preflight não deve depender apenas desse endpoint para estimar liveness; uma futura versão deverá acrescentar `/health/live` leve, sem comprometer a verificação do banco.

## Recuperação de memória via boot Oracle Linux 9 (13/11/2026)

O operador informou que sua instância é **VM.Standard.E2.1.Micro (x86_64)**. A VM apresentava somente **498 MiB** de RAM visível apesar de ter 1 GB nominal. Diagnóstico de leitura confirmou:

```text
crashkernel=1G-64G:448M,64G-:512M
sudo kdumpctl estimate => Reserved crashkernel: 448M; Recommended: 184M
systemctl is-active kdump => active
```

O operador **deliberadamente** executou no host (fora do GitHub Actions) `sudo systemctl disable --now kdump`, `sudo grubby --update-kernel=ALL --remove-args=crashkernel`, conferiu `grubby --info=DEFAULT` sem o argumento e efetuou reboot planejado. Após alguns erros de SSH transitórios esperados durante a inicialização, o SSH voltou a responder. Resultado fornecido pelo operador:

| Métrica | Antes | Após reboot |
| --- | --- | --- |
| RAM visível (`free -h`) | 498 MiB | **945 MiB** |
| RAM disponível (`MemAvailable`) | 226 MiB | **355 MiB** |
| Swap usado | 782 MiB | **48 MiB** |

**Recuperação de RAM visível: +447 MiB**. A redução de swap pode refletir o reboot; não comprova por si só estabilidade sob carga. A mudança retira a reserva de memória para dumps do kernel; em caso de falha grave do kernel, não haverá dump de Kdump. O risco foi aceito pelo operador para privilegiar disponibilidade da aplicação nesta shape pequena.

O workflow read-only de diagnóstico passa a verificar `CRASHKERNEL_ARG_PRESENT` e `KDUMP_SERVICE_STATUS`, juntamente com HTTP local, PostgreSQL e memória. **Não altera a configuração de boot nem reinicia serviços.**

**Gate de deploy ainda bloqueante:** `deploy/oracle/verificar-recursos-deploy.sh` exige `MemTotal >= 1048576 KiB` (1024 MiB), mas a instância agora tem somente 945 MiB visíveis. Não reduzir esse limite automaticamente ou publicar novo JAR somente porque o reboot recuperou memória. Validar capacidade após estabilização da carga, saúde dos quatro serviços e uma estratégia de deploy de baixo risco antes de reavaliar o gate.

### Auditoria pós-reboot com serviços ativos — 13/11/2026 11:12 BRT

O PR [#26](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/26) foi aprovado no CI e mesclado, acrescentando a checagem read-only de reserva de crashkernel. O workflow [#43440493532](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43440493532) terminou **SUCCESS**, sem nenhum restart ou alteração na VM:

```text
CRASHKERNEL_ARG_PRESENT=NO
KDUMP_SERVICE_STATUS=inactive
UNIT_foodbridge_service=active
UNIT_nginx=active
UNIT_docker=active
HTTP_80_CURL_EXIT=0 HTTP_80_STATUS=200
HTTP_8080_CURL_EXIT=0 HTTP_8080_STATUS=200
HTTP_8787_CURL_EXIT=0 HTTP_8787_STATUS=200
POSTGRES_TCP_READY_EXIT=0
MEMINFO_MemTotal_KB=968600
MEMINFO_MemAvailable_KB=222988
MEMINFO_SwapTotal_KB=3064824
MEMINFO_SwapFree_KB=2787272
KERNEL_OOM_SIGNALS=0
CURRENT_JAR_SHA256=8b61592f7f0c783307ed2a0e3799f6cf39066bb77c83923ba18202da5d2083fa
LATEST_BACKUP_SHA256=8b61592f7f0c783307ed2a0e3799f6cf39066bb77c83923ba18202da5d2083fa
INCIDENT_DIAGNOSTIC_FINISHED=YES (somente leitura)
```

**Interpretação:** memória total utilizável ~946 MiB; memória disponível sob carga ~218 MiB; swap usado ~271 MiB. Após reboot, os serviços foram recuperados, mas continuamos abaixo dos dois limiares do gate de publicação (`MemTotal >= 1024 MiB`, `MemAvailable >= 256 MiB`). Não tentar novo deploy manual ou automático antes de reavaliar a capacidade e a estabilidade do serviço. A queda de swap no primeiro `free -h` pós-reboot não foi sustentada após a carga voltar.

### Verificação manual de processos e swap — 13/11/2026 11h31 BRT

Operador confirmou após o reboot: `MemTotal=945 MiB`, `MemAvailable=312 MiB` e **400 MiB de swap ocupada**. A ordem por RSS encontrou `java ~184 MiB` (maior processo), `python3 ~42 MiB`, agente Oracle ~41 MiB, `dockerd ~28 MiB`, `node ~21 MiB` e postgres master ~16 MiB. RSS por processo não captura sozinho a pressão da memória compartilhada ou o pico do Chromium.

No `vmstat 1 10`, a primeira linha foi **média desde boot** (`si=11, so=76`), e os nove intervalos seguintes mostraram `si=0`, `so=0`, CPU `id=99–100%` e `wa=0%` sob carga ociosa. O container AnimeDex estava sem navegação ativa: `18,78 MiB` de memória Docker no snapshot. O grupo systemd `foodbridge.service` reportou `MemoryCurrent=140705792` bytes (~134 MiB) e `MemoryPeak=339656704` bytes (~324 MiB); esses indicadores não são equivalentes ao RSS do processo Java.

**Limitação:** comando remoto de `cat /proc/pressure/memory` foi concatenado incorretamente pelo shell PowerShell/SSH, então PSI não foi medido nessa amostra. O workflow de diagnóstico read-only passa a coletar `PSI some/full avg10`, estatísticas de swap de **cinco intervalos atuais**, eliminando a primeira média de boot, e memória do container. Nenhum teste foi realizado com Chromium sob uso real ou durante restart da API; não é seguro concluir capacidade de publicação a partir do repouso. **Gate conservador segue ativo.**

### Auditoria remota de pressão de memória e troca ativa — 13/11/2026 12h06 BRT

O [PR #27](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/27) passou no CI e foi integrado à main. A auditoria read-only [#43449198044](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43449198044) terminou **SUCCESS**, confirmando saúde do conjunto:

```text
UNIT_foodbridge_service=active
UNIT_nginx=active
UNIT_docker=active
HTTP_80_STATUS=200
HTTP_8080_STATUS=200
HTTP_8787_STATUS=200
POSTGRES_TCP_READY_EXIT=0
MEMINFO_MemTotal_KB=968600
MEMINFO_MemAvailable_KB=372472
MEMINFO_SwapTotal_KB=3064824
MEMINFO_SwapFree_KB=2573760
MEMORY_PSI_STATUS=UNAVAILABLE
VMSTAT_INTERVALS=5
VMSTAT_SWAP_ACTIVE_INTERVALS=1
VMSTAT_MAX_SWAPIN_KIBPS=232
VMSTAT_MAX_SWAPOUT_KIBPS=0
VMSTAT_MIN_CPU_IDLE_PCT=88
VMSTAT_MAX_IOWAIT_PCT=2
DOCKER_CONTAINER=animedex-scraper DOCKER_MEMORY=21.62MiB / 945.9MiB
KERNEL_OOM_SIGNALS=0
```

RAM disponível ~364 MiB e swap ocupada ~480 MiB. **Um dos cinco intervalos registrou entrada de swap de até 232 KiB/s**, sem saída de swap e com espera de I/O de no máximo 2%. Portanto, não se observou `thrashing` contínuo no período, mas não é correto afirmar troca de swap completamente ausente. PSI em `/proc/pressure/memory` apareceu **UNAVAILABLE** na VM e não pode ser usado como critério de publicação sem investigação adicional. O uso do container foi medido sem carga ativa do Chromium. Não houve ensaio de restart nem de carga simultânea; gate de release continua bloqueando publicação nesta Micro.

## Mitigação versionada

- `deploy/oracle/verificar-recursos-deploy.sh` é um **gate somente leitura** que bloqueia a transferência e o restart quando `MemTotal < 1 GiB` ou `MemAvailable < 256 MiB`. O limiar é um mínimo conservador, não um dimensionamento recomendado. Priorizar ao menos **2 GiB de RAM real**, preferencialmente mais se Chrome/Puppeteer e banco permanecerem na mesma máquina.
- `.github/workflows/foodbridge-ci-cd.yml` exige esse gate após preflight e **antes** do passo `Transferir JAR e aplicar com rollback`. Falha do gate impede novo rollout; não afeta serviço já em execução. `tests/ci/test_oracle_release_capacity_gate_contract.py` protege a ordem e o comportamento.
- `OCI_AUTO_DEPLOY_ENABLED=false` deve continuar desligado. Não repetir `deploy=true` antes de dimensionamento, confirmação de backup recuperável e revisão da saúde de inicialização. Não reiniciar PostgreSQL, não executar pg_restore nem modificar RAM/swap automaticamente.

## Correção complementar do rollback

O script `deploy/oracle/atualizar-api.sh` usava uma única chamada de `saudavel` após reiniciar o JAR anterior; uma conexão recusada durante startup gerava o relato **enganoso** de que o rollback não havia recuperado o serviço. O diagnóstico comprovou que a versão antiga voltou a responder posteriormente e que seu SHA-256 confere com o backup.

A correção reutiliza `aguardar_saude`, com janela limitada a 180 segundos **tanto para a versão nova quanto para o rollback**. Após copiar o backup, aguarda health efetivo em vez de concluir indisponibilidade imediatamente. O job continua falhando quando precisar reverter, mesmo que o rollback recupere a saúde; não pode ser contado como deploy aprovado. O gate de RAM introduzido anteriormente bloqueia a publicação na VM atual, antes do restart.

**Esta alteração de script não reinicia serviços ao ser mesclada**; somente muda o comportamento de uma publicação futura que ultrapasse os gates. A melhoria não substitui um endpoint liveness leve nem resolve a limitação de RAM.
## Próximos critérios de liberação

1. Ajustar tamanho da VM ou distribuir serviços; confirmar `MemTotal >= 1GiB` e `MemAvailable >= 256MiB` pelo gate, com recomendação operacional >=2 GiB.
2. Confirmar disponibilidade estável do endpoint da API, PostgreSQL e Nginx por mais de uma sondagem. Implementar liveness leve e readiness funcional distintos em revisão testada.
3. Validar que o **novo** JAR não possui falha de schema/configuração em ambiente semelhante ao da produção; `ddl-auto=validate` não substitui ensaio de upgrade.
4. Somente depois executar um **único** novo deploy manual com backup, health e rollback. Não habilitar auto-deploy até esse ensaio passar.

**Escopo desta revisão:** GitHub Actions e documentação. Não houve acesso mutante a serviço, JAR, frontend, container ou PostgreSQL.
