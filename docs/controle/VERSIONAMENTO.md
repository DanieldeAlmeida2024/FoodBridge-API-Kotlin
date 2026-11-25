# FoodBridge — controle de versão dos documentos

## v1.2.4 — retirada do roteiro de apresentação do repositório (23/11/2026)

O roteiro de apresentação `docs/APRESENTACAO_PROJETO.md` foi retirado da árvore atual do GitHub a pedido do responsável pelo projeto. A apresentação não será mantida como parte da documentação versionada. Os documentos de histórico de mudanças, registro de uso de IA, testes, arquitetura e versionamento continuam disponíveis.

Foram ajustados os links no `README.md`, no índice de documentação e no guia de estudo, além do inventário, que voltou a ter 182 arquivos rastreados. Esta mudança é somente documental; não modifica o código Kotlin, a API, o banco, a VM ou a publicação automática.

O arquivo existiu em revisões anteriores e, por isso, permanece recuperável no histórico de commits do Git. As entradas antigas abaixo são mantidas como histórico, sem links para um arquivo que não existe mais na versão atual.

## v1.2.3 — exemplos de perguntas ligados às mudanças (23/11/2026)

Os exemplos de consultas sobre inteligência artificial no [histórico auxiliar](HISTORICO_MUDANCAS_E_USO_IA.md) foram organizados por etapa, com linguagem mais direta. Cada pergunta apresenta problemas, três cenários e possíveis soluções, além de apontar os PRs onde há alterações correspondentes. Naquela revisão, um roteiro de apresentação também foi ajustado, mas o arquivo foi retirado da árvore atual na v1.2.4.

O objetivo é explicar como analisar os problemas sem confundir uma sugestão de IA com uma alteração comprovada. Não há transcrição original verificada desses prompts; a evidência de implementação continua sendo o Git, os PRs e os testes. Esta revisão altera apenas texto dos documentos, sem mudança de código ou do ambiente Oracle.

## v1.2.2 — histórico das revisões e registro de consultas com IA (23/11/2026)

Esta atualização é apenas da documentação. Foi feita uma revisão do histórico de 10 a 13/11/2026 para registrar as mudanças que aconteceram na aplicação e as verificações realizadas.

