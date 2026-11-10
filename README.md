# FoodBridge — API Kotlin/Spring Boot

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
- A conexão com banco e o contexto completo do Spring não foram executados nesta sessão; este PR deve passar por build, testes e revisão antes de merge.

**Estado:** aplicação em desenvolvimento, não certificada para produção.
