# FoodBridge — plano mestre de testes

> **FoodBridge / Plano de testes** · Documento **FB-TST-001** · Versão **1.1.0** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


**Escopo de referência:** 165 cenários planejados no documento do usuário de 29/10/2026. **Não afirmar que esses 165 foram executados.** Os testes de CI do repositório cobrem apenas parte do plano; ver seção “Estado de execução”.

## 3. Estratégia, critérios e responsabilidade
**Pirâmide:** unitários de services/validators/impacto; integração JPA/JSONB/JWT com PostgreSQL; API com MockMvc/REST Assured; E2E via Postman/Newman; contrato de DTO; segurança/concorrência; não funcionais.
**Ferramentas propostas:** JUnit 5 + Spring Boot Test + MockK/Mockito + MockMvc, Testcontainers PostgreSQL, JaCoCo, OWASP ZAP (ambiente autorizado), k6, Detekt/ktlint, Newman e GitHub Actions. Estas ferramentas são propostas; não foram encontradas todas configuradas no commit.
**Ambientes:** local unitário sem banco, integração efêmera com PostgreSQL (Testcontainers) e homologação com dados sintéticos; jamais usar CPF real, senha real ou produção para cargas destrutivas.
**Entrada:** branch e commit congelados, schema/migrações determinísticas, contas fixture e chaves de JWT de teste, API operacional em homologação, sementes idempotentes.
**Saída sugerida:** 100% dos P0 aprovados; nenhuma vulnerabilidade crítica ou alta aberta; ≥85% de cobertura de linhas em services e ≥80% branch nas regras críticas (meta futura); zero regressões de isolamento/autorização; teste de concorrência sem oversubscription; métricas de desempenho aceitas.
**Critérios para bloqueio:** falha do build; erro 500 em fluxo válido; escalada de privilégios; IDOR em dados pessoais; reserva acima do estoque; vazamento de tokens; inconsistência transacional.
**Severidade de defeitos:** S1 crítico (segurança/estoque/perda); S2 alto (fluxo principal quebrado); S3 médio (inconsistência e validação); S4 baixo (mensagem/documentação).
**Status de execução:** NÃO EXECUTADO — os casos abaixo são planejamento verificável, não relatório de sucesso.

## 4. Dados e fixtures de teste
| ID | Perfil/dado | Características |
|---|---|---|
| ADMIN-01 | Administrador | Usuário e organização VERIFICADO, vínculo ADMIN VERIFICADO, ACCESS. |
| USR-01 | Usuário em cadastro | Usuário PENDENTE_VERIFICACAO, sem vínculo, TEMP de login. |
| DOAD-01 | Organização doadora A | Status VERIFICADO, role DOADOR; usuário verificado; vínculo verificado. |
| DOAD-02 | Organização doadora B | Segunda organização para testes de isolamento horizontal. |
| ONG-01 | ONG solicitante A | Status VERIFICADO, role ONG, vínculo confirmado. |
| ONG-02 | ONG solicitante B | Outra ONG, sem vínculo com os recursos da ONG-01. |
| VOL-01 | Voluntário ativo | CPF fictício válido de teste e vínculo ATIVO com ONG-01. |
| VOL-02 | Voluntário inativo | Mesmo tenant com vínculo INATIVO. |
| DON-01 | Doação com estoque | 10 KG em PUBLICADO, doadora DOAD-01, duas janelas válidas. |
| REQ-01 | Pedido pendente | ONG-01 solicita 4 KG da DON-01. |
**Regras dos dados:** ids gerados após setup; documentos sintéticos e válidos conforme validadores; limpar a base entre cenários; separar estoque/unidades; congelar tempo nas validações temporais.

## 5. Casos de teste detalhados
**Total planejado: 165 casos.** IDs por módulo; P0 = obrigatório para liberação, P1 = alta prioridade de estabilização. Toda expectativa descreve critério de aceitação; bugs conhecidos podem gerar resultado divergente.

### Autenticação, JWT e seleção de organização (16 casos)
**AUT-01 — Login com senha correta e vínculo ADMIN verificado**  
- Prioridade: **P0** | Camada: API  
- Execução: Criar ADMIN habilitado; enviar POST /auth/login  
- Resultado esperado: 200, accessToken preenchido, tempToken nulo; role ADMIN no JWT.  

**AUT-02 — Login válido de usuário não ADMIN**  
- Prioridade: **P0** | Camada: API  
- Execução: Preparar usuário verificado com vínculo ONG; realizar login  
- Resultado esperado: 200, tempToken preenchido, accessToken nulo; organizações listadas.  

**AUT-03 — Senha incorreta**  
- Prioridade: **P0** | Camada: API  
- Execução: Enviar e-mail existente com senha errada  
- Resultado esperado: Erro de credenciais sem emitir token; contrato atual: 409.  

**AUT-04 — E-mail ou senha em branco**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Invocar AuthService.login com cada campo em branco  
- Resultado esperado: ValidationException e nenhuma tokenização.  

**AUT-05 — E-mail inexistente**  
- Prioridade: **P1** | Camada: API  
- Execução: Login com e-mail não cadastrado  
- Resultado esperado: 404 atual; avaliar resposta neutra para não enumerar contas.  

**AUT-06 — Selecionar organização com TEMP e vínculo verificado**  
- Prioridade: **P0** | Camada: API  
- Execução: Login padrão; POST /auth/select-org com organizacaoId válido  
- Resultado esperado: 200, ACCESS escopado à organização e papel do vínculo.  

**AUT-07 — Selecionar organização sem vínculo**  
- Prioridade: **P0** | Camada: API  
- Execução: Usar TEMP de usuário A e id da organização B não vinculada  
- Resultado esperado: Acesso recusado; nenhum token ACCESS gerado.  

**AUT-08 — Selecionar organização não aprovada**  
- Prioridade: **P0** | Camada: API  
- Execução: Vínculo com organização REVISAO; selecionar no TEMP  
- Resultado esperado: Rejeição da seleção e ausência de ACCESS.  

**AUT-09 — Selecionar vínculo pendente**  
- Prioridade: **P0** | Camada: API  
- Execução: TEMP de usuário verificado com vínculo REVISAO  
- Resultado esperado: Rejeição; não gerar ACCESS.  

