package br.gov.mt.seplag.infrastructure.health;

import br.gov.mt.seplag.infrastructure.client.RegionaisClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health Check customizado para verificar conectividade com API externa de regionais.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Readiness
@ApplicationScoped
public class RegionaisApiHealthCheck implements HealthCheck {

    @Inject
    @RestClient
    RegionaisClient regionaisClient;

    @ConfigProperty(name = "quarkus.rest-client.regionais-api.url", defaultValue = "https://aberto.sesp.mt.gov.br/api-regionais")
    String apiUrl;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
            .named("Regionais External API")
            .withData("url", apiUrl);

        try {
            var regionais = regionaisClient.getRegionais();

            return responseBuilder
                .withData("status", "API acessivel")
                .withData("registros", regionais != null ? regionais.size() : 0)
                .up()
                .build();
        } catch (Exception e) {
            return responseBuilder
                .withData("error", e.getMessage())
                .withData("status", "API inacessivel - servico pode funcionar com dados em cache")
                .down()
                .build();
        }
    }
}
