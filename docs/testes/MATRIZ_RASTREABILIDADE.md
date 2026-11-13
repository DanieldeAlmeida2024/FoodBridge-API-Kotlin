# FoodBridge — matriz de rastreabilidade

> **FoodBridge / Rastreabilidade** · Documento **FB-TRC-001** · Versão **1.1.0** · Revisão **2026-11-11**
> **Código atual de referência:** `main` @ `2d0b63e416bc9f5bb994b6364160bb78f8399372` (antes deste PR)
> **Documento histórico auditado:** `611674973467cf859d5925c8306b66df249b7be3` (29/10/2026).
> **Atenção:** o levantamento histórico descreve um snapshot; requisitos de aceitação não provam comportamento atual. Ver [controle de versões](../controle/VERSIONAMENTO.md).


> Transcrição integral da matriz fornecida para o snapshot de 29/10/2026. Ela possui **49 endpoints** e **6 cenários E2E**; revisar a correspondência de rotas quando o código mudar.


Base: main @ 611674973467cf859d5925c8306b66df249b7be3. Os IDs remetem ao capítulo 5 do plano de testes.

|Módulo|Endpoint|Testes prioritários|
|---|---|---|
|ADM|`GET /admin/organizacoes`|`ADM-01,ADM-09`|
|ADM|`GET /admin/organizacoes/pendentes`|`ADM-01,ADM-09`|
|ADM|`PATCH /admin/organizacoes/{id}/aprovar`|`ADM-02,ADM-03,ADM-10`|
|ADM|`PATCH /admin/organizacoes/{id}/reprovar`|`ADM-04,ADM-10`|
|ADM|`GET /admin/usuarios`|`ADM-05,ADM-09`|
|ADM|`GET /admin/usuarios/pendentes`|`ADM-05,ADM-09`|
|ADM|`PATCH /admin/usuarios/{id}/aprovar`|`ADM-06,ADM-07,ADM-10`|
|ADM|`PATCH /admin/usuarios/{id}/reprovar`|`ADM-08,ADM-10`|
|AUT|`POST /auth/login`|`AUT-01,AUT-02,AUT-03,AUT-04,AUT-05,AUT-16`|
|AUT|`POST /auth/select-org`|`AUT-06,AUT-07,AUT-08,AUT-09,AUT-10,AUT-11`|
|DSH|`GET /dashboards/publico`|`DSH-01,DSH-02,DSH-06,DSH-15`|
|DSH|`GET /dashboards/doador`|`DSH-03,DSH-05,DSH-06,DSH-08`|
|DSH|`GET /dashboards/ong`|`DSH-04,DSH-05,DSH-13`|
|DOA|`POST /doacoes`|`DOA-01,DOA-02,DOA-03,DOA-04,DOA-05,DOA-06,DOA-20`|
|DOA|`GET /doacoes/{id}`|`DOA-09,SEG-03`|
|DOA|`GET /doacoes/disponiveis`|`DOA-08,REQ-24`|
|DOA|`GET /doacoes`|`DOA-07,SEG-03`|
|DOA|`PUT /doacoes/{id}`|`DOA-10,DOA-11,DOA-12,DOA-20`|
|DOA|`PATCH /doacoes/{id}/fechar`|`DOA-13,DOA-14,DOA-15,DOA-16`|
|DOA|`DELETE /doacoes/{id}`|`DOA-11,DOA-18,DOA-19`|
|ORG|`POST /organizacoes`|`ORG-01,ORG-02,ORG-03,ORG-04,ORG-05,ORG-06,ORG-07`|
|ORG|`GET /organizacoes/cnpj/{cnpj}`|`ORG-08`|
|ORG|`GET /organizacoes/{id}`|`ORG-09,ORG-17`|
|ORG|`GET /organizacoes`|`ORG-17,ADM-01`|
|ORG|`GET /organizacoes/me`|`ORG-09`|
|ORG|`PUT /organizacoes/{id}`|`ORG-10,ORG-11,SEG-03`|
|ORG|`DELETE /organizacoes/{id}`|`ORG-12,ORG-13`|
|ORG|`PATCH /organizacoes/usuarios/{vinculoId}/aprovar`|`ORG-14,SEG-02`|
|ORG|`PATCH /organizacoes/usuarios/{vinculoId}/reprovar`|`ORG-15,SEG-02`|
|REQ|`POST /requisicoes`|`REQ-01,REQ-02,REQ-03,REQ-04,REQ-05,REQ-06,REQ-07,REQ-26`|
|REQ|`GET /requisicoes/minhas`|`REQ-08,SEG-03`|
|REQ|`GET /requisicoes/recebidas`|`REQ-09,SEG-03`|
|REQ|`GET /requisicoes/{id}`|`REQ-10,SEG-03`|
|REQ|`PATCH /requisicoes/{id}/vincular-voluntario`|`REQ-19,REQ-20`|
|REQ|`PATCH /requisicoes/{id}/aprovar`|`REQ-11,REQ-12,REQ-13,REQ-14,REQ-15`|
|REQ|`PATCH /requisicoes/{id}/rejeitar`|`REQ-16,REQ-18`|
|REQ|`PATCH /requisicoes/{id}/cancelar`|`REQ-17,REQ-18`|
|REQ|`PATCH /requisicoes/{id}/concluir`|`REQ-21,REQ-22,REQ-23,REQ-27`|
|USR|`POST /usuarios`|`USR-01,USR-02,USR-03,USR-04,USR-11`|
|USR|`GET /usuarios/me`|`USR-05,USR-12`|
|USR|`GET /usuarios/me/organizacoes`|`USR-06,SEG-03`|
|USR|`PUT /usuarios/me`|`USR-07,USR-08,USR-09,USR-11`|
|USR|`DELETE /usuarios/me`|`USR-10,SEG-12`|
|VOL|`POST /voluntarios`|`VOL-01,VOL-02,VOL-03,VOL-04,VOL-05,VOL-10`|
|VOL|`GET /voluntarios/{id}`|`VOL-06,VOL-07`|
|VOL|`GET /voluntarios/cpf/{cpf}`|`VOL-06,VOL-07`|
|VOL|`GET /voluntarios`|`VOL-07,VOL-12`|
|VOL|`PUT /voluntarios/{id}`|`VOL-08,VOL-05`|
|VOL|`PATCH /voluntarios/{id}/desativar`|`VOL-09,VOL-11`|

