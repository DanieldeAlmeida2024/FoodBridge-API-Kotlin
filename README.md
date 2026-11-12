# FoodBridge — API Kotlin/Spring Boot

[![FoodBridge API — CI](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/workflows/foodbridge-ci-cd.yml/badge.svg)](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/workflows/foodbridge-ci-cd.yml)

Serviço de backend para redistribuição de alimentos, usuários, organizações e fluxos de doação. Desenvolvido em **Kotlin 2.2**, **Spring Boot 3.2.5**, **Java 21**, **Gradle** e **PostgreSQL**.

## Preparação

1. Instale Java 21 e PostgreSQL.
2. Configure um banco local de desenvolvimento e um usuário com privilégios mínimos.
3. Configure as propriedades a partir de `src/main/resources/application.properties.example`. Não versionar senhas, JWT secrets ou credenciais do administrador.
4. Execute na raiz:

```powershell
.\gradlew.bat bootRun
```

Em Linux/macOS: `./gradlew bootRun`.

## Testar

```powershell
.\gradlew.bat test
```

As coleções Postman estão em `postman/collections`. Confirme URL base e credenciais de teste antes de executar operações que modifiquem dados.

## GitHub Actions e Oracle Cloud

O workflow `.github/workflows/foodbridge-ci-cd.yml` compila, executa testes com PostgreSQL temporário e publica o JAR aprovado como artefato. O deploy na VM só é habilitado após a configuração segura de SSH e a variável `OCI_DEPLOY_ENABLED=true`. A atualização possui backup e rollback automático.

**[Guia de preparação e ativação do deploy Oracle](docs/DEPLOY_ORACLE.md)**. Não inclua credenciais reais no GitHub e não use o banco de produção nos testes de CI.

## Arquitetura

- `controller/`: entrada HTTP e DTOs.
- `service/`: regras e processos.
- `domain/`: entidades, enumerações e repositórios.
- `middleware/JwtFilter.kt`: leitura e validação de JWT.
- `config/`: configuração de autenticação, banco e aplicação.
- `src/test/`: testes JUnit.

Documentos: [requisitos](docs/REQUISITOS.md), [arquitetura](docs/ARQUITETURA.md), [UML](docs/UML.puml), [plano de testes](docs/PLANO_TESTES.md), [instruções para IA](AGENTS.md).

## Segurança e estado

- A política de acesso diferencia tokens temporários e de acesso. Revise as permissões por endpoint ao acrescentar novas rotas.
- Nunca registrar o conteúdo do token, cabeçalho Authorization ou dados de sessão.
- `ddl-auto=update` no exemplo não substitui migrações controladas para produção.
- A suíte automatizada de CI inicia seu próprio PostgreSQL descartável. A execução remota na Oracle Cloud requer preparação da VM e secrets de SSH; a configuração remota ainda não foi validada.

**Estado:** aplicação em desenvolvimento, não certificada para produção.

## Documentação técnica aprofundada (v1.1.0)

- [Projeto, requisitos e 49 endpoints](docs/projeto/ESPECIFICACAO_PROJETO.md)
- [Riscos e regras pendentes](docs/projeto/RISCOS.md)
- [Arquitetura e 16 diagramas UML editáveis](docs/uml/README.md)
- [Plano de testes com 165 casos planejados](docs/testes/PLANO_DE_TESTES.md)
- [Matriz de rastreabilidade rota × teste](docs/testes/MATRIZ_RASTREABILIDADE.md)
- [Versionamento e histórico de revisão](docs/controle/VERSIONAMENTO.md)

> O plano mestre tem como base a análise estática de outubro de 2026; o CI aprovado em novembro não equivale à execução de todos os cenários.

### Pré-flight somente leitura antes de habilitar deploy (FB-OCI-CD v1.2.0 proposta)

O pipeline agora diferencia `workflow_dispatch preflight=true` (SSH/serviço/configuração/saúde, sem deploy) de `deploy=true` (publicação manual após CI). A variável `OCI_AUTO_DEPLOY_ENABLED=true` é necessária **além de `OCI_DEPLOY_ENABLED=true`** para publicar automaticamente a cada push na main. Scripts verificam que o service realmente usa o JAR atualizado e exigem `ddl-auto=validate` ou `none` antes de qualquer restart; os dados e configurações da VM não foram alterados nesta revisão. [Guia operacional](docs/DEPLOY_ORACLE.md) · [Arquitetura de segurança](docs/implantacao/CI_CD_ORACLE_SEGURANCA.md) · [Matriz de testes](docs/testes/CI_CD_ORACLE_TESTES.md) · [UML](docs/uml/17_pipeline_oracle_cicd.puml).
