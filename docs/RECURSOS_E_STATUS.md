# FoodBridge — mapa funcional e estado das entregas

> **FB-FUNC-ATUAL-001 · 13/11/2026.** Mapa obtido dos controllers/services, pages React e workflows. **Implementado no código ≠ totalmente homologado em produção**. “CI PASS” significa execução das suítes presentes, não execução de todos os casos históricos planejados.

## Objetivo e atores

FoodBridge organiza a redistribuição de alimentos e o ciclo institucional entre **DOADOR**, **PRODUTOR**, **DISTRIBUIDOR**, **ONG** e **ADMIN**. Pessoas usuárias possuem conta e podem participar de organizações por meio de vínculo. Voluntários auxiliam coleta vinculados à organização, enquanto doadores publicam e aprovam solicitações.

## Módulos com rastreabilidade

| ID | Recurso | Código de referência | Frontend | Estado observável e ressalvas |
|---|---|---|---|---|
| FB-01 | Cadastro de usuário, login, sessão JWT TEMP/ACCESS | `UsuarioController`, `AuthController`, `AuthService`, `JwtService`, `ContextoAcessoService` | `Register`, `Login`, `AuthContext`, `session.ts` | Código e testes de sessão/integração presentes; sessão web usa localStorage |
| FB-02 | Seleção de contexto organizacional | `AuthService.selectOrganization`, `UsuarioOrganizacao` | `SelectOrganization` | Validado em CI de sessão e fluxo HTTP; dependente de vínculos/status |
| FB-03 | Cadastro e consulta por CNPJ, aprovação de organização | `OrganizacaoController/Service`, `AdminController` | `OrganizationForm`, `Admin` | Código e testes de autorização; revisão operacional de processos reais pendente |
| FB-04 | Administração de usuários e entidades | `AdminController`, `AdminService` | `Admin` | Rotas de pendências/aprovação/reprovação implementadas; evidência de telas ponta a ponta incompleta |
| FB-05 | Criar, listar, editar, excluir e fechar doação | `DoacaoController/Service`, `DoacaoRepository` | `Donations` | Operações, estoque, expiração e regras de quantidade; fechamento manual existe no backend, cobertura visual precisa ser auditada |
| FB-06 | Busca de doações disponíveis | `GET /doacoes/disponiveis` | `Donations` (ONG) | Código presente; frontend também tenta rota legada `/doacoes/publicadas`, que **não consta** nos controllers atuais; possui fallback |
| FB-07 | Criar, aprovar, rejeitar, cancelar e concluir requisição | `RequisicaoDoacaoController/Service` | `Requests`, `Donations` | Fluxo de solicitações, status e testes HTTP/concorrência no CI |
| FB-08 | Reserva de estoque concorrente | `findByIdForUpdate`, `findByIdComDoacaoBloqueada`, `DoacaoService` | refletido em doações/pedidos | Transação e bloqueio pessimista no backend; garantia de estoque exige banco transacional |
| FB-09 | Cadastro, busca por CPF, edição e inativação de vínculo de voluntário | `VoluntarioController/Service` e `VoluntarioOrganizacao` | `Volunteers` (ONG) | Testes de isolamento; atenção especial a dados pessoais |
| FB-10 | Dashboards público, doador e ONG | `DashboardService`, `ImpactoAlimentarCalculator` | `Landing`, `Dashboard`, `Admin` | API pública e telas presentes, unidade da quantidade importa; consulta pública pode ser lenta |
| FB-11 | Liveness da API separado do dashboard | `HealthController` | nenhuma tela específica | `GET /health/live` implementado, usado na publicação e testado |
| FB-12 | Cliente HTTP, sessão, guards de navegação e layout | `SecurityConfig`, `JwtFilter`, DTOs | `api/client.js`, `ProtectedRoute`, layouts/ui | Guards de UI não substituem autorização de service/JWT |
| FB-13 | Identidade visual e página inicial | API pública de indicadores | `Landing`, imagens, tema, Navbar | Interfaces implementadas; WCAG/auditoria mobile não comprovadas |
| FB-14 | CI de backend e testes integrados | Gradle, JUnit, `tests/ci` | — | GitHub Actions CI PASS com PostgreSQL descartável; casos históricos são plano, não medição |
| FB-15 | CI do frontend | — | Vite, ESLint, TypeScript, `tests/session.test.ts` | CI PASS e build aprovado |
| FB-16 | CD automático da API | `foodbridge-ci-cd.yml`, SSH, `atualizar-api.sh` | — | **Deploy real em push na main PASS** run #43465999269 (13/11) |
| FB-17 | CD automático web | — | `deploy.yml`, `atualizar-frontend.sh` | **Deploy real em push na main PASS** run #43465805656 (13/11), Nginx local/externo HTTP 200 |
| FB-18 | Infraestrutura de execução do FoodBridge | Oracle Linux 9; Nginx; Java 21/systemd; PostgreSQL FoodBridge; SSH/CI/CD | build `dist` estático | API e site publicados na Oracle e verificados pelos workflows; a instância é compartilhada, mas serviços de outros projetos não fazem parte do escopo funcional |

