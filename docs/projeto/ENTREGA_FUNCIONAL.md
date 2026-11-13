# FoodBridge — entrega funcional

Documento FB-ENT-001 · v1.0.0 · 13/11/2026.
Entrega significa fluxo completo validado por módulo, integrado ao frontend e operado na infraestrutura.
Esta matriz registra o estado real; código consolidado ou CI isolado não equivalem a entrega total.

| Módulo | Implementação nesta integração | Evidência necessária para conclusão |
|---|---|---|
| Cadastro e login | Cadastro existente; enum corrigido; suspensão conferida no banco; JWT inválido 401 | HTTP e PostgreSQL; UI de cadastro/login; cenários negativos |
| Organizações | Guardas PR #4; vínculo correto na seleção; onboarding transacional | Testes integrados; gestão por organizações não-ADMIN ainda requer RBAC de gestor |
| Sessão web | Auditoria identificou restauração, logout e organização divergentes | Correção no frontend e testes de sessão/rotas |
| Doações | Código atual + lock PR #6; quantidades finitas; disponibilidade por validade | HTTP de criação/edição/exclusão/fechamento; estados terminais e unidades |
| Solicitações e estoque | Lock comum; duplicidade de criação serializada | Teste PostgreSQL de concorrência e ciclo HTTP completo |
| Voluntários | Isolamento PR #5 e coleta pelo vínculo ativo da ONG | Testes de isolamento + HTTP CRUD e revogação antes da retirada |
| Coleta e indicadores | Sete novos testes integrados incluem coleta e indicadores doador/ONG | CI da branch e reconciliação de fechamento manual/dupla contagem |
| Administração | Autorização vigente, suspensão ADMIN e transações | HTTP de aprovações/reprovações, consultas e isolamento |
| Infraestrutura | Código dos PRs #9/#10 preservado; preflight separado de deploy | SSH dedicado legível, host key verificada e preflight remoto real |
| Publicação | Nenhuma executada | Homologação integral, plano de rollback e autorização específica |

## Regressões executáveis

- Suítes preservadas: organização (unitário/HTTP), voluntários, estoque concorrente,
  impacto, rotas públicas, políticas e contexto schema-validation, contratos de CI/CD.
- Nova suíte: `FluxoFuncionalHttpIntegrationTest` (7 métodos).
- Estado inicial deste documento: execução combinada pendente; atualizar com SHA e Actions reais.
- Os 49 endpoints/165 cenários do plano histórico continuam sendo referência de cobertura,
  sem declaração de que todos foram executados.
 
### Evidência combinada e publicação manual
O [Actions #43336071509](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43336071509)
no SHA `c7886081a2130a51ee42786336f731cfceb62bf6` aprovou `clean test bootJar`,
PostgreSQL efêmero, sintaxe Bash e 18 testes de contrato. Preflight/Oracle ficaram skipped.
O frontend integrado está no [PR web #3](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/pull/3).
A revisão atual torna o deploy da API **exclusivamente manual** (`workflow_dispatch`,
`deploy=true`, `preflight=false` e `OCI_DEPLOY_ENABLED=true`).
Um push/merge na main não inicia deploy, independentemente de variáveis antigas de auto-deploy.
O contrato automatizado verifica essa restrição; a nova revisão exige seu próprio CI.
