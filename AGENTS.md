# Instruções para agentes — FoodBridge API

Preserve Kotlin, Spring Boot, Java 21 e PostgreSQL. Leia `README.md`, `build.gradle.kts`, `src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt` e `src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt` antes de alterar autenticação.

- Controller recebe requisições; Service centraliza regras; Repository isola persistência.
- Não alterar fluxos de token TEMP/ACCESS nem liberar endpoints sem testes específicos.
- Nunca imprimir JWT, `Authorization`, dados completos de autenticação ou segredos.
- Não recriar/alterar banco de produção; migrações devem ser revisadas.
- Priorizar nomes em português nos módulos novos, respeitando contratos/API existentes.
- Executar `./gradlew test` e validar fluxos de autorização por método e rota.
- Testes documentais não comprovam que aplicação inicializa com banco.

## Contrato obrigatório de documentação e inventário — 13/11/2026

- Antes de modificar módulos, leia [docs/README_DOCUMENTACAO.md](docs/README_DOCUMENTACAO.md), [docs/INVENTARIO_PROJETO.md](docs/INVENTARIO_PROJETO.md) e a documentação atual do recurso alterado.
- PR que cria, move ou remove arquivos: execute `python3 tools/atualizar_inventario.py --write`, inclua a alteração em `docs/INVENTARIO_PROJETO.md` e verifique `python3 tools/atualizar_inventario.py --check`.
- O teste `tests/ci/test_inventory_contract.py` exige correspondência exata entre o Git e o inventário. Uma mudança de conteúdo sem mudar caminhos **não** é detectada por esse teste; atualize manualmente `RECURSOS_E_STATUS.md` / `RECURSOS_E_TELAS.md`, diagramas e catálogo de API, conforme o escopo.
- Ao alterar contrato HTTP, autorização, modelo de dados, tela, teste ou fluxo de deploy, atualizar documentos vinculados e `docs/controle/VERSIONAMENTO.md` no mesmo PR.
- Diferenciar especificação histórica, código existente, CI aprovado e e2e homologado; registrar evidência real por commit/run. Não afirmar conformidade LGPD/HTTPS/100% de cobertura sem provas.
- O CI/CD atual é disparado automaticamente em push de **código** na main. Um PR somente documental não deve acionar nova publicação na Oracle. Não restabelecer gates manuais legados sem decisão explícita.
