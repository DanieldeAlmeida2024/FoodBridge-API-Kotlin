# FoodBridge — guia de estudo técnico, revisão e entrevista

> **FB-ESTUDO-001 · 13/11/2026.** Trilha prática baseada no código dos repositórios Kotlin e React. Para uma apresentação verídica, distinguir **implementado**, **testado** e **homologado em produção**.

## O que o projeto ensina

O FoodBridge combina engenharia de domínio e uma entrega real: autenticação e autorização por organização, regras de estoque e coleta, contratos REST, persistência PostgreSQL, frontend React e CI/CD automatizado por SSH. É possível estudá-lo como laboratório de arquitetura backend, segurança, bancos, concorrência, testes e operações.

## Roteiro em sete sessões

| Sessão | Tema | Ler no código | Exercício verificável |
|---|---|---|---|
| 1 | Produto e casos de uso | `docs/RECURSOS_E_STATUS.md`, controllers | Desenhar jornada doador→ONG→coleta e apontar atores |
| 2 | Camadas Kotlin | `controller/`, `service/`, `domain/` | Explicar como `POST /doacoes` chega ao repositório |
| 3 | Modelagem de dados | `domain/model/`, `domain/enums/` | Desenhar ER de 7 entidades e 2 objetos de valor |
| 4 | JWT e isolamento | `AuthService`, `JwtFilter`, `ContextoAcessoService` | Comparar TEMP vs ACCESS; descrever vínculo revogado |
| 5 | Concorrência/estoque | `RequisicaoDoacaoService`, `DoacaoRepository` | Simular duas ONGs tentando reservar o último saldo |
| 6 | Testes e CI | `src/test`, `tests/ci`, GitHub Actions | Executar testes locais contra PostgreSQL descartável e explicar limites |
| 7 | Produção e apresentação | `deploy/oracle/*`, docs OCI | Explicar fluxo artefato→SSH→backup→health→rollback e risco Micro |

## Conceitos essenciais

**Controller / Service / Repository.** Controller recebe HTTP e valida entrada; service concentra transações e regras de domínio; repository persiste/consulta. DTO evita expor modelos JPA diretamente.

**Autenticação vs autorização.** O JWT prova identidade/claims quando validado. Não garante que o usuário ou vínculo continue vigente: a aplicação revalida no DB para operações relevantes. `SecurityConfig` filtra rotas, enquanto services verificam negócio, papéis e titularidade.

**Concorrência.** `findByIdForUpdate` e transações ajudam a impedir overbooking quando solicitações disputam uma doação. Explicar por que uma validação simples apenas em memória não basta.

**Consistência dos dados.** Estados de usuário, organização, doação e requisição precisam de transições permitidas; quantidades por unidade não devem ser misturadas. O serviço registra conclusões e evita dupla contabilização conforme regras presentes.

**Banco no CI vs produção.** CI usa PostgreSQL descartável com `create-drop` apenas nos testes; produção usa banco existente e `validate/none`. **Nunca usar o banco da Oracle como base de testes do CI.**

**Liveness vs readiness.** `/health/live` devolve resposta leve sem banco; `/dashboards/publico` valida parte da integração funcional e é mais custoso. Um teste de liveness não comprova que o negócio inteiro esteja funcionando.

**Rollback.** O workflow faz upload do JAR aprovado, valida SHA-256, preserva a versão anterior e tenta restaurá-la se a saúde falhar. Um rollback bem-sucedido **não transforma a publicação falha em sucesso**.

## Comandos de estudo locais

Na API (JDK 21 e PostgreSQL de desenvolvimento configurados; nunca apontar para o banco produtivo):

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootJar
```

No Linux/macOS: `./gradlew clean test bootJar`.

Para testes de contratos locais:

```bash
python3 -m unittest discover -s tests/ci -p 'test_*_contract.py' -v
python3 tools/atualizar_inventario.py --check
```

Frontend React (no repositório separado):

```bash
npm ci
npm run test:session
npm run lint
npm run build
```

## Perguntas de entrevista e respostas orientadoras

**1. Qual problema o sistema resolve?** Coordena excedentes/doações entre organizações e permite solicitações e comprovação operacional de coletas; não afirmar quantidade real distribuída sem métricas.

**2. Por que ter `UsuarioOrganizacao`?** Uma pessoa pode possuir vínculos diferentes; o contexto da sessão precisa representar a organização e o papel válidos naquele momento.

**3. O que impede uma ONG de alterar doação alheia?** `SecurityConfig` verifica token e o service valida titularidade e papel contra dados vigentes; testes de autorização devem cobrir estes casos.

**4. Como impedir duas reservas simultâneas?** Lock no registro e transação ao calcular disponibilidade; testes de concorrência do PostgreSQL demonstram o contrato em ambiente controlado.

**5. Por que a Micro teve problemas de deploy?** Kdump reservava 448 MB; após mudança supervisionada, ~945 MiB ficaram visíveis. Inicialização Java ficou lenta e motivou liveness leve, espera de saúde, backup/rollback e telemetria.

**6. Como o CI/CD funciona sem intervenção?** PR valida; push de código na main constrói e testa; actions separadas publicam frontend e backend na Oracle por SSH fixado e registram o resultado. Histórico: [web](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656), [API](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).

**7. Quais limitações você apontaria?** Tokens no localStorage, HTTPS não homologado, falta de alta disponibilidade, memória pequena, estratégia de migrations, LGPD e E2E real abrangente.

## Atividades para fixação

- [ ] Reescrever com suas palavras o fluxo LOGIN → TEMP → SELECT-ORG → ACCESS.
- [ ] Desenhar os relacionamentos de doação, ONG, voluntário e requisição.
- [ ] Localizar o ponto de lock da doação e defender a regra de quantidade disponível.
- [ ] Identificar quem pode executar cada PATCH de `/requisicoes`.
- [ ] Executar a suíte de testes no ambiente local isolado, registrar commit e resultado.
- [ ] Revisar o diagrama de implantação e explicar falha/recuperação da Micro.
- [ ] Demonstrar uma limitação real e propor um PR específico com teste de regressão.
- [ ] Apresentar o projeto em 3 minutos sem ler o código nem afirmar métricas não medidas.

**Próxima leitura:** [roteiro de apresentação](APRESENTACAO_PROJETO.md), [catálogo HTTP](REFERENCIA_API_ATUAL.md) e [diagrama atual](ARQUITETURA_ATUAL.md).
