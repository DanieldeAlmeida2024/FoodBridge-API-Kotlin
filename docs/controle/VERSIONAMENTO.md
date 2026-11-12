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
