# Indicadores de aproveitamento de alimentos

Os endpoints `GET /dashboards/publico`, `GET /dashboards/doador` e
`GET /dashboards/ong` incluem o objeto `impacto`.

- `quantidadesPorUnidade`: totais independentes em `KG`, `LITRO`, `UNIDADE`,
  `CAIXA` e `LOTE`. Todas as unidades são retornadas, inclusive quando zeradas.
- `coletasConcluidas`: solicitações em `CONCLUIDO`.
- `doacoesAproveitadas`: doações distintas com alguma quantidade confirmada.
- `organizacoesBeneficiadas` e `voluntariosParticipantes`: participantes distintos
  nas coletas concluídas, sem somar novamente quem participou de várias coletas.
- `categoriasDeAlimentos` e `porCategoria`: categorias e totais, preservando a
  unidade de cada alimento.
- `fechamentosManuais`: doações cujo total final acrescentou uma quantidade ainda
  não contabilizada pelas solicitações concluídas.
- `incluiDadosDemonstrativos`: identifica resultados com registros marcados
  como `[DEMO ESCOLAR ...]`, para que a interface apresente o contexto acadêmico.

Pedidos pendentes, aprovados, rejeitados e cancelados não comprovam coleta e não
entram no volume aproveitado. Coletas parciais concluídas entram pelo volume
efetivamente confirmado, mesmo que a oferta ainda tenha saldo disponível.

Para doadores e para o total público, um fechamento manual com
`quantidadeColetadaFinal` acrescenta apenas a diferença positiva em relação às
coletas concluídas da mesma doação. O valor final nunca é somado novamente como
se fosse uma nova coleta. Uma doação apenas marcada como `COMPLETO`, sem
solicitações concluídas nem quantidade final confirmada, não gera volume estimado.

O painel da ONG considera somente suas solicitações concluídas. Quantidades
confirmadas por fechamento manual sem destinatário não são atribuídas a uma ONG.
Os painéis privados usam a organização do token de acesso; não aceitam um ID
arbitrário enviado pelo navegador. O painel do admin e a landing page usam o
consolidado público, que expõe apenas indicadores agregados.

`disponivelPorUnidade` também foi acrescentado ao painel público para consultar
saldo por unidade. Os antigos campos escalares `quantidadeDisponivel`,
`quantidadeDoada` e `quantidadeRecebida` são mantidos por compatibilidade e não
devem ser usados como medida em kg, pois somam unidades diferentes. A nova
interface utiliza exclusivamente os totais separados por unidade.

As contagens públicas de organizações doadoras e ONGs ativas consideram somente
organizações verificadas; o impacto histórico continua contando coletas concluídas.

Validação do cálculo:

```sh
./gradlew test --tests com.br.foodbridge.service.ImpactoAlimentarCalculatorTest
```
