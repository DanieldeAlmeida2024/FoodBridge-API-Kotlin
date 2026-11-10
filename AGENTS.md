# Instruções para agentes — FoodBridge API

Preserve Kotlin, Spring Boot, Java 21 e PostgreSQL. Leia `README.md`, `build.gradle.kts`, `src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt` e `src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt` antes de alterar autenticação.

- Controller recebe requisições; Service centraliza regras; Repository isola persistência.
- Não alterar fluxos de token TEMP/ACCESS nem liberar endpoints sem testes específicos.
- Nunca imprimir JWT, `Authorization`, dados completos de autenticação ou segredos.
- Não recriar/alterar banco de produção; migrações devem ser revisadas.
- Priorizar nomes em português nos módulos novos, respeitando contratos/API existentes.
- Executar `./gradlew test` e validar fluxos de autorização por método e rota.
- Testes documentais não comprovam que aplicação inicializa com banco.
