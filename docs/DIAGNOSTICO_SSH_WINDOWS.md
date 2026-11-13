# Diagnóstico da chave RSA dedicada — Windows PowerShell 5.1

**Trilha:** FB-OCI-SSH-002 · 12/11/2026 · somente diagnóstico, sem deploy.

## Estado inicial comprovado

- Acesso SSH pessoal à Oracle funcionou anteriormente, mas a chave RSA dedicada para GitHub Actions falhou com exit 255.
- A fingerprint pública RSA-4096 aprovada é SHA256:3zV6jlZeYgt19xz4h0KrumznEr3SNlyAEiolj+XrmO8; já constou no authorized_keys da VM.
- A cópia de teste %USERPROFILE%\.ssh\foodbridge-ci-rsa-test continuou pedindo passphrase após ssh-keygen -p, mesmo com ACL restrita.
- O workflow independente oracle-preflight.yml na main falhou no run 43339087021 **antes** da autenticação: configuração SSH ausente no environment; não houve acesso remoto nem deploy.
- O CI do PR #12 terminou com sucesso no run 43340862650, mas não valida SSH, VM ou produção.

## Diagnóstico inicial — não modifica arquivos, banco ou serviço

Na raiz do checkout desta branch, executar **no próprio Windows**:

    powershell.exe -NoProfile -File .\deploy\oracle\diagnosticar-ssh-windows.ps1

O script lê apenas os primeiros campos do envelope openssh-key-v1, sem imprimir os bytes da chave. Exibe SSH_CIPHER, SSH_KDF e fingerprint pública, conferindo-a com a fingerprint aprovada.

| Resultado | Interpretação | Ação |
| --- | --- | --- |
| SSH_CIPHER=none e SSH_KDF=none | Chave privada não criptografada | Prosseguir ao teste SSH |
| Cifra como aes256-ctr e KDF bcrypt | Chave ainda protegida por passphrase | Corrigir **somente a cópia de teste** |
| Fingerprint inesperada (exit 22) | Chave diferente | Não usar nem cadastrar no GitHub |
| Formato não OpenSSH | Diagnóstico específico não aplicável | Inspecionar formato sem exportar segredo |

**Importante:** SSH_KEY_UNENCRYPTED=YES não comprova login remoto.

## Somente se a cópia continuar criptografada

No **Windows PowerShell 5.1**, o encaminhamento de argumentos vazios a executáveis nativos pode ser problemático. O operador --% interrompe o parser do PowerShell para o comando nativo; o caminho deve ser literal:

    ssh-keygen.exe --% -p -N "" -f C:\Users\danie\.ssh\foodbridge-ci-rsa-test

Digitar a passphrase **antiga apenas no prompt interativo**; a opção -N com string vazia solicita nova passphrase vazia. Nunca passar a passphrase antiga por -P nem gravar senhas em comandos, logs, issue ou chat. O comando opera somente sobre a cópia de teste: **não modifica a chave pessoal** C:\foodbridge\ssh-oracle.key nem a chave dedicada original sem nova avaliação.

Repetir o diagnóstico inicial. Só prosseguir se SSH_CIPHER=none, SSH_KDF=none e a fingerprint permanecer idêntica. A mensagem de sucesso do ssh-keygen -p isoladamente **não comprova** remoção da passphrase.

## Gate de autenticação real

Após metadados none/none, conferir previamente que a host key do servidor foi fixada no known_hosts por fingerprint de fonte confiável. Executar:

    powershell.exe -NoProfile -File .\deploy\oracle\diagnosticar-ssh-windows.ps1 -TestSsh

Resultado exigido, **ao mesmo tempo**:

    SSH_RSA_OK
    SSH_EXIT=0

O teste usa BatchMode=yes, IdentityAgent=none, IdentitiesOnly=yes, PreferredAuthentications=publickey, StrictHostKeyChecking=yes e timeout. Em caso de erro, não alterar sshd, FIPS, SELinux, authorized_keys ou produção como tentativa genérica; investigar primeiro a causa local, stderr e known_hosts.

## Gate posterior: Actions sem publicação

Somente após SSH_RSA_OK e exit 0:

