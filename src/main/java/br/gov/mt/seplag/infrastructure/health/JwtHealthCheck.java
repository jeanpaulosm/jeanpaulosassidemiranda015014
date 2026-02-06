package br.gov.mt.seplag.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Health Check customizado para verificar configuracao JWT.
 * Valida se as chaves publicas/privadas estao acessiveis.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Readiness
@ApplicationScoped
public class JwtHealthCheck implements HealthCheck {

    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "publicKey.pem")
    String publicKeyLocation;

    @ConfigProperty(name = "smallrye.jwt.sign.key.location", defaultValue = "privateKey.pem")
    String privateKeyLocation;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
            .named("JWT Configuration");

        boolean publicKeyOk = isKeyAccessible(publicKeyLocation, "Public Key");
        boolean privateKeyOk = isKeyAccessible(privateKeyLocation, "Private Key");

        responseBuilder
            .withData("publicKey", publicKeyOk ? "OK" : "MISSING")
            .withData("privateKey", privateKeyOk ? "OK" : "MISSING");

        if (publicKeyOk && privateKeyOk) {
            return responseBuilder
                .withData("status", "Chaves JWT configuradas corretamente")
                .up()
                .build();
        } else {
            return responseBuilder
                .withData("status", "Chaves JWT nao encontradas")
                .down()
                .build();
        }
    }

    private boolean isKeyAccessible(String location, String keyType) {
        try {
            // Tenta carregar do classpath primeiro
            var resource = getClass().getClassLoader().getResourceAsStream(location);
            if (resource != null) {
                resource.close();
                return true;
            }

            // Tenta como arquivo do sistema
            Path path = Path.of(location);
            return Files.exists(path) && Files.isReadable(path);
        } catch (Exception e) {
            return false;
        }
    }
}
