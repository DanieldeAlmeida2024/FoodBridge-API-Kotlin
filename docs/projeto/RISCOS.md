# FoodBridge — riscos técnicos e não conformidades a validar

> **FoodBridge / Registro de riscos** · Documento **FB-RSK-001** · Versão **1.1.0** · Revisão **2026-11-11**
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

## Critério para encerramento

Nenhum risco é encerrado somente por documentação. Exigir código corrigido, teste reproduzível (preferencialmente negativo + positivo), evidência de CI e atualização da matriz.

## Riscos de publicação Oracle identificados em 12/11/2026 — FB-OCI-CD v1.2.0

- **OCI-R01 · P0:** a API pode atualizar esquema durante restart se `spring.jpa.hibernate.ddl-auto=update`. O pipeline exige `validate` ou `none` no arquivo externo e override systemd compatível, mas **não inspeciona outras fontes de propriedades** nem garante ausência de migrações executadas pela aplicação. Não trocar a configuração sem migrações e backup aprovados.
- **OCI-R02 · P0:** a unit systemd pode iniciar um JAR diferente do substituído pelo pipeline. Preflight, preparo e update verificam ExecStart/WorkingDirectory para impedir falso sucesso.
- **OCI-R03 · P0:** só o CI teve sucesso; nos runs da main o job de deploy foi `skipped`. Falta comprovação SSH/known_hosts/config/health/rollback na VM. A preparação e preflight dependem de ação explícita do mantenedor.
- **OCI-R04 · Alto:** usuário `opc` com `sudo -n` é amplo; limitar a conta de deploy, usar chave dedicada e proteção do environment antes de autorizar auto-deploy.
- **OCI-R05 · Alto:** cancelar um workflow durante swap/restart pode interromper rollback. `cancel-in-progress=false` impede cancelamento automático por novo run do mesmo grupo, mas não evita queda do runner/VM. Ensaiar recuperação manual.
- **OCI-R06 · P0:** PRs #4/#5/#6 de segurança funcional seguem draft; CI verde de `main` não substitui testes de autorização/tenant antes de publicar externamente.

- **OCI-R07 · Alto:** arquivos `application.properties` com definições duplicadas de `spring.jpa.hibernate.ddl-auto` podem sobrepor uma configuração segura por outra mutante. Preflight e script de atualização agora exigem **exatamente uma** definição explícita e com valor `validate` ou `none`, sem imprimir a configuração. Fontes externas ao arquivo ainda exigem auditoria do runtime.
