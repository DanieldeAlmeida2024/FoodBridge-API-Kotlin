# FoodBridge API — catálogo atual de rotas REST

> **FB-API-CAT-001 · 13/11/2026 · referência do código `main` antes desta revisão documental.**
> Fonte: os nove controllers em `src/main/kotlin/com/br/foodbridge/controller/`, `SecurityConfig.kt`, `JwtFilter.kt` e services. Existem **50 mappings HTTP** (49 fluxos de negócio/gestão + liveness). A lista de 49 rotas na especificação histórica não inclui `/health/live`.

## Convenções, autenticação e cuidados

- JSON sobre HTTP; `Authorization: Bearer <token>` nas rotas protegidas.
- `POST /auth/login` autentica e pode devolver token `TEMP` ou `ACCESS`, conforme a situação. `POST /auth/select-org` troca o contexto para vínculo organizacional selecionado.
- `TEMP` **somente** para seleção/cadastro de organização e consulta por CNPJ conforme configuração; `ACCESS` para recursos já vinculados.
- O papel no token não é suficiente sozinho: os serviços validam usuário, organização, vínculo e, quando aplicável, titularidade ou estados no PostgreSQL.
- `/health/live` é liveness HTTP pública **sem consulta ao banco**. `/dashboards/publico` é uma consulta funcional de negócio e pode ser lenta na Micro.
- A classificação abaixo resume a camada de segurança e regras esperadas; para detalhes de autorização por entidade, consultar o service e os testes correspondentes.
- Os caminhos `{id}`, `{cnpj}` e `{vinculoId}` são parâmetros da URL. Não expor dados pessoais nem tokens nas evidências.

## Administração — 8 rotas (`AdminController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| GET | `/admin/organizacoes` | ADMIN | Listar organizações |
| GET | `/admin/organizacoes/pendentes` | ADMIN | Listar pendências institucionais |
| PATCH | `/admin/organizacoes/{id}/aprovar` | ADMIN | Aprovar organização |
| PATCH | `/admin/organizacoes/{id}/reprovar` | ADMIN | Reprovar organização |
| GET | `/admin/usuarios` | ADMIN | Listar usuários |
| GET | `/admin/usuarios/pendentes` | ADMIN | Listar pendências de usuários |
| PATCH | `/admin/usuarios/{id}/aprovar` | ADMIN | Aprovar usuário |
| PATCH | `/admin/usuarios/{id}/reprovar` | ADMIN | Reprovar usuário |

## Autenticação e contexto — 2 rotas (`AuthController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/auth/login` | Público | Login e emissão de token correspondente à situação |
| POST | `/auth/select-org` | TOKEN_TEMP | Selecionar organização e vínculo; emitir ACCESS |

## Indicadores — 3 rotas (`DashboardController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| GET | `/dashboards/publico` | Público | Doações e indicadores agregados |
| GET | `/dashboards/doador` | ACCESS + papel doador | Indicadores de doador/produtor/distribuidor |
| GET | `/dashboards/ong` | ACCESS + ONG | Indicadores e operações da ONG |

## Doações — 7 rotas (`DoacaoController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/doacoes` | ACCESS + regras de doador | Criar doação |
| GET | `/doacoes/{id}` | ACCESS + regras do service | Consultar doação |
| GET | `/doacoes/disponiveis` | ACCESS | Consultar doações disponíveis |
| GET | `/doacoes` | ACCESS | Listar doações da organização |
| PUT | `/doacoes/{id}` | ACCESS + titularidade | Alterar doação |
| PATCH | `/doacoes/{id}/fechar` | ACCESS + titularidade | Fechamento e quantidade coletada |
| DELETE | `/doacoes/{id}` | ACCESS + titularidade | Excluir quando regras permitirem |

## Organizações e vínculos — 9 rotas (`OrganizacaoController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/organizacoes` | TOKEN_TEMP | Criar ou solicitar vínculo |
| GET | `/organizacoes/cnpj/{cnpj}` | TOKEN_TEMP | Consultar organização por CNPJ |
| GET | `/organizacoes/{id}` | ACCESS | Consultar dados institucionais |
| GET | `/organizacoes` | ADMIN | Listar organizações |
| GET | `/organizacoes/me` | ACCESS | Organização selecionada |
| PUT | `/organizacoes/{id}` | ACCESS + checagens de service | Atualizar organização |
| DELETE | `/organizacoes/{id}` | ADMIN | Inativar organização |
| PATCH | `/organizacoes/usuarios/{vinculoId}/aprovar` | ACCESS + administrador vigente | Aprovar vínculo de usuário |
| PATCH | `/organizacoes/usuarios/{vinculoId}/reprovar` | ACCESS + administrador vigente | Reprovar vínculo de usuário |

