package br.gov.mt.seplag.infrastructure.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios para JwtHealthCheck.
 *
 * @author Jean Paulo Sassi de Miranda
 */
class JwtHealthCheckTest {

    private JwtHealthCheck healthCheck;

    @BeforeEach
    void setUp() throws Exception {
        healthCheck = new JwtHealthCheck();

        // Configura campos via reflection (simula injecao de ConfigProperty)
        setField(healthCheck, "publicKeyLocation", "publicKey.pem");
        setField(healthCheck, "privateKeyLocation", "privateKey.pem");
    }

    @Test
    void callShouldReturnUpWhenKeysExist() {
        HealthCheckResponse response = healthCheck.call();

        assertThat(response.getName()).isEqualTo("JWT Configuration");
        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
        assertThat(response.getData()).isPresent();
        assertThat(response.getData().get().get("publicKey")).isEqualTo("OK");
        assertThat(response.getData().get().get("privateKey")).isEqualTo("OK");
    }

    @Test
    void callShouldReturnDownWhenPublicKeyMissing() throws Exception {
        setField(healthCheck, "publicKeyLocation", "nonexistent-public.pem");

        HealthCheckResponse response = healthCheck.call();

        assertThat(response.getName()).isEqualTo("JWT Configuration");
        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
        assertThat(response.getData()).isPresent();
        assertThat(response.getData().get().get("publicKey")).isEqualTo("MISSING");
    }

    @Test
    void callShouldReturnDownWhenPrivateKeyMissing() throws Exception {
        setField(healthCheck, "privateKeyLocation", "nonexistent-private.pem");

        HealthCheckResponse response = healthCheck.call();

        assertThat(response.getName()).isEqualTo("JWT Configuration");
        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
        assertThat(response.getData()).isPresent();
        assertThat(response.getData().get().get("privateKey")).isEqualTo("MISSING");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
