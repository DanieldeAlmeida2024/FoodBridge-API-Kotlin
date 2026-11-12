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
3. Deploy **manual** somente quando `OCI_DEPLOY_ENABLED=true`; deploy **automático** após push requer também `OCI_AUTO_DEPLOY_ENABLED=true`. Para primeiro acesso, usar `workflow_dispatch preflight=true` antes de autorizar publicação.
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
sudo systemctl cat foodbridge.service >/dev/null && echo 'Unit systemd encontrada (conteúdo não exibido)'
test -s /opt/foodbridge/FoodBridge-API-Kotlin/src/main/resources/application.properties && echo "Configuração local disponível"
```

A partir do checkout atualizado na VM, execute:

```bash
cd /opt/foodbridge/FoodBridge-API-Kotlin
sudo -n true
bash deploy/oracle/preparar-vm.sh
sudo systemctl cat foodbridge.service >/dev/null && echo 'Unit systemd encontrada (conteúdo não exibido)'
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
| **Variable** | `OCI_DEPLOY_ENABLED` | Comece com `false`; mude para `true` apenas para deploy manual após preflight e revisão |
| **Variable** | `OCI_AUTO_DEPLOY_ENABLED` | Comece com `false`; somente `true` junto a OCI_DEPLOY_ENABLED ativa deploy em push |

A job usa o environment `oracle-production`; você pode adicionar proteção de aprovação para implantação nessa área do GitHub.

**Conectividade:** o runner do GitHub precisa alcançar o SSH da VM. Restrinja o acesso à origem de confiança; use rede privada ou um túnel seguro se o firewall bloquear runners externos. Não abra indiscriminadamente o SSH à internet para resolver falhas de conexão.

**Segurança:** o usuário `opc` normalmente tem `sudo` amplo em Oracle Linux. Uma conta de deploy com privilégios mínimos é preferível em produção. O workflow nunca precisa de usuário/senha do PostgreSQL nem JWT, pois o arquivo de configuração já está na VM.

## Primeiro teste e diagnóstico

1. Verifique CI verde na aba **Actions**.
2. Rode `deploy/oracle/preparar-vm.sh` na VM após revisar os arquivos.
3. Configure chave SSH e host verificado no GitHub.
4. Faça primeiro `workflow_dispatch` com `preflight=true` e `deploy=false` para verificar SSH/serviço **sem implantação**. Após aprovação de segurança, ative `OCI_DEPLOY_ENABLED=true` para permitir deploy manual. Push na `main` só implanta se `OCI_AUTO_DEPLOY_ENABLED=true` também estiver explicitamente definido.
5. Para uma implantação inicial **sem novo commit**, use `Actions → FoodBridge API - CI e deploy Oracle → Run workflow`, selecione `main` e marque o parâmetro `deploy=true`.
6. Acompanhe `GitHub → Actions → FoodBridge API - CI e deploy Oracle`.

Na VM, somente leitura:

```bash
sudo systemctl status foodbridge.service --no-pager
# Logs podem conter dados internos; consulte localmente e não cole saída bruta em chat/issue.
sudo journalctl -u foodbridge.service -n 80 --no-pager
curl --fail --silent --show-error --max-time 5 -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/dashboards/publico
```

**Rollback:** `deploy/oracle/atualizar-api.sh` restaura automaticamente o JAR anterior se o endpoint público não responder. Os backups ficam em `/opt/foodbridge/FoodBridge-API-Kotlin/deploy-backups`.

**Limite de validação:** esta configuração não significa que o deploy remoto foi executado. A primeira publicação requer os segredos, conectividade e preparação da VM, que não podem ser verificados apenas pela API de leitura/escrita de repositórios.


## Roteiro atualizado: preflight remoto antes do primeiro deploy (FB-OCI-CD v1.2.0, 12/11/2026)

**Esta revisão adiciona apenas validação e controles. Não executou deploy na VM.** A automação em PR só fica disponível na interface Actions da `main` depois de uma revisão/merge apropriado.

### Fase A — acesso SSH sem precisar de senha da console OCI

O login no painel Oracle Cloud e o SSH com chave para Linux são mecanismos diferentes. Se a console OCI estiver inacessível, a VM poderá continuar acessível por chave SSH preexistente. No PowerShell, a partir do computador que contém sua chave administrativa (confirme IP/host atual):

```powershell
ssh -o BatchMode=yes -o ConnectTimeout=10 -i "C:\foodbridge\ssh-oracle.key" opc@163.176.133.210
```

Esse comando é apenas **exemplo do host conhecido historicamente**: confirme endereço atual da instância e a fingerprint do host, nunca aceite mudança inesperada de chave. A chave privada NÃO deve ser enviada no chat ou commitada no GitHub.

