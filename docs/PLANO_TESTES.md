# Plano de testes

- CT-01: POST /auth/login e POST /usuarios são públicos.
- CT-02: GET /dashboards/publico é público; outros métodos não devem receber exceção automática.
- CT-03: endpoints ADMIN exigem papel autorizado.
- CT-04: rotas TEMP aceitam tipo TEMP apenas.
- CT-05: nenhum token/header Authorization aparece no log.
- CT-06: contexto completo inicia com banco de teste e migrações compatíveis.

Executar `./gradlew test` com Java 21 e PostgreSQL de desenvolvimento isolado. Os casos de integração permanecem pendentes até execução em ambiente configurado.