**AUT-10 — Token TEMP fora das rotas permitidas**  
- Prioridade: **P0** | Camada: API  
- Execução: GET /doacoes com Bearer TEMP  
- Resultado esperado: Acesso recusado; padronizar 403/401 após ajuste do tratamento.  

**AUT-11 — Token ACCESS em rota exclusiva de TEMP**  
- Prioridade: **P0** | Camada: API  
- Execução: POST /auth/select-org com Bearer ACCESS  
- Resultado esperado: Acesso recusado; nenhuma troca de organização.  

**AUT-12 — JWT expirado, alterado e malformado**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Repetir GET protegido com tokens nas três condições  
- Resultado esperado: Não autenticar; não revelar dados nem stacktrace.  

**AUT-13 — Validação de claims**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Testar ausência de type, status, role, org e sub não numérico  
- Resultado esperado: Erro controlado sem autenticação e sem vazamento.  

**AUT-14 — Status do usuário muda após emissão do token**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Emitir ACCESS; suspender/inativar usuário; tentar operação  
- Resultado esperado: Política de revogação exigida; validar se token antigo ainda opera (risco atual).  

**AUT-15 — Remover vínculo após emissão de ACCESS**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Emitir token; inativar vínculo; tentar ação da organização  
- Resultado esperado: Deve bloquear acesso após revogação; verificar comportamento atual.  

**AUT-16 — ADMIN com status de usuário não verificado**  
- Prioridade: **P1** | Camada: API  
- Execução: Usar conta ADMIN suspensa/inativa e vínculo validado  
- Resultado esperado: Deve recusar login/uso do ACCESS; risco no bypass de status.  


### Usuários e dados pessoais (12 casos)
**USR-01 — Cadastrar usuário válido**  
- Prioridade: **P0** | Camada: API  
- Execução: POST /usuarios com nome, email e senha válidos  
- Resultado esperado: 201, status PENDENTE_VERIFICACAO; nenhuma senha na resposta.  

**USR-02 — E-mail duplicado**  
- Prioridade: **P0** | Camada: API  
- Execução: Repetir cadastro com mesmo email  
- Resultado esperado: 409 e apenas uma linha de usuário persistida.  

**USR-03 — Nome, e-mail e senha inválidos**  
- Prioridade: **P1** | Camada: API  
- Execução: Enviar vazio, email inválido e senha em branco separadamente  
- Resultado esperado: 400 por validação de contrato; confirmar @Valid ausente na rota.  

**USR-04 — Senha gravada com BCrypt**  
- Prioridade: **P0** | Camada: Unitário  
- Execução: Cadastrar usuário e inspecionar hash  
- Resultado esperado: Hash diferente da senha; PasswordEncoder.matches verdadeiro.  

**USR-05 — Consultar /usuarios/me**  
- Prioridade: **P1** | Camada: API  
- Execução: Usar ACCESS de usuário autenticado  
- Resultado esperado: 200 com somente seus atributos públicos.  

**USR-06 — Listar organizações do próprio usuário**  
- Prioridade: **P0** | Camada: API  
- Execução: GET /usuarios/me/organizacoes com múltiplos vínculos  
- Resultado esperado: 200, organizações corretas com status efetivo calculado.  

**USR-07 — Alterar nome sem alterar e-mail**  
- Prioridade: **P0** | Camada: API  
- Execução: PUT /usuarios/me apenas com novo nome  
- Resultado esperado: E-mail anterior deve permanecer; detectar sobrescrita por valor default vazio.  

**USR-08 — Atualizar e-mail para duplicado**  
- Prioridade: **P0** | Camada: API  
- Execução: PUT /usuarios/me com e-mail pertencente a outro usuário  
- Resultado esperado: 409; não alterar conta.  

**USR-09 — Alterar senha**  
- Prioridade: **P0** | Camada: API  
- Execução: PUT /usuarios/me com senha nova; logins antes/depois  
- Resultado esperado: Nova senha com BCrypt; antiga deixa de autenticar.  

**USR-10 — Excluir usuário e vínculos**  
- Prioridade: **P0** | Camada: Integração  
- Execução: DELETE /usuarios/me de usuário com vínculos  
- Resultado esperado: 204; conta e vínculos tratados sem órfãos; demais entidades preservadas.  

**USR-11 — Não aceitar status fornecido pelo cliente**  
- Prioridade: **P1** | Camada: Segurança  
- Execução: POST e PUT /usuarios com status VERIFICADO  
- Resultado esperado: O cliente não pode autoaprovar conta.  

**USR-12 — Negar edição e exclusão sem ACCESS**  
- Prioridade: **P1** | Camada: API  
- Execução: PUT/DELETE /usuarios/me sem token  
- Resultado esperado: Acesso recusado sem alteração.  


### Cadastro, consulta e gestão de organizações (17 casos)
**ORG-01 — Cadastrar organização nova**  
- Prioridade: **P0** | Camada: API  
- Execução: Usar TEMP válido; POST /organizacoes com CNPJ e dados inéditos  
- Resultado esperado: 201; organização REVISAO e vínculo do autor REVISAO.  

**ORG-02 — Vincular-se a organização existente**  
- Prioridade: **P0** | Camada: API  
- Execução: TEMP do usuário B; cadastrar com CNPJ e mesmo e-mail da organização A  
- Resultado esperado: 201 e vínculo B em REVISAO sem duplicar organização.  

**ORG-03 — Impedir CNPJ com e-mail divergente**  
- Prioridade: **P0** | Camada: API  
- Execução: Informar CNPJ existente ativo e e-mail diferente  
- Resultado esperado: 409; dados originais imutáveis.  

**ORG-04 — Impedir e-mail de organização duplicado**  
- Prioridade: **P1** | Camada: API  
- Execução: Usar CNPJ diferente e e-mail já cadastrado  
- Resultado esperado: 409; sem criar organização.  

**ORG-05 — Reativar organização INATIVO**  
- Prioridade: **P1** | Camada: API  
- Execução: Usar CNPJ de organização inativa com dados alterados  
- Resultado esperado: Organização retorna a REVISAO, verificação limpa.  

