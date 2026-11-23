# FoodBridge — apresentação técnica, portfólio e banca

> **FB-DEMO-001 · atualizado em 23/11/2026.** Roteiro reutilizável para entrevista, demonstração acadêmica e apresentação de projeto. Não contém dados pessoais reais ou números de impacto não medidos.

## Descrição curta para portfólio

**FoodBridge** é uma plataforma full-stack para coordenar a redistribuição de alimentos entre doadores, produtores, distribuidores e organizações sociais. A API Kotlin/Spring Boot implementa identidade e vínculos institucionais, doações, solicitações, coleta por voluntários e indicadores; o frontend React organiza os fluxos por papel. O projeto utiliza PostgreSQL e possui CI/CD automático com testes, SSH seguro, backup e rollback em uma Oracle Cloud E2.1.Micro. É um produto em evolução: o deploy automático foi homologado, e melhorias de segurança web, observabilidade e disponibilidade permanecem no roadmap.

## Pitch de 45–60 segundos

> O FoodBridge nasceu da ideia de reduzir desperdício ao aproximar quem disponibiliza alimentos de organizações que conseguem distribuí-los. Desenvolvi uma API Kotlin/Spring Boot com PostgreSQL, autenticação JWT e permissões por organização. O sistema organiza publicações de doação, pedidos, aprovação, voluntários e confirmação de coleta. No frontend React, cada perfil acessa suas operações e dashboards. Também montei testes integrados com banco descartável e CI/CD que publica automaticamente na Oracle Cloud com SSH, backup e rollback. Uma dificuldade real foi fazer o deploy funcionar numa instância Micro; analisei a memória, removi uma reserva excessiva de Kdump e corrigi a estratégia de health. Hoje tenho evidência de publicação automática bem-sucedida, e sigo evoluindo a segurança e a operação.

## Roteiro de 11 tópicos/slides (7–10 minutos)

| Slide | Título | Mensagem e demonstração sugeridas |
|---|---|---|
| 1 | Problema | Desperdício de alimentos e dificuldade de coordenar oferta/coleta; não usar estatísticas não referenciadas |
| 2 | Usuários | DOADOR, PRODUTOR, DISTRIBUIDOR, ONG, ADMIN e seus papéis |
| 3 | Jornada | Publicar → ONG solicita → doador aprova → voluntário coleta → confirmar → contabilizar |
| 4 | Arquitetura | React/Vite → REST Kotlin/Spring Boot → JPA/PostgreSQL; Nginx e Oracle |
| 5 | Dados e segurança | Vínculos por organização, JWT TEMP/ACCESS e cuidados com os dados dos voluntários |
| 6 | Testes | Exemplos de testes de login, autorização e disputa por estoque; CI com banco descartável |
| 7 | Histórico das mudanças | Mostrar que a aplicação já funcionava, mas precisava de revisão; comparar os PRs de 10 a 13/11 |
| 8 | Versionamento | Abrir `docs/controle/VERSIONAMENTO.md`; explicar versão do documento, data, PR e o que mudou |
| 9 | Uso de IA na análise | Abrir `docs/controle/HISTORICO_MUDANCAS_E_USO_IA.md`; explicar perguntas, três cenários e revisão humana |
| 10 | Publicação da API | GitHub Actions → SSH → JAR → backup → health → rollback; apresentar o run comprovado |
| 11 | Aprendizados e próximos passos | Melhorias de segurança, organização e documentação; pendências como HTTPS, privacidade e testes no navegador |

## Demonstração técnica segura (sem dados sensíveis)

1. Mostrar a landing e as rotas `/cadastro`, `/login`, `/dashboard`, `/doacoes`, `/requisicoes`, `/voluntarios` usando **contas sintéticas em ambiente de demonstração**.
2. Explicar a escolha da organização e por que a troca TEMP→ACCESS é necessária.
3. Demonstrar, em banco de teste, que uma ONG não consegue aprovar um pedido de outra organização e que duas requisições não reservam o mesmo saldo simultaneamente.
4. Abrir os workflows e comparar **CI de PR** com **CD em push main**; mostrar logs reais dos runs comprovados, sem exibir chaves.
5. Mostrar um exemplo de rollback em documentação ou teste controlado. **Não derrubar a API em produção durante uma banca.**

## Como apresentar o histórico, o versionamento e a IA

Esta parte pode ser apresentada em cerca de dois ou três minutos, com os documentos abertos no GitHub.

