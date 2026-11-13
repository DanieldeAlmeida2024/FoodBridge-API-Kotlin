# FoodBridge — Plano de testes do pipeline Oracle

**FB-OCI-TST-001 v1.2.0 proposta · 12/11/2026**  
Escopo: Java 21 + PostgreSQL CI descartável + inspeção remota *sem deploy*. Testes E2E de VM e rollback são separados.

| ID | Ambiente | Passo / entrada | Critério de aceite | Estado |
|---|---|---|---|---|
| OCI-01 | Runner PR | ./gradlew clean test bootJar | CI verde com PostgreSQL 16 descartável; JAR artifact publicado | Executado anteriormente na main (ver runs abaixo); revalidar PR |
| OCI-02 | Runner PR | bash -n dos três scripts Oracle | Sem erro de sintaxe | Automatizado nesta PR; aguarda run final |
| OCI-03 | Runner PR | Python unittest tests/ci | Gates de SSH, usuário, host key, JAR, rollback e DDL analisados | Automatizado nesta PR; aguarda run final |
| OCI-04 | Actions YAML | Push para main, OCI_AUTO_DEPLOY_ENABLED ausente/false | Job deploy ignorado; nunca publica por erro de flag | Contrato estático; E2E pendente |
| OCI-05 | Actions YAML | preflight=true, deploy=false | Somente checks remotos; sem SCP, restart, DB ou escrita | E2E pendente |
| OCI-06 | GitHub Secrets/Vars | Falta chave, user, host ou known_hosts | Falha explícita antes de qualquer SSH/deploy | E2E pendente |
| OCI-07 | SSH | Chave host incorreta/fingerprint não confiável | SSH falha com StrictHostKeyChecking=yes | E2E pendente |
| OCI-08 | VM Oracle | foodbridge.service não existe ou User != opc | Preflight falha sem modificação | E2E pendente |
| OCI-09 | VM Oracle | JAR do systemd diferente do JAR de destino | Preflight e update falham antes de copiar | E2E pendente |
| OCI-10 | VM Oracle | CONFIG inexistente/ilegível ou drop-in ausente | Preflight falha sem imprimir secrets | E2E pendente |
| OCI-11 | VM Oracle | ddl-auto=update/create (ou override incompatível) | Falha antes de restart/alteração de banco | E2E pendente |
| OCI-12 | VM Oracle | API desligada ou /dashboards/publico != HTTP 2xx | Falha com baseline anterior indisponível | E2E pendente |
| OCI-13 | VM homologação autorizada | deploy manual com artifact aprovado + checksum | Backup, troca do JAR, restart e health retornam sucesso | Pendente, NÃO executar no banco de produção sem preparação |
| OCI-14 | VM homologação autorizada | injetar falha controlada no health | Rollback automático restaura JAR anterior e serviço | Pendente, NÃO provocar indisponibilidade em produção |
| OCI-15 | VM Oracle | ambiente com variáveis secretas e SQL reais | Sem vazamento em logs; teste por auditor independente | Pendente |
| OCI-16 | Operação | CI e deploy simultâneos da mesma branch | Nunca cancelar deploy em andamento | Contrato estático, E2E pendente |
| OCI-17 | Simulação do preflight | Arquivo de configuração com duas chaves `spring.jpa.hibernate.ddl-auto` (validate + update) | Recusar propriedades duplicadas antes de qualquer cópia ou restart | Automatizado em teste com comandos simulados; VM real pendente |

### Evidências já existentes na main

