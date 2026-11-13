# FoodBridge — API Kotlin / Spring Boot

[![CI/CD API Oracle](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/workflows/foodbridge-ci-cd.yml/badge.svg)](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/workflows/foodbridge-ci-cd.yml)

O FoodBridge conecta organizações que disponibilizam alimentos a ONGs que os recebem. O backend organiza cadastro, autenticação, vínculos, doações, solicitações, voluntários, aprovações e indicadores sociais.

**Stack real:** Kotlin 2.2.21 · Spring Boot 3.2.5 · Java 21 · Spring Security/JWT · JPA/Hibernate · PostgreSQL · Gradle. **Interface:** [Foodbridge-Frontend-React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React).

## O que existe

- **50 endpoints REST** catalogados nos nove controllers, incluindo `GET /health/live`.
- Usuários, organizações, vínculos e papéis `DOADOR`, `PRODUTOR`, `DISTRIBUIDOR`, `ONG`, `ADMIN`.
- Autenticação JWT com contexto `TEMP` e `ACCESS`; autorização de negócio por vínculo e organização.
- Gestão de doações, solicitações e coleta, estoque concorrente por transação, voluntários e dashboards.
- Testes JUnit com PostgreSQL descartável em CI, contratos de SSH e fluxo automático de publicação.
- CD real na Oracle Linux 9 `VM.Standard.E2.1.Micro`: backup de JAR, restart, health e rollback via GitHub Actions.

**Evidência de CD automático:** [execução #43465999269 — SUCCESS](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269). O frontend publicou automaticamente no [run #43465805656](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656). O deploy é disparado em push de **código** na `main`, sem flags manuais; PRs executam CI, e commits somente de documentação não disparam deploy.

## Documentação completa para estudar e apresentar

**[Central de documentação](docs/README_DOCUMENTACAO.md)** · [Inventário de todos os arquivos](docs/INVENTARIO_PROJETO.md) · [Recursos e status](docs/RECURSOS_E_STATUS.md) · [50 rotas da API](docs/REFERENCIA_API_ATUAL.md) · [Arquitetura e Mermaid](docs/ARQUITETURA_ATUAL.md) · [Guia de estudo](docs/GUIA_ESTUDO.md) · [Roteiro de apresentação](docs/APRESENTACAO_PROJETO.md).

A documentação histórica continua versionada: [requisitos](docs/projeto/ESPECIFICACAO_PROJETO.md), [UML PlantUML](docs/uml/README.md), [testes planejados](docs/testes/PLANO_DE_TESTES.md), [matriz de rastreabilidade](docs/testes/MATRIZ_RASTREABILIDADE.md), [incidente e recuperação da Oracle](docs/implantacao/INCIDENTE_DEPLOY_API_20261113.md).

## Executar em ambiente local

Instale JDK 21 e disponibilize um **PostgreSQL local de desenvolvimento**. Configure propriedades com base em `src/main/resources/application.properties.example` sem versionar senha do banco, segredo JWT ou credenciais de administrador.

```powershell
.\gradlew.bat bootRun
.\gradlew.bat clean test
.\gradlew.bat bootJar
```

Linux/macOS: `./gradlew bootRun` e `./gradlew clean test`. O CI monta seu próprio PostgreSQL efêmero; **nunca apontar testes para o banco de produção**. O exemplo de propriedades contém `ddl-auto=update` para ambiente de desenvolvimento; produção exige `validate` ou `none`, conforme preflight.

Para conferir inventário rastreável:

```bash
python3 tools/atualizar_inventario.py --check
```

Ao adicionar/remover/renomear arquivos, usar `--write` e incluir o inventário atualizado no PR. O CI verifica essa consistência.

## Limites conhecidos

Ter CI/CD funcionando não significa todos os fluxos homologados em navegador real. A Oracle Micro tem recursos limitados; frontend usa tokens no localStorage, TLS/HTTPS e backups restauráveis de banco exigem verificação própria, e dados pessoais de voluntários requerem proteção e política LGPD. A [matriz de recursos](docs/RECURSOS_E_STATUS.md) distingue implementação, testes e lacunas.

**Orientação para contribuições:** [AGENTS.md](AGENTS.md), e registre impacto documental em [docs/controle/VERSIONAMENTO.md](docs/controle/VERSIONAMENTO.md).
