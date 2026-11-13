# FoodBridge — SSH do GitHub Actions: timeout de rede no runner

**FB-OCI-NET-001 · 12/11/2026 BRT / 13/11 UTC · evidência real, sem deploy.**

## Evidências do primeiro run remoto

- [Run #43355411573](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573) do workflow independente Oracle SSH/preflight foi disparado manualmente na `main` após cadastro dos secrets e variables no environment `oracle-production`.
- O operador verificou no Windows a chave RSA dedicada sem passphrase e `SSH_EXIT=0` a partir de seu próprio PC; os fingerprints das host keys registrados localmente coincidiram com as chaves públicas da VM consultadas por SSH autenticado.
- No runner GitHub: checkout e passo de validação da chave privada/fingerprint RSA foram **SUCCESS**. A tentativa de conexão à `163.176.133.210:22` retornou `Connection timed out` em ~10 segundos; `SSH_EXIT=255` e job **FAILURE**.
- O timeout ocorreu antes de qualquer autenticação SSH remota ou execução de `deploy/oracle/verificar-vm.sh`; **não há preflight da VM aprovado**. O serviço e PostgreSQL de produção não foram acessados por essa execução.
- `OCI_DEPLOY_ENABLED=false` e `OCI_AUTO_DEPLOY_ENABLED=false` informados pelo script local; nenhuma publicação foi realizada.

## Hipóteses e diagnóstico sem mudanças

O runner GitHub e a estação Windows possuem origens de rede diferentes. Um timeout do TCP porta 22 aponta a rede/filtragem/rota como candidata principal, **não identifica a camada exata**. Investigar, sem alterar regras ainda: VCN security list/NSG e suas origens permitidas, firewall local firewalld/nftables, fail2ban/defesas de reputação, disponibilidade pública da VM e endereço de destino efetivo.

Para verificar componentes locais da VM, a partir do **CMD Windows**, usando somente a chave RSA de CI aprovada (não copiar nem publicar a chave):

```bat
ssh -n -i "%USERPROFILE%\.ssh\foodbridge-ci-rsa-test" -o BatchMode=yes -o IdentitiesOnly=yes -o IdentityAgent=none -o StrictHostKeyChecking=yes opc@163.176.133.210 "printf 'TAILSCALE_INSTALLED='; if command -v tailscale >/dev/null 2>&1; then echo yes; else echo no; fi; printf 'TAILSCALED='; systemctl is-active tailscaled 2>/dev/null || true; printf 'SSHD='; systemctl is-active sshd 2>/dev/null || true; printf 'FIREWALLD='; systemctl is-active firewalld 2>/dev/null || true"
```

Esse comando não executa `sudo`, não imprime conteúdo de configuração e não muda a VM. O resultado identifica se a alternativa de rede privada Tailscale já está presente, **não comprova** qual firewall causou timeout.

## Decisão proposta: rede privada para o runner

Evitar abrir a porta SSH globalmente e evitar allowlisting de todos os ranges de runners GitHub comuns: [GitHub recomenda não fazê-lo](https://docs.github.com/en/actions/reference/runners/github-hosted-runners). Opções para análise:

1. **Preferida, condicionada a configuração de tailnet e identidade:** Tailscale na VM e `tailscale/github-action@v4` no runner GitHub, autenticado por OIDC federado/credencial de workload quando disponível, nó efêmero `tag:ci`, regras de tailnet permitindo **apenas TCP 22 da identidade do CI para a VM**, destino SSH usando IP privado/MagicDNS, e `known_hosts` pinado para o destino de rede privada. Não configurar até confirmar tailnet e controles.
2. Runner autogerenciado em host **isolado de produção**, ligado à rede privada da VM, com escopo mínimo no repo e jobs protegidos, evitando execução de código de PR não confiável nesse runner.
3. Runner maior com endereços públicos fixos e allowlist estrita, somente se o plano e custo permitirem.

**Não** adicionar regra ingress `0.0.0.0/0:22`; não usar `StrictHostKeyChecking=no`, não trocar chave privada válida ou desativar SELinux/FIPS. Tailscale também não deve ganhar acesso ao PostgreSQL ou a outros serviços além da necessidade declarada.

## Gates antes de chamar preflight de PASS

- Verificar conectividade SSH privada a partir de runner hospedado e fingerprint da host key;
- obter `SSH_RSA_OK` e `SSH_EXIT=0` no próprio GitHub Actions;
- executar `deploy/oracle/verificar-vm.sh` e exigir a última linha `PREFLIGHT ORACLE: PASS`;
- manter publicação de JAR, restart, alteração de banco e auto-deploy fora do escopo.

Referências: [issue #8](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/issues/8), [diagnóstico Windows](../DIAGNOSTICO_SSH_WINDOWS.md), [Tailscale GitHub Action oficial](https://tailscale.com/docs/integrations/github/github-action), [Oracle SSH troubleshooting](https://docs.oracle.com/en-us/iaas/Content/Compute/Tasks/troubleshooting-ssh-connection.htm).
