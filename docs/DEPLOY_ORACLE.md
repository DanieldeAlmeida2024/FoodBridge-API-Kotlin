# CI/CD FoodBridge API → Oracle Cloud

## Ambiente existente (não recriar)

- Oracle Linux 9; acesso via SSH ao usuário `opc`.
- Aplicação: `/opt/foodbridge/FoodBridge-API-Kotlin`.
- Serviço systemd: `foodbridge.service`; backend atende `127.0.0.1:8080`.
- Banco PostgreSQL local existente: `foodbridge`; **não criar, apagar ou migrar automaticamente**.
- Nginx continua encaminhando as rotas da API para `127.0.0.1:8080`.
- Java 21; artefato: `build/libs/foodbridge-0.0.1-SNAPSHOT.jar`.

## Como funciona

1. PR para `main`: Actions compila e roda `./gradlew clean test bootJar` com Java 21.
2. Push na `main`: repete o CI e salva o JAR.
3. Deploy **apenas quando** a variável `OCI_DEPLOY_ENABLED=true` estiver configurada.
4. A job `deploy-oracle` obtém **o mesmo JAR aprovado** pela job de testes.
5. SSH verifica a identidade do host via `known_hosts`, transfere o JAR e executa `deploy/oracle/atualizar-api.sh`.
6. A VM guarda backup do JAR, faz substituição atômica, reinicia `foodbridge.service`, testa `GET http://127.0.0.1:8080/dashboards/publico` e faz rollback se falhar.

**Nenhum arquivo `application.properties`, banco PostgreSQL ou configuração Nginx é substituído pelo pipeline.**

## Pré-requisito na VM (uma vez, antes de habilitar deploy)

Conecte usando a chave SSH **existente**, sem copiá-la para o Git:

```powershell
ssh -i C:\foodbridge\ssh-oracle.key opc@163.176.133.210
```

Na VM, confira a versão atual e os dados em disco sem mostrar os segredos:

```bash
sudo systemctl status foodbridge.service --no-pager
sudo systemctl cat foodbridge.service
test -s /opt/foodbridge/FoodBridge-API-Kotlin/src/main/resources/application.properties && echo "Configuração local disponível"
```

A partir do checkout atualizado na VM, execute:

```bash
cd /opt/foodbridge/FoodBridge-API-Kotlin
sudo -n true
bash deploy/oracle/preparar-vm.sh
sudo systemctl cat foodbridge.service
sudo test -s /etc/foodbridge/application.properties && echo "Configuração externa encontrada"
```

O script copia a configuração local existente para `/etc/foodbridge/application.properties` (dono `root`, grupo `opc`, modo `0640`; diretório `0750`), se ainda não existir, e registra `SPRING_CONFIG_ADDITIONAL_LOCATION` no systemd. Ele **não reinicia** o serviço, e não expõe senhas no GitHub.

Se o arquivo local de origem não existir, configure `/etc/foodbridge/application.properties` manualmente e repita a preparação. Antes do primeiro deploy, revise `spring.jpa.hibernate.ddl-auto`: em produção prefira migrações e `validate`, após checar o esquema real. Faça backup do PostgreSQL **separadamente** antes de alterações de banco.

Não rode `git reset --hard` ou `git pull` automaticamente no diretório da VM se houver alterações manuais; o pipeline atualiza **somente o JAR**.

## Chave de implantação dedicada

Crie uma chave ED25519 exclusiva para o Actions na sua máquina (não use sua chave administrativa):

```powershell
ssh-keygen -t ed25519 -C "foodbridge-gh-actions" -f "$HOME\.ssh\foodbridge-gh-actions"
```

Adicione `foodbridge-gh-actions.pub` a `~opc/.ssh/authorized_keys` na VM, sem remover a chave existente. Não compartilhe a chave privada no chat ou em arquivos do repositório.

Confirme a impressão digital da chave SSH do **servidor**, através da sua conexão confiável:

```bash
sudo ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Obtenha a entrada `known_hosts` com `ssh-keyscan -t ed25519 163.176.133.210` **e compare o fingerprint** do resultado com o do servidor antes de cadastrar o valor. Não desative `StrictHostKeyChecking`.

## GitHub: Secrets e variables

Em `Settings → Secrets and variables → Actions` do repositório, crie:

| Tipo | Nome | Valor |
|---|---|---|
| **Secret** | `OCI_SSH_PRIVATE_KEY` | Conteúdo integral da chave privada ED25519 **dedicada** |
| **Secret** | `OCI_SSH_KNOWN_HOSTS` | Entrada ED25519 do host, após validar fingerprint |
| **Variable** | `OCI_SSH_HOST` | `163.176.133.210` |
| **Variable** | `OCI_SSH_USER` | `opc` |
| **Variable** | `OCI_DEPLOY_ENABLED` | Comece com `false`; mude para `true` após pré-configuração |

A job usa o environment `oracle-production`; você pode adicionar proteção de aprovação para implantação nessa área do GitHub.

**Conectividade:** o runner do GitHub precisa alcançar o SSH da VM. Restrinja o acesso à origem de confiança; use rede privada ou um túnel seguro se o firewall bloquear runners externos. Não abra indiscriminadamente o SSH à internet para resolver falhas de conexão.

**Segurança:** o usuário `opc` normalmente tem `sudo` amplo em Oracle Linux. Uma conta de deploy com privilégios mínimos é preferível em produção. O workflow nunca precisa de usuário/senha do PostgreSQL nem JWT, pois o arquivo de configuração já está na VM.

## Primeiro teste e diagnóstico

1. Verifique CI verde na aba **Actions**.
2. Rode `deploy/oracle/preparar-vm.sh` na VM após revisar os arquivos.
3. Configure chave SSH e host verificado no GitHub.
4. Ative a variable `OCI_DEPLOY_ENABLED=true`. A partir daí, um novo push na `main` executa CI e deploy.
5. Para uma implantação inicial **sem novo commit**, use `Actions → FoodBridge API - CI e deploy Oracle → Run workflow`, selecione `main` e marque o parâmetro `deploy=true`.
6. Acompanhe `GitHub → Actions → FoodBridge API - CI e deploy Oracle`.

Na VM, somente leitura:

```bash
sudo systemctl status foodbridge.service --no-pager
sudo journalctl -u foodbridge.service -n 80 --no-pager
curl --fail --silent --show-error --max-time 5 -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/dashboards/publico
```

**Rollback:** `deploy/oracle/atualizar-api.sh` restaura automaticamente o JAR anterior se o endpoint público não responder. Os backups ficam em `/opt/foodbridge/FoodBridge-API-Kotlin/deploy-backups`.

**Limite de validação:** esta configuração não significa que o deploy remoto foi executado. A primeira publicação requer os segredos, conectividade e preparação da VM, que não podem ser verificados apenas pela API de leitura/escrita de repositórios.
