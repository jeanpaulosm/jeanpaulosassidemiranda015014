#!/bin/sh
# =============================================================================
# PSS Backend MT - Docker Entrypoint Script
# =============================================================================
# Este script:
# 1. Gera automaticamente as chaves JWT se nao existirem
# 2. Le Docker Secrets e exporta como variaveis de ambiente
# 3. Inicia a aplicacao Quarkus
#
# Padrao de mercado para integracao de secrets com containers Java.
#
# @author Jean Paulo Sassi de Miranda
# =============================================================================

set -e

# Diretorio das chaves JWT
JWT_SECRETS_DIR="/app/secrets"
PRIVATE_KEY="${JWT_SECRETS_DIR}/privateKey.pem"
PUBLIC_KEY="${JWT_SECRETS_DIR}/publicKey.pem"

# =============================================================================
# Geracao automatica de chaves JWT (se nao existirem)
# =============================================================================
generate_jwt_keys() {
    echo "=== Verificando chaves JWT ==="

    if [ ! -f "$PRIVATE_KEY" ] || [ ! -f "$PUBLIC_KEY" ]; then
        echo "Chaves JWT nao encontradas. Gerando automaticamente..."

        # Cria o diretorio se nao existir
        mkdir -p "$JWT_SECRETS_DIR"

        # Gera par de chaves RSA 2048 bits
        openssl genrsa -out "$PRIVATE_KEY" 2048 2>/dev/null
        openssl rsa -in "$PRIVATE_KEY" -pubout -out "$PUBLIC_KEY" 2>/dev/null

        echo "Chaves JWT geradas com sucesso!"
        echo "  - Chave privada: $PRIVATE_KEY"
        echo "  - Chave publica: $PUBLIC_KEY"
    else
        echo "Chaves JWT encontradas. Usando chaves existentes."
    fi
}

# Funcao para ler secret de arquivo se existir
read_secret() {
    local secret_file="$1"
    if [ -f "$secret_file" ]; then
        cat "$secret_file"
    fi
}

# =============================================================================
# Geracao de chaves JWT
# =============================================================================
generate_jwt_keys

# =============================================================================
# Configuracao de Docker Secrets (para ambientes de producao)
# =============================================================================

# Database password
if [ -f "/run/secrets/db_password" ]; then
    export QUARKUS_DATASOURCE_PASSWORD=$(read_secret /run/secrets/db_password)
fi

# MinIO credentials
if [ -f "/run/secrets/minio_access_key" ]; then
    export QUARKUS_S3_AWS_CREDENTIALS_STATIC_PROVIDER_ACCESS_KEY_ID=$(read_secret /run/secrets/minio_access_key)
fi

if [ -f "/run/secrets/minio_secret_key" ]; then
    export QUARKUS_S3_AWS_CREDENTIALS_STATIC_PROVIDER_SECRET_ACCESS_KEY=$(read_secret /run/secrets/minio_secret_key)
fi

# JWT keys - usa paths diretamente (Quarkus suporta file paths)
if [ -f "/run/secrets/jwt_public_key" ]; then
    export MP_JWT_VERIFY_PUBLICKEY_LOCATION=/run/secrets/jwt_public_key
fi

if [ -f "/run/secrets/jwt_private_key" ]; then
    export SMALLRYE_JWT_SIGN_KEY_LOCATION=/run/secrets/jwt_private_key
fi

# =============================================================================
# Inicio da aplicacao
# =============================================================================
echo ""
echo "=== PSS Backend MT - Iniciando aplicacao ==="
echo "Database URL: ${QUARKUS_DATASOURCE_JDBC_URL}"
echo "MinIO Endpoint: ${QUARKUS_S3_ENDPOINT_OVERRIDE}"
echo "JWT Private Key: ${SMALLRYE_JWT_SIGN_KEY_LOCATION:-$PRIVATE_KEY}"
echo "JWT Public Key: ${MP_JWT_VERIFY_PUBLICKEY_LOCATION:-$PUBLIC_KEY}"
echo "Profile: ${QUARKUS_PROFILE:-default}"
echo "============================================="
echo ""

# Executa o comando passado (java -jar ...)
exec "$@"
