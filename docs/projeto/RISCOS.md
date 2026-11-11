# FoodBridge — riscos técnicos e não conformidades a validar

> **FoodBridge / Registro de riscos** · Documento **FB-RSK-001** · Versão **1.2.0** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


Os itens abaixo são **achados de inspeção estática do commit de outubro**. Não os tratar automaticamente como vulnerabilidades presentes em `main` em 11/11/2026 sem reinspeção. O item R01 teve correção parcial posterior relacionada aos logs JWT.

## 7. Análise de riscos e não conformidades a validar
A classificação abaixo resulta da inspeção do código (risco potencial ou defeito provável), não de execução em ambiente real.
| ID | Severidade | Local | Evidência e risco | Tratamento / testes |
|---|---|---|---|---|
| R01 | Crítico | JwtFilter.kt | Logs imprimem Bearer completo e token extraído, incluindo dados do token. | Remover prints; mascarar logs; testes SEG-01. |
| R02 | Crítico | OrganizacaoController.kt | role do token é String? e sofre cast para OrganizacaoRole, podendo causar erro 500 nos PATCHs de vínculo. | Converter com valueOf validado ou usar role tipado; ORG-14/15. |
| R03 | Crítico | OrganizacaoController / OrganizacaoService | PUT /organizacoes/{id} não verifica organização do token nem role; qualquer ACCESS pode editar outras organizações. | Autorizar ownership e perfil; ORG-10/11, SEG-03. |
| R04 | Alto | VoluntarioController / VoluntarioService | GET geral/por CPF/ID e PUT não verificam pertencimento; expõem PII e permitem edição por outro tenant. | Restringir por vínculo e finalidade, definir política LGPD; VOL-07/08. |
| R05 | Crítico | DoacaoService / RequisicaoDoacaoService | Aprovação lê saldo sem lock/versionamento explícitos; aprovações simultâneas podem comprometer o estoque. | Pessimistic lock ou CAS/versionamento e testes concorrentes; REQ-15. |
| R06 | Alto | DoacaoService.kt | POST/PUT aceitam status de cliente; PUT não valida quantidade > 0, dataExpiracao ou transições. | DTO comando separado, FSM/guards no service; DOA-05/12/20. |
| R07 | Alto | AuthService / JwtFilter | ADMIN tem acesso direto se vínculo verificado, sem guarda explícita de status verificado na branch admin; JwtFilter isenta ADMIN da validação de status. | Revalidar status e políticas de revogação; AUT-16, SEG-06. |
| R08 | Alto | OrganizacaoController / DTO | @Valid não aplicado nas operações de organização; restrições anotadas podem não ser executadas. | Aplicar @Valid + normalização/validação CNPJ/e-mail; ORG-16. |
| R09 | Alto | UsuarioController / UsuarioService | PUT /usuarios/me com DTO de criação usa defaults vazios; nome apenas pode apagar e-mail; @Valid ausente. | Separar CreateUser/UpdateUser com nulls reais; USR-07. |
| R10 | Alto | DoacaoService.kt | Fechar manualmente não reconcilia requisições aprovadas; recálculo pode alterar COMPLETO indevidamente. | Definir política de fechamento e consistência de estados; DOA-16/17. |
| R11 | Alto | OrganizacaoService / VoluntarioService | Criação da entidade e do vínculo envolve múltiplos saves sem @Transactional nesses fluxos. | Adicionar fronteiras transacionais; INT-05/06. |
| R12 | Médio | Postman collections | Coleções referenciam /usuarios/criar e /doacao, diferentes dos endpoints reais. | Regenerar coleção ou OpenAPI do código; INT-11. |
| R13 | Alto | JwtService.kt | generateAccessFromTemp tenta cast usuario.status as String (enum), caminho potencialmente inválido quando usado. | Trocar para .name e adicionar teste unitário específico. |
| R14 | Alto | DoacaoController / DoacaoService | Criar doação não exige role de doador; ONG com ACCESS e organização pode publicar. | Restringir por DOADOR/PRODUTOR/DISTRIBUIDOR; DOA-06. |
| R15 | Médio | DTO/Entidades | Saídas de organização contêm entidades JPA aninhadas, com risco de serialização LAZY, circularidade, dados excessivos e N+1. | Respostas DTO planas, testes de integração de JSON. |
| R16 | Médio | application.properties.example | Exemplo habilita ddl-auto=update e show-sql=true sem perfil de produção. | Flyway/Liquibase e configuração segregada; NFR-05. |
| R17 | Médio | RequisicaoDoacaoService | Quantidade disponível calculada com Double, sem política de precisão por unidade. | Precisão decimal e restrição por unidade; REQ-25. |
| R18 | Alto | AdminService / Organização | Fluxos de mudança de status não invalidam os JWTs previamente emitidos; contexto pode ficar obsoleto. | Política de revogação/consulta do vínculo; AUT-14/15. |
| R19 | Médio | Model/Usuario.kt | Status do usuário enum sem @Enumerated; por padrão ORM usa ordinal, sensível a ordem. | Migrar com script e controle de dados; INT-08. |
| R20 | Alto | VoluntarioService.kt | Reativação de vínculo INATIVO pelo POST não ocorre, pois existsBy... retorna true mesmo inativo. | Criar endpoint de reativação ou branch de reuso; VOL-10. |

