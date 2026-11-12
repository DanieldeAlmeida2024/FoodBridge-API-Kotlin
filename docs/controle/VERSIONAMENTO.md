# FoodBridge — controle de versão dos documentos

**Identificador:** FB-DOC-CTRL · **Versão do conjunto:** 1.1.0 · **Atualização:** 11/11/2026 · **Responsável técnico:** mantenedor do repositório / revisão de código por PR.

## Documentos controlados

| ID | Documento | Versão | Base de código | Situação |
|---|---|---|---|---|
| FB-PROJ-001 | [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) | 1.1.0 | Histórico 6116749; confronto com main 2d0b63e41 | Atualização documental |
| FB-RSK-001 | [Riscos](../projeto/RISCOS.md) | 1.1.0 | Histórico 6116749 | Revalidação pendente |
| FB-UML-001 | [Arquitetura e catálogo UML](../uml/README.md) | 1.1.0 | Histórico 6116749 | Diagramas reconstruídos |
| FB-TST-001 | [Plano de testes](../testes/PLANO_DE_TESTES.md) | 1.1.0 | Histórico 6116749 | 165 planejados, parcial CI |
| FB-TRC-001 | [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md) | 1.1.0 | Histórico 6116749 | 49 endpoints auditados |

## Histórico

| Versão | Data | Alteração | Evidência |
|---|---|---|---|
| 1.0.0 | 29/10/2026 | Plano mestre com 165 casos, 49 endpoints e 16 diagramas catalogados | Anexos fornecidos; commit 611674973467cf859d5925c8306b66df249b7be3 |
| 1.1.0 | 11/11/2026 | Separação das especificações, testes, rastreabilidade, riscos e fontes PlantUML; distinção do snapshot histórico e CI atual | PR atual, base 2d0b63e416bc9f5bb994b6364160bb78f8399372 |

## Política de versionamento

- **MAJOR:** mudança de contrato/API, fluxo de negócio ou arquitetura que exija nova linha de base.
- **MINOR:** módulos/endpoints, diagramas ou cenários adicionados sem romper semântica existente.
- **PATCH:** correção editorial, link, typo, referência de commit ou evidência de execução sem mudança de requisito.
- Cada PR que modifica comportamento deve apontar quais IDs/documentos mudou e atualizar a tabela do controle.
- Evidências de testes são versionadas **por commit da aplicação**; o histórico nunca substitui o status de um run real.

## Fonte e procedência

Os anexos de entrada foram: `Plano_de_Testes_FoodBridge(1).md`, `Matriz_de_Rastreabilidade(1).md` e correspondente PDF. O pacote atual separa seus capítulos e preserva os casos identificados. Fontes PlantUML foram reconstruídas no PR e devem ser revisadas contra o código.


## FB-OCI-CD v1.2.0 — preflight SSH e gate de banco (12/11/2026)

| ID | Artefato | Alteração e decisão |
|---|---|---|
| OCI-PIPE | `.github/workflows/foodbridge-ci-cd.yml` | `workflow_dispatch preflight` somente leitura, auto-deploy opt-in separado, `cancel-in-progress=false` e CI contratuais |
| OCI-VM | `deploy/oracle/verificar-vm.sh` | Inspeção remota SSH: service, User, ExecStart, config externa, ddl-auto e health, sem alterar DB/JAR |
| OCI-UPDATE | `deploy/oracle/atualizar-api.sh` | Revalida Jar efetivo, usuário, saúde anterior e política JPA DDL antes de fazer backup/swap/restart |
| OCI-PREP | `deploy/oracle/preparar-vm.sh` | Rejeita preparação para serviço cujo JAR não corresponda ao destino de publicação |
| OCI-CI | `tests/ci/test_oracle_deploy_contract.py` | Valida em runner isolado portas de autorização, pinning SSH, rollback, DDL e bash -n |
| OCI-ARCH | `docs/implantacao/CI_CD_ORACLE_SEGURANCA.md` | Fluxo e critérios de segurança detalhados, específico da VM Oracle Linux |
| OCI-TST | `docs/testes/CI_CD_ORACLE_TESTES.md` | Casos OCI-01 a OCI-16 com separação executado/pendente |
| OCI-UML | `docs/uml/17_pipeline_oracle_cicd.puml` | Sequência PlantUML versionada de CI, preflight, CD e rollback |

Histórico de evidências **anteriores**: [#43038670928](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43038670928) e [#43032061904](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43032061904), CI PASS, CD SKIPPED. Esse PR de segurança cria mecanismo de preflight mas **não prova deploy remoto**. A nova execução deve ser anexada somente após o CI do HEAD.

### Correção de gate detectada pelo CI (12/11/2026)

O run [#43233569951](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43233569951) reprovou **corretamente** a sintaxe de `atualizar-api.sh` após uma alteração na validação Hibernate. O build Kotlin/Gradle passou, mas `bash -n` bloqueou o CI. O script foi reconstruído a partir do commit anterior válido e a política `ddl-auto=validate|none` reaplicada com teste de duplicação/inteireza do arquivo. Verificar novo CI no HEAD corrigido antes de marcar PASS. Nenhuma VM foi alcançada ou alterada por esses runs de PR.

### Evidência CI do código de preflight e controles de implantação — 12/11/2026

- Commit `5da02052170e12f888e00d279b80594c44f4ab90`: [Actions #43235395978](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43235395978) **SUCCESS**, incluindo `./gradlew clean test bootJar` (Java 21/PostgreSQL CI), `bash -n` dos três scripts, suíte `tests/ci/test_oracle_deploy_contract.py` e **sete testes simulados** de `verificar-vm.sh` (`tests/ci/test_oracle_preflight_contract.py`).
- Jobs **`Publicar na VM Oracle: SKIPPED`** e **`Preflight Oracle somente leitura: SKIPPED`**, como esperado em um pull request. **Nenhuma conexão SSH ou alteração da VM foi comprovada/executada**.
- Run anterior [#43233569951](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43233569951) reprovou verificação `bash -n` após um patch incorreto; corrigido a partir de versão válida antes de voltar a PASS. Não ocultar a regressão.
- Uma atualização posterior apenas de documentação gera novo HEAD, cujo CI deve ser consultado antes de merge. A conexão com a VM e o primeiro deploy manual permanecem fora do escopo dos testes locais.

### OCI-R07 — 8 casos simulados e proteção contra valores duplicados

O teste `tests/ci/test_oracle_preflight_contract.py` passou de sete para **oito casos**: arquivo com `ddl-auto` duplicado reprova, mesmo que haja uma linha segura. `verificar-vm.sh` e `atualizar-api.sh` utilizam inspeção somente leitura e exigem uma única propriedade com `validate` ou `none`. A execução CI do último SHA precisa ser consultada antes da integração.
