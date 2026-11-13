# Inventário completo — FoodBridge API Kotlin

> **FB-INVENT-001 · 13/11/2026 · revisão documental 1.2.0.** Inventário dos arquivos *rastreáveis pelo Git* do repositório backend, incluindo documentação, CI, scripts de operação, modelos, serviços, testes e exemplos. Versões instaladas na VM, backups e banco de produção não são arquivos versionados e não constam desta lista.

## Visão do repositório

| Área | Diretório ou arquivo | Função |
|---|---|---|
| Aplicação | `src/main/kotlin/com/br/foodbridge/controller/` | 9 controllers e 50 mapeamentos REST |
| Regras | `src/main/kotlin/com/br/foodbridge/service/` | Operações de conta, organização, doação, reserva, coleta, dashboards |
| Persistência | `src/main/kotlin/com/br/foodbridge/domain/model/`, `domain/repository/`, `domain/enums/` | 7 entidades JPA, 2 objetos de valor, 7 repositories e estados/papéis |
| Segurança e configuração | `src/main/kotlin/com/br/foodbridge/config/`, `middleware/`, `exception/` | Spring Security, JWT, validação de schema e erros |
| Contratos | `controller/dto/`, `postman/` | DTOs REST, mapeadores, Postman |
| Testes funcionais | `src/test/kotlin/` | JUnit 5, HTTP/MockMvc, concorrência e políticas |
| Testes operacionais | `tests/ci/` | Contratos estáticos de CI/CD, scripts, inventário |
| Build | `build.gradle.kts`, `gradlew*`, `gradle/wrapper/` | Gradle/JDK 21 e dependências |
| CD automático | `.github/workflows/`, `deploy/oracle/` | CI PostgreSQL efêmero, SSH, preflight, backup JAR, health/rollback |
| Docs | `docs/`, `README.md`, `AGENTS.md` | Projeto, API, UML, testes, estudo, apresentação e operação |

## Catálogos de recursos

- [Mapa de implementação e status](RECURSOS_E_STATUS.md): identifica entrega em código, CI, CD e pendências.
- [50 endpoints da API](REFERENCIA_API_ATUAL.md) e [arquitetura atual](ARQUITETURA_ATUAL.md).
- [Estudo](GUIA_ESTUDO.md) e [apresentação](APRESENTACAO_PROJETO.md).
- [Documentação completa](README_DOCUMENTACAO.md), [UML PlantUML](uml/README.md), [versionamento](controle/VERSIONAMENTO.md).
- API usa Oracle Linux 9, Java 21/Spring Boot/PostgreSQL; web em repositório separado [inventário React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/INVENTARIO_PROJETO.md). **Não inventariar arquivos do outro repositório como se estivessem aqui.**

## Regra de atualização obrigatória

```bash
python3 tools/atualizar_inventario.py --write
python3 tools/atualizar_inventario.py --check
```

O script usa `git ls-files -z`, agrupa por pasta e reconstrói **apenas o bloco delimitado abaixo**. O teste `tests/ci/test_inventory_contract.py` reprova PR quando houver caminho a mais/menos no inventário. Fazer `--write` após adicionar/remover/renomear arquivos e antes do merge. Alterações no conteúdo de uma classe ou módulo exigem também atualização *semântica* em `RECURSOS_E_STATUS.md`, `REFERENCIA_API_ATUAL.md`, diagramas e rastreabilidade; o teste de caminhos não garante isso.

## Relação completa de arquivos rastreados

<!-- INVENTARIO_ARQUIVOS_INICIO -->
**Total de arquivos rastreados:** 182.

**Distribuicao por pasta de primeiro nivel:**
- `(raiz)`: 9
- `.github`: 5
- `.postman`: 1
- `deploy`: 12
- `docs`: 48
- `gradle`: 2
- `postman`: 4
- `src`: 89
- `tests`: 11
- `tools`: 1

**Lista completa de caminhos versionados (gerada do Git):**

