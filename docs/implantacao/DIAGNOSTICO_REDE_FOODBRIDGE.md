# Diagnóstico de rede e serviços do FoodBridge na Oracle

**FB-NET-003 · Revisão 13/11/2026 · escopo exclusivo FoodBridge**

Este documento cobre **somente** os componentes que atendem o FoodBridge na VM Oracle. A máquina é compartilhada, mas recursos de aplicações não relacionadas não integram a arquitetura, a operação ou o inventário de serviços do FoodBridge.

## Componentes e portas do FoodBridge

| Componente | Execução | Porta/uso | Evidência |
|---|---|---|---|
| Frontend React/Vite | Arquivos `dist` servidos por Nginx | HTTP TCP 80; frontend público | [Deploy web #43465805656](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656): HTTP local e externo 200 |
| API Kotlin/Spring Boot | Java 21 gerenciado por `foodbridge.service` | HTTP TCP 8080; verificações da API por loopback | [Deploy API #43465999269](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269): saúde/integração aprovadas |
| Banco de dados FoodBridge | PostgreSQL da aplicação | TCP 5432 somente para os clientes autorizados | Preflights confirmaram disponibilidade; **não publicar na internet** |
| Administração de deploy | GitHub Actions → SSH `opc` com host key verificada | TCP 22 | [Preflight SSH #43355411573](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573): autenticação e comandos remotos aprovados |
| Arquivos e configurações | `/opt/foodbridge/`, `/etc/foodbridge/`, systemd e Nginx | Não têm porta própria | Backup e validação automatizados pelos scripts do projeto |

A VM opera com Oracle Linux 9 x86_64, shape `VM.Standard.E2.1.Micro`; após remoção planejada da reserva do Kdump, o Linux passou a reconhecer aproximadamente 945 MiB de RAM. Esse limite é do **host físico compartilhado**, não de um serviço que o FoodBridge deva gerenciar.

## Diagnóstico restrito a aplicações FoodBridge

As verificações de rede e saúde pertinentes são:

- **Frontend:** `curl -I http://127.0.0.1/` dentro da VM e requisição à raiz pública da aplicação. A implantação exige HTTP 200 local e registra o código externo.
- **API:** `GET http://127.0.0.1:8080/health/live` (liveness leve) e `GET /dashboards/publico` (consulta funcional ao PostgreSQL).
- **PostgreSQL:** `pg_isready` com parâmetros adequados, sem exibir credenciais nem realizar alterações no banco.
- **systemd/Nginx:** `systemctl is-active foodbridge.service`, `systemctl is-active nginx` e `nginx -t` como auditorias não destrutivas.
- **SSH:** autenticação por chave exclusiva do GitHub Actions, `BatchMode=yes`, verificação estrita da chave pública do host; nenhuma senha no log.
- **Host:** memória `free -h`, uso de swap e sinais de OOM para avaliar a margem disponível ao Java e PostgreSQL.

**Importante:** a ausência de erros no endpoint `/health/live` não comprova todas as jornadas de doação, solicitação ou coleta. A sondagem pública do dashboard valida uma integração específica, não a disponibilidade de todas as funções.

## Interpretação das falhas

| Sinal | Possível área de investigação |
|---|---|
| SSH indisponível | Configuração de acesso à OCI, porta 22, `sshd` e host key |
| Nginx local HTTP 403 | Permissões dos diretórios/arquivos `dist`, política SELinux ou configuração do `root` |
| Nginx local HTTP 200, público não responde | Ingress VCN/NSG, `firewalld`, DNS, proxy, uso de HTTPS sem configuração |
| API 8080 recusando conexões | `foodbridge.service`, tempo de inicialização Java, configuração externa e memória |
| API liveness 200, dashboard falha | Conexão/consultas PostgreSQL, autorização ou erro funcional da aplicação |
| Banco indisponível | Estado do PostgreSQL e suas permissões; nunca recriar banco automaticamente como etapa de diagnóstico |

Não há homologação de TLS/HTTPS 443 nesta entrega; não afirmar que há certificado ativo sem evidência. Para a API e o banco, evitar acesso público direto às portas internas. O acesso público deve ser definido na configuração de Nginx/infraestrutura específica do FoodBridge, com segurança revisada.

## Linha do tempo operacional útil ao FoodBridge

- [Auditoria inicial de rede #43419709855](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43419709855): confirmou conectividade ao Nginx via HTTP 80. O workflow histórico possui outros checks, mas **somente os relativos ao FoodBridge** fundamentam este documento.
- [Readiness de publicação #43424042926](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43424042926): confirmou JAR, configurações systemd, Nginx, permissões e ferramentas usadas no deploy.
- [Deploy frontend #43465805656](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/actions/runs/43465805656): build e publicação via SSH aprovados, HTTP 200 local e externo.
- [Deploy API #43465999269](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43465999269): build/testes com PostgreSQL descartável, cópia do JAR, restart e verificação de saúde aprovados.

Os scripts operacionais podem consultar métricas gerais do host; esse comportamento não autoriza incorporar outras aplicações como dependências ou recursos do FoodBridge. Este documento **não é inventário da VM inteira**.

## Referências de manutenção

- [Arquitetura FoodBridge](../ARQUITETURA_ATUAL.md)
- [CI/CD automático API](OCI_MICRO_AUTOMATIC_CICD.md)
- [Incidente e recuperação de memória](INCIDENTE_DEPLOY_API_20261113.md)
- [CI/CD automático do frontend](https://github.com/DanieldeAlmeida2024/Foodbridge-Frontend-React/blob/main/docs/OCI_AUTOMATIC_CICD.md)
