# Requisitos — FoodBridge API

## Funcionais observados
- RF-01 Autenticar usuários e emitir credenciais compatíveis com os fluxos configurados.
- RF-02 Diferenciar tokens temporários de tokens de acesso.
- RF-03 Gerenciar organizações e perfis com verificações de permissão.
- RF-04 Expor operações do domínio FoodBridge somente para papéis autorizados.

## Não funcionais
- RNF-01 Não registrar token JWT, header Authorization ou dados sensíveis.
- RNF-02 Usar PostgreSQL e configuração por ambiente.
- RNF-03 Separar controlador, regra de domínio e repositório.
- RNF-04 Testar autorização em rotas públicas, protegidas e administrativas.

## Estado
O escopo exato de todos os endpoints deve ser confirmado nos controllers. A lista é uma orientação inicial, não substitui testes de integração.