**ORG-06 — Reativar vínculo INATIVO**  
- Prioridade: **P1** | Camada: API  
- Execução: Usar mesmo usuário e organização com vínculo inativo  
- Resultado esperado: Reativação em REVISAO; não criar linha duplicada.  

**ORG-07 — Bloquear vínculo duplicado ativo**  
- Prioridade: **P0** | Camada: API  
- Execução: Reenviar cadastro pelo mesmo usuário  
- Resultado esperado: 409; única associação.  

**ORG-08 — Consultar por CNPJ com TEMP**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /organizacoes/cnpj/{cnpj} válido e inválido  
- Resultado esperado: 200 ou 404; nenhuma informação confidencial extra.  

**ORG-09 — Consultar ID e organização do token**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /organizacoes/{id} e /me  
- Resultado esperado: 200; /me recupera apenas a organização autenticada.  

**ORG-10 — Impugnar PUT de outra organização**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: ACCESS da org A; PUT /organizacoes/{idB}  
- Resultado esperado: Esperado seguro: 403/404; verificar possível edição cruzada atual.  

**ORG-11 — Impugnar alteração do role**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: PUT organização com role ADMIN e status VERIFICADO  
- Resultado esperado: Alteração de privilégios e autoaprovação proibidas.  

**ORG-12 — Inativar organização como ADMIN**  
- Prioridade: **P0** | Camada: API  
- Execução: DELETE /organizacoes/{id} com role ADMIN  
- Resultado esperado: 204, status INATIVO, preservação do histórico.  

**ORG-13 — Negar DELETE de não ADMIN**  
- Prioridade: **P0** | Camada: API  
- Execução: ACCESS ONG/DOADOR; excluir organização  
- Resultado esperado: 403, sem alteração.  

**ORG-14 — Aprovar vínculo organizacional**  
- Prioridade: **P0** | Camada: API  
- Execução: Role ADMIN do contexto, ID de vínculo da própria organização  
- Resultado esperado: 204, vínculo VERIFICADO; não afetar outro tenant.  

**ORG-15 — Reprovar vínculo organizacional**  
- Prioridade: **P0** | Camada: API  
- Execução: Role ADMIN do contexto, vínculo da organização  
- Resultado esperado: 204, vínculo INATIVO; verificar cast role String -> enum.  

**ORG-16 — Validar formatos de CNPJ e e-mail**  
- Prioridade: **P1** | Camada: API  
- Execução: Enviar CNPJ não numérico, e-mail inválido, campos vazios  
- Resultado esperado: 400 nos contratos definidos; @Valid ausente precisa ser corrigido.  

**ORG-17 — Consultar dados de outra organização**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: ACCESS comum em GET /organizacoes/{id} e listagens  
- Resultado esperado: Aplicar política explícita de exposição; não disponibilizar dados sensíveis por ID.  


### Administração e verificações (12 casos)
**ADM-01 — Listar organizações e pendentes**  
- Prioridade: **P0** | Camada: API  
- Execução: ADMIN GET das duas rotas  
- Resultado esperado: 200; pendentes incluem DOCUMENTOS_PENDENTES/REVISAO, não verificadas.  

**ADM-02 — Aprovar organização em REVISAO**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /admin/organizacoes/{id}/aprovar  
- Resultado esperado: 204, status VERIFICADO, verificationDate preenchida.  

**ADM-03 — Aprovar org e atualizar vínculos elegíveis**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Criar vínculos REVISAO com usuários verificados e não verificados  
- Resultado esperado: Só vínculos com usuário VERIFICADO tornam-se VERIFICADO.  

**ADM-04 — Reprovar organização**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /admin/organizacoes/{id}/reprovar  
- Resultado esperado: 204, organização INATIVO, vínculos INATIVO.  

**ADM-05 — Listar usuários e pendentes**  
- Prioridade: **P0** | Camada: API  
- Execução: ADMIN GET das duas rotas  
- Resultado esperado: 200; pendentes apenas PENDENTE_VERIFICACAO.  

**ADM-06 — Aprovar usuário**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /admin/usuarios/{id}/aprovar  
- Resultado esperado: 204, usuário VERIFICADO, approvedAt preenchida.  

**ADM-07 — Aprovar usuário com organização verificada**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Usuário pendente com vínculo REVISAO e organização verificada  
- Resultado esperado: Vínculo elegível passa a VERIFICADO.  

**ADM-08 — Reprovar usuário**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /admin/usuarios/{id}/reprovar  
- Resultado esperado: 204, usuário INATIVO e approvedAt limpo.  

