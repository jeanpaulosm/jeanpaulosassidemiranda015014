package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para AuthResource.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class AuthResourceTest {

    @Test
    void shouldReturnTokenOnValidLogin() {
        String json = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("refreshToken", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", equalTo(300));
    }

    @Test
    void shouldReturn401OnInvalidCredentials() {
        String json = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn401OnNonExistentUser() {
        String json = """
            {
                "username": "nonexistent",
                "password": "password"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(401);
    }
}
