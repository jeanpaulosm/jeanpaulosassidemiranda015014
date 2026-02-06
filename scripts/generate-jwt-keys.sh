#!/bin/bash
# =============================================================================
# PSS Backend MT - Script de Geracao de Chaves JWT RS256
# =============================================================================
# Este script gera um par de chaves RSA-2048 para autenticacao JWT.
#
# Uso:
#   ./scripts/generate-jwt-keys.sh [output_dir]
#
# Argumentos:
#   output_dir  - Diretorio de saida (padrao: ./secrets)
#
# Saidas:
#   - privateKey.pem: Chave privada RSA (NUNCA compartilhe ou versione!)
#   - publicKey.pem:  Chave publica RSA (pode ser distribuida)
#
# Seguranca:
#   - Chaves sao geradas com 2048 bits (minimo recomendado NIST)
#   - Permissoes restritas: 600 para privada, 644 para publica
#   - Chaves NUNCA devem ser versionadas em repositorios Git
#
# @author Jean Paulo Sassi de Miranda
# =============================================================================

set -euo pipefail

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Diretorio de saida
OUTPUT_DIR="${1:-./secrets}"

echo -e "${GREEN}==============================================================================${NC}"
echo -e "${GREEN}PSS Backend MT - Geracao de Chaves JWT RS256${NC}"
echo -e "${GREEN}==============================================================================${NC}"

# Verifica se OpenSSL esta instalado
if ! command -v openssl &> /dev/null; then
    echo -e "${RED}ERRO: OpenSSL nao esta instalado.${NC}"
    echo "Instale com: sudo apt-get install openssl (Debian/Ubuntu)"
    echo "          ou: brew install openssl (macOS)"
    exit 1
fi

# Cria diretorio de saida se nao existir
mkdir -p "$OUTPUT_DIR"

PRIVATE_KEY="$OUTPUT_DIR/privateKey.pem"
PUBLIC_KEY="$OUTPUT_DIR/publicKey.pem"

# Verifica se as chaves ja existem
if [ -f "$PRIVATE_KEY" ] || [ -f "$PUBLIC_KEY" ]; then
    echo -e "${YELLOW}AVISO: Chaves JWT ja existem em $OUTPUT_DIR${NC}"
    read -p "Deseja sobrescrever? (s/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Operacao cancelada."
        exit 0
    fi
fi

echo ""
echo -e "${GREEN}[1/4]${NC} Gerando chave privada RSA-2048..."
openssl genrsa -out "$PRIVATE_KEY" 2048 2>/dev/null

echo -e "${GREEN}[2/4]${NC} Extraindo chave publica..."
openssl rsa -pubout -in "$PRIVATE_KEY" -out "$PUBLIC_KEY" 2>/dev/null

echo -e "${GREEN}[3/4]${NC} Configurando permissoes de seguranca..."
# Chave privada: somente leitura para o dono
chmod 600 "$PRIVATE_KEY"
# Chave publica: leitura para todos
chmod 644 "$PUBLIC_KEY"

echo -e "${GREEN}[4/4]${NC} Validando chaves geradas..."
# Valida se as chaves sao validas
if openssl rsa -in "$PRIVATE_KEY" -check -noout 2>/dev/null; then
    echo -e "  ${GREEN}[OK]${NC} Chave privada valida"
else
    echo -e "  ${RED}[ERRO]${NC} Chave privada invalida"
    exit 1
fi

if openssl rsa -pubin -in "$PUBLIC_KEY" -noout 2>/dev/null; then
    echo -e "  ${GREEN}[OK]${NC} Chave publica valida"
else
    echo -e "  ${RED}[ERRO]${NC} Chave publica invalida"
    exit 1
fi

echo ""
echo -e "${GREEN}==============================================================================${NC}"
echo -e "${GREEN}Chaves JWT geradas com sucesso!${NC}"
echo -e "${GREEN}==============================================================================${NC}"
echo ""
echo "Arquivos criados:"
echo "  - Chave privada: $PRIVATE_KEY"
echo "  - Chave publica: $PUBLIC_KEY"
echo ""
echo -e "${YELLOW}IMPORTANTE - SEGURANCA:${NC}"
echo "  1. NUNCA versione a chave privada em repositorios Git"
echo "  2. Armazene em um cofre de segredos (Vault, AWS Secrets Manager, etc.)"
echo "  3. Use variaveis de ambiente ou volumes Docker em producao"
echo ""
echo "Para usar com Docker Compose:"
echo "  1. Copie as chaves para ./secrets/"
echo "  2. Execute: docker-compose up -d"
echo ""
echo "Para usar com Kubernetes:"
echo "  kubectl create secret generic jwt-keys \\"
echo "    --from-file=privateKey.pem=$PRIVATE_KEY \\"
echo "    --from-file=publicKey.pem=$PUBLIC_KEY"
echo ""
