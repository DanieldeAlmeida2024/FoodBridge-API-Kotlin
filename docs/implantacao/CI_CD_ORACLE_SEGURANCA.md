# FoodBridge — integração segura do CI/CD com Oracle Linux

**FB-OCI-CD-001 · v1.2.0 (proposta, 12/11/2026)**  
Base: branch main @ a6a0c611868b1d27fe35ecfb5f5944fabe8d50d5. PR de integração sem alteração em produção. Revisar evidências do commit final.

## Topologia existente, sem substituições silenciosas

- GitHub Actions compila Kotlin 2.2 / Spring Boot 3.2.5 no Java 21, com PostgreSQL 16 **descartável**. Os testes do CI NÃO usam o banco Oracle.
- O build publica o **mesmo JAR aprovado** como artifact. O job de deploy transferirá o artefato por SSH, verificará SHA-256 e chamará o script versionado de publicação.
- Oracle Linux 9: usuário SSH e serviço systemd existentes como **opc**; aplicação em /opt/foodbridge/FoodBridge-API-Kotlin; JAR em build/libs/foodbridge-0.0.1-SNAPSHOT.jar.
- A configuração de runtime deve estar em /etc/foodbridge/application.properties, legível somente pelo serviço e root, carregada via override systemd SPRING_CONFIG_ADDITIONAL_LOCATION. **Nenhum secret ou configuração do PostgreSQL deve entrar no JAR versionado ou logs.**
- PostgreSQL existente **foodbridge**, Nginx e serviço systemd permanecem como estão. Não executar migração automática, reinicialização de banco, reconfiguração de firewall ou rebuild do frontend.

## Estados do workflow (mutuamente controlados)

| Evento / input | Secrets SSH | Condição | Impacto esperado |
|---|---|---|---|
| pull_request → main | Não | Sempre | Testes Kotlin, PostgreSQL descartável, lint bash e testes contratuais. Deploy skipped. |
| push → main | Não para CI | Padrão | CI novamente; deploy skipped enquanto OCI_AUTO_DEPLOY_ENABLED != true. |
| workflow_dispatch: preflight=true, deploy=false | Sim | main + environment oracle-production | CI e auditoria remota via SSH **somente leitura**. Nenhuma cópia, substituição de JAR ou restart. |
| workflow_dispatch: preflight=false, deploy=true | Sim | main + OCI_DEPLOY_ENABLED=true | CI → preflight obrigatório → SSH/SCP JAR → SHA-256 → backup → swap → restart → health → rollback em falha. |
| push → main com OCI_DEPLOY_ENABLED=true e OCI_AUTO_DEPLOY_ENABLED=true | Sim | ambas vars explícitas | Deploy automático, condicionado a CI, pré-flight e proteção do environment. Não habilitar antes da homologação. |

Quando preflight=true e deploy=true forem simultâneos, o workflow não executa o job de publicação: **preflight prevalece**.

A concorrência tem cancel-in-progress=false para que um novo run não interrompa a sequência de troca, restart e rollback. A restrição vale para o grupo de branch. A VM e o host SSH são identificados por secrets e variables do GitHub, nunca inseridos no código do pipeline.

## Verificações remotas que NÃO alteram a VM

**Script:** deploy/oracle/verificar-vm.sh. O runner passa o script por stdin à sessão SSH, sem depender de git pull na VM. O preflight confirma:

1. chave SSH e fingerprint do host (StrictHostKeyChecking=yes; arquivo known_hosts pré-validado), sudo não interativo e existência do service;
2. User=opc no systemd, existência e legibilidade de /etc/foodbridge/application.properties pelo serviço e override de configuração aplicado;
3. valor explícito **spring.jpa.hibernate.ddl-auto=validate ou none** no arquivo externo (sem imprimir valores), além de recusar override de Environment incompatível;
4. ExecStart do serviço aponta para o **mesmo caminho do JAR que o deploy altera** (path absoluto ou relativo à WorkingDirectory esperada);
5. serviço já ativo e GET /dashboards/publico respondendo localmente, antes de substituir qualquer artefato.

Se qualquer item falhar, nenhuma implantação é tentada. Atenção: Hibernate pode receber propriedades via outras fontes (flags Java, arquivos importados, etc.); essas origens ainda precisam de inspeção presencial antes de autorizar produção. Não tratar o grep do arquivo como prova de ausência de todas as mutações de esquema.

## Política de dados

Exigir ddl-auto validate ou none evita que **essa propriedade** autorize atualização automática de esquema durante o restart. **Não garante** ausência de migrações executadas por código de inicialização ou Flyway/Liquibase futuro; essas dependências devem ser avaliadas antes da implantação. Não alterar a propriedade atual da VM automaticamente. Se hoje a configuração usar update, o preflight deve reprovar: verificar compatibilidade de esquema e preparar migrações/backup por processo separado.

O script de deploy existente mantém backup do JAR, valida hash e possui rollback por falha de health no prazo definido. Isso não é rollback de banco; a segurança de dados exige mudanças de esquema compatíveis e backup independente previamente autorizado.

## Privilégios e impedimentos

A chave de CI deve ser **dedicada**, não reutilizar chave pessoal usada para manutenção. O uso de opc + sudo não interativo herda risco de ampla autorização no host; uma conta de deploy com privilégios específicos deve ser planejada antes de auto-deploy permanente. Não registrar chave privada no chat, nos logs ou no Git. Environment oracle-production deve exigir aprovação e limitar branch à main quando o plano GitHub permitir.

A autenticação da console Oracle Cloud não é a mesma que o SSH da VM: se o painel OCI estiver inacessível, o SSH por chave existente ainda pode funcionar se a instância/rede não mudou. Sem SSH não há como executar o preflight remoto, e CI verde não comprova publicação.

O controle de acesso por organização e voluntários permanece com PRs #4/#5/#6 em revisão. **Não publicar a aplicação como segura enquanto esses P0 estiverem pendentes.**

[Plano de testes do pipeline](../testes/CI_CD_ORACLE_TESTES.md) · [UML](../uml/17_pipeline_oracle_cicd.puml) · [Guia operacional](../DEPLOY_ORACLE.md) · [Versionamento](../controle/VERSIONAMENTO.md).