**Histórico do projeto.** A aplicação FoodBridge já tinha cadastro, login, organizações, doações, pedidos e voluntários funcionando. Ao revisar o código, apareceram pontos para melhorar: informações de token em logs, checagem de permissões, proteção de dados pessoais, concorrência de estoque e validações. Não é correto dizer que a aplicação inteira não funcionava ou que todas as falhas listadas foram exploradas. Para mostrar as mudanças, abrir o [histórico de mudanças](controle/HISTORICO_MUDANCAS_E_USO_IA.md) e os PRs relacionados, por exemplo [#1](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/1) e [#11](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/11).

**Versionamento.** Abrir o arquivo [VERSIONAMENTO.md](controle/VERSIONAMENTO.md). Explicar que as revisões dos documentos foram anotadas com datas e assuntos, e que o Git registra as alterações do código em commits e pull requests. Mostrar a revisão documental v1.2.2 (23/11/2026) e a v1.2.0 (13/11/2026). Essas versões são dos **documentos**: o arquivo `build.gradle.kts` ainda usa a versão de build `0.0.1-SNAPSHOT` e isso não deve ser confundido com a versão documental.

**Uso de inteligência artificial.** Explicar que a IA foi usada como apoio para pensar sobre possíveis problemas, pedir explicações e organizar opções de solução, testes e textos. No [histórico auxiliar](controle/HISTORICO_MUDANCAS_E_USO_IA.md), mostrar três cenários de um mesmo assunto: um caso normal, um caso com acesso indevido e um caso de regra ou operação concorrente. Mostrar as perguntas de análise organizadas por etapa, com seus três cenários e possíveis soluções. Esses textos são exemplos para explicar o estudo dos problemas, não registros literais de conversas. O que comprova uma mudança feita no projeto são os commits, os PRs, o código e os testes.

**Exemplo de explicação em voz alta:**

> O sistema já funcionava, mas fizemos uma revisão para encontrar pontos de segurança e melhorar o código. No GitHub estão registrados os PRs e as datas das mudanças. Nesse arquivo de versionamento fica o resumo do que foi documentado em cada revisão. Também usamos inteligência artificial como apoio para analisar problemas e pensar em testes. Aqui estão exemplos de perguntas de análise, com cenários normais e de erro, para explicar os problemas que estudamos. As correções podem ser conferidas no código e nos testes do repositório.

**Ordem recomendada para a demonstração:** [histórico auxiliar](controle/HISTORICO_MUDANCAS_E_USO_IA.md) → [versionamento](controle/VERSIONAMENTO.md) → [PR #11](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/11) → [execução de CI da API](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).

## Evidência pública para apresentar

- [Repositório API Kotlin](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin) e [repositório frontend React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React).
- [CI/CD automático da API: PASS](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).
- [CI/CD automático do React: PASS, Nginx HTTP 200](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656).
- [Incidente de deploy e aprendizado](implantacao/INCIDENTE_DEPLOY_API_20261113.md) (memória e rollback).
- [Catálogo atual de 50 endpoints](REFERENCIA_API_ATUAL.md) e [UML editável](uml/README.md).
- [Histórico de mudanças e uso de IA](controle/HISTORICO_MUDANCAS_E_USO_IA.md) e [versionamento dos documentos](controle/VERSIONAMENTO.md).
- [Plano de testes](testes/PLANO_DE_TESTES.md) e [matriz de rastreabilidade](testes/MATRIZ_RASTREABILIDADE.md), distinguindo casos planejados de testes executados.

## Perguntas difíceis que podem surgir

**“Está pronto para produção?”** Resposta sustentada: há deploy automático real comprovado na Oracle, e fluxos têm testes de código/CI. Isso não comprova homologação de todo requisito em utilização real; faltam revisão de TLS, privacidade, observabilidade e recuperação de dados.

**“O que foi particularmente difícil?”** Depurar o restart do Spring Boot com memória limitada e o HTTP 403 do Nginx após publicar arquivos mode 0700. Ambas as correções foram versionadas e validadas em runs.

**“Como você evita que outro tenant leia CPF?”** Guardas de service consultam vínculos e contexto organizacional; testes de isolamento existem, mas políticas de retenção, auditoria e base legal precisam de revisão própria.

**“Por que Kotlin e React?”** Kotlin oferece tipagem forte e integração com Spring/JPA para regras transacionais; React facilita a organização de múltiplas telas com sessão/contexto. Essa é uma justificativa de engenharia, não prova de desempenho superior.

**“O que faria para escalar?”** Separar serviços, melhorar observabilidade, usar migrations versionadas e backups testados, habilitar HTTPS e adequar recursos à carga.

**“A IA fez as correções sozinha?”** A IA serviu de apoio na análise e no estudo de possibilidades. Para confirmar uma correção, consultar o PR, o arquivo alterado e os testes. As perguntas do documento auxiliar ilustram a análise; não são registros literais. As alterações devem ser conferidas no GitHub.

**“A versão v1.2.2 é a versão da API?”** Não. É uma revisão da documentação. A versão do artefato de build está declarada separadamente no Gradle.

## Checklist antes da apresentação

- [ ] Confirmar que a UI responde na rede da apresentação, sem supor que HTTP 80 prova HTTPS.
- [ ] Usar conta e dados sintéticos; não mostrar CPF, senha, JWT, valores de `application.properties` ou segredos do Actions.
- [ ] Abrir o histórico auxiliar e o versionamento da branch principal, com links para os PRs e o CI correspondente.
- [ ] Explicar os três cenários dos exemplos de análise e diferenciar versão documental de versão do Gradle.
- [ ] Conferir se a lista de rotas e inventário do Git estão atualizados.
- [ ] Ensaiar pitch de 1 minuto e demonstração de 5 minutos.
- [ ] Ter alternativa sem internet (prints sem dados pessoais e diagramas Mermaid/PlantUML).

**Relação com o frontend:** [roteiro dedicado ao React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/APRESENTACAO_FRONTEND.md).
