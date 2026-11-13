# Diagnostico nao destrutivo da chave RSA usada no GitHub Actions.
# Windows PowerShell 5.1+; nao imprime conteudo da chave privada ou publica.
[CmdletBinding()]
param(
    [string]$KeyPath = (Join-Path $HOME '.ssh\foodbridge-ci-rsa-test'),
    [string]$ExpectedFingerprint = 'SHA256:3zV6jlZeYgt19xz4h0KrumznEr3SNlyAEiolj+XrmO8',
    [string]$SshHost = '163.176.133.210',
    [string]$SshUser = 'opc',
    [switch]$TestSsh
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Read-SshMetadataString {
    param(
        [byte[]]$Buffer,
        [ref]$Offset
    )
    if (($Buffer.Length - $Offset.Value) -lt 4) {
        throw 'Cabecalho OpenSSH truncado antes do campo de tamanho.'
    }
    $length = 0
    for ($i = 0; $i -lt 4; $i++) {
        $length = ($length * 256) + [int]$Buffer[$Offset.Value]
        $Offset.Value += 1
    }
    if ($length -lt 1 -or $length -gt 64 -or ($Offset.Value + $length) -gt $Buffer.Length) {
        throw 'Campo OpenSSH invalido/truncado; diagnostico interrompido.'
    }
    $value = [Text.Encoding]::ASCII.GetString($Buffer, $Offset.Value, [int]$length)
    $Offset.Value += [int]$length
    return $value
}

$key = (Resolve-Path -LiteralPath $KeyPath).ProviderPath
$pem = [IO.File]::ReadAllText($key)
$pattern = '(?s)-----BEGIN OPENSSH PRIVATE KEY-----\s*(.*?)\s*-----END OPENSSH PRIVATE KEY-----'
$match = [regex]::Match($pem, $pattern)
if (-not $match.Success) {
    throw 'Formato inesperado. Esperado: chave OpenSSH privada (nao PEM RSA legado).'
}

# A leitura restringe-se aos primeiros metadados publicos do envelope OpenSSH.
$bytes = [Convert]::FromBase64String(($match.Groups[1].Value -replace '\s', ''))
$magic = [Text.Encoding]::ASCII.GetBytes('openssh-key-v1' + [char]0)
if ($bytes.Length -le $magic.Length) {
    throw 'Envelope OpenSSH incompleto.'
}
for ($i = 0; $i -lt $magic.Length; $i++) {
    if ($bytes[$i] -ne $magic[$i]) {
        throw 'Assinatura openssh-key-v1 ausente ou corrompida.'
    }
}
$offset = $magic.Length
$cipher = Read-SshMetadataString -Buffer $bytes -Offset ([ref]$offset)
$kdf = Read-SshMetadataString -Buffer $bytes -Offset ([ref]$offset)
Write-Host "SSH_CIPHER=$cipher"
Write-Host "SSH_KDF=$kdf"

# Apenas a fingerprint publica e exposta, nunca o campo base64 da chave.
$fpLines = & ssh-keygen.exe -lf $key -E sha256 2>$null
if ($LASTEXITCODE -ne 0) {
    throw 'ssh-keygen nao conseguiu ler a fingerprint da chave.'
}
$fpMatch = [regex]::Match(($fpLines -join [Environment]::NewLine), 'SHA256:[A-Za-z0-9+/=]+')
if (-not $fpMatch.Success) {
    throw 'Saida inesperada ao consultar fingerprint.'
}
$fingerprint = $fpMatch.Value
Write-Host "SSH_FINGERPRINT=$fingerprint"
if ($fingerprint -ne $ExpectedFingerprint) {
    Write-Error 'A fingerprint difere da RSA dedicada esperada. Nenhuma autenticacao sera tentada.'
    exit 22
}

if ($cipher -ne 'none' -or $kdf -ne 'none') {
    Write-Warning 'Chave ainda criptografada; modo nao interativo impedido. Nenhum SSH executado.'
    exit 21
}

Write-Host 'SSH_KEY_UNENCRYPTED=YES'
if (-not $TestSsh) {
    Write-Host 'Somente diagnostico local executado. Use -TestSsh para realizar login nao interativo.'
    exit 0
}

# Falha fechada para chave e host desconhecidos; nunca aceita host key automatica.
$sshArgs = @(
    '-n', '-i', $key,
    '-o', 'BatchMode=yes',
    '-o', 'IdentityAgent=none',
    '-o', 'IdentitiesOnly=yes',
    '-o', 'PreferredAuthentications=publickey',
    '-o', 'PasswordAuthentication=no',
    '-o', 'KbdInteractiveAuthentication=no',
    '-o', 'StrictHostKeyChecking=yes',
    '-o', 'ConnectTimeout=10',
    "$SshUser@$SshHost",
    'printf "SSH_RSA_OK\n"'
)
& ssh.exe @sshArgs
$sshExit = $LASTEXITCODE
Write-Host "SSH_EXIT=$sshExit"
exit $sshExit
