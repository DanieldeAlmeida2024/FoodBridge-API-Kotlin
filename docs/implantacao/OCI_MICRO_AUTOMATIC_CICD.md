# FoodBridge API — CI/CD automático na Oracle E2.1.Micro

**FB-OCI-CD-MICRO-001 — 13/11/2026**

## Comportamento

- `pull_request -> main`: testa Kotlin/Java 21 contra PostgreSQL descartável; **nunca publica**.
- `push -> main` em código, build ou arquivos de deploy: CI cria JAR e passa por testes; job `Publicar na VM Oracle` executa automaticamente com environment `oracle-production`, **sem variáveis de habilitação** (`OCI_DEPLOY_ENABLED` e `OCI_AUTO_DEPLOY_ENABLED` não são consultadas).
- Mudanças exclusivamente na documentação não acionam deploy; continuam versionadas. `workflow_dispatch deploy=true` é opção suplementar; `preflight=true` continua sem escrita.
- O workflow usa RSA privada e known_hosts no environment GitHub, SHA-256 do artefato do próprio CI, backup do JAR anterior, políticas Hibernate não mutantes (`validate`/`none`) e rollback em erro.
- O teste de capacidade registra RAM total, RAM disponível e swap **sem bloquear a Micro por MemTotal inferior a 1024 MiB**. Alerta quando MemAvailable <128 MiB. Isso remove o limite fisicamente impossível da shape, não a possibilidade de falha operacional.
- Preflight verifica usuário, config externa, systemd e JAR; atraso isolado da consulta de dashboard passa a ser **aviso**. A checagem da versão nova é obrigatória: `GET /health/live` (leve, sem banco), seguido de `GET /dashboards/publico` (integração com PostgreSQL), com tentativas e rollback.
- O deploy e o rollback aguardam até 180 segundos por saúde. Em rollback, o job termina em falha, mesmo se restaurar corretamente a versão anterior. Processamento em concorrência não interrompe outro deploy em andamento.

## Infraestrutura existente

Oracle Linux 9 / x86_64 / `VM.Standard.E2.1.Micro` / host `163.176.133.210`. Após a remoção de `crashkernel`/Kdump pelo operador, o Linux identifica ~945 MiB e há swap de ~2,9 GiB. Java 21, PostgreSQL, Nginx e Docker/AnimeDex compartilham a instância. Nada desta mudança cria outro servidor, altera firewall, apaga banco ou requer Tailscale.

## Risco e como verificar

**O primeiro push com esta configuração também acionará uma publicação real.** Os runs anteriores de API falharam por startup/health na VM quando havia só ~498 MiB visíveis; o artefato antigo foi restaurado e voltou a responder. A recuperação de memória e a checagem leve devem ser avaliadas **no primeiro run real**, não presumidas bem-sucedidas.

Após cada merge, conferir o CI e o job `Publicar na VM Oracle`: a sequência deve registrar JAR do mesmo run, preflight, `DEPLOY_CAPACITY=PASS`, `DEPLOY OK: API respondeu...`, backup e SUCCESS. Caso a versão nova não esteja saudável, verificar logs/rollback e corrigir; **não esconder falhas** nem declarar CD homologado antes de um run real verde.

Os secrets não são versionados. O GitHub environment continua sendo uma dependência de segurança e credenciais — não é uma flag manual para bloquear deploy. Regras de PR/branch protection são recomendadas para evitar código não revisado em produção.

Relatório anterior: `docs/implantacao/INCIDENTE_DEPLOY_API_20261113.md`.
