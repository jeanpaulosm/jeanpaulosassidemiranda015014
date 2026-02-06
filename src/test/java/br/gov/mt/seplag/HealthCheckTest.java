package br.gov.mt.seplag;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de health check.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class HealthCheckTest {

    @Test
    void healthCheckShouldReturnUp() {
        given()
            .when()
            .get("/q/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void livenessCheckShouldReturnUp() {
        given()
            .when()
            .get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void readinessCheckShouldReturnUp() {
        given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
