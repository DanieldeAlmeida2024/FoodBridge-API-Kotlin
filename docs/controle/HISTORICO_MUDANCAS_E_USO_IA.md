# Histórico de mudanças da aplicação FoodBridge e uso de inteligência artificial

Data deste registro: 23/11/2026  
Período observado: 10 a 13/11/2026  
Projeto: FoodBridge API Kotlin

## 1. Situação antes das mudanças

O FoodBridge já tinha as funções principais da API: cadastro de usuários e organizações, login, doações, solicitações, voluntários e indicadores. Era uma aplicação funcional, mas ainda havia problemas para revisar.

Na análise do código antigo apareceram riscos como mensagens de log com informações do token, permissões que precisavam de conferência entre organizações, acesso a dados de voluntários, possibilidade de duas solicitações usarem a mesma quantidade em estoque e validações incompletas. Também havia partes do código e dos documentos que poderiam ficar mais organizadas.

O levantamento de outubro descrevia 49 rotas e 20 riscos possíveis. Esses riscos eram apontamentos de análise do código daquela época, e não a confirmação de que todos estavam presentes ou continuaram presentes depois. Os 165 testes do plano eram cenários previstos, não 165 execuções comprovadas.

## 2. Mudanças registradas por data

As datas abaixo seguem os registros dos PRs e commits no GitHub (horários registrados em UTC). Não são horários de conversas com IA.

### 10/11/2026 — início da revisão de segurança e organização

