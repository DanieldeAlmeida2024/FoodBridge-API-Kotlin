# Diagnóstico de acesso externo — FoodBridge/Nginx e AnimeDex scraper

Registro FB-NET-002 · 13/11/2026 · inspeção somente leitura.

## Infraestrutura identificada

- VM Oracle Linux 9: `163.176.133.210`, SSH `opc`. A conexão GitHub Actions → SSH já foi homologada: `SSH_RSA_OK`, `SSH_EXIT=0`, `PREFLIGHT ORACLE: PASS`, job 131848870328 no [run #43355411573](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573).
- Nginx FoodBridge atende HTTP público porta TCP 80; backend Spring Boot atende em loopback `127.0.0.1:8080`.
- Outro projeto hospedado na mesma VM: [AnimeDex](https://github.com/DanieldeAlmeida2024/AnimeDex), worker Docker `animedex-scraper`, historicamente publicação `0.0.0.0:8787`, `/health`.
- Supabase AnimeDex project `ievgjejyyhirohyoubdu`, Edge Function `animedex` ativa. Seu código utiliza `BROWSER_SCRAPER_URL` ou `http://163.176.133.210:8787` como fallback. **Não imprimir o valor real de um secret do Supabase.**

## Evidência efetiva — 13/11/2026 08:46 BRT

O diagnóstico [Actions #43419709855](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43419709855), job 131866343010, terminou **SUCCESS**. Tanto da VM (loopback) quanto do runner hospedado externo:

| Serviço | Loopback VM | Runner GitHub externo |
| --- | --- | --- |
| Nginx em `:80/` | HTTP 200 | HTTP 200 |
| AnimeDex `:8787/health` | HTTP 200 | HTTP 200 |

Estados observados: `sshd`, `firewalld`, `nginx`, `docker` e `foodbridge.service` **active**; Nginx `nginx -t` **PASS**. Listeners TCP `0.0.0.0:22`, `0.0.0.0:80`, `0.0.0.0:8787` e `*:8080`; **nenhum listener `:443`**. Container `animedex-scraper` **running**, porta publicada `0.0.0.0:8787->8787/tcp`.

Na zona `public` do `firewalld`, `ssh` e `http` aparecem permitidos por serviço, `8787/tcp` permitido por porta, `https` e `443/tcp` não aparecem permitidos por regra direta; uma zona Docker também está ativa (suas regras não substituem a zona pública). Essas leituras não cobrem todas as políticas possíveis, mas há prova de HTTP funcional para 80/8787 de fora.

**Conclusão estritamente limitada à origem de teste:** não há bloqueio geral da VPC/VM para TCP 80 ou 8787; as duas rotas HTTP responderam à origem do runner. O relato de falha pode envolver outro cliente/origem, DNS, browser HTTPS-first, URL HTTPS em `:443` sem listener, ou endpoint específico da aplicação. A chamada efetiva *Supabase Edge Function → worker da VM* não foi executada por esse diagnóstico. A Edge Function `animedex` está ativa no Supabase, mas isso não equivale a sucesso ponta a ponta. Os registros de saúde do Supabase exibiram respostas 521 transitórias seguidas de 200; essas verificações internas não provam indisponibilidade do worker.

Risco adicional: Spring Boot escuta em `*:8080`, em vez de aparecer apenas no loopback. Não há evidência de exposição pública da porta 8080 porque a inspeção externa não a testou. Manter 8080/5432 fechadas na OCI/host; não publicar APIs internas além do necessário.

## Sintoma

O operador relatou perda de acesso externo tanto ao Nginx quanto ao scraper após alterações nos filtros de rede OCI para habilitar SSH do GitHub. O SSH `22/tcp` funciona, porém o preflight SSH verificou a API somente via loopback, não comprovando disponibilidade externa em `80` ou `8787`.

Possíveis causas incluem: (1) segurança de ingresso da VCN/NSG sem TCP 80 e/ou 8787; (2) `firewalld` zona/regra sem permissão; (3) serviço Nginx/Docker parado, bind em loopback ou erro de configuração; (4) container AnimeDex indisponível. **Sem diagnóstico direto, nenhuma delas está provada.**

## Diagnóstico versionado via runner GitHub

O workflow `.github/workflows/oracle-network-diagnostics.yml` é **somente leitura** e dispara em `workflow_dispatch` ou alteração da própria definição na `main`. Reutiliza environment `oracle-production` já homologado, chave RSA pinada e validação estrita da host key. O runner testa externamente `http://163.176.133.210/` e `http://163.176.133.210:8787/health`; por SSH, examina listeners de portas `22/80/443/8080/8787`, estados dos serviços, Nginx, containers Docker relacionados a AnimeDex, `firewalld` e os mesmos endpoints via `127.0.0.1`. Não instala software, não altera firewall, rede, serviço, banco ou arquivos do site.

**Interpretação rápida:**

| Local 80 | Externo 80 | Interpretação |
| --- | --- | --- |
| HTTP responde | Timeout/refused | Checar ingress VCN/NSG e `firewalld`; não alterar o Nginx sem evidência |
| Timeout/refused | Timeout/refused | Checar Nginx status, listener e configuração |
| HTTP responde | HTTP responde | Verificar domínio, DNS, HTTPS ou aplicação/navegador em vez da conectividade IP:80 |

Aplicar a mesma comparação à porta 8787. Se local responde e externo não, a prioridade é VCN/NSG/firewalld. Respostas HTTP 403/404/500 provam conexão TCP, mas apontam falhas de rota/configuração/aplicação específicas, não ausência de rede.

**Observação:** `firewall-cmd --query-service`/`--query-port` não inclui automaticamente todos os rich rules, políticas e regras de Docker/nftables; `ALLOW` em uma regra local não prova liberação na OCI, e `NOT_ALLOW_BY_PORT` não prova bloqueio se houver serviço HTTP, rich rule ou regra em outra política.

## Regras OCI a conferir, sem mudar cegamente

Na subnet da VM, examinar todas as security lists e os NSGs anexados à VNIC. Para **site HTTP público**: ingress TCP destino 80, origem apropriada; para HTTPS se configurado: TCP 443. Para o scraper, TCP 8787: preferir acesso restrito/HTTPS autenticado se possível, pois um endpoint Puppeteer publicamente acessível aumenta superfície de ataque. **Não expor** `8080` (Spring Boot interno) nem `5432` (PostgreSQL) à internet. Se ingress da VCN está ausente, a OCI descarta pacotes antes de chegarem ao Nginx ou Docker.

Referências: [OCI Security Lists](https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securitylists.htm), [OCI Security Rules](https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm), [AnimeDex scraper](https://github.com/DanieldeAlmeida2024/AnimeDex).
