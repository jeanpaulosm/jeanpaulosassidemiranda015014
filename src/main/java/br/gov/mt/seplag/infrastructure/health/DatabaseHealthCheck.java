package br.gov.mt.seplag.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health Check customizado para verificar conectividade com banco de dados.
 * Executa query simples para validar conexao.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
            .named("PostgreSQL Database");

        try {
            // Query simples para verificar conectividade
            Object result = entityManager
                .createNativeQuery("SELECT 1")
                .getSingleResult();

            // Obtem versao do PostgreSQL
            String version = (String) entityManager
                .createNativeQuery("SELECT version()")
                .getSingleResult();

            return responseBuilder
                .withData("status", "Conectado")
                .withData("version", version != null ? version.split(",")[0] : "Unknown")
                .up()
                .build();
        } catch (Exception e) {
            return responseBuilder
                .withData("error", e.getMessage())
                .withData("status", "Desconectado")
                .down()
                .build();
        }
    }
}
