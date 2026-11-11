# FoodBridge — controle de versão dos documentos

**Identificador:** FB-DOC-CTRL · **Versão do conjunto:** 1.2.0 (proposta P0/R05) · **Atualização:** 11/11/2026 · **Responsável técnico:** mantenedor do repositório / revisão de código por PR.

## Documentos controlados

| ID | Documento | Versão | Base de código | Situação |
|---|---|---|---|---|
| FB-PROJ-001 | [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) | 1.2.0 | Histórico 6116749; confronto com main 2d0b63e41 | Atualização documental |
| FB-RSK-001 | [Riscos](../projeto/RISCOS.md) | 1.2.0 | Histórico 6116749 | Revalidação pendente |
| FB-UML-001 | [Arquitetura e catálogo UML](../uml/README.md) | 1.2.0 | Histórico 6116749 | Diagramas reconstruídos |
| FB-TST-001 | [Plano de testes](../testes/PLANO_DE_TESTES.md) | 1.2.0 | Histórico 6116749 | 165 planejados, parcial CI |
| FB-TRC-001 | [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md) | 1.2.0 | Histórico 6116749 | 49 endpoints auditados |

## Revisão 1.2.0 — proteção de estoque (PR independente da série #4/#5)

**Código na branch:** `59113750d56a4318e8f8bdb3dc3caa9e55f96498`; a numeração 1.2.0 é **proposta para este PR independente**, e deverá ser reconciliada com as versões documentais propostas em outros PRs antes do merge.

- **R05 / REQ-15:** aprovação lê o ID de doação por projeção, faz `SELECT FOR UPDATE` da linha da doação sob `@Transactional` **antes** de carregar a requisição e calcular o saldo.
- Rejeição, cancelamento e conclusão compartilham a disciplina de lock para impedir disputas de estado.
- Alteração, exclusão e fechamento de doação passam por lock transacional; PUT não pode reduzir quantidade abaixo do total aprovado/concluído; DELETE com requisições passa a ser rejeitado.
- **Novo teste PostgreSQL concorrente:** `RequisicaoDoacaoConcorrenciaIntegrationTest.kt` abre duas transações simultâneas de ONGs pedindo 7 kg da mesma oferta de 10 kg; espera um sucesso e uma recusa, 7 kg aprovados no máximo. Necessita CI `PostgreSQL 16` efêmero do workflow atual.
- **Não resolvido:** precisão monetária/quantidade Double, ciclo de fechamento manual e regras de status aceitas do cliente, lock em caminhos novos que venham a alterar requisições, validação com PostgreSQL real/CI antes da publicação.

## Histórico

| Versão | Data | Alteração | Evidência |
|---|---|---|---|
| 1.2.0 (proposta) | 11/11/2026 | Lock transacional da doação nas aprovações e mutações, teste concorrente PostgreSQL, matriz e UML R05 | CI #43088958325 Java 21/Gradle/Postgres aprovado; Oracle skipped |
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


### Evidência de build e teste — revisão de 11/11/2026

- [GitHub Actions #43088958325](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43088958325): `Compilar e testar Kotlin / Java 21` = **success**, `Publicar na VM Oracle` = **skipped**.
- O log confirma `> Task :test` e `BUILD SUCCESSFUL` para o commit de código/doc anterior (`e43e980`); o teste concorrente usa threads e acesso JPA real ao PostgreSQL efêmero.
- Esta documentação posterior não altera as regras de estoque; a próxima execução de CI valida o novo HEAD. O CI não substitui teste de carga nem revisão de todos os interleavings.
