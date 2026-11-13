# FoodBridge — gate de prontidão para o primeiro deploy SSH direto

**FB-OCI-SSH-READY-001 | 13/11/2026 | verificação read-only (não é publicação).**

## Contexto operacional

- API e frontend possuem workflows CI/CD diretos por SSH na main, com gates manuais e automáticos inicialmente desabilitados.
- [Preflight API no runner GitHub](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573) comprovou `SSH_RSA_OK`, `SSH_EXIT=0` e `PREFLIGHT ORACLE: PASS`.
- [Diagnóstico de rede e serviços](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43419709855) confirmou Nginx HTTP 200, worker AnimeDex HTTP 200, serviços ativos e ausência de listener HTTPS 443 na inspeção.
- Usuário confirmou que voltou a conseguir acessar os serviços HTTP; incidente de rede não impede avançar na homologação do deploy.

## Por que um segundo gate?

O preflight inicial verifica saúde geral, mas o script que troca o JAR exige também o marcador systemd `/etc/systemd/system/foodbridge.service.d/90-github-deploy.conf`. E o frontend precisa que `opc` possa criar staging, mover `dist`, executar `sudo -n nginx -t` e `restorecon`. Esses pré-requisitos de publicação não ficam demonstrados só pelo HTTP 200.

## Execução automática sem escrita

O workflow `.github/workflows/oracle-deploy-readiness.yml` executa após sua integração na `main` e também por `workflow_dispatch`. Usa a mesma chave e host key pinada de `oracle-production`, envia por SSH o script `deploy/oracle/verificar-predeploy-completo.sh` e verifica os pré-requisitos sem modificar a máquina.

Comprova:

- `API_GITHUB_DEPLOY_MARKER`, JAR atual, `foodbridge.service`, configuração externa legível e GET local `/dashboards/publico`;
- Nginx ativo e configuração válida, `dist/index.html` presente, pai do frontend gravável por `opc`, ferramentas de tar/Python/SELinux;
- worker do AnimeDex em verificação informativa, sem criar dependência operacional entre os produtos.

Relata `DEPLOY_READINESS=PASS` ou `DEPLOY_READINESS=BLOCKED` com rótulos de falha, **sem imprimir variáveis de ambiente, senhas, arquivos de configuração nem chaves**. O workflow não usa SCP, não modifica dados, não reinicia serviços.

## Resultado real — 13/11/2026 09:18 BRT

Execução de [GitHub Actions #43424042926](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43424042926), job `131880867329`: **SUCCESS**, SSH via environment `oracle-production`, executou auditoria remota **somente leitura**.

```text
API_SUDO_NONINTERACTIVE=PASS
API_SYSTEMD_ACTIVE=PASS
API_EXTERNAL_CONFIG_PRESENT=PASS
API_GITHUB_DEPLOY_MARKER=PASS
API_OLD_JAR_PRESENT=PASS
API_HTTP_HEALTH=PASS
API_SERVICE_USER=PASS
WEB_NGINX_ACTIVE=PASS
WEB_NGINX_CONFIG=PASS
WEB_DIST_PRESENT=PASS
WEB_PARENT_WRITABLE=PASS
WEB_TOOLS_COMMANDS=PASS
WEB_RESTORECON_SUDO=PASS
ANIMEDEX_WORKER_HEALTH=PASS (informativo, nao bloqueia o deploy FoodBridge)
DEPLOY_READINESS_FAILURES=0
DEPLOY_READINESS=PASS (somente leitura; nao comprova o primeiro deploy real)
```

**Condição liberada:** pré-requisitos do host para publicação manual estão presentes. **Não liberado automaticamente:** secrets do repositório frontend, primeira troca real do JAR/`dist`, restart e rollback de produção, ativação de auto-deploy. Os dois gates `OCI_DEPLOY_ENABLED` e `OCI_AUTO_DEPLOY_ENABLED` continuam intencionalmente desligados até homologação de execução real.

## Próximos gates de publicação após o resultado

1. Corrigir somente os pré-requisitos que retornarem FAIL, preservar owner e SELinux com mudança mínima após inspecionar estado real, e repetir o readiness até PASS.
2. Configurar **separadamente** o environment do repositório frontend (`OCI_SSH_*`) com o script Windows versionado `deploy/oracle/preparar-secrets-ssh-windows.ps1` do frontend. Os secrets do backend não migram automaticamente para outro repositório.
3. Executar o preflight frontend `oracle-frontend-preflight.yml` e exigir `FRONTEND ORACLE PREFLIGHT: PASS`.
4. Executar o primeiro deploy da API com `OCI_DEPLOY_ENABLED=true`, `OCI_AUTO_DEPLOY_ENABLED=false` e dispatch manual com `deploy=true` e `preflight=false`, após confirmar backup do banco e compatibilidade da versão. Verificar rollback e HTTP.
5. Executar primeiro deploy manual do frontend com gates equivalentes e validar domínio e build publicado.
6. Só depois ativar `OCI_AUTO_DEPLOY_ENABLED=true` em cada repositório, com branch protection e revisão adequadas.

**Não** usar a alternativa de agente pull/cron, Tailscale ou abrir o banco PostgreSQL à internet. Portas 22, 80 e 8787 são tratadas separadamente no firewall; nenhuma alteração de rede faz parte deste gate.
