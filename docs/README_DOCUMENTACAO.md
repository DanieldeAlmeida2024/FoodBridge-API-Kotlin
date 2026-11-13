# Central de documentação — FoodBridge (API + Web)

> **FB-DOC-HUB-001 · 13/11/2026.** Este índice é o ponto de entrada **atual** para estudar, apresentar, operar ou manter a plataforma. Fontes primárias: código e testes versionados, seguidos de logs de CI/CD. Não confundir documentação histórica com comportamento atual.

## Comece por aqui

| Objetivo | Documento principal | O que encontrará |
|---|---|---|
| Entender o produto | [Mapa de recursos e status](RECURSOS_E_STATUS.md) | Fluxo social, módulos, cobertura e lacunas reais |
| Auditar todos os arquivos | [Inventário completo do repositório](INVENTARIO_PROJETO.md) | Árvore de todos os arquivos Git, organização por área e regra de manutenção |
| Estudar arquitetura | [Arquitetura atual](ARQUITETURA_ATUAL.md) | Componentes, dados, sequência JWT, fluxo de coleta e implantação |
| Consultar API | [Catálogo atual de 50 rotas REST](REFERENCIA_API_ATUAL.md) | Método, caminho, guarda e finalidade |
| Estudar para prova/entrevista | [Guia de estudo](GUIA_ESTUDO.md) | Trilha por sessão, exercícios, testes e perguntas |
| Apresentar o projeto | [Roteiro de apresentação](APRESENTACAO_PROJETO.md) | Pitch, apresentação de 8 telas, demonstração e evidências |
| Conferir CD real | [CI/CD automático na Micro](implantacao/OCI_MICRO_AUTOMATIC_CICD.md) | Jobs, requisitos, backup/rollback e logs PASS |
| Ver incidente e aprendizado | [Incidente de implantação](implantacao/INCIDENTE_DEPLOY_API_20261113.md) | Diagnóstico de RAM, Kdump, saúde e mitigação |
| Histórico de requisitos | [Especificação histórica](projeto/ESPECIFICACAO_PROJETO.md), [riscos](projeto/RISCOS.md) | Baseline documentada, nem toda afirmação atual |
| Testes planejados | [Plano](testes/PLANO_DE_TESTES.md) e [matriz](testes/MATRIZ_RASTREABILIDADE.md) | Casos de teste, critérios e rastreabilidade |
| UML editável | [Catálogo PlantUML](uml/README.md) | Casos de uso, classes, sequências, estados e deploy |
| Versões documentais | [Controle de versões](controle/VERSIONAMENTO.md) | Revisões, decisões e evidências |

## Repositórios e divisão de responsabilidade

**API**: [FoodBridge-API-Kotlin](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin), Kotlin/Spring/Java21/PostgreSQL. **Web**: [Foodbridge-Frontend-React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React), React/Vite/TypeScript e JavaScript.

Documentação dedicada da interface: [central Web](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/README_DOCUMENTACAO.md), [10 rotas de UI](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/RECURSOS_E_TELAS.md), [estudo React](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/GUIA_ESTUDO.md).

## Qual documento tem precedência?

1. Código no commit atual, testes executáveis e logs de Actions; o código manda sobre tabelas antigas.
2. Os documentos **ATUAL**, **RECURSOS**, **INVENTARIO**, **ESTUDO** e **APRESENTACAO** deste índice.
3. Especificações antigas com versões explícitas; úteis para histórico e planejamento, não comprovação da release.
4. Plano de testes planejado: não converter “165 cenários planejados” em “165 casos executados”.

## Versão e manutenção

Documento vivo em Markdown. A cada PR que altera controllers, services, DTOs, entidades, segurança, workflows ou UI correlata, atualizar os documentos relevantes e reconciliar o inventário:

```bash
python3 tools/atualizar_inventario.py --write
python3 tools/atualizar_inventario.py --check
```

O CI contém `tests/ci/test_inventory_contract.py`, que reprova um PR quando a lista de arquivos versionados diverge do Git. **A checagem é objetiva para caminhos, não interpreta mudanças semânticas**: revisar manualmente os módulos e diagramas.

## Evidência de publicação automática

- API: [run #43465999269 — SUCCESS](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).
- Web: [run #43465805656 — SUCCESS](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656).

A implantação automática foi homologada em 13/11/2026, mas não equivale a certificação de segurança, LGPD, disponibilidade 24/7 ou 100% dos fluxos de produção.
