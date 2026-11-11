# FoodBridge — arquitetura e catálogo UML

> **FoodBridge / Arquitetura / UML** · Documento **FB-UML-001** · Versão **2.0.0 proposta** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


## Convenções

- Arquivos PlantUML `.puml` editáveis, um diagrama por finalidade.
- Diagramas **reconstruídos a partir do inventário de 29/10/2026** e da arquitetura observada, não extraídos automaticamente das classes atuais.
- `note` e legendas distinguem restrições desejadas de comportamento implementado.
- Ao alterar entidade, endpoint ou regra, editar o diagrama correspondente, o plano de testes e a matriz de rastreabilidade.

## Catálogo de diagramas

## 8. UML — catálogo e interpretação
O catálogo histórico contém 16 diagramas PlantUML. A revisão 1.2.0 acrescentou o 17º diagrama e a proposta 2.0.0 acrescenta o 18º sobre voluntários; referências a Word e caminhos `UML/` pertencem ao inventário original. Diagramas de estados descrevem transições observadas, sem assumir validação central inexistente.
- `01_casos_de_uso_geral.puml` — Casos de uso de todos os papéis e módulos.
- `02_componentes_backend.puml` — Arquitetura em componentes e dependências.
- `03_modelo_de_dominio_classes.puml` — Modelo de classes e cardinalidades JPA.
- `04_sequencia_login_e_selecao.puml` — Sequência de login TEMP/ACCESS.
- `05_atividade_cadastro_organizacao.puml` — Atividade do cadastro e vinculação de organização.
- `06_maquina_estados_usuario.puml` — Estados do usuário.
- `07_maquina_estados_organizacao.puml` — Estados da organização.
- `08_maquina_estados_doacao.puml` — Estados possíveis de doação e fragilidade de status controlado pelo cliente.
- `09_sequencia_solicitacao_aprovacao.puml` — Sequência solicitar e aprovar doação.
- `10_maquina_estados_requisicao.puml` — FSM de solicitação.
- `11_sequencia_coleta_conclusao.puml` — Vinculação de voluntário e conclusão de coleta.
- `12_casos_uso_voluntarios.puml` — Casos de uso de voluntários e superfície de autorização.
- `13_sequencia_dashboard_impacto.puml` — Sequência de cálculo e dashboards.
- `14_implantacao_logica.puml` — Diagrama lógico de implantação.
- `15_atividade_aprovacoes.puml` — Processo de verificação administrativa.
- `16_atividade_fechamento_doacao.puml` — Atividade de fechamento manual.
- `17_sequencia_autorizacao_organizacao.puml` — Guarda do service e revalidação por tenant para PUT, DELETE e PATCH, mais integração HTTP do PR #4.
- `18_sequencia_isolamento_voluntarios.puml` — Operações GET/PUT/POST/PATCH com verificação de vínculos, anti-CPF cross-tenant e escopo da coleta.

## Camadas e código-fonte

- `controller/`: HTTP/DTOs (8 controllers no snapshot auditado).
- `service/`: operações e regras de domínio, incluindo `ImpactoAlimentarCalculator`.
- `middleware/JwtFilter.kt`, `service/utils/JwtService.kt`, `config/SecurityConfig.kt`: fronteiras de autenticação e autorização.
- `domain/model/`, `domain/repository/`: entidades e consultas via JPA.
- PostgreSQL: persistência canônica; dados com JSONB e referências entre doações, requisições e voluntários.
- Nginx/systemd/Oracle: implantação (ver [guia Oracle](../DEPLOY_ORACLE.md)).

## Visualização

Abrir `.puml` no PlantUML, extensão VS Code ou renderizador de confiança. Alterações na estrutura de pastas exigem revisão de links. Nenhum diagrama deve ser usado como prova automática de autorização segura ou de serialização sem erros.
