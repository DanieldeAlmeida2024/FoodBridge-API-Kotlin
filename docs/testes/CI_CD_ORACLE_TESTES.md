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

### Evidências já existentes na main

- [Actions #43038670928](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43038670928): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.
- [Actions #43032061904](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43032061904): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.
- [Actions #43031465973](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43031465973): build-e-testes **SUCCESS**, deploy-oracle **SKIPPED**.

Esses três runs **não comprovam SSH, deploy, health remoto nem rollback**. As etapas OCI-05..OCI-15 dependem de variáveis/secrets e acesso SSH verificado. Não simular PASS com comandos de inspeção de código.

### Reprodução local sem tocar na VM

\`\`\`bash
bash -n deploy/oracle/preparar-vm.sh
bash -n deploy/oracle/verificar-vm.sh
bash -n deploy/oracle/atualizar-api.sh
python3 -m unittest discover -s tests/ci -p 'test_*_contract.py' -v
\`\`\`

Nunca executar testes de failover sobre o PostgreSQL real. Registrar run, SHA, resultado e contexto de execução por ID, sem capturar valores de configuração de produção.