**ADM-09 — Bloquear usuários comuns em /admin/****  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Usar ACCESS com role ONG/DOADOR  
- Resultado esperado: 403, sem mutações ou listagens.  

**ADM-10 — Alvos inexistentes e ids inválidos**  
- Prioridade: **P1** | Camada: API  
- Execução: Testar 0, -1 e ID inexistente nos PATCHs  
- Resultado esperado: 400/404 respectivamente; sem 500.  

**ADM-11 — Idempotência de aprovação/reprovação**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Repetir mesmo PATCH 2x  
- Resultado esperado: Status e histórico coerentes; decidir e registrar contrato de idempotência.  

**ADM-12 — Bootstrapping administrador inicial**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Subir banco vazio e reiniciar a aplicação  
- Resultado esperado: Exatamente um admin/organização/vínculo, sem duplicação.  


### Publicação, manutenção e encerramento de doações (20 casos)
**DOA-01 — Criar doação válida**  
- Prioridade: **P0** | Camada: API  
- Execução: POST /doacoes com quantidade>0, janela válida, origem do token  
- Resultado esperado: 201, registro com organizacaoId do token; DTO correto.  

**DOA-02 — Recusar quantidade zero e negativa**  
- Prioridade: **P0** | Camada: API  
- Execução: POST com 0 e -0.1  
- Resultado esperado: 400 e nenhuma doação persistida.  

**DOA-03 — Exigir uma janela de coleta**  
- Prioridade: **P0** | Camada: API  
- Execução: POST com janelasDisponiveis=[]  
- Resultado esperado: 400; nenhuma persistência.  

**DOA-04 — Validar janela invertida ou duração zero**  
- Prioridade: **P0** | Camada: API  
- Execução: POST com fim <= inicio  
- Resultado esperado: 400; sem persistência.  

**DOA-05 — Validar doação expirada**  
- Prioridade: **P1** | Camada: API  
- Execução: POST com dataExpiracao anterior à atual  
- Resultado esperado: Regra de negócio desejada: recusar ou aplicar status EXPIRADO.  

**DOA-06 — Somente perfil doador publica**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: ACCESS ONG em POST /doacoes  
- Resultado esperado: Esperado: proibir; comportamento atual aceita qualquer ACCESS com org.  

**DOA-07 — Listar doações da organização**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /doacoes com ACCESS da org A  
- Resultado esperado: 200, apenas doações da A.  

**DOA-08 — Listar doações disponíveis de terceiros**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /doacoes/disponiveis  
- Resultado esperado: Somente PUBLICADO, DISPONIVEL ou PARCIAL, excluindo origem própria.  

**DOA-09 — Buscar por ID de outra organização**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: GET /doacoes/{idB} com ACCESS org A  
- Resultado esperado: Aplicar política explícita de visibilidade; avaliar exposição de dados fora do escopo.  

**DOA-10 — Editar doação própria**  
- Prioridade: **P0** | Camada: API  
- Execução: PUT /doacoes/{id} alterando campos legítimos  
- Resultado esperado: 200 e persistência das alterações.  

**DOA-11 — Editar ou excluir doação alheia**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: PUT e DELETE com token de outra organização  
- Resultado esperado: 409 atual por regra de domínio; não alterar/excluir.  

**DOA-12 — Não aceitar quantidade negativa na edição**  
- Prioridade: **P0** | Camada: API  
- Execução: PUT /doacoes/{id} com quantidade -4  
- Resultado esperado: Esperado 400; serviço atual não revalida quantidade.  

**DOA-13 — Fechar doação com coleta válida**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /doacoes/{id}/fechar, quantidade 0..quantidade  
- Resultado esperado: 200, status COMPLETO, campos finais e timestamp preenchidos.  

**DOA-14 — Fechar com coletada acima do total**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH com quantidadeColetada > quantidade  
- Resultado esperado: 400; status original preservado.  

**DOA-15 — Fechar doação não fechável**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH em RASCUNHO, EXPIRADO, CANCELADO e COMPLETO  
- Resultado esperado: 409 e sem alteração.  

**DOA-16 — Fechamento e requisições abertas**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Fechar manualmente doação que possui requisições APROVADO  
- Resultado esperado: Requisições e estado final devem permanecer consistentes.  

**DOA-17 — Recálculo não reabre doação fechada**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Fechar como COMPLETO; executar atualização das requisições  
- Resultado esperado: A doação não deve voltar para DISPONIVEL/PARCIAL.  

**DOA-18 — Remover doação sem requisições**  
- Prioridade: **P1** | Camada: API  
- Execução: DELETE /doacoes/{id} própria sem dependências  
- Resultado esperado: 204; remoção e integridade mantidas.  

**DOA-19 — Excluir doação com requisições**  
- Prioridade: **P0** | Camada: Integração  
- Execução: DELETE própria com requisições associadas  
- Resultado esperado: Rejeitar com regra controlada ou preservar integridade; nunca 500 ou perda de histórico.  

**DOA-20 — Impedir manipulação arbitrária do status**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: POST/PUT de doação com status COMPLETO ou TOTALMENTE_REIVINDICADO  
- Resultado esperado: Somente transições autorizadas pelo servidor.  


### Solicitações e ciclo de coleta (27 casos)
**REQ-01 — ONG requisita quantidade disponível**  
- Prioridade: **P0** | Camada: API  
- Execução: POST /requisicoes com ONG e doação PUBLICADO de terceiro  
- Resultado esperado: 201, status PENDENTE, requisitante do token.  

**REQ-02 — Bloquear requisição por perfil não ONG**  
- Prioridade: **P0** | Camada: API  
- Execução: POST com DOADOR/PRODUTOR/DISTRIBUIDOR  
- Resultado esperado: 409 atual; não persistir.  

**REQ-03 — ONG não pode requisitar a própria doação**  
- Prioridade: **P0** | Camada: API  
- Execução: Preparar organização ONG com doação própria e requisitar  
- Resultado esperado: 409; não persistir.  

**REQ-04 — Status não requisitável**  
- Prioridade: **P0** | Camada: API  
- Execução: Solicitar RASCUNHO, TOTAL, COMPLETO, EXPIRADO, CANCELADO  
- Resultado esperado: 409; nenhum pedido criado.  

**REQ-05 — Quantidade maior que saldo**  
- Prioridade: **P0** | Camada: API  
- Execução: Solicitar 11 de doação com 10 disponíveis  
- Resultado esperado: 409 e nenhuma reserva.  

**REQ-06 — Quantidade zero/negativa abaixo do mínimo**  
- Prioridade: **P0** | Camada: API  
- Execução: POST com 0, -5 e 0.09  
- Resultado esperado: 400 de Bean Validation; serviço isolado deve também validar.  

**REQ-07 — Bloquear pedido PENDENTE duplicado**  
- Prioridade: **P0** | Camada: API  
- Execução: Mesma ONG e doação, enviar segundo pedido PENDENTE  
- Resultado esperado: 409; um pedido aberto.  

**REQ-08 — Listar minhas requisições**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /requisicoes/minhas com ONG A  
- Resultado esperado: Somente solicitações da ONG A.  

**REQ-09 — Listar requisições recebidas**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /requisicoes/recebidas com DOADOR A  
- Resultado esperado: Somente pedidos de doações da organização A.  

**REQ-10 — Buscar requisição fora das partes**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: GET /requisicoes/{id} com org não doadora nem solicitante  
- Resultado esperado: 409 atual; dados não expostos.  

**REQ-11 — Doador aprova solicitação pendente**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /requisicoes/{id}/aprovar como dono da doação  
- Resultado esperado: 200, status APROVADO e respondedAt preenchido.  

**REQ-12 — Aprovação parcial atualiza saldo/status**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Aprovar 4 de 10 unidades de doação PUBLICADO  
- Resultado esperado: Doação PARCIALMENTE_REIVINDICADO, saldo=6.  

**REQ-13 — Aprovação total atualiza status**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Aprovar integral de uma doação de 10  
- Resultado esperado: Status TOTALMENTE_REIVINDICADO, saldo=0.  

**REQ-14 — Aprovar após redução de saldo**  
- Prioridade: **P0** | Camada: API  
- Execução: Criar vários PENDENTE; aprovar um; aprovar outro > saldo  
- Resultado esperado: Segundo PATCH 409, sem sobre-reserva.  

**REQ-15 — Aprovações simultâneas sobre mesma doação**  
- Prioridade: **P0** | Camada: Concorrência  
- Execução: Duas transações em paralelo aprovam pedidos 7 e 7, estoque 10  
- Resultado esperado: No máximo 10 aprovados; testar race condition atual sem lock.  

**REQ-16 — Rejeitar somente PENDENTE**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /requisicoes/{id}/rejeitar como doadora  
- Resultado esperado: REJEITADO e respondedAt; re-rejeitar negado.  

**REQ-17 — ONG cancela somente PENDENTE**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /requisicoes/{id}/cancelar como ONG solicitante  
- Resultado esperado: CANCELADO; segunda tentativa ou após APROVADO rejeitada.  

**REQ-18 — Bloquear operação por organização estranha**  
- Prioridade: **P0** | Camada: API  
- Execução: Aprovar, rejeitar, cancelar e concluir com terceiro  
- Resultado esperado: 409 atual; nenhuma alteração de status.  

**REQ-19 — Vincular voluntário somente após aprovação**  
- Prioridade: **P0** | Camada: API  
- Execução: Tentar vincular com PENDENTE e depois APROVADO  
- Resultado esperado: PENDENTE 409; APROVADO com voluntário elegível 200.  

**REQ-20 — Voluntário deve pertencer à ONG e estar ativo**  
- Prioridade: **P0** | Camada: API  
- Execução: Tentar voluntário sem vínculo, INATIVO e ATIVO  
- Resultado esperado: Só ATIVO pertencente à ONG é aceito.  

**REQ-21 — Concluir requisição aprovada e vinculada**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /requisicoes/{id}/concluir como doadora  
- Resultado esperado: 200, CONCLUIDO, completedAt e status da doação recalculado.  

**REQ-22 — Concluir sem voluntário**  
- Prioridade: **P0** | Camada: API  
- Execução: Aprovar pedido mas não vincular voluntário; concluir  
- Resultado esperado: 409 sem atualização.  

**REQ-23 — Concluir pedido não aprovado**  
- Prioridade: **P0** | Camada: API  
- Execução: Concluir pedido PENDENTE, REJEITADO, CANCELADO e CONCLUIDO  
- Resultado esperado: 409; preservar estado.  

**REQ-24 — Soma não pode incluir pedido cancelado ou rejeitado**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Aprovar 4, rejeitar 3, cancelar 2, concluir 4  
- Resultado esperado: Saldo subtrai apenas APROVADO/CONCLUIDO; impacto soma apenas CONCLUIDO.  

**REQ-25 — Precisão com quantidades fracionárias**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Usar valores 0.1 e 0.2 para estoque e aprovações  
- Resultado esperado: Evitar arredondamento inesperado e sub/sobre-reserva.  

**REQ-26 — Criar dois pedidos pendentes simultâneos da mesma ONG**  
- Prioridade: **P0** | Camada: Concorrência  
- Execução: Enviar requisições concorrentes com mesmo doacaoId e ONG  
- Resultado esperado: No máximo um PENDENTE; exigir proteção transacional/constraint.  

**REQ-27 — Completar parcialmente sem confundir oferta com coleta**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Doação 100, concluir requisição 15  
- Resultado esperado: Impacto registra 15, não 100; status preserva saldo real.  


### Voluntários e vínculos (12 casos)
**VOL-01 — Criar voluntário e vínculo ATIVO**  
- Prioridade: **P0** | Camada: API  
- Execução: POST /voluntarios com dados válidos  
- Resultado esperado: 201; voluntário e vínculo ativo na organização do token.  

**VOL-02 — Vincular voluntário existente via CPF**  
- Prioridade: **P0** | Camada: API  
- Execução: CPF existente e organização ainda não vinculada  
- Resultado esperado: 201; novo vínculo, sem duplicar voluntário.  

**VOL-03 — Bloquear duplicidade de vínculo**  
- Prioridade: **P1** | Camada: API  
- Execução: Cadastrar CPF já vinculado à organização  
- Resultado esperado: 409; sem novo vínculo.  

**VOL-04 — Bloquear e-mail utilizado por outro voluntário**  
- Prioridade: **P1** | Camada: API  
- Execução: CPF novo e e-mail já cadastrado  
- Resultado esperado: 409; sem linha duplicada.  

**VOL-05 — Validar CPF, telefone, nome e email**  
- Prioridade: **P1** | Camada: API  
- Execução: POST com CPF inválido e telefone fora de 10..13 dígitos  
- Resultado esperado: 400 de validação; verificar uso de @Valid na rota.  

**VOL-06 — Buscar por ID e CPF**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /voluntarios/{id} e /cpf/{cpf}  
- Resultado esperado: 200 correto ou 404 inexistente.  

**VOL-07 — Impedir consulta global de CPF e dados pessoais**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: ACCESS org A busca voluntários sem vínculo com A  
- Resultado esperado: Esperado escopo/autorização; hoje listagens são globais.  

**VOL-08 — Bloquear edição de voluntário de terceiro**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: ACCESS org A envia PUT /voluntarios/{idB}  
- Resultado esperado: Esperado recusar; comportamento atual sem validação de posse.  

**VOL-09 — Desativar somente vínculo da organização**  
- Prioridade: **P0** | Camada: API  
- Execução: PATCH /voluntarios/{id}/desativar com org A  
- Resultado esperado: 204, vínculo A INATIVO, vínculos em B inalterados.  

**VOL-10 — Reativação do vínculo inativo**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Desativar vínculo; tentar POST com CPF existente na mesma organização  
- Resultado esperado: Contrato desejado: reativar; código atual acusa já vinculado.  

**VOL-11 — Vínculo desativado não pode coletar**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Desativar vínculo; tentar associar em requisição APROVADO  
- Resultado esperado: 409; voluntário inativo não pode ser associado.  

**VOL-12 — Listagem de voluntários**  
- Prioridade: **P1** | Camada: API  
- Execução: GET /voluntarios com perfis variados  
- Resultado esperado: Contrato atual lista todos; avaliar paginação e isolamento por organização.  


### Dashboards e impacto alimentar (15 casos)
**DSH-01 — Dashboard público sem autenticação**  
- Prioridade: **P0** | Camada: API  
- Execução: GET /dashboards/publico sem token  
- Resultado esperado: 200, somente agregados.  

**DSH-02 — Contar apenas organizações verificadas**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Criar doadoras/ONGs verificadas, REVISAO e INATIVO  
- Resultado esperado: Contagens públicas ignoram organizações não verificadas.  

**DSH-03 — Dashboard doador exige papel correto**  
- Prioridade: **P0** | Camada: API  
- Execução: GET /dashboards/doador com doador e com ONG  
- Resultado esperado: Doador 200, ONG rejeitada.  

**DSH-04 — Dashboard ONG exige ONG**  
- Prioridade: **P0** | Camada: API  
- Execução: GET /dashboards/ong com ONG e doador  
- Resultado esperado: ONG 200, doador rejeitado.  

**DSH-05 — Dashboard privado isola organização**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Várias doadoras/ONGs, consultas com respectivos ACCESS  
- Resultado esperado: Contadores consideram apenas a organização do token.  

**DSH-06 — Somar separadamente cinco unidades**  
- Prioridade: **P0** | Camada: Unitário  
- Execução: Concluir coletas de KG, LITRO, UNIDADE, CAIXA e LOTE  
- Resultado esperado: Mapas separam unidades, não convertem nem misturam no impacto.  

**DSH-07 — Somar apenas CONCLUIDO**  
- Prioridade: **P0** | Camada: Unitário  
- Execução: Misturar solicitações de cinco estados  
- Resultado esperado: Quantidades de pedidos não concluídos são desconsideradas.  

**DSH-08 — Fechamento manual não duplica coleta**  
- Prioridade: **P0** | Camada: Unitário  
- Execução: Concluir 30 e fechar com quantidade final 80  
- Resultado esperado: Impacto 80 e complemento 50, não 110.  

**DSH-09 — Fechamento sem quantidade não estima volume**  
- Prioridade: **P0** | Camada: Unitário  
- Execução: Concluir doação sem quantidadeColetadaFinal nem pedidos concluídos  
- Resultado esperado: Acréscimo zero ao impacto.  

**DSH-10 — Contar indivíduos/ONGs distintos**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Coletas múltiplas com mesmos voluntário e organização  
- Resultado esperado: Contagens distintas, sem duplicação.  

**DSH-11 — Dados demonstrativos sinalizados**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Usar descrição ou observação com [DEMO ESCOLAR]  
- Resultado esperado: incluiDadosDemonstrativos = true apenas quando compõe impacto.  

**DSH-12 — Retorno vazio cobre cinco unidades**  
- Prioridade: **P1** | Camada: Unitário  
- Execução: Executar calculadora com listas vazias  
- Resultado esperado: Todas as unidades com zero, totais zero.  

**DSH-13 — ONG não herda fechamento manual sem destinatário**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Doação com fechamento 100 e ONG concluiu 25  
- Resultado esperado: ONG vê 25; painel global/doador vê complemento 75.  

**DSH-14 — Não apresentar soma escalar como quilogramas**  
- Prioridade: **P1** | Camada: Contrato  
- Execução: Combinar 10 KG e 20 LITRO nos dashboards  
- Resultado esperado: Usar somente mapa por unidade; campo escalar documentado como legado.  

**DSH-15 — Saldo público desconta reservas aprovadas**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Criar doação 10 com aprovações somando 4  
- Resultado esperado: disponivelPorUnidade contabiliza 6 sem negativos.  


### Segurança, isolamento e robustez (12 casos)
**SEG-01 — Token não pode aparecer em logs**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Ativar fluxo JWT com header Authorization  
- Resultado esperado: Nenhum token integral, Authorization ou credencial aparece nos logs.  

**SEG-02 — Política explícita de RBAC por operação**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Testar cada endpoint com visitante, TEMP, ADMIN, DOADOR, ONG  
- Resultado esperado: Permissões conformes à matriz aprovada, sem permissões implícitas.  

**SEG-03 — Impedir acesso horizontal entre organizações**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Repetir GET/PUT/PATCH/DELETE com ids da org B e token A  
- Resultado esperado: Dados/alterações de B não acessíveis sem autorização.  

**SEG-04 — Impedir elevação de privilégio via DTO**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Enviar role ADMIN e status VERIFICADO em corpos de cadastro/atualização  
- Resultado esperado: Servidor ignora/rejeita campos controlados indevidamente.  

**SEG-05 — JWT assinado com chave adequada**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Usar chaves curtas, inválidas, alteradas e algoritmo inesperado  
- Resultado esperado: Inicialização/validação falha com segurança; sem fallback inseguro.  

**SEG-06 — Revalidação de status atual após suspensão**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Suspender usuário/organização com JWT já emitido  
- Resultado esperado: Permissões revogadas sem aguardar expiração longa.  

**SEG-07 — Não exposição de senha e CPF**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Verificar JSONs, logs de erros e listagens  
- Resultado esperado: Dados minimizados, senha nunca serializada e CPF apenas com autorização.  

**SEG-08 — CORS de origem permitida e não permitida**  
- Prioridade: **P1** | Camada: Segurança  
- Execução: OPTIONS e chamada de navegador localhost:5173 vs origem externa  
- Resultado esperado: Somente origens explicitamente autorizadas; preflight consistente.  

**SEG-09 — Entrada SQL e conteúdo HTML**  
- Prioridade: **P1** | Camada: Segurança  
- Execução: Enviar strings de injeção em e-mail, nome, CPF, descrição  
- Resultado esperado: Sem execução de SQL nem saída perigosa em consumidores.  

**SEG-10 — Limitação de tentativas de login**  
- Prioridade: **P1** | Camada: Segurança  
- Execução: Executar sequência de tentativas falhas da mesma origem/conta  
- Resultado esperado: Política de rate limit/bloqueio operacional, sem negar serviço.  

**SEG-11 — Erros não revelam estruturas internas**  
- Prioridade: **P1** | Camada: Segurança  
- Execução: Testar NullPointer, JSON inválido e cast incorreto  
- Resultado esperado: Resposta padronizada, logs seguros e correlacionáveis.  

**SEG-12 — Sem acesso a dados após exclusão de usuário**  
- Prioridade: **P0** | Camada: Segurança  
- Execução: Excluir usuário e repetir requisição com token anterior  
- Resultado esperado: Token sem efeito e dados pessoais fora da consulta normal.  


### Persistência, integrações e infraestrutura (12 casos)
**INT-01 — JPA com PostgreSQL real isolado**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Subir banco de testes e executar persistência de entidades  
- Resultado esperado: Mapeamentos, constraints, JSONB e transações funcionam.  

**INT-02 — Unicidade de email, CNPJ e CPF**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Inserir registros duplicados concorrentes  
- Resultado esperado: Uma linha por chave natural; erros tratados sem 500.  

**INT-03 — JSONB Endereco round-trip**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Persistir e ler organização, doação e voluntário com endereço  
- Resultado esperado: Campos JSONB recuperados integralmente.  

**INT-04 — JSONB JanelaHorario round-trip**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Persistir doação com duas janelas  
- Resultado esperado: Lista e instantes preservados com fuso/semântica definidos.  

**INT-05 — Atomicidade criar organização e vínculo**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Simular falha ao persistir vínculo após criação da organização  
- Resultado esperado: Não deixar organização órfã por transação parcial.  

**INT-06 — Atomicidade criar voluntário e vínculo**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Simular falha ao salvar vínculo  
- Resultado esperado: Sem voluntário órfão por operação parcial.  

**INT-07 — Rollback da conclusão de requisição**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Simular falha no recálculo de doação  
- Resultado esperado: Estado da requisição e doação consistente após rollback.  

**INT-08 — Migração do enum de usuário**  
- Prioridade: **P0** | Camada: Integração  
- Execução: Verificar persistência da coluna Usuario.status entre versões  
- Resultado esperado: Valores estáveis; documentar estratégia antes de alterar @Enumerated.  

**INT-09 — Build e testes na branch main**  
- Prioridade: **P0** | Camada: CI  
- Execução: Executar ./gradlew clean test e build sob Java 21  
- Resultado esperado: Build executa sem conflitos e reporta testes.  

**INT-10 — Verificar compatibilidade Boot/Kotlin/JDK**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Inicializar contexto e JPA com matriz de versões declarada  
- Resultado esperado: Testes de startup sem falhas na stack do commit.  

**INT-11 — Atualizar coleções Postman obsoletas**  
- Prioridade: **P1** | Camada: Contrato  
- Execução: Importar coleções e executar smoke contra rotas reais  
- Resultado esperado: Sem 404 em /usuarios/criar ou /doacao; rotas revisadas.  

**INT-12 — Tratamento previsível de exceções HTTP**  
- Prioridade: **P1** | Camada: Integração  
- Execução: Executar erros de negócio, validação, not found e JSON incorreto  
- Resultado esperado: Respostas coerentes (400, 404, 409), sem 500 indevido.  


### Requisitos não funcionais e operação (10 casos)
**NFR-01 — Consultar dashboard com volume significativo**  
- Prioridade: **P1** | Camada: Carga  
- Execução: Popular 10 mil doações e requisições; simular clientes  
- Resultado esperado: Latência p95 alvo <= 500ms em ambiente controlado; definir SLO final.  

**NFR-02 — Listar doações e voluntários em massa**  
- Prioridade: **P1** | Camada: Carga  
- Execução: Povoar centenas de milhares de registros  
- Resultado esperado: Paginação/filtros necessários; sem respostas gigantescas ou OOM.  

**NFR-03 — Impedir oversubscription**  
- Prioridade: **P0** | Camada: Concorrência  
- Execução: Simular 50 aprovações de estoque limitado  
- Resultado esperado: Soma APROVADO+CONCLUIDO não ultrapassa estoque.  

**NFR-04 — Monitorar falhas, latência e pool DB**  
- Prioridade: **P1** | Camada: Operação  
- Execução: Exercitar rotas e medir métricas/logs  
- Resultado esperado: Métricas mínimas, rastreio e alertas configurados.  

**NFR-05 — Checar segredos e logs em produção**  
- Prioridade: **P1** | Camada: Operação  
- Execução: Auditar propriedades e logs  
- Resultado esperado: Segredos por variáveis seguras, show-sql/debug desativados em prod.  

**NFR-06 — Backup e restauração do PostgreSQL**  
- Prioridade: **P1** | Camada: Operação  
- Execução: Fazer snapshot e restaurar em ambiente descartável  
- Resultado esperado: Dados e constraints íntegros; RPO/RTO acordados.  

**NFR-07 — Banco indisponível temporariamente**  
- Prioridade: **P1** | Camada: Resiliência  
- Execução: Derrubar conexão em ambiente de teste  
- Resultado esperado: Falha controlada sem corrupção, com recuperação.  

**NFR-08 — Evitar N+1 na listagem de organizações**  
- Prioridade: **P1** | Camada: Performance  
- Execução: Coletar SQL de GET /organizacoes e /admin/organizacoes  
- Resultado esperado: Consultas em volume previsível, sem explosão de joins.  

**NFR-09 — Executar análise estática e cobertura**  
- Prioridade: **P1** | Camada: Qualidade  
- Execução: Rodar detekt/ktlint e JaCoCo após configurados  
- Resultado esperado: Relatórios em CI, baseline e limites aprovados.  

**NFR-10 — Minimizar retenção de dados pessoais**  
- Prioridade: **P1** | Camada: LGPD  
- Execução: Revisar CPF, e-mail, telefone, endereço, exclusão  
- Resultado esperado: Bases legais, perfis de acesso e política de retenção documentados.  

## 6. Partições, valores-limite e tabelas de decisão
### 6.1 Quantidade e disponibilidade
| Variável | Classes/limites | Regra esperada |
|---|---|---|
| Doacao.quantidade | -0.01, 0, 0.01, 1, 10, muito grande | Somente >0 e com unidade válida, inclusive na edição. |
| Requisicao.quantidadeSolicitada | 0, 0.09, 0.1, saldo exato, saldo+0.01 | Bean Validation ≥0.1 e quantidade ≤ saldo; consistente entre camadas. |
| FecharDoacao.quantidadeColetada | -0.01, 0, quantidade exata, quantidade+0.01 | 0 ≤ coletado ≤ ofertado; fechar apenas em estados permitidos. |
| JanelaHorario | fim antes, igual e depois do início | fim > início; política para sobreposições precisa ser definida. |
| Token TEMP | expiração 5 minutos, antes/no/depois do limite | Acesso bloqueado no vencimento e fora do conjunto de rotas TEMP. |
| Vínculo organizacional | inexistente, REVISAO, VERIFICADO, INATIVO | Somente vínculo verificado deve produzir acesso operacional. |

### 6.2 Matriz das principais decisões de requisição
| Ação | Papel | Parte correta | Status prévio | Voluntário | Resultado esperado |
|---|---|---|---|---|---|
| Criar | ONG | ONG ≠ doadora | Doação disponível | n/a | PENDENTE se saldo suficiente |
| Aprovar | doador | Doador da doação | PENDENTE | n/a | APROVADO se saldo persistir |
| Rejeitar | doador | Doador da doação | PENDENTE | n/a | REJEITADO |
| Cancelar | ONG | Solicitante | PENDENTE | n/a | CANCELADO |
| Vincular | ONG | Solicitante | APROVADO | ATIVO na ONG | Voluntário associado |
| Concluir | doador | Doador da doação | APROVADO | Associado | CONCLUIDO + recálculo de doação |
| Concluir | doador | Doador da doação | APROVADO | Ausente | Recusa, sem alterar persistência |

## 9. Plano de execução em ondas
1. Onda 0 — Verificar build; atualizar Postman/OpenAPI, provisionar Testcontainers/fixtures, sanear dados e remover logs sensíveis. Gate: CI verde e ambiente reproduzível.
2. Onda 1 — Segurança P0: autenticação, JWT, matriz RBAC, isolamento entre organizações, proteção PII e revogação. Gate: nenhuma falha S1/S2.
3. Onda 2 — Fluxos funcionais P0: cadastros, verificações, doações, requisições, voluntários e fechamentos. Gate: regras persistentes e status coerentes.
4. Onda 3 — Concorrência e transações: aprovações simultâneas, duplicidades, rollback e constraints no PostgreSQL. Gate: nenhuma reserva acima do estoque.
5. Onda 4 — Indicadores, contrato, limites e desempenho: impacto unitário, cinco unidades, cargas, N+1 e LGPD. Gate: critérios NFR acordados.
6. Onda 5 — Regressão E2E e aceite: Newman smoke, cenários reais com dados sintéticos, relatório de cobertura e aceite técnico.

## 10. Backlog de correções priorizado
**Imediato (antes de homologação pública):** R01, R02, R03, R04, R05, R07, R14, R18. **Próxima iteração:** R06, R08, R09, R10, R11, R13, R20. **Higiene técnica/escala:** R12, R15, R16, R17, R19.
**Entregáveis esperados por sprint:** teste automatizado reproduzindo defeito; correção no service/controller/DTO; teste de regressão; atualização UML/contrato; revisão de segurança; aprovação em PR.

## 11. Registro de evidências e rastreabilidade
Para cada caso registrar: ID, commit, ambiente, fixture, data, executor, input JSON, status HTTP, assertivas, logs sanitizados, resultado PASS/FAIL/BLOCKED, número da issue/PR e evidência anexada. Os resultados atuais são todos **NÃO EXECUTADO**.
**Código-fonte:** todo arquivo citado está disponível em `src/main/kotlin/com/br/foodbridge` do commit de referência. Testes existentes em `src/test/kotlin/com/br/foodbridge`; coleções em `postman/collections`.
**Links estáveis:**
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/build.gradle.kts
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/main/kotlin/com/br/foodbridge/service/DoacaoService.kt
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/main/kotlin/com/br/foodbridge/service/RequisicaoDoacaoService.kt
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/main/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculator.kt
- https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/blob/611674973467cf859d5925c8306b66df249b7be3/src/test/kotlin/com/br/foodbridge/service/ImpactoAlimentarCalculatorTest.kt

## 12. Modelo sugerido de reporte de ciclo
**Ciclo:** identificação / commit / ambiente / janela. **Cobertura:** P0 planejados, executados, aprovados, falhos, bloqueados. **Defeitos:** distribuição por severidade, idade, responsável e links de PR. **Go/no-go:** decisão colegiada baseada nos critérios de saída. **Observação:** não confundir planejamento com resultados de execução.

## Estado de execução (registro separado do planejamento)

- **Snapshot de 29/10/2026:** 165 casos especificados, sem execução indicada na fonte.
- **11/11/2026:** CI baseado em Java 21, Gradle e PostgreSQL 16 efêmero aprovado em PR e `main`, evidenciando compilação e testes automatizados existentes na base. Não corresponde a 165 casos funcionais/E2E.
- **Ainda sem evidência consolidada:** controle horizontal entre organizações, segurança de CPF, revogação de JWT, concorrência no estoque, E2E completos, desempenho e restauração de backup.

## Gestão de execução

Registrar para cada ID: data, commit testado, ambiente, versão documental, fixture sintética, executor, saída observada, evidência sanitizada, PASS/FAIL/BLOCKED e issue/PR correlatos. A [matriz de rastreabilidade](MATRIZ_RASTREABILIDADE.md) vincula o endpoint aos casos.

## Contrato adicional de CI/CD Oracle (FB-OCI-TST v1.2.0 proposta)

A automação de publicação tem **matriz específica OCI-01..OCI-16** para validação de scripts, postgreSQL descartável, SSH pinado, known_hosts, service opc, ExecStart do JAR, arquivo de configuração externo, ddl-auto validate/none, preflight sem alterações, rollback e deploy manual autorizado. Consultar [plano próprio](CI_CD_ORACLE_TESTES.md). A cobertura de CI não inclui execuções na VM; **OCI-05..OCI-15 requerem evidência externa e não podem ser marcados PASS pela suite unitária**.
