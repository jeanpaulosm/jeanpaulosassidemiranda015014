# =============================================================================
# PSS Backend MT - Dockerfile
# =============================================================================
# Dockerfile otimizado para producao (conforme requisito do edital) com:
# - Multi-stage build para imagem minima
# - Usuario nao-root por seguranca
# - Health checks configurados
# - JVM otimizada para containers
# - Suporte a Docker Secrets ou variaveis de ambiente
#
# Uso: docker-compose up -d
#
# @author Jean Paulo Sassi de Miranda
# =============================================================================

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -Dquarkus.package.type=uber-jar

# Runtime stage - Imagem minima
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instala apenas o necessario para health checks e geracao de chaves JWT
RUN apk add --no-cache wget openssl

# Cria usuario nao-root para seguranca
RUN addgroup -S quarkus && adduser -S quarkus -G quarkus

# Cria diretorio para secrets (sera montado em runtime)
RUN mkdir -p /app/secrets && chown quarkus:quarkus /app/secrets

# Copy the built artifact (sem as chaves JWT - serao montadas via secrets)
# Quarkus uber-jar inclui sufixo -runner
COPY --from=build --chown=quarkus:quarkus /app/target/pss-backend-mt-1.0.0-runner.jar /app/application.jar

# Copy entrypoint script para leitura de secrets
COPY --chown=quarkus:quarkus scripts/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Muda para usuario nao-root
USER quarkus

# Expose port
EXPOSE 8080

# Health check - Verifica se a aplicacao esta respondendo
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/q/health/live || exit 1

# Labels para metadados da imagem
LABEL maintainer="Jean Paulo Sassi de Miranda" \
      version="1.0.0" \
      description="PSS Backend MT - API de Artistas e Albuns" \
      org.opencontainers.image.source="https://github.com/jeanpaulossasidemiranda/pss-backend-mt"

# JVM otimizada para containers
# -XX:+UseContainerSupport: Respeita limites de memoria do container
# -XX:MaxRAMPercentage=75.0: Usa ate 75% da memoria disponivel
# -Djava.security.egd: Melhora performance de geracao de numeros aleatorios
ENTRYPOINT ["/app/docker-entrypoint.sh", "java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/application.jar"]