- Foi aberto o [PR #1](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/1) para revisar o filtro JWT, retirar logs que poderiam expor tokens e corrigir o tratamento das rotas públicas.
- Também foram preparados testes do filtro e uma primeira estrutura de CI para compilar e testar a API.
- A proposta de publicação na Oracle incluía verificação de chave SSH, backup do JAR, teste de saúde e retorno à versão anterior em caso de problema. Isso ainda não significava publicação realizada.

### 11/11/2026 — documentação, permissões e estoque

- O [PR #1](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/1) foi integrado, com mudanças no JWT e no CI.
- O [PR #3](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/3) organizou especificação, riscos, testes, rastreabilidade, diagramas UML e controle de versões.
- Começaram as revisões de autorização entre organizações ([PR #4](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/4)) e concorrência de estoque ([PR #6](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/6)).
- Foram estudadas situações em que um usuário poderia tentar alterar dados de outra organização ou em que duas ONGs poderiam solicitar a mesma quantidade de alimentos.

### 12/11/2026 — verificações da API e do ambiente Oracle

- O [PR #7](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/7) acrescentou verificações antes de publicar a aplicação, incluindo SSH, serviço Java e cuidados com a configuração do banco.
- O [PR #9](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/9) trabalhou o problema de demora na resposta do dashboard, que podia ser confundida com falha da aplicação.
- O [PR #10](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/10) tratou da validação do esquema usando um banco de teste restaurado, sem usar o banco produtivo como ambiente de experimentação.
- A documentação e os testes de CI passaram a separar melhor o que podia ser confirmado automaticamente e o que ainda dependia de verificação na VM.

### 13/11/2026 — integração, testes, implantação e documentação

- O [PR #11](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/11) reuniu correções de autorização, proteção de dados de voluntários, transações e controle concorrente do estoque. Também acrescentou testes HTTP com PostgreSQL para autenticação, vínculos e o caminho de doação até a coleta.
- A proposta de proteção dos voluntários do [PR #5](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/5) foi incorporada por meio da integração do PR #11. O PR #5, porém, ainda aparece aberto como rascunho e não deve ser integrado novamente sem comparar as mudanças.
- Os [PRs #22 a #27](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pulls?q=is%3Apr+is%3Aclosed) registraram diagnóstico de falha, acompanhamento de memória, cuidados com rollback e testes operacionais. Essas mudanças foram sendo revistas conforme os resultados observados.
- O [PR #28](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/28) passou a publicar a API automaticamente após o CI de mudanças de código na `main`, com `GET /health/live`, backup e rollback. A execução [#43465999269](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269) foi registrada como bem-sucedida.
- O [PR #29](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/29) ampliou o inventário, a referência atual das 50 rotas, a arquitetura e o material de estudo.
- O [PR #30](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/30) ajustou os documentos de infraestrutura para mencionar somente os serviços relacionados ao FoodBridge.

Observação: o [PR #12](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/pull/12) trata de melhorias no cadastro e na edição de perfil, mas continua aberto como rascunho nesta revisão. Por isso ele é uma pendência, não uma entrega da `main`.

## 3. Problemas analisados e três cenários para cada caso

O objetivo desta parte é mostrar de forma simples o que poderia acontecer e que tipo de solução poderia ser estudada.

### 3.1 Login e uso dos tokens

Problema: informações de autenticação aparecendo em logs e regras de acesso precisando de revisão.

1. **Acesso público:** uma pessoa consulta a página pública sem estar logada. A API deve responder sem exigir um token.
2. **Token incorreto:** alguém manda um token inválido ou expirado. A API deve negar o acesso sem divulgar detalhes do token.
3. **Token válido, conta sem permissão:** a assinatura está correta, mas o usuário ou vínculo foi bloqueado. A API precisa conferir a situação atual no banco.

Possíveis soluções: limpar logs, separar rotas públicas de protegidas, verificar o tipo de token e testar a revogação de acesso. Arquivos relacionados: `JwtFilter.kt`, `SecurityConfig.kt`, `ContextoAcessoService.kt` e testes de autenticação.

### 3.2 Permissões das organizações

Problema: um usuário não pode modificar uma organização diferente daquela para a qual tem autorização.

1. **Mesma organização:** o usuário tem vínculo ativo e faz uma operação permitida.
2. **Outra organização:** o usuário tenta usar o ID de uma organização que não é a dele.
3. **Vínculo revogado:** o usuário ainda possui um token antigo, mas já perdeu a permissão.

Possíveis soluções: conferir o ID e a situação do vínculo no service, validar papéis e retornar um erro adequado sem alterar dados de outra organização. Relacionado ao PR #4 e à integração #11.

### 3.3 Voluntários e dados pessoais

Problema: CPF, endereço e outros dados de voluntários não podem ficar disponíveis para qualquer organização.

1. **Consulta autorizada:** uma ONG consulta seu voluntário com vínculo ativo.
2. **Consulta de outra ONG:** uma organização tenta acessar o cadastro de outra, por ID ou CPF.
3. **CPF já registrado:** alguém tenta cadastrar uma pessoa existente sem autorização para vinculá-la.

Possíveis soluções: restringir listagens por organização, conferir os vínculos e evitar revelar dados no caso de tentativa não autorizada. Também é importante estudar a quantidade de informações devolvida pela API. Relacionado aos PRs #5 e #11.

### 3.4 Quantidade disponível para doação

Problema: duas aprovações muito próximas podem usar uma quantidade de alimentos que já estava reservada.

1. **Quantidade suficiente:** a soma das solicitações aprovadas é menor que o estoque.
2. **Quantidade no limite:** as solicitações chegam exatamente ao total disponível.
3. **Quantidade acima do estoque:** duas solicitações simultâneas ultrapassariam o total.

Possíveis soluções: realizar a aprovação dentro de uma transação, bloquear o registro durante a conferência do saldo e testar duas operações concorrentes. Relacionado aos PRs #6 e #11.

### 3.5 Organização do código e validação

Problema: alguns campos, respostas e verificações ficavam confusos ou repetidos, aumentando a chance de erros.

1. **Dados completos:** todos os campos informados são válidos.
2. **Dados faltando:** campos que não foram enviados não deveriam receber valores inesperados.
3. **Dados inválidos:** o sistema recebe um valor vazio, repetido ou com formato incorreto.

Possíveis soluções: separar a validação dos dados das regras do service, utilizar DTOs adequados e evitar repetir conferências desnecessárias. A correção mais específica de atualização parcial do perfil ainda está proposta no PR #12.

### 3.6 Testes da aplicação

Problema: o funcionamento manual de uma tela ou rota não demonstra que outros cenários também estão corretos.

1. **Teste esperado:** uma operação válida termina com sucesso.
2. **Teste de erro:** dados ou permissões inválidos são rejeitados.
3. **Teste de regressão:** um erro corrigido é testado novamente para evitar que volte.

Possíveis soluções: criar testes unitários para regras menores, testes HTTP com JWT e PostgreSQL para os fluxos e registrar no plano quais cenários realmente foram executados. A integração #11 e os testes de CI são referências.

### 3.7 Banco, publicação e recuperação de falhas

Problema: uma mudança pode funcionar localmente e falhar ao iniciar na VM ou atingir o banco errado.

1. **Configuração correta:** o sistema inicia, encontra o banco esperado e responde às verificações.
2. **Configuração ou ambiente incompleto:** falta uma propriedade, o serviço não inicia ou a consulta fica lenta.
3. **Nova versão com falha:** o JAR é publicado, mas a aplicação não fica saudável.

Possíveis soluções: testar em PostgreSQL descartável, verificar configuração sem mostrar senhas, separar liveness de consultas ao banco, guardar o JAR anterior e conferir a recuperação no rollback. Relacionado aos PRs #7, #9, #10 e #20 a #28.

### 3.8 Documentação da aplicação

Problema: a documentação pode ficar diferente do código ou apresentar como concluído algo que só foi planejado.

1. **Recurso implementado:** existe no código e o documento informa onde encontrá-lo.
2. **Recurso testado:** há um teste executado e um link para o resultado.
3. **Recurso planejado:** existe como proposta, mas não deve aparecer como entregue.

Possíveis soluções: manter a lista de rotas e o inventário atualizados, registrar PRs e resultados reais dos testes e revisar diagramas quando a estrutura mudar. Relacionado aos PRs #3, #29 e #30.

## 4. Registro do uso de inteligência artificial

A inteligência artificial pode ajudar a localizar problemas, explicar partes do código, comparar opções, propor casos de teste e organizar documentos. Mesmo assim, é preciso conferir as respostas com os arquivos reais e com os testes.

**Sobre os prompts:** não foram encontradas transcrições completas e verificáveis das perguntas feitas à IA nos dias abaixo. Os textos a seguir são **prompts reconstituídos em 23/11/2026 com base nos assuntos trabalhados em cada data**. Eles mostram exemplos de perguntas compatíveis com as mudanças, mas não comprovam que estas palavras foram usadas na ocasião.

### 10/11/2026 — prompt reconstituído: segurança do login

> Estou revisando a parte de login da minha API Kotlin com Spring Boot. Ela funciona, mas quero entender se tem algum problema no filtro JWT e nos logs. Pode olhar as regras de segurança e me explicar o que está errado, por que isso seria um risco e quais soluções eu poderia considerar? Analise três situações: uma rota pública sem token, uma requisição com token inválido e uma conta que perdeu o acesso. Fale também quais testes seriam úteis e o que precisaria ser registrado na documentação.

### 11/11/2026 — prompt reconstituído: permissões e doações

> Quero verificar se as permissões de organização e as quantidades das doações estão corretas no FoodBridge. O sistema já faz essas operações, mas pode ter problemas quando alguém tenta acessar dados de outra organização ou quando chegam duas solicitações juntas. Compare três casos: uma operação normal, uma tentativa sem permissão e duas aprovações que disputam o mesmo estoque. Descreva os problemas, possíveis formas de corrigir e quais testes ajudariam a conferir. Também quero saber como deixar essas regras mais claras nos documentos.

### 12/11/2026 — prompt reconstituído: revisão do código e testes de implantação

> Pode analisar os arquivos de configuração e os scripts da minha API antes da publicação na Oracle? Quero entender o que pode causar erro no início da aplicação e se as verificações estão organizadas. Considere três situações: a API inicia normalmente, a consulta de saúde demora e uma configuração do banco está incorreta. Explique as possíveis causas, como conferir cada caso sem alterar o banco de produção, quais testes seriam necessários e o que devo descrever no histórico das mudanças.

### 13/11/2026 — prompt reconstituído: revisão final e documentação

> Fizemos várias mudanças no FoodBridge e quero conferir se o código, os testes e os documentos estão combinando. Analise o fluxo de login, organizações, voluntários, doações e coleta, além das regras básicas de segurança e da organização dos arquivos. Use três situações como exemplo: um fluxo que funciona, uma operação que deveria ser negada e um erro que pode voltar depois de uma mudança. Me mostre o que merece revisão, alternativas de melhoria, testes que poderiam comprovar cada parte e quais documentos precisam ser atualizados. Separe também o que foi feito do que ainda está pendente.

## 5. O que foi usado como referência

- Código e histórico de PRs do [FoodBridge API Kotlin](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin).
- [Especificação do projeto](../projeto/ESPECIFICACAO_PROJETO.md) e [registro de riscos](../projeto/RISCOS.md).
- [Plano de testes](../testes/PLANO_DE_TESTES.md) e [matriz de rastreabilidade](../testes/MATRIZ_RASTREABILIDADE.md).
- [Referência atual das rotas](../REFERENCIA_API_ATUAL.md) e [mapa funcional](../RECURSOS_E_STATUS.md).
- [Controle de versões](VERSIONAMENTO.md) e [execução de CI/CD da API](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269).

## 6. Situação em 23/11/2026

A branch `main` consultada estava no commit `1113c3fe9f11897163c828fe38f053b395f27d55`. Foram identificadas melhorias de segurança, testes e implantação já incorporadas, mas ainda existem itens que precisam ser verificados, como a revisão de perfil, proteção adicional de dados pessoais, HTTPS e testes completos no navegador.

Este arquivo é um resumo para acompanhar a evolução do projeto. Ele não substitui os testes nem o histórico técnico do GitHub.
