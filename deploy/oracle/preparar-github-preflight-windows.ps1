# Provisiona exclusivamente o preflight Oracle no environment GitHub (sem deploy).
# Requer: Windows PowerShell 5.1, GitHub CLI autenticado, host previamente fixado no known_hosts.
# Nao imprime nem grava a chave privada em logs ou no repositorio.
[CmdletBinding()]
param(
    [string]$Repo = 'DanieldeAlmeida2024/FoodBridge-API-Kotlin',
    [string]$EnvironmentName = 'oracle-production',
    [string]$SshHost = '163.176.133.210',
    [string]$SshUser = 'opc',
    [string]$KeyPath = (Join-Path $HOME '.ssh\foodbridge-ci-rsa-test'),
    [switch]$RunPreflight
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Assert-LastExit {
    param([string]$Step)
    if ($LASTEXITCODE -ne 0) {
        throw ("Falha em: {0}. Exit={1}" -f $Step, $LASTEXITCODE)
    }
}

if (-not (Get-Command gh.exe -ErrorAction SilentlyContinue)) {
    throw 'GitHub CLI (gh.exe) nao foi encontrado. Instale-o/autorize-o antes de continuar.'
}
if (-not (Get-Command ssh.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command ssh-keygen.exe -ErrorAction SilentlyContinue)) {
    throw 'Ferramentas OpenSSH nao disponiveis.'
}
$key = (Resolve-Path -LiteralPath $KeyPath).ProviderPath
$khPath = Join-Path $HOME '.ssh\known_hosts'
if (-not (Test-Path -LiteralPath $khPath)) {
    throw 'known_hosts inexistente. Verifique a identidade do servidor por canal confiavel antes de continuar.'
}
$diagnostic = Join-Path $PSScriptRoot 'diagnosticar-ssh-windows.ps1'
if (-not (Test-Path -LiteralPath $diagnostic)) {
    throw 'Script de diagnostico local nao encontrado.'
}

Write-Host '=== VALIDANDO CHAVE E SSH LOCAL (SEM ALTERAR VM) ===' -ForegroundColor Cyan
& powershell.exe -NoProfile -File $diagnostic -KeyPath $key -SshHost $SshHost -SshUser $SshUser -TestSsh
Assert-LastExit 'diagnostico RSA e SSH local'

Write-Host '=== CONFERINDO HOST KEY FIXADA ===' -ForegroundColor Cyan
# ssh-keygen -F tambem funciona se o arquivo local usa nomes de host com hash.
$entriesRaw = @(& ssh-keygen.exe -F $SshHost -f $khPath)
Assert-LastExit 'consulta local known_hosts'
$entries = @($entriesRaw | Where-Object {
    $_ -match '^\S+\s+(ssh-ed25519|ssh-rsa|ecdsa-sha2-\S+)\s+\S+'
})
if ($entries.Count -lt 1) {
    throw 'Nenhuma entrada valida para a VM no known_hosts existente.'
}

$tempFile = [IO.Path]::GetTempFileName()
try {
    [IO.File]::WriteAllLines($tempFile, [string[]]$entries, [Text.Encoding]::ASCII)
    $localResults = @(& ssh-keygen.exe -lf $tempFile -E sha256)
    Assert-LastExit 'fingerprint host key local'
    $sshOpts = @(
        '-n', '-i', $key,
        '-o', 'BatchMode=yes',
        '-o', 'IdentityAgent=none',
        '-o', 'IdentitiesOnly=yes',
        '-o', 'StrictHostKeyChecking=yes',
        '-o', 'ConnectTimeout=10'
    )
    # Mostra somente impressões digitais de host keys PUBLICAS da VM ja autenticada.
    $remoteCommand = 'for f in /etc/ssh/ssh_host_ed25519_key.pub /etc/ssh/ssh_host_rsa_key.pub /etc/ssh/ssh_host_ecdsa_key.pub; do if test -r "$f"; then ssh-keygen -lf "$f" -E sha256; fi; done'
    $remoteResults = @(& ssh.exe @sshOpts "$SshUser@$SshHost" $remoteCommand)
    Assert-LastExit 'consulta host keys publicas na VM'
    $pattern = 'SHA256:[A-Za-z0-9+/=]+'
    $localFingerprints = @([regex]::Matches(($localResults -join [Environment]::NewLine), $pattern) |
        ForEach-Object { $_.Value })
    $remoteFingerprints = @([regex]::Matches(($remoteResults -join [Environment]::NewLine), $pattern) |
        ForEach-Object { $_.Value })
    $sharedFingerprints = @($localFingerprints | Where-Object { $remoteFingerprints -contains $_ } | Select-Object -Unique)
    if ($sharedFingerprints.Count -lt 1) {
        throw 'Host keys fixadas nao coincidem com as chaves publicas reportadas pelo servidor autenticado.'
    }
    Write-Host ("HOST_KEY_MATCH={0}" -f ($sharedFingerprints -join ','))
} finally {
    Remove-Item -LiteralPath $tempFile -Force -ErrorAction SilentlyContinue
}

Write-Host '=== CONFERINDO GH E ENVIRONMENT ===' -ForegroundColor Cyan
& gh.exe auth status
Assert-LastExit 'gh auth status'
$environmentFound = @(& gh.exe api "repos/$Repo/environments/$EnvironmentName" --jq '.name')
Assert-LastExit 'consulta do environment oracle-production'
if ($environmentFound -notcontains $EnvironmentName) {
    throw 'Environment inesperado. Interrompendo antes de enviar secrets.'
}

Write-Host '=== GARANTINDO DEPLOY DESATIVADO ===' -ForegroundColor Cyan
& gh.exe variable set OCI_DEPLOY_ENABLED --body 'false' --repo $Repo
Assert-LastExit 'desativar deploy no repositorio'
& gh.exe variable set OCI_AUTO_DEPLOY_ENABLED --body 'false' --repo $Repo
Assert-LastExit 'desativar auto deploy no repositorio'
& gh.exe variable set OCI_DEPLOY_ENABLED --body 'false' --env $EnvironmentName --repo $Repo
Assert-LastExit 'desativar deploy no environment'
& gh.exe variable set OCI_AUTO_DEPLOY_ENABLED --body 'false' --env $EnvironmentName --repo $Repo
Assert-LastExit 'desativar auto deploy no environment'

Write-Host '=== CONFIGURANDO VARIAVEIS E SECRETS NO ENVIRONMENT ===' -ForegroundColor Cyan
& gh.exe variable set OCI_SSH_HOST --body $SshHost --env $EnvironmentName --repo $Repo
Assert-LastExit 'OCI_SSH_HOST'
& gh.exe variable set OCI_SSH_USER --body $SshUser --env $EnvironmentName --repo $Repo
Assert-LastExit 'OCI_SSH_USER'

# GH CLI le os secrets do stdin, criptografa localmente e nao os mostra em argumentos.
Get-Content -LiteralPath $key -Raw -Encoding ASCII |
    & gh.exe secret set OCI_SSH_PRIVATE_KEY --env $EnvironmentName --repo $Repo
Assert-LastExit 'OCI_SSH_PRIVATE_KEY'
($entries -join [Environment]::NewLine) |
    & gh.exe secret set OCI_SSH_KNOWN_HOSTS --env $EnvironmentName --repo $Repo
Assert-LastExit 'OCI_SSH_KNOWN_HOSTS'

Write-Host 'ENVIRONMENT_READY=YES' -ForegroundColor Green
Write-Host 'DEPLOY_ENABLED=false' -ForegroundColor Green
Write-Host 'AUTO_DEPLOY_ENABLED=false' -ForegroundColor Green
Write-Host 'Nenhuma chave privada foi exibida. Nenhuma modificacao na VM foi realizada.'

if ($RunPreflight) {
    Write-Host '=== DISPARANDO SOMENTE O PREFLIGHT REMOTO DA MAIN ===' -ForegroundColor Cyan
    & gh.exe workflow run oracle-preflight.yml --ref main --repo $Repo
    Assert-LastExit 'disparo de oracle-preflight.yml'
    & gh.exe run list --workflow oracle-preflight.yml --limit 3 --repo $Repo
    Assert-LastExit 'consulta de runs'
}
