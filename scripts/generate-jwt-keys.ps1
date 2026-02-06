# =============================================================================
# PSS Backend MT - Script de Geracao de Chaves JWT RS256 (Windows PowerShell)
# =============================================================================
# Este script gera um par de chaves RSA-2048 para autenticacao JWT.
#
# Uso:
#   .\scripts\generate-jwt-keys.ps1 [-OutputDir <path>]
#
# Parametros:
#   -OutputDir  - Diretorio de saida (padrao: .\secrets)
#
# Saidas:
#   - privateKey.pem: Chave privada RSA (NUNCA compartilhe ou versione!)
#   - publicKey.pem:  Chave publica RSA (pode ser distribuida)
#
# Requisitos:
#   - OpenSSL instalado e no PATH
#   - Ou: Git Bash instalado (inclui OpenSSL)
#
# @author Jean Paulo Sassi de Miranda
# =============================================================================

param(
    [string]$OutputDir = ".\secrets"
)

$ErrorActionPreference = "Stop"

Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "PSS Backend MT - Geracao de Chaves JWT RS256" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""

# Procura OpenSSL em varios locais comuns
$opensslPaths = @(
    "openssl",
    "C:\Program Files\Git\usr\bin\openssl.exe",
    "C:\Program Files\OpenSSL-Win64\bin\openssl.exe",
    "C:\OpenSSL-Win64\bin\openssl.exe"
)

$openssl = $null
foreach ($path in $opensslPaths) {
    try {
        $null = & $path version 2>$null
        $openssl = $path
        break
    } catch {
        continue
    }
}

if (-not $openssl) {
    Write-Host "ERRO: OpenSSL nao encontrado." -ForegroundColor Red
    Write-Host "Instale OpenSSL ou Git for Windows (inclui OpenSSL)."
    Write-Host "Download: https://slproweb.com/products/Win32OpenSSL.html"
    exit 1
}

Write-Host "OpenSSL encontrado: $openssl" -ForegroundColor Cyan

# Cria diretorio de saida
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$privateKey = Join-Path $OutputDir "privateKey.pem"
$publicKey = Join-Path $OutputDir "publicKey.pem"

# Verifica se as chaves ja existem
if ((Test-Path $privateKey) -or (Test-Path $publicKey)) {
    Write-Host "AVISO: Chaves JWT ja existem em $OutputDir" -ForegroundColor Yellow
    $confirm = Read-Host "Deseja sobrescrever? (s/N)"
    if ($confirm -ne "s" -and $confirm -ne "S") {
        Write-Host "Operacao cancelada."
        exit 0
    }
}

Write-Host ""
Write-Host "[1/4] Gerando chave privada RSA-2048..." -ForegroundColor Green
& $openssl genrsa -out $privateKey 2048 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha ao gerar chave privada." -ForegroundColor Red
    exit 1
}

Write-Host "[2/4] Extraindo chave publica..." -ForegroundColor Green
& $openssl rsa -pubout -in $privateKey -out $publicKey 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha ao extrair chave publica." -ForegroundColor Red
    exit 1
}

Write-Host "[3/4] Validando chaves geradas..." -ForegroundColor Green
$privateCheck = & $openssl rsa -in $privateKey -check -noout 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Chave privada valida" -ForegroundColor Green
} else {
    Write-Host "  [ERRO] Chave privada invalida" -ForegroundColor Red
    exit 1
}

$publicCheck = & $openssl rsa -pubin -in $publicKey -noout 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Chave publica valida" -ForegroundColor Green
} else {
    Write-Host "  [ERRO] Chave publica invalida" -ForegroundColor Red
    exit 1
}

Write-Host "[4/4] Configurando permissoes..." -ForegroundColor Green
# No Windows, configuramos ACLs para restringir acesso
try {
    $acl = Get-Acl $privateKey
    $acl.SetAccessRuleProtection($true, $false)
    $currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule($currentUser, "FullControl", "Allow")
    $acl.AddAccessRule($rule)
    Set-Acl $privateKey $acl
    Write-Host "  [OK] Permissoes restritas aplicadas" -ForegroundColor Green
} catch {
    Write-Host "  [AVISO] Nao foi possivel restringir permissoes automaticamente" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "Chaves JWT geradas com sucesso!" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Arquivos criados:"
Write-Host "  - Chave privada: $privateKey"
Write-Host "  - Chave publica: $publicKey"
Write-Host ""
Write-Host "IMPORTANTE - SEGURANCA:" -ForegroundColor Yellow
Write-Host "  1. NUNCA versione a chave privada em repositorios Git"
Write-Host "  2. Armazene em um cofre de segredos (Vault, AWS Secrets Manager, etc.)"
Write-Host "  3. Use variaveis de ambiente ou volumes Docker em producao"
Write-Host ""
Write-Host "Para usar com Docker Compose:"
Write-Host "  1. Copie as chaves para .\secrets\"
Write-Host "  2. Execute: docker-compose up -d"
Write-Host ""
