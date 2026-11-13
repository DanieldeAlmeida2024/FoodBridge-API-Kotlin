# Comprovação de SSH RSA para o CI/CD Oracle

Registro operacional de 13/11/2026; material de chave privada **não é** versionado.

A chave RSA-4096 dedicada aprovada tem fingerprint pública `SHA256:3zV6jlZeYgt19xz4h0KrumznEr3SNlyAEiolj+XrmO8`.

## Diagnóstico Windows

A chave cópia `%USERPROFILE%\.ssh\foodbridge-ci-rsa-test` inicialmente usava `SSH_CIPHER=aes256-ctr` e `SSH_KDF=bcrypt`. A remoção da passphrase ocorreu **apenas nessa cópia** usando OpenSSH/PowerShell 5.1. Depois o script `deploy/oracle/diagnosticar-ssh-windows.ps1` confirmou `SSH_CIPHER=none`, `SSH_KDF=none`, fingerprint inalterada, `SSH_KEY_UNENCRYPTED=YES`, `SSH_RSA_OK`, `SSH_EXIT=0`.

O script `deploy/oracle/preparar-github-preflight-windows.ps1` foi executado no Windows: confirmou três fingerprints das host keys de Oracle via SSH autenticado, configurou environment `oracle-production`, variáveis e secrets do **repositório API**, com `OCI_DEPLOY_ENABLED=false` e `OCI_AUTO_DEPLOY_ENABLED=false`. Não imprimiu o conteúdo das chaves nem alterou PostgreSQL/VM.

## Evidência GitHub Actions

Workflow de leitura `oracle-preflight.yml`, [run #43355411573](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43355411573), última tentativa, job 131848870328:

```text
SSH_RSA_OK
SSH_EXIT=0
OK: systemd service e usuário esperados
OK: configuração externa existente e legível (sem exibir valores)
OK: JPA ddl-auto explicitamente não mutante
OK: JAR corresponde ao comando de inicialização efetivo
Dashboard público respondeu em 7.684649s
OK: serviço atual saudável via loopback
PREFLIGHT ORACLE: PASS (somente leitura, nenhum deploy executado)
```

O timeout TCP observado nos runs anteriores foi superado após alteração de filtro de rede informada pelo operador. Hoje a porta SSH está acessível do runner. A VPC ficou com ingress `0.0.0.0/0:22` por decisão do operador; nenhuma mudança de firewall é feita pelos workflows. Monitorar tentativas SSH, manter autenticação somente por chave, host key pinada e sudo limitado.

**O preflight comprova a conexão e o estado pré-deploy, não o deploy real.** Para habilitação manual e automática, consultar [CI_CD_ORACLE_SSH_DIRETO.md](CI_CD_ORACLE_SSH_DIRETO.md). O frontend é repositório independente e precisa de secrets equivalentes próprios. Tailscale e agentes de pull **não fazem parte da configuração selecionada**.