## Solicitações, coleta e estoque — 9 rotas (`RequisicaoDoacaoController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/requisicoes` | ACCESS + ONG | Solicitar parte de uma doação |
| GET | `/requisicoes/minhas` | ACCESS + ONG | Pedidos da ONG |
| GET | `/requisicoes/recebidas` | ACCESS + doador | Pedidos recebidos |
| GET | `/requisicoes/{id}` | ACCESS + participante | Consultar pedido |
| PATCH | `/requisicoes/{id}/vincular-voluntario` | ACCESS + ONG titular | Designar voluntário ativo |
| PATCH | `/requisicoes/{id}/aprovar` | ACCESS + doador titular | Reservar e aprovar |
| PATCH | `/requisicoes/{id}/rejeitar` | ACCESS + doador titular | Rejeitar pedido |
| PATCH | `/requisicoes/{id}/cancelar` | ACCESS + ONG titular | Cancelar pedido |
| PATCH | `/requisicoes/{id}/concluir` | ACCESS + doador titular | Concluir retirada |

## Usuários — 5 rotas (`UsuarioController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/usuarios` | Público | Cadastro inicial |
| GET | `/usuarios/me` | ACCESS | Perfil do usuário autenticado |
| GET | `/usuarios/me/organizacoes` | ACCESS | Vínculos do usuário |
| PUT | `/usuarios/me` | ACCESS | Editar perfil |
| DELETE | `/usuarios/me` | ACCESS | Remover conta conforme regras |

## Voluntários — 6 rotas (`VoluntarioController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| POST | `/voluntarios` | ACCESS + vínculo elegível | Criar ou vincular voluntário |
| GET | `/voluntarios/{id}` | ACCESS + organização autorizada | Consulta por ID |
| GET | `/voluntarios/cpf/{cpf}` | ACCESS + organização autorizada | Consulta por CPF |
| GET | `/voluntarios` | ACCESS + organização autorizada | Listagem |
| PUT | `/voluntarios/{id}` | ACCESS + organização autorizada | Atualizar cadastro |
| PATCH | `/voluntarios/{id}/desativar` | ACCESS + vínculo elegível | Desativar vínculo |

## Saúde operacional — 1 rota (`HealthController`)

| Método | Rota | Acesso resumido | Operação |
|---|---|---|---|
| GET | `/health/live` | Público | Retorna `{"status":"UP"}`, não consulta PostgreSQL |

**Total conferido:** 8 + 2 + 3 + 7 + 9 + 9 + 5 + 6 + 1 = **50 mappings**.

## Domínio dos payloads

- **Identidade:** `LoginRequest`, `LoginResponse`, `SelectOrganizationRequest`, `TokenData`, `UsuarioDTO`.
- **Organização:** `OrganizacaoDTO`, `UsuarioOrganizacao`, papéis e aprovação.
- **Doações:** `DoacaoDTO`, unidade, tipo, janela de horário, vencimento, local e fechamento.
- **Solicitações:** `RequisicaoDoacaoDTO`, quantidade, observação, voluntário, status e timestamps de resposta/conclusão.
- **Voluntários:** `VoluntarioDTO`, vínculos por organização, CPF e endereço; são dados pessoais, com acesso contextual.
- **Indicadores:** `DashboardDTO`, `ImpactoAlimentarDTO`, agregação por unidade para evitar mistura conceitual de Kg e litros.

Para exemplos de JSON, consultar [API_DOCUMENTATION.md](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/API_DOCUMENTATION.md) e as duas coleções Postman. **Exemplos e documentação legada podem divergir do código**; os controllers e testes são a referência executável.

## Erros e testes

Erros `400`, `401`, `403`, `404` e `409` são relevantes; os contratos de segurança e `GlobalExceptionHandler` definem o comportamento final. A autorização em organizações e voluntários e a concorrência de estoque têm testes próprios. A existência da rota **não** é evidência de teste ponta a ponta em produção.

Leitura complementar: [arquitetura atual](ARQUITETURA_ATUAL.md), [recursos e evidências](RECURSOS_E_STATUS.md), [inventário integral](INVENTARIO_PROJETO.md), [guia de estudo](GUIA_ESTUDO.md).
