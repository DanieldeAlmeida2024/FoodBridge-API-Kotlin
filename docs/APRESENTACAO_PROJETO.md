# FoodBridge — apresentação técnica, portfólio e banca

> **FB-DEMO-001 · 13/11/2026.** Roteiro reutilizável para entrevista, demonstração acadêmica e apresentação de projeto. Não contém dados pessoais reais ou números de impacto não medidos.

## Descrição curta para portfólio

**FoodBridge** é uma plataforma full-stack para coordenar a redistribuição de alimentos entre doadores, produtores, distribuidores e organizações sociais. A API Kotlin/Spring Boot implementa identidade e vínculos institucionais, doações, solicitações, coleta por voluntários e indicadores; o frontend React organiza os fluxos por papel. O projeto utiliza PostgreSQL e possui CI/CD automático com testes, SSH seguro, backup e rollback em uma Oracle Cloud E2.1.Micro. É um produto em evolução: o deploy automático foi homologado, e melhorias de segurança web, observabilidade e disponibilidade permanecem no roadmap.

## Pitch de 45–60 segundos

> O FoodBridge nasceu da ideia de reduzir desperdício ao aproximar quem disponibiliza alimentos de organizações que conseguem distribuí-los. Desenvolvi uma API Kotlin/Spring Boot com PostgreSQL, autenticação JWT e permissões por organização. O sistema organiza publicações de doação, pedidos, aprovação, voluntários e confirmação de coleta. No frontend React, cada perfil acessa suas operações e dashboards. Também montei testes integrados com banco descartável e CI/CD que publica automaticamente na Oracle Cloud com SSH, backup e rollback. Uma dificuldade real foi fazer o deploy funcionar numa instância Micro; analisei a memória, removi uma reserva excessiva de Kdump e corrigi a estratégia de health. Hoje tenho evidência de publicação automática bem-sucedida, e sigo evoluindo a segurança e a operação.

## Roteiro de 8 telas/slides (5–8 minutos)

| Slide | Título | Mensagem e demonstração sugeridas |
|---|---|---|
| 1 | Problema | Desperdício de alimentos e dificuldade de coordenar oferta/coleta; não usar estatísticas não referenciadas |
| 2 | Usuários | DOADOR, PRODUTOR, DISTRIBUIDOR, ONG, ADMIN e seus papéis |
| 3 | Jornada | Publicar → ONG solicita → doador aprova → voluntário coleta → confirmar → contabilizar |
| 4 | Arquitetura | React/Vite → REST Kotlin/Spring Boot → JPA/PostgreSQL; Nginx e Oracle |
| 5 | Dados e segurança | 7 entidades de domínio JPA, vínculos, TEMP/ACCESS, autorização por organização |
| 6 | Engenharia de qualidade | Testes Kotlin e de sessão; CI com PostgreSQL descartável; locks de estoque |
| 7 | DevOps real | GitHub Actions → SSH pinado → JAR/dist → backup → health → rollback; runs PASS |
| 8 | Aprendizados e próximos passos | Limitação E2.1.Micro, Kdump e Nginx 403 solucionados; TLS, LGPD, observabilidade e E2E |

## Demonstração técnica segura (sem dados sensíveis)

1. Mostrar a landing e as rotas `/cadastro`, `/login`, `/dashboard`, `/doacoes`, `/requisicoes`, `/voluntarios` usando **contas sintéticas em ambiente de demonstração**.
2. Explicar a escolha da organização e por que a troca TEMP→ACCESS é necessária.
3. Demonstrar, em banco de teste, que uma ONG não consegue aprovar um pedido de outra organização e que duas requisições não reservam o mesmo saldo simultaneamente.
4. Abrir os workflows e comparar **CI de PR** com **CD em push main**; mostrar logs reais dos runs comprovados, sem exibir chaves.
5. Mostrar um exemplo de rollback em documentação ou teste controlado. **Não derrubar a API em produção durante uma banca.**

## Evidência pública para apresentar

- [Repositório API Kotlin](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin) e [repositório frontend React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React).
- [CI/CD automático da API: PASS](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).
- [CI/CD automático do React: PASS, Nginx HTTP 200](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656).
- [Incidente de deploy e aprendizado](implantacao/INCIDENTE_DEPLOY_API_20261113.md) (memória e rollback).
- [Catálogo atual de 50 endpoints](REFERENCIA_API_ATUAL.md) e [UML editável](uml/README.md).
- [Plano de testes](testes/PLANO_DE_TESTES.md) e [matriz de rastreabilidade](testes/MATRIZ_RASTREABILIDADE.md), distinguindo casos planejados de testes executados.

## Perguntas difíceis que podem surgir

**“Está pronto para produção?”** Resposta sustentada: há deploy automático real comprovado na Oracle, e fluxos têm testes de código/CI. Isso não comprova homologação de todo requisito em utilização real; faltam revisão de TLS, privacidade, observabilidade e recuperação de dados.

**“O que foi particularmente difícil?”** Depurar o restart do Spring Boot com memória limitada e o HTTP 403 do Nginx após publicar arquivos mode 0700. Ambas as correções foram versionadas e validadas em runs.

**“Como você evita que outro tenant leia CPF?”** Guardas de service consultam vínculos e contexto organizacional; testes de isolamento existem, mas políticas de retenção, auditoria e base legal precisam de revisão própria.

**“Por que Kotlin e React?”** Kotlin oferece tipagem forte e integração com Spring/JPA para regras transacionais; React facilita a organização de múltiplas telas com sessão/contexto. Essa é uma justificativa de engenharia, não prova de desempenho superior.

**“O que faria para escalar?”** Separar serviços, melhorar observabilidade, usar migrations versionadas e backups testados, habilitar HTTPS e adequar recursos à carga.

## Checklist antes da apresentação

- [ ] Confirmar que a UI responde na rede da apresentação, sem supor que HTTP 80 prova HTTPS.
- [ ] Usar conta e dados sintéticos; não mostrar CPF, senha, JWT, valores de `application.properties` ou segredos do Actions.
- [ ] Abrir a documentação e uma versão de branch/commit específica.
- [ ] Conferir se a lista de rotas e inventário do Git estão atualizados.
- [ ] Ensaiar pitch de 1 minuto e demonstração de 5 minutos.
- [ ] Ter alternativa sem internet (prints sem dados pessoais e diagramas Mermaid/PlantUML).

**Relação com o frontend:** [roteiro dedicado ao React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/APRESENTACAO_FRONTEND.md).
