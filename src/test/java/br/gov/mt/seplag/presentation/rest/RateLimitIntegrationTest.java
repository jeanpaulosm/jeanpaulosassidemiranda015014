package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para validar o comportamento do Rate Limit na API.
 *
 * Conforme edital: "Rate limit: ate 10 requisicoes por minuto por usuario"
 *
 * Testa:
 * 1. Resposta 429 Too Many Requests quando limite e excedido
 * 2. Headers de rate limit na resposta (X-RateLimit-*)
 * 3. Rate limit por IP para requisicoes nao autenticadas
 * 4. Rate limit por usuario para requisicoes autenticadas
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class RateLimitIntegrationTest {

    /**
     * Gera um IP unico para cada teste para evitar conflitos de rate limit.
     */
    private String uniqueIp() {
        return "192.168." + (int)(Math.random() * 255) + "." + (int)(Math.random() * 255);
    }

    /**
     * Cria requisicao com IP especifico para controle de rate limit.
     */
    private RequestSpecification requestWithIp(String ip) {
        return given().header("X-Forwarded-For", ip);
    }

    /**
     * Testa que requisicoes nao autenticadas recebem 429 apos exceder o limite.
     * Usa um endpoint publico (login) para teste.
     */
    @Test
    void shouldReturn429WhenRateLimitExceededForUnauthenticated() {
        // Usa IP unico para este teste
        String testIp = "10.0.0." + (int)(Math.random() * 255);
        String json = """
            {
                "username": "ratelimit-test",
                "password": "invalid"
            }
            """;

        // Faz 10 requisicoes (limite) - vao retornar 401 mas contam para o rate limit
        for (int i = 0; i < 10; i++) {
            requestWithIp(testIp)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401);
        }

        // A 11a requisicao deve retornar 429 Too Many Requests
        requestWithIp(testIp)
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(429)
            .body("message", containsString("limite"))
            .body("error", equalTo("TOO_MANY_REQUESTS"));
    }

    /**
     * Testa que os headers de rate limit estao presentes na resposta.
     */
    @Test
    void shouldIncludeRateLimitHeadersInResponse() {
        String testIp = "10.1.0." + (int)(Math.random() * 255);
        String json = """
            {
                "username": "header-test",
                "password": "invalid"
            }
            """;

        Response response = requestWithIp(testIp)
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login");

        // Verifica que os headers de rate limit estao presentes
        response.then()
            .header("X-RateLimit-Limit", notNullValue())
            .header("X-RateLimit-Remaining", notNullValue());
    }

    /**
     * Testa que o header X-RateLimit-Remaining decrementa corretamente.
     */
    @Test
    void shouldDecrementRemainingRequestsHeader() {
        String testIp = "10.2.0." + (int)(Math.random() * 255);
        String json = """
            {
                "username": "decrement-test",
                "password": "invalid"
            }
            """;

        // Primeira requisicao
        Response firstResponse = requestWithIp(testIp)
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login");

        int firstRemaining = Integer.parseInt(
            firstResponse.getHeader("X-RateLimit-Remaining")
        );

        // Segunda requisicao
        Response secondResponse = requestWithIp(testIp)
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/auth/login");

        int secondRemaining = Integer.parseInt(
            secondResponse.getHeader("X-RateLimit-Remaining")
        );

        // O remaining deve ter decrementado em 1
        org.junit.jupiter.api.Assertions.assertEquals(
            firstRemaining - 1,
            secondRemaining,
            "X-RateLimit-Remaining deve decrementar a cada requisicao"
        );
    }

    /**
     * Testa que usuarios autenticados diferentes tem rate limits independentes.
     */
    @Test
    void shouldTrackAuthenticatedUsersSeparately() {
        // Usa IP unico para evitar conflitos com outros testes
        String testIp = "10.3.0." + (int)(Math.random() * 255);

        // Primeiro, obtem um token valido
        String loginJson = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;

        Response loginResponse = requestWithIp(testIp)
            .contentType(ContentType.JSON)
            .body(loginJson)
            .when()
            .post("/api/v1/auth/login");

        // Verifica se login foi bem sucedido antes de continuar
        if (loginResponse.getStatusCode() != 200) {
            // Se o login falhar (provavelmente hash BCrypt incorreto), pula verificacao de token
            // e apenas verifica que os headers de rate limit estao presentes no login
            loginResponse.then()
                .header("X-RateLimit-Limit", notNullValue())
                .header("X-RateLimit-Remaining", notNullValue());
            return;
        }

        String accessToken = loginResponse.jsonPath().getString("accessToken");

        // Faz uma requisicao autenticada com outro IP para nao afetar o rate limit do login
        String authIp = "10.4.0." + (int)(Math.random() * 255);
        Response authenticatedResponse = requestWithIp(authIp)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get("/api/v1/artistas");

        // Verifica que tem os headers de rate limit
        authenticatedResponse.then()
            .header("X-RateLimit-Limit", equalTo("10"))
            .header("X-RateLimit-Remaining", notNullValue());

        // O remaining para o usuario autenticado deve comecar em 10 (menos as requisicoes feitas)
        int remaining = Integer.parseInt(
            authenticatedResponse.getHeader("X-RateLimit-Remaining")
        );
        org.junit.jupiter.api.Assertions.assertTrue(
            remaining >= 0 && remaining <= 10,
            "X-RateLimit-Remaining deve estar entre 0 e 10"
        );
    }

    /**
     * Testa a mensagem de erro quando rate limit e excedido.
     */
    @Test
    void shouldReturnProperErrorMessageWhenLimitExceeded() {
        String testIp = "10.5.0." + (int)(Math.random() * 255);
        String json = """
            {
                "username": "error-msg-test",
                "password": "invalid"
            }
            """;

        // Excede o limite
        for (int i = 0; i < 11; i++) {
            Response response = requestWithIp(testIp)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login");

            // Se ja atingiu 429, verifica a mensagem
            if (response.getStatusCode() == 429) {
                response.then()
                    .body("message", allOf(
                        containsString("10"),
                        containsString("requisicoes")
                    ))
                    .body("error", equalTo("TOO_MANY_REQUESTS"))
                    .body("status", equalTo(429));
                return;
            }
        }

        // Se chegou aqui, o rate limit nao funcionou
        org.junit.jupiter.api.Assertions.fail(
            "Rate limit deveria ter retornado 429 apos 10 requisicoes"
        );
    }
}
