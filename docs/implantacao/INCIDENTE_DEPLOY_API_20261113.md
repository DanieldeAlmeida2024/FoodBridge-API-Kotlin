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

## Mitigação versionada

- `deploy/oracle/verificar-recursos-deploy.sh` é um **gate somente leitura** que bloqueia a transferência e o restart quando `MemTotal < 1 GiB` ou `MemAvailable < 256 MiB`. O limiar é um mínimo conservador, não um dimensionamento recomendado. Priorizar ao menos **2 GiB de RAM real**, preferencialmente mais se Chrome/Puppeteer e banco permanecerem na mesma máquina.
- `.github/workflows/foodbridge-ci-cd.yml` exige esse gate após preflight e **antes** do passo `Transferir JAR e aplicar com rollback`. Falha do gate impede novo rollout; não afeta serviço já em execução. `tests/ci/test_oracle_release_capacity_gate_contract.py` protege a ordem e o comportamento.
- `OCI_AUTO_DEPLOY_ENABLED=false` deve continuar desligado. Não repetir `deploy=true` antes de dimensionamento, confirmação de backup recuperável e revisão da saúde de inicialização. Não reiniciar PostgreSQL, não executar pg_restore nem modificar RAM/swap automaticamente.

## Próximos critérios de liberação

1. Ajustar tamanho da VM ou distribuir serviços; confirmar `MemTotal >= 1GiB` e `MemAvailable >= 256MiB` pelo gate, com recomendação operacional >=2 GiB.
2. Confirmar disponibilidade estável do endpoint da API, PostgreSQL e Nginx por mais de uma sondagem. Implementar liveness leve e readiness funcional distintos em revisão testada.
3. Validar que o **novo** JAR não possui falha de schema/configuração em ambiente semelhante ao da produção; `ddl-auto=validate` não substitui ensaio de upgrade.
4. Somente depois executar um **único** novo deploy manual com backup, health e rollback. Não habilitar auto-deploy até esse ensaio passar.

**Escopo desta revisão:** GitHub Actions e documentação. Não houve acesso mutante a serviço, JAR, frontend, container ou PostgreSQL.