## Cenários transversais end-to-end

- **E2E-01 Onboarding:** cadastrar usuário → login TEMP → cadastrar organização → admin aprovar usuário e organização → login TEMP → selecionar organização → obter ACCESS → GET /usuarios/me.
- **E2E-02 Ciclo da doação:** DOADOR publica 10 KG → ONG A solicita 4 KG → DOADOR aprova → ONG vincula voluntário ATIVO → DOADOR conclui → dashboards refletem somente 4 KG coletados.
- **E2E-03 Multi-organização:** usuário vinculado a duas organizações → alternar contexto via novo login/seleção → consultar recursos de cada uma → tentativa IDOR deve falhar.
- **E2E-04 Fechamento manual:** doação 100 KG, coleta concluída 30 KG → fechar com total confirmado 80 KG → dashboard público/doador contabiliza 80 KG; ONG vê somente 30 KG.
- **E2E-05 Disputa por estoque:** duas ONGs solicitam 7 KG cada de uma doação 10 KG → aprovações simultâneas → apenas uma aprovada; outra recusada sem inconsistência.
- **E2E-06 Auditoria pós-desativação:** revogar vínculo, usuário e organização → tokens antigos não podem alterar dados e operações auditáveis deixam trilha sem PII desnecessária.

## Notas de engenharia

Os cenários E2E-05 e E2E-06 são critérios pretendidos que podem falhar na implementação inspecionada. Não constituem afirmação de funcionamento atual.
Os casos de uso, diagramas e estados são modelagem do código e/ou comportamento de aceitação marcado; qualquer alteração em regra exige atualização desta matriz e dos testes de regressão.

## Integração funcional FB-INT-001 — 13/11/2026

Branch de integração baseada em main `32e13fdd8c2b3c9fd4784ccc7e77fdd2df462224`.
Consolida o código dos PRs #4 (organizações), #5 (voluntários), #6 (estoque),
#9 (timeout de 20 s) e #10 (validação no clone), preservando suas suítes.
O conflito funcional da coleta foi resolvido com `findByIdForColeta`, sem liberar acesso global a voluntários.

Correções adicionais: revalidação de usuário/vínculo/organização em cada JWT autenticado;
bloqueio de contas suspensas inclusive ADMIN; vínculo real na resposta de seleção;
validação do enum de organização; transações de onboarding/admin/exclusão;
criação concorrente de requisições sob o mesmo lock da doação;
recusa de quantidades não finitas e doações expiradas; voluntário ativo novamente conferido na conclusão;
JWT inválido → 401 e método HTTP incorreto → 405, sem revelar credenciais.

`FluxoFuncionalHttpIntegrationTest` acrescenta sete testes HTTP/JWT/PostgreSQL:
login/seleção, vínculo revogado, ADMIN suspenso, cadastro organizacional sem autoaprovação,
ciclo doação → solicitação → aprovação → voluntário → coleta → indicadores,
método incorreto e JWT malformado. **Novos testes pendentes da execução do CI nesta revisão.**

Esta integração não comprova os 165 cenários planejados, o navegador real,
a prontidão dos papéis de gestor, o SSH do runner, deploy ou rollback.
A entrega total exige fechar a matriz [ENTREGA_FUNCIONAL](../projeto/ENTREGA_FUNCIONAL.md)
com evidência por módulo. Produção, serviço, Nginx e banco original não foram alterados.