| Período | Mudanças documentadas | Referências |
|---|---|---|
| 10–11/11/2026 | Logs JWT, rotas públicas, primeiros testes e organização das especificações | PRs [#1](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/1), [#3](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/3) |
| 11–13/11/2026 | Autorização por organização, proteção de voluntários, estoque e testes integrados | PRs [#4](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/4), [#6](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/6), [#11](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/11) |
| 12–13/11/2026 | Verificações Oracle, validação de banco, falhas de startup e rollback | PRs [#7](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/7), [#9](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/9), [#10](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/10), [#22](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/22) a [#28](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/28) |
| 13/11/2026 | Publicação automática, testes de CI, catálogo de 50 rotas e documentação de infraestrutura | PRs [#28](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/28), [#29](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/29), [#30](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/30) |

O sistema já tinha seus fluxos principais funcionando antes das revisões. Os ajustes foram motivados por riscos de segurança, verificações incompletas, possíveis problemas de concorrência e necessidade de organizar melhor testes e documentação.

Foi criado o documento auxiliar [HISTORICO_MUDANCAS_E_USO_IA.md](HISTORICO_MUDANCAS_E_USO_IA.md), com resumo por dia, problemas estudados, três cenários para cada tema e exemplos de prompts. Os exemplos de perguntas foram organizados a partir dos problemas do período e não são transcrições verificadas de conversas.

O [PR #12](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/12), sobre cadastro e edição de perfil, segue aberto como rascunho e não foi tratado como concluído. Os 165 testes históricos continuam sendo planejamento; a evidência de execução depende dos resultados do CI.

**Arquivos atualizados nesta revisão:** `docs/controle/VERSIONAMENTO.md`, `docs/controle/HISTORICO_MUDANCAS_E_USO_IA.md`, `docs/README_DOCUMENTACAO.md`, `docs/INVENTARIO_PROJETO.md` e `docs/APRESENTACAO_PROJETO.md`. Nenhum código da aplicação ou ambiente produtivo foi alterado.

Naquela revisão, um roteiro de apresentação (posteriormente retirado do repositório) foi atualizado para explicar histórico do código, controle das versões documentais e uso de IA. A versão dos documentos não é a mesma versão do build Gradle (`0.0.1-SNAPSHOT`). Os exemplos de perguntas ajudam a explicar as análises; commits, PRs e testes demonstram as mudanças efetivamente realizadas.

## v1.2.0 — inventário integral, documentação atual e trilha didática (13/11/2026)

Esta revisão é **exclusivamente documental e de verificação de documentação**. O material se baseia no código API Kotlin e web React corrente, após homologação do CD automático nos runs [API #43465999269](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269) e [web #43465805656](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656).

| ID | Documento | Finalidade |
|---|---|---|
| FB-DOC-HUB-001 | [Índice atual](../README_DOCUMENTACAO.md) | Navegar toda a documentação |
| FB-INVENT-001 | [Inventário de arquivos](../INVENTARIO_PROJETO.md) | Todos os caminhos versionados, com validação Git por CI |
| FB-API-CAT-001 | [Catálogo REST](../REFERENCIA_API_ATUAL.md) | 50 endpoints de nove controllers |
| FB-ARC-ATUAL-001 | [Arquitetura atual](../ARQUITETURA_ATUAL.md) | Diagramas Mermaid de camadas, ER, fluxo e Oracle |
| FB-FUNC-ATUAL-001 | [Recursos e estado](../RECURSOS_E_STATUS.md) | Implementado x testado x riscos pendentes |
| FB-ESTUDO-001 | [Guia de estudo](../GUIA_ESTUDO.md) | Trilha de aprendizado, exercícios e perguntas |
| FB-DEMO-001 | Apresentação (arquivo retirado da árvore atual na v1.2.4) | Pitch, slides e demonstração: registro histórico |

O gerador versionado `tools/atualizar_inventario.py` executa `git ls-files`; o contrato `tests/ci/test_inventory_contract.py` falha se qualquer arquivo rastreado não constar no inventário, ou houver arquivo obsoleto. **Isso controla a lista de caminhos, não garante a atualização semântica de texto ao alterar apenas conteúdo.** PRs futuros devem atualizar o mapa funcional, API e diagrams quando alterarem comportamento.

As especificações de 29/10 e 11/11 permanecem como histórico: o número antigo de 49 endpoints foi substituído por 50 com `/health/live`, e as flags de deploy manual não descrevem mais a operação atual.


**Identificador:** FB-DOC-CTRL · **Versão do conjunto:** 1.1.0 · **Atualização:** 11/11/2026 · **Responsável técnico:** mantenedor do repositório / revisão de código por PR.

## Documentos controlados

| ID | Documento | Versão | Base de código | Situação |
|---|---|---|---|---|
| FB-PROJ-001 | [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) | 1.1.0 | Histórico 6116749; confronto com main 2d0b63e41 | Atualização documental |
| FB-RSK-001 | [Riscos](../projeto/RISCOS.md) | 1.1.0 | Histórico 6116749 | Revalidação pendente |
| FB-UML-001 | [Arquitetura e catálogo UML](../uml/README.md) | 1.1.0 | Histórico 6116749 | Diagramas reconstruídos |
| FB-TST-001 | [Plano de testes](../testes/PLANO_DE_TESTES.md) | 1.1.0 | Histórico 6116749 | 165 planejados, parcial CI |
| FB-TRC-001 | [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md) | 1.1.0 | Histórico 6116749 | 49 endpoints auditados |

## v1.2.1 — escopo da documentação de infraestrutura (13/11/2026)

Revisão somente documental: diagramas, mapa de recursos e relatórios de implantação passam a representar **exclusivamente serviços e dados do FoodBridge** hospedados na Oracle. O diagnóstico de rede passa a ser [DIAGNOSTICO_REDE_FOODBRIDGE.md](../implantacao/DIAGNOSTICO_REDE_FOODBRIDGE.md), restrito ao site/Nginx, API/systemd, PostgreSQL da aplicação, SSH e saúde. O documento anterior, que misturava projetos hospedados na VM, sai da árvore corrente (permanece recuperável pelo histórico Git).

O inventário de arquivos do repositório acompanha a alteração de caminho. Não foram alterados scripts de deploy, configuração da VM, banco, serviços nem workflows do CI/CD automático.

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

## Integração funcional FB-INT-001 — 13/11/2026

Branch de integração baseada em main `32e13fdd8c2b3c9fd4784ccc7e77fdd2df462224`.
Consolida o código dos PRs #4 (organizações), #5 (voluntários), #6 (estoque),
#9 (timeout de 20 s) e #10 (validação no clone), preservando suas suítes.
O conflito funcional da coleta foi resolvido com `findByIdForColeta`, sem liberar acesso global a voluntários.

Correções adicionais: revalidação de usuário/vínculo/organização em cada JWT autenticado;
bloqueio de contas suspensas inclusive ADMIN; vínculo real na resposta de seleção;
validação do enum de organização; transações de onboarding/admin/exclusão;
criação concorrente de requisições sob o mesmo lock da doação;
recusa de quantidades não finitas e doações expiradas; voluntário ativo novamente conferido na conclusão;
JWT inválido → 401 e método HTTP incorreto → 405, sem revelar credenciais.

`FluxoFuncionalHttpIntegrationTest` acrescenta sete testes HTTP/JWT/PostgreSQL:
login/seleção, vínculo revogado, ADMIN suspenso, cadastro organizacional sem autoaprovação,
ciclo doação → solicitação → aprovação → voluntário → coleta → indicadores,
método incorreto e JWT malformado. **Novos testes pendentes da execução do CI nesta revisão.**

Esta integração não comprova os 165 cenários planejados, o navegador real,
a prontidão dos papéis de gestor, o SSH do runner, deploy ou rollback.
A entrega total exige fechar a matriz [ENTREGA_FUNCIONAL](../projeto/ENTREGA_FUNCIONAL.md)
com evidência por módulo. Produção, serviço, Nginx e banco original não foram alterados.
 
### Evidência combinada e publicação manual
O [Actions #43336071509](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43336071509)
no SHA `c7886081a2130a51ee42786336f731cfceb62bf6` aprovou `clean test bootJar`,
PostgreSQL efêmero, sintaxe Bash e 18 testes de contrato. Preflight/Oracle ficaram skipped.
O frontend integrado está no [PR web #3](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/pull/3).
A revisão atual torna o deploy da API **exclusivamente manual** (`workflow_dispatch`,
`deploy=true`, `preflight=false` e `OCI_DEPLOY_ENABLED=true`).
Um push/merge na main não inicia deploy, independentemente de variáveis antigas de auto-deploy.
O contrato automatizado verifica essa restrição; a nova revisão exige seu próprio CI.

## Perfil de usuário — FB-USR-002
Correção de USR-07: campos ausentes no PUT preservam nome/e-mail/senha.
Cadastro usa Bean Validation; atualização aceita campos opcionais e recusa nome/e-mail
em branco. Status informado pelo cliente não é aplicado. Exclusão segue transacional.

| Métodos em PerfilHttpIntegrationTest | Casos do plano |
|---|---|
| cadastroValidoUsaBcryptENaoAceitaAutoAprovacao | USR-01/02/04/11 |
| cadastroInvalidoRecusaNomeEmailESenha | USR-03 |
| alterarApenasNomePreservaEmailSenhaEStatus | USR-07/11 |
| emailDuplicadoNaoAlteraPerfil | USR-08 |
| trocaDeSenhaInvalidaSenhaAntigaEPermiteNova | USR-09 |
| exclusaoRemoveContaEVinculosEPreservaOrganizacao | USR-10/AUT-15 |

Seis novos testes HTTP/PostgreSQL estão adicionados no PR #12.
Execução do CI ainda pendente nesta revisão; não é homologação do navegador/VM.

### FB-USR-003 — Security matcher DELETE /usuarios/me (13/11/2026)

- O CI [#43339468251](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43339468251) reprovou 1/60 testes: `PerfilHttpIntegrationTest.exclusaoRemoveContaEVinculosEPreservaOrganizacao`, `expected 204 but was 403`. Evidencia direta do XML `foodbridge-relatorios` do run.
- Causa: `.requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasRole("ADMIN")` em `SecurityConfig` interceptava `/usuarios/me` antes da regra de token ACCESS.
- Correcao isolada: matcher exato `DELETE /usuarios/me` com `TOKEN_ACCESS` antes da restricao administrativa. Permanecem proibidas exclusoes de terceiros por usuario comum. Teste HTTP/PostgreSQL negativo adicionado para `DELETE /usuarios/{id}`; teste de exclusao transacional existente deve passar (204 e posteriormente JWT 403).
- O resultado final depende de novo CI; nenhum deploy Oracle/alteracao na VM foi feito.