Na VM, inspecione **sem exibir a configuração**:

```bash
sudo -n systemctl is-active foodbridge.service
sudo -n systemctl show foodbridge.service -p User -p WorkingDirectory
sudo -n test -s /etc/foodbridge/application.properties && echo "Config externa existe"
curl --fail --silent --show-error --max-time 5 -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/dashboards/publico
```

Antes de executar `preparar-vm.sh`, **revise seu diff e seu conteúdo**. O script prepara configuração externa e override systemd, mas não reinicia serviço nem mexe no banco. Se o serviço usa um JAR diferente do caminho do pipeline, ele agora recusa a preparação; corrigir essa discrepância manualmente após identificar a unidade real. Não fazer git reset/pull em checkout com alterações locais.

### Fase B — credenciais GitHub, separadas por tipo

Em **Repository Settings → Secrets and variables → Actions**, configure:

| Tipo | Nome | Para que serve |
|---|---|---|
| Secret | OCI_SSH_PRIVATE_KEY | Chave ED25519 **nova e dedicada** ao GitHub Actions, com chave pública autorizada na VM |
| Secret | OCI_SSH_KNOWN_HOSTS | Linha `known_hosts` ED25519 da VM **após comparar fingerprint** com a chave pública do host consultada via canal SSH confiável |
| Variable | OCI_SSH_HOST | IP/DNS atual da instância |
| Variable | OCI_SSH_USER | `opc` enquanto esse usuário e o unit atual forem os esperados |
| Variable | OCI_DEPLOY_ENABLED | Manter `false` enquanto apenas preflight for usado; `true` libera **apenas deploy manual** |
| Variable | OCI_AUTO_DEPLOY_ENABLED | Manter `false` inicialmente; `true` + OCI_DEPLOY_ENABLED=true permite deploy automático em pushes da main |

Confirme que **environment oracle-production** existe e revise suas proteções de aprovação e branch. Os secrets podem ser cadastrados no environment quando a configuração do GitHub for gerida dessa forma; não criar novas credenciais de banco no Actions. A chave SSH administrativa existente não é substituída.

### Fase C — somente preflight (sem publicação)

Na aba **Actions → FoodBridge API - CI e deploy Oracle → Run workflow**, selecione `main`, marque:

- `preflight = true`
- `deploy = false`

O job `verificar-oracle` será habilitado **mesmo com OCI_DEPLOY_ENABLED=false**. Ele executa leitura SSH, testa existência do service, `opc`, JAR real do ExecStart, configuração externa, ddl-auto em `validate` ou `none`, serviço ativo e saúde HTTP local. Não transfere JAR nem reinicia.

Se retornar `SKIPPED`, confirme que a execução foi realmente `workflow_dispatch` na `main`, o parâmetro preflight foi marcado e o workflow atualizado já está integrado na main. Se falhar, investigar mensagem do job sem desabilitar pin de host key.

### Fase D — autorização de publicação manual

**Somente após preflight PASS, revisão dos PRs de autorização #4/#5/#6 e aprovação do risco de migração/backup.** Ajuste `OCI_DEPLOY_ENABLED=true`; mantenha `OCI_AUTO_DEPLOY_ENABLED=false`. Então `workflow_dispatch` com `preflight=false`, `deploy=true`. Observe em tempo real o job, verifique serviço/caminho e smoke test HTTP após a troca.

Não ativar `OCI_AUTO_DEPLOY_ENABLED=true` antes de ensaiar rollback em homologação, configurar aprovação em `oracle-production` e aprovar alterações automáticas do backend. Se o preflight reprovar por `ddl-auto=update`, **não altere a opção cegamente**: confira esquema real/migrações e construa plano compatível de banco.

**Falhas e limites:** CI verde na GitHub não significa acesso SSH; a conexão do runner depende de DNS, rede, firewall e SSH da VM. O workflow de CI/CD não reinicia ou faz dump do PostgreSQL; mudanças de esquema executadas pela própria aplicação devem ser tratadas separadamente. A revisão de riscos e testes está em [especificação específica do canal Oracle](implantacao/CI_CD_ORACLE_SEGURANCA.md) e [matriz de testes](testes/CI_CD_ORACLE_TESTES.md).

### CI do contrato comprovado (não equivale ao deploy remoto)