1. Conferir environment oracle-production e configurar OCI_SSH_HOST, OCI_SSH_USER, OCI_SSH_PRIVATE_KEY e OCI_SSH_KNOWN_HOSTS em seus respectivos campos (vars/secrets), sem registrar valores sigilosos.
2. Manter OCI_DEPLOY_ENABLED=false e OCI_AUTO_DEPLOY_ENABLED=false.
3. Disparar manualmente o workflow independente **Oracle - SSH RSA e preflight somente leitura** na main, ou o workflow CI com preflight=true, deploy=false.
4. Confirmar logs de SSH_RSA_OK, SSH_EXIT=0, verificar-vm.sh e resultado real. Não considerar CI de PR como prova.
5. Nenhuma transferência de JAR, reinício de serviço, alteração de SQL ou substituição de configuração está autorizada por este documento.

## Evidência e limites

### Evidência local recebida em 12/11/2026

O operador clonou a branch em C:\foodbridge-ssh-check e executou o diagnóstico real no Windows:

- Primeiro resultado: `SSH_CIPHER=aes256-ctr`, `SSH_KDF=bcrypt`, fingerprint RSA esperada, `DIAGNOSTICO_EXIT=21`. Isso confirmou que a cópia permanecia criptografada; nenhum SSH foi tentado nesse estágio.
- Após executar `ssh-keygen.exe --% -p -N "" -f C:\Users\danie\.ssh\foodbridge-ci-rsa-test` e informar a passphrase antiga no prompt: `SSH_CIPHER=none`, `SSH_KDF=none`, fingerprint inalterada e `SSH_KEY_UNENCRYPTED=YES`.
- A execução local com `-TestSsh` retornou **`SSH_EXIT=0`**, com marcador visual `SSH_RSA_OKn` (escape de linha interpretado pelo PowerShell 5.1); isso comprova autenticação do teste remoto, mas o marcador não saiu formatado como planejado.
- O marcador do script foi ajustado para `echo SSH_RSA_OK` no commit posterior; **esse ajuste cosmético ainda não foi retestado no desktop**.
- Nenhum deploy, modificação do banco original, JAR ou reinício de `foodbridge.service` foi realizado.

**Gate SSH local: PASS (exit 0). Gate GitHub Actions remoto: pendente.**

### Próximo controle: environment do GitHub Actions

1. Confirmar `oracle-production`, preferencialmente protegido para execuções aprovadas na `main`.
2. Definir vars de environment `OCI_SSH_HOST=163.176.133.210` e `OCI_SSH_USER=opc`.
3. Definir `OCI_DEPLOY_ENABLED=false` e `OCI_AUTO_DEPLOY_ENABLED=false` como vars do repositório. A condição de habilitação do job de deploy deve continuar falsa.
4. Conferir no servidor a fingerprint da chave pública de host por sessão SSH já autenticada, e só então fixar uma entrada correspondente de `known_hosts` para o IP. Não tratar `ssh-keyscan` isolado como verificação.
5. Cadastrar a cópia privada RSA sem passphrase como secret de environment `OCI_SSH_PRIVATE_KEY`, e a entrada de host validada como `OCI_SSH_KNOWN_HOSTS`. Não enviar nenhum conteúdo sensível para Git, issue, console ou chat.
6. Disparar exclusivamente `Oracle - SSH RSA e preflight somente leitura` via `workflow_dispatch` na `main`. Exigir confirmação dos passos de fingerprint da RSA, `SSH_RSA_OK`, `SSH_EXIT=0` e `verificar-vm.sh` PASS. Falhas de ambiente/configuração não provam falha de SSH.
7. Nenhum passo de publicação de JAR, restart, acesso SQL destrutivo ou alteração da VM faz parte desse gate.

Em cada execução, anotar somente estado cipher/kdf, fingerprint pública, resultado do SSH e código de saída (sem chaves, senhas nem conteúdo de arquivos).

Referências: [issue #8](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/issues/8), [run CI #43340862650](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43340862650), [run Oracle #43339087021](https://github.com/DanieldeAlmeida2024/FoodBridge-API-Kotlin/actions/runs/43339087021), [segurança CI/CD](implantacao/CI_CD_ORACLE_SEGURANCA.md).
