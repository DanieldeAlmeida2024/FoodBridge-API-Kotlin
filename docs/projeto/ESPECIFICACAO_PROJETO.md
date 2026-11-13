# FoodBridge — especificação do projeto

> **FoodBridge / Projeto e requisitos** · Documento **FB-PROJ-001** · Versão **1.1.0** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


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
