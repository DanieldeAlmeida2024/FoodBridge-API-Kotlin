# FoodBridge — controle de versão dos documentos

**Identificador:** FB-DOC-CTRL · **Versão do conjunto:** 2.0.0 (proposta, depende do PR #4) · **Atualização:** 11/11/2026 · **Responsável técnico:** mantenedor do repositório / revisão de código por PR.

## Documentos controlados

| ID | Documento | Versão | Base de código | Situação |
|---|---|---|---|---|
| FB-PROJ-001 | [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) | 2.0.0 | Histórico 6116749; confronto com main 2d0b63e41 | Revisão PR #4; integração pendente |
| FB-RSK-001 | [Riscos](../projeto/RISCOS.md) | 2.0.0 | Histórico 6116749 | R02/R03 no PR #4; R04/R11 mitigados parcialmente neste PR |
| FB-UML-001 | [Arquitetura e catálogo UML](../uml/README.md) | 2.0.0 | Histórico 6116749 | 18 fontes UML; fluxo de voluntários adicionado |
| FB-TST-001 | [Plano de testes](../testes/PLANO_DE_TESTES.md) | 2.0.0 | Histórico 6116749 | 165 cenários planejados; 5 unitários do PR #4 aprovados + 9 novos sob verificação |
| FB-TRC-001 | [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md) | 2.0.0 | Histórico 6116749 | 49 endpoints, API-44..49 revistos |

## Estado da versão 1.2.0 (proposta no PR #4)

- **Código associado:** commit `9d96afb6e86a0fbb5044c21eaf7619fb6d311a62`; [PR #4](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/4), **rascunho, sem merge**.
- **Evidência automatizada:** [GitHub Actions run 43081581178](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43081581178), job “Compilar e testar Kotlin / Java 21” **success**; publicação Oracle **skipped**.
- **Escopo:** correção de cast de role, autorização por vínculo ADMIN verificado no banco e teste unitário de isolamento para PUT e aprovação.
- **Não concluído:** testes HTTP/integração com PostgreSQL, revisão independente de acesso, política futura de gestão para papéis não-ADMIN e liberação de produção. Nenhum release da aplicação é inferido desta versão documental.

## Estado da versão 2.0.0 proposta — isolamento de voluntários

**Branch:** `fix/p0-voluntarios-isolamento-20261111` (dependência direta: [PR #4 — autorização organizacional](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/4)). A v2.0.0 documenta uma **mudança incompatível de contrato motivada por proteção de dados pessoais**: antes o POST permitia vinculação por CPF entre organizações; agora a operação é bloqueada até existir consentimento/autorização do titular.

- [Política de acesso e ciclo de vida de voluntários](../projeto/POLITICA_DADOS_VOLUNTARIOS.md).
- [Diagrama de autorização de voluntários](../uml/18_sequencia_isolamento_voluntarios.puml).
- **Código:** novo `VoluntarioService` com validação de vínculo vigente, consultas filtradas por organização e transações de escrita; `VoluntarioController` transmite o contexto autenticado; `RequisicaoDoacaoService` consulta voluntário vinculado ao tenant solicitante.
- **Testes novos:** 9 testes unitários em `VoluntarioServiceAutorizacaoTest.kt`. Execução e CI devem ser verificados na revisão do PR; testes HTTP/SQL não foram executados neste documento.
- **Não encerrados:** controle de gestores por papel (RBAC), consentimento/reativação do titular, ofuscação/retirada de CPF do DTO, validação de CPF/email, testes reais de Postgres e política LGPD.
- A versão 1.2.0 do PR #4 e a versão 2.0.0 deste PR **não são releases publicados**.

## Histórico

| Versão | Data | Alteração | Evidência |
|---|---|---|---|
| 1.0.0 | 29/10/2026 | Plano mestre com 165 casos, 49 endpoints e 16 diagramas catalogados | Anexos fornecidos; commit 611674973467cf859d5925c8306b66df249b7be3 |
| 2.0.0 (proposta) | 11/11/2026 | Isolamento por tenant nas rotas de voluntários, bloqueio da vinculação automática por CPF, prevenção de edição do cadastro global compartilhado; 9 testes unitários e UML 18 | PR de segurança dependente do #4; CI pendente |
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
