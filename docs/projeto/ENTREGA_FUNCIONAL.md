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

### Execução remota dedicada
`.github/workflows/oracle-preflight.yml` testa SSH e preflight na main,
no environment oracle-production. Roda manualmente ou quando seu próprio arquivo/script muda;
não publica artefatos na VM. Exige a fingerprint da RSA dedicada, chave sem prompt
e host key já validada em OCI_SSH_KNOWN_HOSTS. O resultado ainda é pendente até o Actions real.

### Privacidade de consultas e cadastro administrativo
Consultas por CNPJ e por ID de outro tenant retornam os dados institucionais sem listas
de usuários/voluntários. Organizações serializadas dentro de outros recursos não incluem
relações JPA de usuários, voluntários ou doações. A validação Bean Validation é efetivamente
ativada no POST/PUT de organização; ADMIN é recusado no cadastro comum.
Dois testes HTTP adicionais comprovam minimização de vínculos e recusa de ADMIN sem persistência.
A suíte integrada passa de sete para nove testes HTTP. CI desta revisão ainda pendente.

## Perfil de usuário — FB-USR-002
Correção de USR-07: campos ausentes no PUT preservam nome/e-mail/senha.
Cadastro usa Bean Validation; atualização aceita campos opcionais e recusa nome/e-mail
em branco. Status informado pelo cliente não é aplicado. Exclusão segue transacional.

| Métodos em PerfilHttpIntegrationTest | Casos do plano |
|---|---|
| cadastroValidoUsaBcryptENaoAceitaAutoAprovacao | USR-01/02/04/11 |
| cadastroInvalidoRecusaNomeEmailESenha | USR-03 |
| alterarApenasNomePreservaEmailSenhaEStatus | USR-07/11 |
| emailDuplicadoNaoAlteraPerfil | USR-08 |
| trocaDeSenhaInvalidaSenhaAntigaEPermiteNova | USR-09 |
| exclusaoRemoveContaEVinculosEPreservaOrganizacao | USR-10/AUT-15 |

Seis novos testes HTTP/PostgreSQL estão adicionados no PR #12.
Execução do CI ainda pendente nesta revisão; não é homologação do navegador/VM.

### Evidência remota — SSH e preflight
[Actions #43339087021](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43339087021)
executou o workflow dedicado na main `6b78bf47e5a4113f46098dad47a8978db7635f75`.
O job parou com `Configuração ausente: OCI_SSH_HOST no environment oracle-production`.
No job, OCI_SSH_HOST, OCI_SSH_USER, OCI_SSH_PRIVATE_KEY e OCI_SSH_KNOWN_HOSTS
não estavam disponíveis. A autenticação SSH e o preflight da VM ficaram skipped.
Nenhum SSH_RSA_OK, SSH_EXIT=0, deploy, restart ou SQL foi executado.
O acesso à administração de variáveis/secrets não está exposto pelo conector atual.
