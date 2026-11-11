# FoodBridge — controle de versão dos documentos

**Identificador:** FB-DOC-CTRL · **Versão do conjunto:** 1.2.0 · **Atualização:** 11/11/2026 · **Responsável técnico:** mantenedor do repositório / revisão de código por PR.

## Documentos controlados

| ID | Documento | Versão | Base de código | Situação |
|---|---|---|---|---|
| FB-PROJ-001 | [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) | 1.2.0 | Histórico 6116749; confronto com main 2d0b63e41 | Revisão PR #4; integração pendente |
| FB-RSK-001 | [Riscos](../projeto/RISCOS.md) | 1.2.0 | Histórico 6116749 | R02/R03 mitigados em PR #4; outros abertos |
| FB-UML-001 | [Arquitetura e catálogo UML](../uml/README.md) | 1.2.0 | Histórico 6116749 | 17 diagramas; fluxo tenant/admin em PR #4 |
| FB-TST-001 | [Plano de testes](../testes/PLANO_DE_TESTES.md) | 1.2.0 | Histórico 6116749 | 165 planejados + 5 testes unitários P0 aprovados em CI |
| FB-TRC-001 | [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md) | 1.2.0 | Histórico 6116749 | 49 endpoints, guardas API-26/28/29 atualizadas |

## Estado da versão 1.2.0 (proposta no PR #4)

- **Código associado:** commit `9d96afb6e86a0fbb5044c21eaf7619fb6d311a62`; [PR #4](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/4), **rascunho, sem merge**.
- **Evidência automatizada:** [GitHub Actions run 43081581178](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43081581178), job “Compilar e testar Kotlin / Java 21” **success**; publicação Oracle **skipped**.
- **Escopo:** correção de cast de role, autorização por vínculo ADMIN verificado no banco e teste unitário de isolamento para PUT e aprovação.
- **Não concluído:** testes HTTP/integração com PostgreSQL, revisão independente de acesso, política futura de gestão para papéis não-ADMIN e liberação de produção. Nenhum release da aplicação é inferido desta versão documental.

## Incremento de segurança na mesma versão 1.2.0 — DELETE + integração HTTP

- `DELETE /organizacoes/{id}` (API-27) passa pela mesma política transacional de autorização do contexto administrativo, eliminando a possibilidade de `ADMIN` válido da organização A inativar a organização B.
- `OrganizacaoAutorizacaoHttpIntegrationTest.kt`: **7 testes Spring MockMvc + banco PostgreSQL do CI** cobrindo PUT/DELETE cross-tenant, DELETE permitido, vínculo revogado, role insuficiente e PATCH aprovação/reprovação cross-tenant. A suíte ainda exige resultado de CI no commit da revisão.
- Os cinco testes unitários do primeiro commit do PR continuam preservados; os sete são testes de integração adicionais (não novos cenários do catálogo de 165).
- Esta alteração é parte da proposta documental 1.2.0 do PR #4, **não está mesclada nem publicada**. O PR #5 é dependente desta branch e precisa sincronizar o novo HEAD antes de merge.

## Histórico

| Versão | Data | Alteração | Evidência |
|---|---|---|---|
| 1.0.0 | 29/10/2026 | Plano mestre com 165 casos, 49 endpoints e 16 diagramas catalogados | Anexos fornecidos; commit 611674973467cf859d5925c8306b66df249b7be3 |
| 1.2.0 | 11/11/2026 | Atualização do controle, especificação, riscos, testes, matriz e UML para mitigação R02/R03 em PR #4; CI de compilação/testes aprovado, sem deploy | PR #4, commit 9d96afb; run 43081581178 |
| 1.1.0 | 11/11/2026 | Separação das especificações, testes, rastreabilidade, riscos e fontes PlantUML; distinção do snapshot histórico e CI atual | PR atual, base 2d0b63e416bc9f5bb994b6364160bb78f8399372 |

## Política de versionamento

- **MAJOR:** mudança de contrato/API, fluxo de negócio ou arquitetura que exija nova linha de base.
- **MINOR:** módulos/endpoints, diagramas ou cenários adicionados sem romper semântica existente.
- **PATCH:** correção editorial, link, typo, referência de commit ou evidência de execução sem mudança de requisito.
- Cada PR que modifica comportamento deve apontar quais IDs/documentos mudou e atualizar a tabela do controle.
- Evidências de testes são versionadas **por commit da aplicação**; o histórico nunca substitui o status de um run real.

## Fonte e procedência

Os anexos de entrada foram: `Plano_de_Testes_FoodBridge(1).md`, `Matriz_de_Rastreabilidade(1).md` e correspondente PDF. O pacote atual separa seus capítulos e preserva os casos identificados. Fontes PlantUML foram reconstruídas no PR e devem ser revisadas contra o código.