- [`.gitattributes`](../.gitattributes)
- [`.github/workflows/foodbridge-ci-cd.yml`](../.github/workflows/foodbridge-ci-cd.yml)
- [`.github/workflows/oracle-api-incident.yml`](../.github/workflows/oracle-api-incident.yml)
- [`.github/workflows/oracle-deploy-readiness.yml`](../.github/workflows/oracle-deploy-readiness.yml)
- [`.github/workflows/oracle-network-diagnostics.yml`](../.github/workflows/oracle-network-diagnostics.yml)
- [`.github/workflows/oracle-preflight.yml`](../.github/workflows/oracle-preflight.yml)
- [`.gitignore`](../.gitignore)
- [`.postman/config.json`](../.postman/config.json)
- [`AGENTS.md`](../AGENTS.md)
- [`IMPACTO_ALIMENTAR.md`](../IMPACTO_ALIMENTAR.md)
- [`README.md`](../README.md)
- [`build.gradle.kts`](../build.gradle.kts)
- [`deploy/oracle/atualizar-api.sh`](../deploy/oracle/atualizar-api.sh)
- [`deploy/oracle/diagnosticar-falha-startup.sh`](../deploy/oracle/diagnosticar-falha-startup.sh)
- [`deploy/oracle/diagnosticar-ssh-windows.ps1`](../deploy/oracle/diagnosticar-ssh-windows.ps1)
- [`deploy/oracle/preparar-github-preflight-windows.ps1`](../deploy/oracle/preparar-github-preflight-windows.ps1)
- [`deploy/oracle/preparar-vm.sh`](../deploy/oracle/preparar-vm.sh)
- [`deploy/oracle/publicar-api-manual-windows.ps1`](../deploy/oracle/publicar-api-manual-windows.ps1)
- [`deploy/oracle/pull-approved-artifact.sh`](../deploy/oracle/pull-approved-artifact.sh)
- [`deploy/oracle/systemd/foodbridge-pull@.service`](../deploy/oracle/systemd/foodbridge-pull%40.service)
- [`deploy/oracle/systemd/foodbridge-pull@.timer`](../deploy/oracle/systemd/foodbridge-pull%40.timer)
- [`deploy/oracle/verificar-predeploy-completo.sh`](../deploy/oracle/verificar-predeploy-completo.sh)
- [`deploy/oracle/verificar-recursos-deploy.sh`](../deploy/oracle/verificar-recursos-deploy.sh)
- [`deploy/oracle/verificar-vm.sh`](../deploy/oracle/verificar-vm.sh)
- [`docs/APRESENTACAO_PROJETO.md`](../docs/APRESENTACAO_PROJETO.md)
- [`docs/ARQUITETURA.md`](../docs/ARQUITETURA.md)
- [`docs/ARQUITETURA_ATUAL.md`](../docs/ARQUITETURA_ATUAL.md)
- [`docs/DEPLOY_ORACLE.md`](../docs/DEPLOY_ORACLE.md)
- [`docs/GUIA_ESTUDO.md`](../docs/GUIA_ESTUDO.md)
- [`docs/INVENTARIO_PROJETO.md`](../docs/INVENTARIO_PROJETO.md)
- [`docs/PLANO_TESTES.md`](../docs/PLANO_TESTES.md)
- [`docs/README_DOCUMENTACAO.md`](../docs/README_DOCUMENTACAO.md)
- [`docs/RECURSOS_E_STATUS.md`](../docs/RECURSOS_E_STATUS.md)
- [`docs/REFERENCIA_API_ATUAL.md`](../docs/REFERENCIA_API_ATUAL.md)
- [`docs/REQUISITOS.md`](../docs/REQUISITOS.md)
- [`docs/UML.puml`](../docs/UML.puml)
- [`docs/controle/VERSIONAMENTO.md`](../docs/controle/VERSIONAMENTO.md)
- [`docs/implantacao/CI_CD_ORACLE_SEGURANCA.md`](../docs/implantacao/CI_CD_ORACLE_SEGURANCA.md)
- [`docs/implantacao/CI_CD_ORACLE_SSH_DIRETO.md`](../docs/implantacao/CI_CD_ORACLE_SSH_DIRETO.md)
- [`docs/implantacao/CI_CD_POR_ARTEFATOS.md`](../docs/implantacao/CI_CD_POR_ARTEFATOS.md)
- [`docs/implantacao/DIAGNOSTICO_REDE_FOODBRIDGE.md`](../docs/implantacao/DIAGNOSTICO_REDE_FOODBRIDGE.md)
- [`docs/implantacao/INCIDENTE_DEPLOY_API_20261113.md`](../docs/implantacao/INCIDENTE_DEPLOY_API_20261113.md)
- [`docs/implantacao/OCI_FIRST_DEPLOY_READINESS.md`](../docs/implantacao/OCI_FIRST_DEPLOY_READINESS.md)
- [`docs/implantacao/OCI_MICRO_AUTOMATIC_CICD.md`](../docs/implantacao/OCI_MICRO_AUTOMATIC_CICD.md)
- [`docs/implantacao/SSH_RSA_COMPROVACAO.md`](../docs/implantacao/SSH_RSA_COMPROVACAO.md)
- [`docs/projeto/ENTREGA_FUNCIONAL.md`](../docs/projeto/ENTREGA_FUNCIONAL.md)
- [`docs/projeto/ESPECIFICACAO_PROJETO.md`](../docs/projeto/ESPECIFICACAO_PROJETO.md)
- [`docs/projeto/POLITICA_DADOS_VOLUNTARIOS.md`](../docs/projeto/POLITICA_DADOS_VOLUNTARIOS.md)
- [`docs/projeto/RISCOS.md`](../docs/projeto/RISCOS.md)
- [`docs/testes/CI_CD_ORACLE_TESTES.md`](../docs/testes/CI_CD_ORACLE_TESTES.md)
- [`docs/testes/MATRIZ_RASTREABILIDADE.md`](../docs/testes/MATRIZ_RASTREABILIDADE.md)
- [`docs/testes/PLANO_DE_TESTES.md`](../docs/testes/PLANO_DE_TESTES.md)
- [`docs/uml/01_casos_de_uso_geral.puml`](../docs/uml/01_casos_de_uso_geral.puml)
- [`docs/uml/02_componentes_backend.puml`](../docs/uml/02_componentes_backend.puml)
- [`docs/uml/03_modelo_de_dominio_classes.puml`](../docs/uml/03_modelo_de_dominio_classes.puml)
- [`docs/uml/04_sequencia_login_e_selecao.puml`](../docs/uml/04_sequencia_login_e_selecao.puml)
- [`docs/uml/05_atividade_cadastro_organizacao.puml`](../docs/uml/05_atividade_cadastro_organizacao.puml)
- [`docs/uml/06_maquina_estados_usuario.puml`](../docs/uml/06_maquina_estados_usuario.puml)
- [`docs/uml/07_maquina_estados_organizacao.puml`](../docs/uml/07_maquina_estados_organizacao.puml)
- [`docs/uml/08_maquina_estados_doacao.puml`](../docs/uml/08_maquina_estados_doacao.puml)
- [`docs/uml/09_sequencia_solicitacao_aprovacao.puml`](../docs/uml/09_sequencia_solicitacao_aprovacao.puml)
- [`docs/uml/10_maquina_estados_requisicao.puml`](../docs/uml/10_maquina_estados_requisicao.puml)
- [`docs/uml/11_sequencia_coleta_conclusao.puml`](../docs/uml/11_sequencia_coleta_conclusao.puml)
- [`docs/uml/12_casos_uso_voluntarios.puml`](../docs/uml/12_casos_uso_voluntarios.puml)
- [`docs/uml/13_sequencia_dashboard_impacto.puml`](../docs/uml/13_sequencia_dashboard_impacto.puml)
- [`docs/uml/14_implantacao_logica.puml`](../docs/uml/14_implantacao_logica.puml)
- [`docs/uml/15_atividade_aprovacoes.puml`](../docs/uml/15_atividade_aprovacoes.puml)
- [`docs/uml/16_atividade_fechamento_doacao.puml`](../docs/uml/16_atividade_fechamento_doacao.puml)
- [`docs/uml/17_pipeline_oracle_cicd.puml`](../docs/uml/17_pipeline_oracle_cicd.puml)
- [`docs/uml/17_sequencia_autorizacao_organizacao.puml`](../docs/uml/17_sequencia_autorizacao_organizacao.puml)
- [`docs/uml/18_sequencia_isolamento_voluntarios.puml`](../docs/uml/18_sequencia_isolamento_voluntarios.puml)
- [`docs/uml/README.md`](../docs/uml/README.md)
- [`gradle/wrapper/gradle-wrapper.jar`](../gradle/wrapper/gradle-wrapper.jar)
- [`gradle/wrapper/gradle-wrapper.properties`](../gradle/wrapper/gradle-wrapper.properties)
- [`gradlew`](../gradlew)
- [`gradlew.bat`](../gradlew.bat)
- [`postman/collections/FoodBridge Automated Flow.postman_collection.json`](../postman/collections/FoodBridge%20Automated%20Flow.postman_collection.json)
- [`postman/collections/FoodBridge.postman_collection.json`](../postman/collections/FoodBridge.postman_collection.json)
- [`postman/environments/FoodBridge_Local.postman_environment.json`](../postman/environments/FoodBridge_Local.postman_environment.json)
- [`postman/globals/workspace.postman_globals.json`](../postman/globals/workspace.postman_globals.json)
- [`settings.gradle.kts`](../settings.gradle.kts)
- [`src/main/kotlin/com/br/foodbridge/FoodbridgeApplication.kt`](../src/main/kotlin/com/br/foodbridge/FoodbridgeApplication.kt)
- [`src/main/kotlin/com/br/foodbridge/annotation/ValidOrganizacaoRole.kt`](../src/main/kotlin/com/br/foodbridge/annotation/ValidOrganizacaoRole.kt)
- [`src/main/kotlin/com/br/foodbridge/config/AppConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/AppConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/config/CorsConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/CorsConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/config/DataInitializer.kt`](../src/main/kotlin/com/br/foodbridge/config/DataInitializer.kt)
- [`src/main/kotlin/com/br/foodbridge/config/JwtConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/JwtConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/config/PasswordEncoderConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/PasswordEncoderConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/config/SchemaValidationGuard.kt`](../src/main/kotlin/com/br/foodbridge/config/SchemaValidationGuard.kt)
- [`src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/config/WebConfig.kt`](../src/main/kotlin/com/br/foodbridge/config/WebConfig.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/AdminController.kt`](../src/main/kotlin/com/br/foodbridge/controller/AdminController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/AuthController.kt`](../src/main/kotlin/com/br/foodbridge/controller/AuthController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/DashboardController.kt`](../src/main/kotlin/com/br/foodbridge/controller/DashboardController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/DoacaoController.kt`](../src/main/kotlin/com/br/foodbridge/controller/DoacaoController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/HealthController.kt`](../src/main/kotlin/com/br/foodbridge/controller/HealthController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/OrganizacaoController.kt`](../src/main/kotlin/com/br/foodbridge/controller/OrganizacaoController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/RequisicaoDoacaoController.kt`](../src/main/kotlin/com/br/foodbridge/controller/RequisicaoDoacaoController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/UsuarioController.kt`](../src/main/kotlin/com/br/foodbridge/controller/UsuarioController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/VoluntarioController.kt`](../src/main/kotlin/com/br/foodbridge/controller/VoluntarioController.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/auth/AuthResponse.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/auth/AuthResponse.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/auth/LoginRequest.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/auth/LoginRequest.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/auth/LoginResponse.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/auth/LoginResponse.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/auth/SelectOrganizationRequest.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/auth/SelectOrganizationRequest.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/auth/TokenData.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/auth/TokenData.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/dashboard/DashboardDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/dashboard/DashboardDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/dashboard/ImpactoAlimentarDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/dashboard/ImpactoAlimentarDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/doacao/DoacaoDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/doacao/DoacaoDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/mapper/DoacaoMapper.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/mapper/DoacaoMapper.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/mapper/OrganizacaoMapper.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/mapper/OrganizacaoMapper.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/mapper/UsuarioMapper.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/mapper/UsuarioMapper.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/organizacao/OrganizacaoDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/organizacao/OrganizacaoDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/requisicao/RequisicaoDoacaoDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/requisicao/RequisicaoDoacaoDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/usuario/UsuarioDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/usuario/UsuarioDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/controller/dto/voluntario/VoluntarioDTO.kt`](../src/main/kotlin/com/br/foodbridge/controller/dto/voluntario/VoluntarioDTO.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/LevelQualidade.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/LevelQualidade.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/OrganizacaoRole.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/OrganizacaoRole.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/StatusDoacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/StatusDoacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/StatusDocumento.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/StatusDocumento.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/StatusOrganizacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/StatusOrganizacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/StatusReivindicacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/StatusReivindicacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/StatusVoluntario.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/StatusVoluntario.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/TipoDoacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/TipoDoacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/Unidade.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/Unidade.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/enums/UserStatus.kt`](../src/main/kotlin/com/br/foodbridge/domain/enums/UserStatus.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/Doacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/Doacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/Endereco.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/Endereco.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/JanelaHorario.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/JanelaHorario.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/Organizacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/Organizacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/RequisicaoDoacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/RequisicaoDoacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/Usuario.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/Usuario.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/UsuarioOrganizacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/UsuarioOrganizacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/Voluntario.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/Voluntario.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/model/VoluntarioOrganizacao.kt`](../src/main/kotlin/com/br/foodbridge/domain/model/VoluntarioOrganizacao.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/DoacaoRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/DoacaoRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/OrganizacaoRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/OrganizacaoRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/RequisicaoDoacaoRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/RequisicaoDoacaoRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/UsuarioOrganizacaoRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/UsuarioOrganizacaoRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/UsuarioRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/UsuarioRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/VoluntarioOrganizacaoRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/VoluntarioOrganizacaoRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/domain/repository/VoluntarioRepository.kt`](../src/main/kotlin/com/br/foodbridge/domain/repository/VoluntarioRepository.kt)
- [`src/main/kotlin/com/br/foodbridge/exception/GlobalExceptionHandler.kt`](../src/main/kotlin/com/br/foodbridge/exception/GlobalExceptionHandler.kt)
- [`src/main/kotlin/com/br/foodbridge/exception/custom/BusinessException.kt`](../src/main/kotlin/com/br/foodbridge/exception/custom/BusinessException.kt)
- [`src/main/kotlin/com/br/foodbridge/exception/custom/ResourceNotFoundException.kt`](../src/main/kotlin/com/br/foodbridge/exception/custom/ResourceNotFoundException.kt)
- [`src/main/kotlin/com/br/foodbridge/exception/custom/ValidationException.kt`](../src/main/kotlin/com/br/foodbridge/exception/custom/ValidationException.kt)
- [`src/main/kotlin/com/br/foodbridge/exception/response/ErrorResponse.kt`](../src/main/kotlin/com/br/foodbridge/exception/response/ErrorResponse.kt)
- [`src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt`](../src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt)
- [`src/main/kotlin/com/br/foodbridge/service/AdminService.kt`](../src/main/kotlin/com/br/foodbridge/service/AdminService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/AuthService.kt`](../src/main/kotlin/com/br/foodbridge/service/AuthService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/ContextoAcessoService.kt`](../src/main/kotlin/com/br/foodbridge/service/ContextoAcessoService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/DashboardService.kt`](../src/main/kotlin/com/br/foodbridge/service/DashboardService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/DoacaoService.kt`](../src/main/kotlin/com/br/foodbridge/service/DoacaoService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculator.kt`](../src/main/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculator.kt)
- [`src/main/kotlin/com/br/foodbridge/service/OrganizacaoService.kt`](../src/main/kotlin/com/br/foodbridge/service/OrganizacaoService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/RequisicaoDoacaoService.kt`](../src/main/kotlin/com/br/foodbridge/service/RequisicaoDoacaoService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/UsuarioService.kt`](../src/main/kotlin/com/br/foodbridge/service/UsuarioService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/VoluntarioService.kt`](../src/main/kotlin/com/br/foodbridge/service/VoluntarioService.kt)
- [`src/main/kotlin/com/br/foodbridge/service/utils/JwtService.kt`](../src/main/kotlin/com/br/foodbridge/service/utils/JwtService.kt)
- [`src/main/resources/application.properties.example`](../src/main/resources/application.properties.example)
- [`src/test/kotlin/com/br/foodbridge/FoodbridgeApplicationTests.kt`](../src/test/kotlin/com/br/foodbridge/FoodbridgeApplicationTests.kt)
- [`src/test/kotlin/com/br/foodbridge/config/SchemaValidationContextTest.kt`](../src/test/kotlin/com/br/foodbridge/config/SchemaValidationContextTest.kt)
- [`src/test/kotlin/com/br/foodbridge/config/SchemaValidationPolicyTest.kt`](../src/test/kotlin/com/br/foodbridge/config/SchemaValidationPolicyTest.kt)
- [`src/test/kotlin/com/br/foodbridge/controller/FluxoFuncionalHttpIntegrationTest.kt`](../src/test/kotlin/com/br/foodbridge/controller/FluxoFuncionalHttpIntegrationTest.kt)
- [`src/test/kotlin/com/br/foodbridge/controller/HealthControllerTest.kt`](../src/test/kotlin/com/br/foodbridge/controller/HealthControllerTest.kt)
- [`src/test/kotlin/com/br/foodbridge/controller/OrganizacaoAutorizacaoHttpIntegrationTest.kt`](../src/test/kotlin/com/br/foodbridge/controller/OrganizacaoAutorizacaoHttpIntegrationTest.kt)
- [`src/test/kotlin/com/br/foodbridge/middleware/JwtFilterRotasPublicasTest.kt`](../src/test/kotlin/com/br/foodbridge/middleware/JwtFilterRotasPublicasTest.kt)
- [`src/test/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculatorTest.kt`](../src/test/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculatorTest.kt)
- [`src/test/kotlin/com/br/foodbridge/service/OrganizacaoServiceAutorizacaoTest.kt`](../src/test/kotlin/com/br/foodbridge/service/OrganizacaoServiceAutorizacaoTest.kt)
- [`src/test/kotlin/com/br/foodbridge/service/RequisicaoDoacaoConcorrenciaIntegrationTest.kt`](../src/test/kotlin/com/br/foodbridge/service/RequisicaoDoacaoConcorrenciaIntegrationTest.kt)
- [`src/test/kotlin/com/br/foodbridge/service/VoluntarioServiceAutorizacaoTest.kt`](../src/test/kotlin/com/br/foodbridge/service/VoluntarioServiceAutorizacaoTest.kt)
- [`tests/ci/test_inventory_contract.py`](../tests/ci/test_inventory_contract.py)
- [`tests/ci/test_oracle_api_incident_contract.py`](../tests/ci/test_oracle_api_incident_contract.py)
- [`tests/ci/test_oracle_api_manual_release_contract.py`](../tests/ci/test_oracle_api_manual_release_contract.py)
- [`tests/ci/test_oracle_deploy_contract.py`](../tests/ci/test_oracle_deploy_contract.py)
- [`tests/ci/test_oracle_network_diagnostic_contract.py`](../tests/ci/test_oracle_network_diagnostic_contract.py)
- [`tests/ci/test_oracle_predeploy_readiness_contract.py`](../tests/ci/test_oracle_predeploy_readiness_contract.py)
- [`tests/ci/test_oracle_preflight_contract.py`](../tests/ci/test_oracle_preflight_contract.py)
- [`tests/ci/test_oracle_release_capacity_gate_contract.py`](../tests/ci/test_oracle_release_capacity_gate_contract.py)
- [`tests/ci/test_oracle_remote_preflight_contract.py`](../tests/ci/test_oracle_remote_preflight_contract.py)
- [`tests/ci/test_outbound_deploy_contract.py`](../tests/ci/test_outbound_deploy_contract.py)
- [`tests/ci/test_schema_validation_contract.py`](../tests/ci/test_schema_validation_contract.py)
- [`tools/atualizar_inventario.py`](../tools/atualizar_inventario.py)
<!-- INVENTARIO_ARQUIVOS_FIM -->

**Limite do inventário:** paths e estruturas versionadas; não contém dados de clientes, secrets, histórico completo de commits, servidores terceirizados ou arquivos gerados em `build/`.