## Atualização posterior ao snapshot

| Risco | Estado da verificação em 11/11/2026 | Evidência |
|---|---|---|
| R01 — logs JWT | Correção do filtro integrada; exigir regressão para toda a aplicação | PR #1; inspeção do `JwtFilter`; CI passou |
| R02–R20 | **Pendente de nova inspeção e testes**; não declarar corrigido | Remeter aos IDs do plano de testes; abrir tickets por risco P0 |

## Revisão de riscos na branch de segurança (PR #4, 11/11/2026)

| ID | Situação em PR #4 | Evidência objetiva | Bloqueio restante |
|---|---|---|---|
| R02 — cast `String → OrganizacaoRole` | **Mitigação implementada, PR não integrado** | Remoção do cast nos dois PATCHs; compilação/testes CI #43081581178 aprovados | Testar HTTP de aprovar **e** reprovar com autenticação real |
| R03 — PUT cross-tenant | **Mitigação implementada, PR não integrado** | Revalidação do vínculo no banco; testes unitários de organização diversa, não-admin, revogação e operação permitida | Integração PostgreSQL/MockMvc; revisão de papéis |
| R07/R18 — tokens desatualizados | **Mitigação parcial somente nestas mutações** | Service consulta status do usuário/vínculo/organização antes de escrever/aprovar | Outros endpoints ainda podem aceitar JWT antigo; política de revogação central pendente |
| R04 — CPF e voluntários | **Aberto** | Não alterado no PR | Guards por tenant e minimização de dados |
| R15 — DTO com JPA aninhado | **Aberto** | Não alterado no PR | DTOs de resposta estáveis sem relações internas |
| Modelo de gestor | **Pendente de decisão** | ADMIN é o único papel administrativo disponível nesta regra | Separar tipo de organização de papel do membro, sem ampliação implícita de acesso |

**Não classificar R02/R03 como encerrados** apenas pelo CI unitário. O PR permanece rascunho e não foi publicado em produção.

## Incremento no PR #4 — R03 e API-27

**Achado adicional confirmado:** `OrganizacaoController.deletar` verificava somente `tokenData.role == ADMIN`, permitindo que um ADMIN de outra organização inativasse qualquer `id`. O serviço `inativarOrganizacao(id, tokenData)` agora revalida **ID/role/status do vínculo atual no banco** e tenant alvo, como no PUT.

**Teste de integração real:** `OrganizacaoAutorizacaoHttpIntegrationTest` possui 7 métodos com `@SpringBootTest @AutoConfigureMockMvc`: PUT cross-tenant, DELETE cross-tenant, DELETE permitido, DELETE com vínculo revogado, DELETE sem role ADMIN, aprovação/reprovação em outro tenant. Requer CI para confirmar, pois erros 403 podem depender de filtros HTTP e persistência.

**Estado de R03:** mitigação ampliada no código do PR #4; **não encerrada em produção**. Devem ser revisadas as regras de leitura organizacional, DTOs e papéis futuros.

## Critério para encerramento

Nenhum risco é encerrado somente por documentação. Exigir código corrigido, teste reproduzível (preferencialmente negativo + positivo), evidência de CI e atualização da matriz.