## Regras de negócio observadas

- Estado de usuários: `PENDENTE_VERIFICACAO`, `VERIFICADO`, `SUSPENSO`, `INATIVO`. Organizações e vínculos têm revisão/aprovação próprios.
- JWT temporário para cadastro/seleção; token ACCESS permite contexto organizacional; serviço revalida autorização e situação de vínculo no PostgreSQL.
- A doação possui tipo de alimento, unidade, quantidade, vencimento, janelas de retirada, local, estado e fechamento; pedidos não podem consumir saldo além do disponível.
- Requisição percorre `PENDENTE → APROVADO → CONCLUIDO`, com `REJEITADO`/`CANCELADO` em caminhos alternativos. Voluntário deve ter vínculo ativo para coleta.
- Indicadores agregados consideram estados concluídos e cálculos por unidade; não é válido somar Kg e litros como medida única.
- Valores de exemplo ou dashboards existentes não representam métrica real de impacto alcançado. Para apresentação, usar dados demonstrativos ou medições autorizadas.

## Evidência de testes e deploy

**Backend**: [run #43465999269](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269) — Kotlin, teste PostgreSQL, artefato, preflight Oracle, deploy SSH, backup, reinício e saúde **SUCCESS**. JAR anterior salvo no host. **Frontend**: [run #43465805656](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656) — teste/lint/build, deploy, backup e HTTP 200 **SUCCESS**.

Esses runs demonstram publicação automatizada, **não** provam cada fluxo de negócio sendo utilizado por organizações reais.

## Pendências que NÃO devem ser vendidas como entregas completas

1. Política de sessão web: tokens em `localStorage`; revisar risco de XSS/CSP e considerar cookies de sessão seguros.
2. Transportes públicos: Nginx HTTP 80 comprovado; TLS/HTTPS, DNS, certificados e redirect 80→443 ainda não foram homologados nesta entrega.
3. Banco PostgreSQL: validação de schema por `ddl-auto=validate`, porém não há prova de migrações versionadas com Flyway/Liquibase nem teste de restauração recente do banco produtivo.
4. Acesso legal a CPF, endereço e contatos; exercer minimização, auditoria, retenção e LGPD (sem presumir certificação).
5. Observabilidade/uptime, alertas, disponibilidade contínua e ensaio de desastre sob falha real continuam importantes.
6. E2.1.Micro tem ~945 MiB utilizáveis e swap; o CD automático passou, mas ainda há risco de lentidão em picos de acesso ao frontend, consultas ao PostgreSQL e inicialização da API Java.
7. O frontend tenta `/doacoes/publicadas` como fallback, endpoint não presente no backend atual; racionalizar chamadas.
8. Cadastro `Register.tsx` faz `fetch("/usuarios")` relativo, enquanto outros componentes usam Axios com `VITE_API_BASE_URL`; padronizar para evitar comportamento distinto entre ambientes.
9. Cobertura browser E2E, acessibilidade e testes de integração externa cobrindo todos os módulos ainda não foram comprovados. O plano histórico lista 165 cenários planejados, não executados integralmente.

**Fonte da verdade**: [50 rotas](REFERENCIA_API_ATUAL.md), [arquitetura](ARQUITETURA_ATUAL.md), [inventário de arquivos](INVENTARIO_PROJETO.md) e [documentação histórica](projeto/ENTREGA_FUNCIONAL.md).
