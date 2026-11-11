# FoodBridge — especificação do projeto

> **FoodBridge / Projeto e requisitos** · Documento **FB-PROJ-001** · Versão **2.0.0 proposta** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


## Atualização de segurança — PR #4 (proposta, ainda não integrada à main)

**Fonte de código:** [commit 9d96afb](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/commit/9d96afb6e86a0fbb5044c21eaf7619fb6d311a62). Esta seção atualiza somente o comportamento da branch de correção; as demais tabelas do documento preservam o inventário histórico.

- `PUT /organizacoes/{id}` (API-26): `OrganizacaoService.update` agora recebe `TokenData` e **antes de consultar ou persistir** verifica `organizacaoId` do token contra o ID-alvo, `role == ADMIN`, `vinculoId`, status de usuário verificado e vínculo vigente do usuário/organização. A consulta ao repositório compara ID, role, status do vínculo, status do usuário e organização.
- `PATCH /organizacoes/usuarios/{vinculoId}/aprovar` (API-28) e `/reprovar` (API-29): removido o cast inválido de `TokenData.role: String?` para `OrganizacaoRole`. As duas operações validam o administrador pelo banco **e** que o vínculo-alvo pertença à organização do contexto.
- `GlobalExceptionHandler`: `AccessDeniedException` recebe HTTP 403 com resposta neutra.
- **Restrições atuais:** o enum `OrganizacaoRole` ainda representa tipo organizacional e papel de membro sem separação de função de gestor. Como medida temporária e conservadora, **somente vínculo ADMIN verificado** tem permissão de atualização/aprovação; gestão por DOADOR/ONG/PRODUTOR/DISTRIBUIDOR exige decisão de RBAC e evolução segura do contrato.
- **Não coberto:** `GET /organizacoes/{id}`, informações de `Voluntario`/CPF, `DELETE` em nível de vínculo, revogação de outros endpoints, DTOs JPA aninhados e autorização de outros módulos. Não extrapolar o resultado destes testes.
- **Evidência:** cinco novos métodos em `OrganizacaoServiceAutorizacaoTest`; build e testes aprovados no [CI #43081581178](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43081581178). Nenhum teste de integração PostgreSQL/HTTP nem deploy foi validado.

| Rota | Guarda original do inventário | Guarda adicionada na branch de PR #4 | Status |
|---|---|---|---|
| API-26 `PUT /organizacoes/{id}` | ACCESS apenas | Vínculo ADMIN verificado, organização igual ao alvo, reconsulta ao banco | Código + unitários CI aprovados; integrar |
| API-28 `PATCH .../{vinculoId}/aprovar` | ACCESS apenas + cast inválido | Vínculo ADMIN verificado; vínculo-alvo no mesmo tenant | Código + unitários parciais; integrar |
| API-29 `PATCH .../{vinculoId}/reprovar` | ACCESS apenas + cast inválido | Vínculo ADMIN verificado; vínculo-alvo no mesmo tenant | Código compilado; falta caso específico |

## Política de dados de voluntários — revisão 2.0.0 proposta

**Mudança de contrato:** os 6 endpoints API-44..49 em `/voluntarios` passam a exigir contexto de organização efetivo e vínculo autenticado, com status verificado consultado no banco. CPF, e-mail, telefone e endereço são dados pessoais; o endpoint não deve consultar perfis globais sem autorização por tenant. A consulta por ID/CPF não retorna dados de outra organização (404). A listagem retorna apenas vínculos ATIVOS da organização e o PUT recusa perfis associados a outras organizações por compartilharem uma entidade global.

| Rota | Guarda introduzida | Limite explícito |
|---|---|---|
| POST /voluntarios | Vínculo de usuário VERIFICADO em banco + transação de criação/vínculo | CPF/e-mail já existentes retornam 409 genérico; transferência entre organizações **não implementada** |
| GET /voluntarios/{id} | Vínculo de usuário e de voluntário ATIVO na mesma organização | Negar 404 a voluntário não vinculado |
| GET /voluntarios/cpf/{cpf} | Vínculo de usuário e de voluntário ATIVO na mesma organização | Consulta CPF global é apenas resolução interna, não entrega acesso a outro tenant |
| GET /voluntarios | Repositorio de vínculos por `organizacaoId`, ATIVO | Lista exclusivamente registros acessíveis |
| PUT /voluntarios/{id} | Vínculo ativo + bloqueio para cadastro global com outro vínculo organizacional | Edição por qualquer membro verificado ainda exige RBAC mais granular |
| PATCH /voluntarios/{id}/desativar | Revalida vínculo de usuário + altera somente vínculo do mesmo tenant | Não apaga cadastro global ou outros vínculos |

**Compatibilidade da coleta:** `RequisicaoDoacaoService` não consulta mais voluntário por ID global sem escopo; utiliza `findByIdForColeta` com organização da requisição, já validada contra a organização solicitante. **O modelo continua global e compartilhado:** a autorização do titular, manutenção de dados e acesso por diferentes organizações exigem projeto de domínio específico.

**Evidência:** 13 testes unitários novos, pendentes de execução de CI na data da edição. Os 165 casos do plano original permanecem com identificação, mas os critérios VOL-02, VOL-07, VOL-08, VOL-10 e VOL-12 são substituídos nesta versão. [Documento próprio de política](POLITICA_DADOS_VOLUNTARIOS.md).

## Propósito
Sistema de coordenação de redistribuição de alimentos entre organizações doadoras, produtoras, distribuidoras e ONGs, com cadastros de usuários, vínculos institucionais, voluntários, solicitações de retirada e indicadores de impacto. A especificação abaixo preserva a análise estática de 29/10/2026.

## 1. Objetivo, escopo e limites
Elaborar uma estratégia de verificação funcional e não funcional com rastreabilidade direta aos 49 endpoints REST do FoodBridge e aos componentes do seu domínio. O pacote UML cobre os fluxos implementados, seus estados e as principais integrações internas.
- Base estática: branch main e commit 6116749 de 22/10/2026; fonte de verdade é o código do commit, não o histórico de deploy.
- Inspecionados 8 controllers, services correlatos, modelos JPA, enums, repositories, DTOs, configurações, Postman e src/test.
- Existem dois arquivos de classes de teste JUnit identificados: um teste de contexto e oito métodos @Test na classe de cálculo de impacto alimentar; não há suíte automatizada cobrindo os demais controllers no snapshot.
- Os serviços definem regras e estados observados; expectativas de segurança representam critério de aceitação e não funcionamento validado.
- Nenhuma execução de ./gradlew test, testes contra banco ou inspeção de produção foi efetuada nesta análise.

## 2. Inventário de arquitetura e funcionalidades
**Stack observada:** Kotlin 2.2.21; Spring Boot 3.2.5; Java 21; Gradle Wrapper 8.7; Spring Security/JWT (jjwt 0.11.5); JPA/Hibernate; PostgreSQL; validação Bean Validation; JUnit 5.
**Estrutura:** Controllers REST → Services / JwtFilter / JwtService → Spring Data JPA → PostgreSQL, com JSONB para endereços e janelas de coleta. Os papéis de organização são DOADOR, PRODUTOR, DISTRIBUIDOR, ONG e ADMIN.

| Módulo | Controller | Serviço principal | Operações |
|---|---|---|---:|
| Administração e verificações | `AdminController` | `AdminService` | 8 |
| Autenticação, JWT e seleção de organização | `AuthController` | `AuthService / JwtService` | 2 |
| Dashboards e impacto alimentar | `DashboardController` | `DashboardService / ImpactoAlimentarCalculator` | 3 |
| Publicação, manutenção e encerramento de doações | `DoacaoController` | `DoacaoService` | 7 |
| Cadastro, consulta e gestão de organizações | `OrganizacaoController` | `OrganizacaoService` | 9 |
| Solicitações e ciclo de coleta | `RequisicaoDoacaoController` | `RequisicaoDoacaoService` | 9 |
| Usuários e dados pessoais | `UsuarioController` | `UsuarioService` | 5 |
| Voluntários e vínculos | `VoluntarioController` | `VoluntarioService` | 6 |

### 2.1 Catálogo completo de endpoints e autorização observada
**Atenção:** TOKEN_ACCESS não implica necessariamente autorização por entidade/tenant. As notas de serviço são checagens específicas; lacunas são analisadas nos riscos.
| ID | Método | Rota | Guarda observada | Finalidade |
|---|---|---|---|---|
| API-01 | GET | `/admin/organizacoes` | ROLE_ADMIN | Listar organizações |
| API-02 | GET | `/admin/organizacoes/pendentes` | ROLE_ADMIN | Pendências organizacionais |
| API-03 | PATCH | `/admin/organizacoes/{id}/aprovar` | ROLE_ADMIN | Aprovar organização |
| API-04 | PATCH | `/admin/organizacoes/{id}/reprovar` | ROLE_ADMIN | Reprovar organização |
| API-05 | GET | `/admin/usuarios` | ROLE_ADMIN | Listar usuários |
| API-06 | GET | `/admin/usuarios/pendentes` | ROLE_ADMIN | Listar pendências de usuários |
| API-07 | PATCH | `/admin/usuarios/{id}/aprovar` | ROLE_ADMIN | Aprovar usuário |
| API-08 | PATCH | `/admin/usuarios/{id}/reprovar` | ROLE_ADMIN | Reprovar usuário |
| API-09 | POST | `/auth/login` | Público | Login e emissão TEMP/ACCESS |
| API-10 | POST | `/auth/select-org` | TOKEN_TEMP | Seleção de organização |
| API-11 | GET | `/dashboards/publico` | Público | Indicadores agregados |
| API-12 | GET | `/dashboards/doador` | TOKEN_ACCESS + role doadora | Indicadores de organização doadora |
| API-13 | GET | `/dashboards/ong` | TOKEN_ACCESS + role ONG | Indicadores de ONG |
| API-14 | POST | `/doacoes` | TOKEN_ACCESS | Criar doação |
| API-15 | GET | `/doacoes/{id}` | TOKEN_ACCESS | Consultar doação |
| API-16 | GET | `/doacoes/disponiveis` | TOKEN_ACCESS | Listar disponíveis de outras organizações |
| API-17 | GET | `/doacoes` | TOKEN_ACCESS | Listar da organização do token |
| API-18 | PUT | `/doacoes/{id}` | TOKEN_ACCESS + dono (service) | Editar doação |
| API-19 | PATCH | `/doacoes/{id}/fechar` | TOKEN_ACCESS + dono (service) | Fechar doação |
| API-20 | DELETE | `/doacoes/{id}` | TOKEN_ACCESS + dono (service) | Excluir doação |
| API-21 | POST | `/organizacoes` | TOKEN_TEMP | Cadastrar/vincular organização |
| API-22 | GET | `/organizacoes/cnpj/{cnpj}` | TOKEN_TEMP | Consulta por CNPJ |
| API-23 | GET | `/organizacoes/{id}` | TOKEN_ACCESS | Consultar organização |
| API-24 | GET | `/organizacoes` | ROLE_ADMIN | Listar organizações |
| API-25 | GET | `/organizacoes/me` | TOKEN_ACCESS | Organização no token |
| API-26 | PUT | `/organizacoes/{id}` | TOKEN_ACCESS | Atualizar organização |
| API-27 | DELETE | `/organizacoes/{id}` | ROLE_ADMIN | Inativar organização |
| API-28 | PATCH | `/organizacoes/usuarios/{vinculoId}/aprovar` | TOKEN_ACCESS | Aprovar vínculo interno |
| API-29 | PATCH | `/organizacoes/usuarios/{vinculoId}/reprovar` | TOKEN_ACCESS | Reprovar vínculo interno |
| API-30 | POST | `/requisicoes` | TOKEN_ACCESS + ONG (service) | Criar solicitação |
| API-31 | GET | `/requisicoes/minhas` | TOKEN_ACCESS + ONG (service) | Solicitações da ONG |
| API-32 | GET | `/requisicoes/recebidas` | TOKEN_ACCESS + papel doador (service) | Solicitações recebidas |
| API-33 | GET | `/requisicoes/{id}` | TOKEN_ACCESS + parte (service) | Visualizar solicitação |
| API-34 | PATCH | `/requisicoes/{id}/vincular-voluntario` | TOKEN_ACCESS + ONG titular (service) | Vincular voluntário ativo |
| API-35 | PATCH | `/requisicoes/{id}/aprovar` | TOKEN_ACCESS + doador titular (service) | Aprovar solicitação |
| API-36 | PATCH | `/requisicoes/{id}/rejeitar` | TOKEN_ACCESS + doador titular (service) | Rejeitar solicitação |
| API-37 | PATCH | `/requisicoes/{id}/cancelar` | TOKEN_ACCESS + ONG titular (service) | Cancelar solicitação |
| API-38 | PATCH | `/requisicoes/{id}/concluir` | TOKEN_ACCESS + doador titular (service) | Concluir coleta |
| API-39 | POST | `/usuarios` | Público | Registrar usuário |
| API-40 | GET | `/usuarios/me` | TOKEN_ACCESS | Consultar perfil |
| API-41 | GET | `/usuarios/me/organizacoes` | TOKEN_ACCESS | Listar organizações do usuário |
| API-42 | PUT | `/usuarios/me` | TOKEN_ACCESS | Editar perfil |
| API-43 | DELETE | `/usuarios/me` | TOKEN_ACCESS | Excluir conta e vínculos |
| API-44 | POST | `/voluntarios` | TOKEN_ACCESS | Criar ou vincular voluntário |
| API-45 | GET | `/voluntarios/{id}` | TOKEN_ACCESS | Buscar voluntário por ID |
| API-46 | GET | `/voluntarios/cpf/{cpf}` | TOKEN_ACCESS | Buscar por CPF |
| API-47 | GET | `/voluntarios` | TOKEN_ACCESS | Listar voluntários |
| API-48 | PUT | `/voluntarios/{id}` | TOKEN_ACCESS | Editar voluntário |
| API-49 | PATCH | `/voluntarios/{id}/desativar` | TOKEN_ACCESS + vínculo (service) | Desativar vínculo |

### 2.2 Entidades e enumerações
**Entidades:** Usuario, Organizacao, UsuarioOrganizacao, Voluntario, VoluntarioOrganizacao, Doacao e RequisicaoDoacao. **Objetos de valor:** Endereco e JanelaHorario (JSONB).
**Estados:** Usuario: PENDENTE_VERIFICACAO, VERIFICADO, SUSPENSO, INATIVO. Organizacao: DOCUMENTOS_PENDENTES, REVISAO, VERIFICADO, SUSPENSO, INATIVO. VoluntarioOrganizacao: ATIVO, INATIVO, SUSPENSO. Doacao: RASCUNHO, PUBLICADO, DISPONIVEL, PARCIALMENTE_REIVINDICADO, TOTALMENTE_REIVINDICADO, COMPLETO, EXPIRADO, CANCELADO. Requisicao: PENDENTE, APROVADO, CONCLUIDO, REJEITADO, CANCELADO.
**Unidades:** KG, LITRO, UNIDADE, CAIXA e LOTE. **Categorias:** VEGETAIS, FRUTAS, GRAOS, LATICINIO, CARNE, COMIDA_PREPARADA, PADARIA, BEBIDAS e OUTROS.

## Regras de negócio e decisões pendentes

- **Contexto organizacional:** autenticação inicial e seleção de organização, com tokens `TEMP` e `ACCESS` e autorização por papel e vínculo; autenticação não significa permissão sobre qualquer registro.
- **Doação e requisição:** solicitação só deve reservar saldo da doação disponível; aprovação deve considerar concorrência entre ONGs.
- **Coleta:** voluntário deve possuir vínculo ativo; quantidade concluída deve refletir dados confirmados, não a quantidade originalmente ofertada.
- **Indicadores:** valores consolidados respeitam a unidade (KG/LITRO/UNIDADE/CAIXA/LOTE). Fechamentos manuais e coleta parcial não podem produzir dupla contagem.
- **Dados pessoais:** limitar leitura de CPF, e-mail e endereço ao papel/organização legitimados; critérios de LGPD dependem de análise jurídica e operacional adicional.

## Evidência e atualização

A tabela de endpoints acima reflete **49 rotas auditadas no commit histórico**. Revalidar controllers, DTOs, guardas e contratos a cada alteração no código. Os casos de aceitação estão no [plano mestre](../testes/PLANO_DE_TESTES.md), e a correspondência rota-teste na [matriz](../testes/MATRIZ_RASTREABILIDADE.md).

## Correções posteriores conhecidas

- O PR #1 de 11/11/2026 retirou logs sensíveis do `JwtFilter`, corrigiu sintaxe do arquivo e incorporou CI com PostgreSQL efêmero. Isso **não** resolve automaticamente todos os riscos de autorização e concorrência do snapshot histórico.
- O CI da `main` validou compilação/testes após o PR, mas **não executou os 165 cenários** descritos no plano de testes.
- O deploy Oracle Cloud é preparado por workflow, porém a ativação do SSH na VM e a liberação de produção exigem procedimentos próprios.

## Documentos complementares

- [Arquitetura e catálogo UML](../uml/README.md)
- [Riscos e pendências](RISCOS.md)
- [Plano mestre de testes](../testes/PLANO_DE_TESTES.md)
- [Matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md)
- [Versionamento documental](../controle/VERSIONAMENTO.md)