- [Actions #43038670928](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43038670928): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.
- [Actions #43032061904](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43032061904): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.
- [Actions #43031465973](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43031465973): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.

Esses três runs **não comprovam SSH, deploy, health remoto nem rollback**. As etapas OCI-05..OCI-15 dependem de variáveis/secrets e acesso SSH verificado. Não simular PASS com comandos de inspeção de código.

### Reprodução local sem tocar na VM

```bash
bash -n deploy/oracle/preparar-vm.sh
bash -n deploy/oracle/verificar-vm.sh
bash -n deploy/oracle/atualizar-api.sh
python3 -m unittest discover -s tests/ci -p 'test_*_contract.py' -v
```

Nunca executar testes de failover sobre o PostgreSQL real. Registrar run, SHA, resultado e contexto de execução por ID, sem capturar valores de configuração de produção.

### Resultado verificado do PR (sem VM)

[Actions #43235395978](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43235395978) no SHA `5da02052170e12f888e00d279b80594c44f4ab90`: **CI SUCCESS**, Gradle Kotlin/Java 21 e PostgreSQL descartável, sintaxe Bash, contrato GitHub Actions e sete casos de preflight remoto simulado. **Não marcar OCI-05..OCI-15 PASS:** jobs Oracle reais não executaram (SKIPPED) nem houve SSH, JAR substituído, Nginx, banco ou serviço reiniciado.

### Expansão da proteção de propriedades duplicadas

A versão proposta agora usa `awk` de forma somente leitura para validar **exatamente uma** definição de `spring.jpa.hibernate.ddl-auto` no arquivo externo, com valor `validate` ou `none`. Um segundo valor `update` no mesmo arquivo deve bloquear a publicação. O teste `test_rejects_duplicate_ddl_auto_properties` foi adicionado à suíte de preflight simulado. O comportamento real na VM continua pendente, mesmo com o CI verde.

## Integração funcional FB-INT-001 — 13/11/2026

Branch de integração baseada em main `32e13fdd8c2b3c9fd4784ccc7e77fdd2df462224`.
Consolida o código dos PRs #4 (organizações), #5 (voluntários), #6 (estoque),
#9 (timeout de 20 s) e #10 (validação no clone), preservando suas suítes.
O conflito funcional da coleta foi resolvido com `findByIdForColeta`, sem liberar acesso global a voluntários.

Correções adicionais: revalidação de usuário/vínculo/organização em cada JWT autenticado;
bloqueio de contas suspensas inclusive ADMIN; vínculo real na resposta de seleção;
validação do enum de organização; transações de onboarding/admin/exclusão;
criação concorrente de requisições sob o mesmo lock da doação;
recusa de quantidades não finitas e doações expiradas; voluntário ativo novamente conferido na conclusão;
JWT inválido → 401 e método HTTP incorreto → 405, sem revelar credenciais.

`FluxoFuncionalHttpIntegrationTest` acrescenta sete testes HTTP/JWT/PostgreSQL:
login/seleção, vínculo revogado, ADMIN suspenso, cadastro organizacional sem autoaprovação,
ciclo doação → solicitação → aprovação → voluntário → coleta → indicadores,
método incorreto e JWT malformado. **Novos testes pendentes da execução do CI nesta revisão.**

Esta integração não comprova os 165 cenários planejados, o navegador real,
a prontidão dos papéis de gestor, o SSH do runner, deploy ou rollback.
A entrega total exige fechar a matriz [ENTREGA_FUNCIONAL](../projeto/ENTREGA_FUNCIONAL.md)
com evidência por módulo. Produção, serviço, Nginx e banco original não foram alterados.

### USR-10 / AUT-15 — Rota de exclusao de perfil e restricao ADMIN (13/11/2026)

- Falha inicial [Actions #43339468251](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43339468251): teste de exclusao via `DELETE /usuarios/me` obteve HTTP 403 em vez de 204 devido a matcher ADMIN generico `/usuarios/*`.
- A regra exata `DELETE /usuarios/me` foi adicionada **antes** da regra de administrador, exigindo `TOKEN_ACCESS`. Teste existente exige exclusao de conta/vinculos, organizacao preservada e token antigo rejeitado.
- Novo teste negativo garante que usuario comum receba 403 ao tentar `DELETE /usuarios/{id}` de outra conta.
- Execucao de CI da revisao posterior: PENDENTE. Nenhum teste de delete foi feito contra o banco original.
