# FoodBridge — CI/CD da Oracle por artefatos aprovados (sem VPN)

FB-OCI-PULL-001 | v1.0.0 | 13/11/2026 | implementação em revisão; produção NÃO atualizada.

## Objetivo e arquitetura

- GitHub Actions compila e testa backend Kotlin e frontend React em runners hospedados separados da VM.
- API: CI já publica `foodbridge-api-jar`. Frontend: o PR correspondente passa a publicar `foodbridge-frontend-dist` após testes, lint e build.
- Oracle Linux consulta resultados pela saída HTTPS 443 usando GitHub CLI (`gh`) e token de **somente leitura**. Não precisa receber SSH dos runners públicos.
- O agente `deploy/oracle/pull-approved-artifact.sh` procura exclusivamente um run com `event=push`, branch `main`, conclusão `success` e SHA igual à HEAD atual de `main`.
- No modo `--check`, baixa/verifica o pacote sem publicar. Em `--apply`, só publica quando `FOODBRIDGE_PULL_DEPLOY_ENABLED=true`.
- O JAR é publicado pelo script existente `atualizar-api.sh` com preflight, backup e rollback. O React é entregue como `dist` ao Nginx, com backup e recuperação se as validações locais falharem.
- Não há Docker em produção conforme arquivos consultados; aqui 'imagem do site' corresponde ao frontend estático, **não** à imagem do sistema operacional da VM.
- As buscas ocorrem por timer systemd a cada aproximadamente 5 minutos, não por push SSH. A publicação pode levar alguns minutos após o CI aprovado.

## Controles de segurança

1. Habilitar proteção da `main` em **ambos** os repositórios e checks obrigatórios antes de auto-publicar. Não publicar PRs não revisados.
2. Criar token GitHub **fine-grained** com acesso somente aos dois repositórios e permissões `Actions: read` e `Contents: read`; não usar o token amplo do desktop.
3. Guardar o token exclusivamente na VM como `/etc/foodbridge/actions-read.token` (`root:opc`, `0640`), sem colá-lo em issue, chat, arquivo versionado ou comando de shell.
4. `opc` executa o agente; o uso de `sudo -n` pelos scripts existentes deve ser revisado antes de publicar. Ideal futuro: conta de deploy separada e sudoers mínimo.
5. O script reconsulta a HEAD da main depois do download e não reutiliza SHA já publicada em `/var/lib/foodbridge-deploy/{api,frontend}.sha`.
6. Nenhuma publicação acontece com `FOODBRIDGE_PULL_DEPLOY_ENABLED=false`. Os workflows antigos de deploy SSH externo também devem permanecer desabilitados.

## Preparação da VM (executar apenas via sessão SSH autenticada)

```bash
id opc
command -v gh || echo FALTA_GITHUB_CLI
command -v sha256sum
command -v flock
sudo -n nginx -t
sudo -n systemctl is-active foodbridge.service
test -d /opt/foodbridge/foodbridge-frontend/foodbridge-frontend/dist
sudo -n test -s /etc/foodbridge/application.properties
```

Se `gh` estiver ausente, instalar conforme o [guia oficial GitHub CLI para Linux](https://github.com/cli/cli/blob/trunk/docs/install_linux.md), revisando comandos e assinaturas. Verificar saída HTTPS e permitir DNS/443.

Após revisar `sudoers` e os caminhos, copiar da branch revisada os scripts `pull-approved-artifact.sh`, `atualizar-api.sh`, `verificar-vm.sh` para `/opt/foodbridge/deploy-agent/` sob propriedade root, somente leitura/execução para `opc`. Instalar os units `foodbridge-pull@.service` e `foodbridge-pull@.timer` em `/etc/systemd/system/`.

```bash
sudo install -d -o root -g opc -m 0750 /opt/foodbridge/deploy-agent
sudo install -d -o opc -g opc -m 0700 /var/lib/foodbridge-deploy
sudo install -d -o root -g opc -m 0750 /etc/foodbridge
sudo install -o root -g opc -m 0640 /dev/null /etc/foodbridge/actions-read.token
printf 'FOODBRIDGE_PULL_DEPLOY_ENABLED=false\n' | sudo tee /etc/foodbridge/pull-deploy.conf >/dev/null
sudo chown root:opc /etc/foodbridge/pull-deploy.conf
sudo chmod 0640 /etc/foodbridge/pull-deploy.conf
```

Para gravar o token sem expô-lo no histórico, entrar com `sudo tee /etc/foodbridge/actions-read.token >/dev/null`, colar o valor no **stdin** e encerrar com Ctrl+D. Nunca imprimir conteúdo do arquivo; conferir somente `sudo stat -c '%U %G %a %n' /etc/foodbridge/actions-read.token`.

## Homologação pré-publicação

```bash
sudo -u opc bash /opt/foodbridge/deploy-agent/pull-approved-artifact.sh api --check
sudo -u opc bash /opt/foodbridge/deploy-agent/pull-approved-artifact.sh frontend --check
```

Exigir `CHECK PASS` e identificar os run IDs reais. Sem CI SUCCESS correspondente à HEAD da main, o agente **não publica nada**. Rever backup do banco, JAR anterior, Nginx, SELinux, política `ddl-auto=validate` e diretórios antes da primeira publicação real. O check não substitui teste de restart e rollback.

## Ativação após sucesso dos testes

```bash
printf 'FOODBRIDGE_PULL_DEPLOY_ENABLED=true\n' | sudo tee /etc/foodbridge/pull-deploy.conf >/dev/null
sudo systemctl daemon-reload
sudo systemctl enable --now foodbridge-pull@api.timer
sudo systemctl enable --now foodbridge-pull@frontend.timer
sudo systemctl start foodbridge-pull@api.service
sudo systemctl start foodbridge-pull@frontend.service
sudo journalctl -u foodbridge-pull@api.service -n 40 --no-pager
sudo journalctl -u foodbridge-pull@frontend.service -n 40 --no-pager
```

**Desligamento:** definir novamente `FOODBRIDGE_PULL_DEPLOY_ENABLED=false` e parar os dois timers. Isso impede próximas publicações, mas não reverte alterações já realizadas.

## Evidência que ainda falta

- PRs backend/frontend mesclados após CI verde.
- Instalação dos scripts e units na VM conferida (não realizada nesta etapa).
- Validação de token read-only sem vazamento, `--check` real de API e frontend.
- Primeiro deploy supervisionado, backup, saúde de API e Nginx e rollback de ensaio.
- Comprovar que um novo merge na main atualiza JAR/`dist` sem SSH iniciado pelo runner.

Referências: [GitHub CLI](https://cli.github.com/manual/), [Actions Artifacts](https://docs.github.com/en/actions/concepts/workflows-and-actions/workflow-artifacts), [issue #8](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/issues/8).
