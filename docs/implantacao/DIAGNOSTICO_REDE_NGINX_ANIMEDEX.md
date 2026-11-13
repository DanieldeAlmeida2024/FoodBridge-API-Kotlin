# Diagnóstico de acesso externo — FoodBridge/Nginx e AnimeDex scraper

Registro FB-NET-002 · 13/11/2026 · inspeção somente leitura.

## Infraestrutura identificada

- VM Oracle Linux 9: `163.176.133.210`, SSH `opc`. A conexão GitHub Actions → SSH já foi homologada: `SSH_RSA_OK`, `SSH_EXIT=0`, `PREFLIGHT ORACLE: PASS`, job 131848870328 no [run #43355411573](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573).
- Nginx FoodBridge atende HTTP público porta TCP 80; backend Spring Boot atende em loopback `127.0.0.1:8080`.
- Outro projeto hospedado na mesma VM: [AnimeDex](https://github.com/DanieldeAlmeida2024/AnimeDex), worker Docker `animedex-scraper`, historicamente publicação `0.0.0.0:8787`, `/health`.
- Supabase AnimeDex project `ievgjejyyhirohyoubdu`, Edge Function `animedex` ativa. Seu código utiliza `BROWSER_SCRAPER_URL` ou `http://163.176.133.210:8787` como fallback. **Não imprimir o valor real de um secret do Supabase.**

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
