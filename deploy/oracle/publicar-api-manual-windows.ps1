# FoodBridge API: disparo manual opcional (CD na main ja e automatico).
# Windows PowerShell 5.1, gh autenticado. Nao imprime segredo.
[CmdletBinding()]
param(
    [switch]$Publicar,
    [string]$Repo = 'DanieldeAlmeida2024/FoodBridge-API-Kotlin'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Check-LastExit {
    param([string]$Step)
    if ($LASTEXITCODE -ne 0) {
        throw ("Falha em {0}: exit={1}" -f $Step, $LASTEXITCODE)
    }
}

if (-not $Publicar) {
    throw 'Publicacao nao solicitada. Execute explicitamente com -Publicar apos revisar o runbook.'
}
if (-not (Get-Command gh.exe -ErrorAction SilentlyContinue)) {
    throw 'GitHub CLI nao foi encontrado.'
}

& gh.exe auth status
Check-LastExit 'gh auth status'
    # Identifica exatamente o workflow recem-disparado, sem corrida com outros runs.
    $dispatchOutput = (& gh.exe workflow run foodbridge-ci-cd.yml --ref main -f preflight=false -f deploy=true --repo $Repo | Out-String)
    Check-LastExit 'workflow_dispatch API'
    $match = [regex]::Match($dispatchOutput, '/actions/runs/([0-9]+)')
    if (-not $match.Success) {
        Write-Host $dispatchOutput
        throw 'Trigger pode ter sido aceito, mas sem run ID. Consulte Actions antes de repetir.'
    }
    $runId = $match.Groups[1].Value
    Write-Host "API_DEPLOY_RUN_ID=$runId"

    & gh.exe run watch $runId --repo $Repo --exit-status
    Check-LastExit 'acompanhar CI/CD API'

    $jobJson = (& gh.exe run view $runId --repo $Repo --json jobs | Out-String)
    Check-LastExit 'inspecionar jobs reais'
    $jobs = @((ConvertFrom-Json $jobJson).jobs)
    $deployJobs = @($jobs | Where-Object { $_.name -eq 'Publicar na VM Oracle' })
    if ($deployJobs.Count -ne 1 -or $deployJobs[0].conclusion -ne 'success') {
        throw 'Workflow terminou mas job Publicar na VM Oracle nao ficou SUCCESS.'
    }
    Write-Host 'API_DEPLOY_JOB=SUCCESS' -ForegroundColor Green

    # O workflow valida a API diretamente por loopback na VM, com a rota /dashboards/publico.
    # O Nginx externo pode expor a API com prefixo diferente; nao assumir a mesma rota.
    # Verificar separadamente que o site continua respondendo apos o restart do backend.
    $siteUrl = 'http://163.176.133.210/'
    $code = (& curl.exe -4 --noproxy '*' --silent --show-error --connect-timeout 3 --max-time 20 --output NUL --write-out '%{http_code}' $siteUrl | Out-String).Trim()
    Check-LastExit 'smoke HTTP externo do site'
    if ($code -ne '200') {
        throw "O job de deploy API passou, mas o Nginx externo retornou $code. Investigar a rota externa do Nginx."
    }
    Write-Host 'SITE_EXTERNAL_HTTP=200' -ForegroundColor Green
    Write-Host 'API_INTERNAL_HEALTH=VERIFIED_BY_REMOTE_DEPLOY_JOB' -ForegroundColor Green
    Write-Host 'API_MANUAL_DEPLOY=PASS' -ForegroundColor Green