[Actions #43235395978](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43235395978) no código `5da02052170e12f888e00d279b80594c44f4ab90` comprovou build/testes, sintaxe e **sete cenários de preflight simulado**. Os jobs de SSH/deploy ficaram **SKIPPED**, pois o evento era um PR. Após merge, só um `workflow_dispatch` na main com `preflight=true` e secrets SSH autorizados verificará de verdade a instância Oracle. Nenhum valor sensível precisa ser compartilhado no chat.

## FB-OCI-VAL-001 — Validar Hibernate em clone restaurado (v1.2.2, 12/11/2026)

**Evidência da VM informada pelo mantenedor:** backup pg_dump Custom com 38.212 bytes e permissão 0600 em /var/backups/foodbridge/foodbridge-20261112T183930Z.dump. Restauração com pg_restore --single-transaction --exit-on-error na cópia foodbridge_restore_check_20261112 concluída; 7 tabelas, 61 colunas e 8 FKs. O banco foodbridge original permaneceu intacto. Não é evidência de compatibilidade JPA.

O arquivo atual da VM continua configurado como spring.jpa.hibernate.ddl-auto=update. O DataInitializer.kt pode gravar dados administrativos no startup, por isso **não executar o JAR atual** apontando para a produção nem tentar validar sem isolamento.

### Novo perfil opcional schema-validation

- DataInitializer usa @Profile(!schema-validation) e é excluído apenas nesse modo; produção sem o perfil permanece inalterada.
- SchemaValidationGuard, em fase anterior à criação do DataSource/JPA, exige conexão JDBC loopback 127.0.0.1:5432 ou localhost:5432 para foodbridge_restore_check_*, usuário foodbridge_validate_*, ddl-auto=validate, web-application-type=none e hikari.read-only=true.
- Se falhar qualquer requisito, aborta o startup; não exibe a URL nem credenciais. Também rejeita override nativo hibernate.hbm2ddl.auto.
- Essas verificações **não substituem privilégios do PostgreSQL**: a role de teste deve ser sem SUPERUSER/CREATEDB/CREATEROLE, ter apenas CONNECT na cópia, USAGE em schema public e SELECT nas tabelas; não conceder DML/DDL em nenhum banco e não conceder permissões novas no banco original. Atenção: PostgreSQL pode já conceder CONNECT ao banco original via PUBLIC; não revogar PUBLIC na produção por causa do teste. A guarda da URL exige somente o clone, e a role não deve ter USAGE/SELECT nem permissão de escrita em tabelas do banco original.

### Procedimento operacional pendente

1. Revisar a PR e obter um **novo** JAR aprovado por CI contendo o perfil; não tocar no JAR associado a foodbridge.service.
2. Criar role temporária com nome foodbridge_validate_20261112, senha inserida por prompt (nunca em GitHub, logs ou chat), privilégios mínimos sobre foodbridge_restore_check_20261112 e default_transaction_read_only=on.
3. Verificar pelo próprio login de teste SHOW default_transaction_read_only=on, current_database()=foodbridge_restore_check_20261112 e que a role não tenha privilégios de leitura ou escrita em tabelas do banco foodbridge original. A permissão CONNECT herdada de PUBLIC, se existir, não prova acesso aos dados.
4. Executar o **processo Java secundário** com spring.profiles.active=schema-validation, URL/role do clone, spring.jpa.hibernate.ddl-auto=validate, spring.main.web-application-type=none e spring.datasource.hikari.read-only=true. Configuração JWT/admin deve ser lida somente de arquivo local protegido, sem vazar valores.
5. Conferir conclusão real de inicialização EntityManagerFactory/Hibernate validate; não interpretar CI verde como execução na VM. Aplicação não deve abrir porta HTTP nem executar DataInitializer.
6. Somente após ensaio, planejar descarte seguro de clone/role por procedimento autorizado; preservar backup por política de retenção e fazer cópia criptografada fora da mesma VM.

Não habilitar OCI_DEPLOY_ENABLED nem OCI_AUTO_DEPLOY_ENABLED antes de revisar segurança, configuração externa e esquema. O teste Hibernate real ainda não aconteceu.

### Evidência da role e ACL do PostgreSQL 13 (12/11/2026)

O mantenedor criou a role temporária `foodbridge_validate_20261112` sem superuser/createdb/createrole, definiu sua senha interativamente e estabeleceu `default_transaction_read_only=on`. A conexão com `foodbridge_restore_check_20261112` retornou `foodbridge_restore_check_20261112|foodbridge_validate_20261112|on`.

**Atenção PG 13:** a criação do banco restaurado pode trazer permissão `CREATE` no schema `public` herdada de `PUBLIC`. Antes de iniciar o JAR secundário, executar **somente no banco clonado**, nunca em `foodbridge`:

```sql
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
```

Depois verificar na **cópia**: `has_schema_privilege('foodbridge_validate_20261112','public','CREATE')=false`, `has_database_privilege(...,'CREATE')=false`, `has_database_privilege(...,'TEMP')=false` e nenhuma tabela com `has_table_privilege(...,'INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES,TRIGGER')=true`. `transaction_read_only=on` sozinho não substitui ACL (o usuário pode mudar a preferência por sessão). Não tocar nas ACLs do banco de produção.

O CI [#43292633447](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43292633447) no commit `d24ed934c060f1d8650c7ba0274c88082c28205c` compilou e testou com sucesso o profile; o artefato `foodbridge-api-jar` deve ser obtido **dessa execução** e copiado para diretório de ensaio independente (`/home/opc/foodbridge-validation`) sem tocar em `/opt/foodbridge/FoodBridge-API-Kotlin/build/libs/`. O profile ainda não foi executado na VM.

### Diagnóstico real de startup no clone: componente MVC carregado indevidamente (12/11/2026)

**Evidência do mantenedor:** o JAR aprovado no CI anterior foi executado apenas com `spring.profiles.active=schema-validation`, `spring.main.web-application-type=none`, role `foodbridge_validate_20261112`, PostgreSQL clonado `foodbridge_restore_check_20261112` e `ddl-auto=validate`. A senha foi fornecida de modo interativo; o processo terminou com **exit code 1**. O trecho `Description` do Spring Boot indica: `Parameter 1 of constructor in com.br.foodbridge.middleware.JwtFilter required a bean of type org.springframework.web.servlet.HandlerExceptionResolver that could not be found`.

**Causa:** o perfil sem HTTP desativa a infraestrutura Spring MVC, mas o `JwtFilter` anterior era um `@Component` incondicional que exigia `HandlerExceptionResolver`; `SecurityConfig` tinha dependência do filtro. Não é evidência de divergência de colunas ou falha do PostgreSQL. A execução não chegou a aprovar Hibernate `validate`.

**Correção proposta neste PR:** `@Profile("!schema-validation")` em `JwtFilter` e `SecurityConfig`. `PasswordEncoder` foi movido para `PasswordEncoderConfig`, **sem mudar o algoritmo BCrypt**, de modo que os serviços possam inicializar sem a configuração de segurança HTTP; a configuração padrão de produção permanece ativa. O teste de contexto Spring com perfil `schema-validation` agora verifica que `jwtFilter` e `securityConfig` não são criados, que `passwordEncoder` continua disponível e que o `DataInitializer` está desativado. A homologação real precisa de **novo JAR** gerado após essas alterações; o arquivo antigo na VM mantém o defeito.

**Nunca fazer deploy automático ou copiar a versão de testes ao caminho do JAR do systemd.** Manter o banco original sem alteração e repetir teste somente com artefato do novo run, checksum conferido, e role já validada sem escrita no clone.

### Homologacao real Hibernate validate no clone — PASS (12/11/2026)

**Evidencia relatada pelo mantenedor:** JAR corrigido foodbridge-pr10-43321134207.jar executado isoladamente de foodbridge.service; CI [#43321134207](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43321134207) no SHA 9c8e86ccaf0fdab5ff932b9d1a5ca91b99eed63a aprovou build, testes e bootJar; jobs preflight e deploy ficaram SKIPPED.

- Perfil schema-validation; web-application-type=none; JDBC 127.0.0.1:5432/foodbridge_restore_check_20261112; role foodbridge_validate_20261112; hikari.read-only=true; ddl-auto=validate. Segredos descartaveis, senha da role inserida em prompt e nao publicada.
- O mantenedor havia executado gate de ACL no clone, incluindo REVOKE CREATE no schema public e ausencia de CREATE, TEMP e DML para role de testes; resultado PASS.
- O script secundario so registra PASS quando a execucao Java termina com exit 0 e o log inclui os marcadores Initialized JPA EntityManagerFactory e Started FoodbridgeApplication.

**Saida recebida:** Codigo de saida: 0 ; PASS: Hibernate inicializou com o banco restaurado.

**Conclusao:** mapeamento JPA/Hibernate validate compativel com o banco RESTAURADO. Nao ocorreu deploy ao banco original, restart de foodbridge.service, teste SSH do runner, ensaio de rollback ou revisao completa dos PRs de autorizacao. Log privado permanece na VM; nao publicar dados de clientes nem senhas.

**Proxima fase pendente:** revisar de forma confidencial o arquivo produtivo em src/main/resources/application.properties, preparar /etc/foodbridge/application.properties com root:opc 0640 e ddl-auto=validate unico, apos confirmar origens das propriedades Spring; criar drop-in de systemd e daemon-reload sem restart. Depois configurar chave SSH exclusiva do CI, validar preflight real do GitHub Actions e somente entao decidir primeiro deploy manual, sujeito aos P0 funcionais. Nao habilitar auto-deploy neste estagio.
