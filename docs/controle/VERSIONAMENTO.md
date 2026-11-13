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
