# Política de dados de voluntários — FoodBridge API Kotlin

> **ID:** FB-PRIV-VOL-001 · **Versão:** 2.0.0 (proposta) · **Data:** 2026-11-11 · **Código:** branch `fix/p0-voluntarios-isolamento-20261111` dependente do PR #4.

## Finalidade e dados tratados

O voluntário pode colaborar com a coleta de alimentos. O cadastro atual armazena CPF, nome, e-mail, telefone e endereço na entidade global `Voluntario`, associada a organizações por `VoluntarioOrganizacao`. Esses dados exigem **minimização, finalidade definida, controle de acesso e rastreabilidade**. Esta versão é uma mitigação técnica e não constitui parecer de conformidade LGPD.

## Matriz de acesso implementada na branch

| Operação | Operador | Recurso | Decisão |
|---|---|---|---|
| POST novo | Usuário com vínculo vigente VERIFICADO | CPF/e-mail ainda não cadastrados | Criar voluntário e vínculo ATIVO na mesma transação |
| POST CPF/e-mail existente | Mesmo operador | Cadastro global já existente | Negar 409 sem revelar ficha ou estabelecer vínculo entre tenants |
| GET ID ou CPF | Usuário com vínculo vigente VERIFICADO | Voluntário com vínculo ATIVO ao seu tenant | Permitir |
| GET ID ou CPF | Mesmo operador | Voluntário sem vínculo ATIVO no tenant | 404 com mensagem neutra |
| GET lista | Mesmo operador | Vínculos ATIVOS do próprio tenant | Listar somente vinculados |
| PUT | Mesmo operador | Voluntário vinculado exclusivamente ao tenant | Permitir edição |
| PUT | Mesmo operador | Cadastro compartilhado com outra organização | Negar 403 até autorização do titular |
| PATCH desativar | Mesmo operador | Vínculo próprio ATIVO | Marcar vínculo do tenant como INATIVO |
| Coleta | ONG da requisição | Voluntário com vínculo ATIVO na organização solicitante | Consulta interna escopada ao tenant da requisição |

Em todas as operações HTTP, `TokenData` deve referenciar vínculo que ainda exista em `UsuarioOrganizacaoRepository` com ID, role, usuário, organização e status coerentes/VERIFICADO. **Não confiar apenas na assinatura do JWT.**

## Decisões que ainda precisam de implementação

1. **Consentimento/vinculação interorganizacional:** fluxo verificável do titular, sem reutilização por mero conhecimento do CPF; reativação deve passar por validação e auditoria.
2. **Modelo de papéis:** separar função da organização (ONG/DOADOR etc.) da função do membro (GESTOR, OPERADOR, LEITOR). Hoje qualquer membro verificado da organização recebe o mesmo acesso à ficha sensível.
3. **Minimização de resposta:** não retornar CPF e endereço completos quando um identificador ou informação parcial bastar; revisar `VoluntarioDTO` e APIs consumidoras.
4. **Proteção de identificação:** não usar CPF em URL se possível; revisar logs/proxies, retenção, tratamentos de incidentes e segurança de observabilidade.
5. **Edição de cadastro compartilhado:** separar atributos canônicos do titular dos atributos específicos do vínculo organizacional ou exigir mudança aprovada pelo titular.
6. **Validações e auditoria:** CPF/e-mail únicos (normalização), documentos sintéticos em testes, transações e tratamento de duplicidade concorrente sem revelar existência a atores não legitimados.
7. **Segurança do fluxo de coleta:** teste E2E com duas ONGs, voluntários suspensos, requisições de outra ONG e dados históricos.

## Regressões e gates

- Unitários: [VoluntarioServiceAutorizacaoTest.kt](../../src/test/kotlin/com/br/foodbridge/service/VoluntarioServiceAutorizacaoTest.kt).
- Critérios VOL-02/06/07/08/09/10/12 em [plano](../testes/PLANO_DE_TESTES.md) e [matriz](../testes/MATRIZ_RASTREABILIDADE.md).
- UML: [sequência 18](../uml/18_sequencia_isolamento_voluntarios.puml).
- Não encerrar risco R04 até testes de integração HTTP/PostgreSQL, revisão RBAC e minimização de dados pessoais.
- Não há alteração de produção Oracle; usar sempre fixtures inventadas e autorizadas.

## Dependências de implementação

Esta proposta depende do PR #4 (autorização de organização, erro HTTP 403). Para integração, revisar/mesclar #4 com gates aprovados, rebasear esta branch na main atual e executar novamente todos os testes antes de aceitar a alteração.
