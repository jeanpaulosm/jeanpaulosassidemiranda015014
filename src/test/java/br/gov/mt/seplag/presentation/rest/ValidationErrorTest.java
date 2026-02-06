package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de cenarios de erro e validacao de DTOs.
 * Cobertura completa de validacoes de entrada e tratamento de erros.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("Testes de Validacao e Cenarios de Erro")
class ValidationErrorTest {

    /**
     * Cria uma requisicao com IP unico para evitar rate limit.
     */
    private RequestSpecification uniqueRequest() {
        return given().header("X-Forwarded-For", UUID.randomUUID().toString());
    }

    // ====================
    // TESTES DE VALIDACAO - ARTISTA REQUEST
    // ====================

    @Nested
    @DisplayName("Validacao de ArtistaRequest")
    class ArtistaRequestValidationTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com nome vazio")
        void shouldRejectArtistaWithEmptyNome() {
            String json = """
                {
                    "nome": "",
                    "tipo": "CANTOR"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com nome muito curto (menos de 2 caracteres)")
        void shouldRejectArtistaWithShortNome() {
            String json = """
                {
                    "nome": "A",
                    "tipo": "CANTOR"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com nome muito longo (mais de 200 caracteres)")
        void shouldRejectArtistaWithLongNome() {
            String longName = "A".repeat(201);
            String json = """
                {
                    "nome": "%s",
                    "tipo": "CANTOR"
                }
                """.formatted(longName);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com caracteres invalidos no nome")
        void shouldRejectArtistaWithInvalidCharactersInNome() {
            String json = """
                {
                    "nome": "Artista <script>alert('xss')</script>",
                    "tipo": "CANTOR"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com tipo invalido")
        void shouldRejectArtistaWithInvalidTipo() {
            String json = """
                {
                    "nome": "Artista Teste",
                    "tipo": "INVALIDO"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar artista com descricao muito longa (mais de 2000 caracteres)")
        void shouldRejectArtistaWithLongDescricao() {
            String longDesc = "D".repeat(2001);
            String json = """
                {
                    "nome": "Artista Teste",
                    "tipo": "CANTOR",
                    "descricao": "%s"
                }
                """.formatted(longDesc);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve aceitar artista com caracteres especiais validos no nome")
        void shouldAcceptArtistaWithValidSpecialCharacters() {
            String json = """
                {
                    "nome": "Guns N' Roses & Co. %s",
                    "tipo": "BANDA"
                }
                """.formatted(System.currentTimeMillis());

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(201);
        }
    }

    // ====================
    // TESTES DE VALIDACAO - ALBUM REQUEST
    // ====================

    @Nested
    @DisplayName("Validacao de AlbumRequest")
    class AlbumRequestValidationTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com titulo vazio")
        void shouldRejectAlbumWithEmptyTitulo() {
            String json = """
                {
                    "titulo": "",
                    "artistaIds": [1]
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com titulo muito longo (mais de 300 caracteres)")
        void shouldRejectAlbumWithLongTitulo() {
            String longTitle = "T".repeat(301);
            String json = """
                {
                    "titulo": "%s",
                    "artistaIds": [1]
                }
                """.formatted(longTitle);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album sem artistas associados")
        void shouldRejectAlbumWithoutArtistas() {
            String json = """
                {
                    "titulo": "Album Sem Artista",
                    "artistaIds": []
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com ano de lancamento muito antigo (antes de 1900)")
        void shouldRejectAlbumWithOldYear() {
            String json = """
                {
                    "titulo": "Album Antigo",
                    "anoLancamento": 1899,
                    "artistaIds": [1]
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com ano de lancamento muito futuro (apos 2100)")
        void shouldRejectAlbumWithFutureYear() {
            String json = """
                {
                    "titulo": "Album Futuro",
                    "anoLancamento": 2101,
                    "artistaIds": [1]
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com descricao muito longa (mais de 2000 caracteres)")
        void shouldRejectAlbumWithLongDescricao() {
            String longDesc = "D".repeat(2001);
            String json = """
                {
                    "titulo": "Album Teste",
                    "descricao": "%s",
                    "artistaIds": [1]
                }
                """.formatted(longDesc);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve rejeitar album com artista inexistente")
        void shouldRejectAlbumWithNonExistentArtista() {
            String json = """
                {
                    "titulo": "Album com Artista Inexistente",
                    "artistaIds": [999999]
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(404)));
        }
    }

    // ====================
    // TESTES DE VALIDACAO - LOGIN REQUEST
    // ====================

    @Nested
    @DisplayName("Validacao de LoginRequest")
    class LoginRequestValidationTests {

        @Test
        @DisplayName("Deve rejeitar login com username vazio")
        void shouldRejectLoginWithEmptyUsername() {
            String json = """
                {
                    "username": "",
                    "password": "senha123"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com username muito curto (menos de 3 caracteres)")
        void shouldRejectLoginWithShortUsername() {
            String json = """
                {
                    "username": "ab",
                    "password": "senha123"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com username muito longo (mais de 50 caracteres)")
        void shouldRejectLoginWithLongUsername() {
            String longUsername = "u".repeat(51);
            String json = """
                {
                    "username": "%s",
                    "password": "senha123"
                }
                """.formatted(longUsername);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com caracteres invalidos no username")
        void shouldRejectLoginWithInvalidUsernameCharacters() {
            String json = """
                {
                    "username": "user@name!",
                    "password": "senha123"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com senha vazia")
        void shouldRejectLoginWithEmptyPassword() {
            String json = """
                {
                    "username": "admin",
                    "password": ""
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com senha muito curta (menos de 6 caracteres)")
        void shouldRejectLoginWithShortPassword() {
            String json = """
                {
                    "username": "admin",
                    "password": "12345"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve rejeitar login com senha muito longa (mais de 100 caracteres)")
        void shouldRejectLoginWithLongPassword() {
            String longPassword = "p".repeat(101);
            String json = """
                {
                    "username": "admin",
                    "password": "%s"
                }
                """.formatted(longPassword);

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Deve aceitar login com caracteres validos no username")
        void shouldAcceptLoginWithValidUsernameCharacters() {
            // Este teste verifica caracteres validos: letras, numeros, ponto, underline, hifen
            String json = """
                {
                    "username": "user_name.test-1",
                    "password": "senha123"
                }
                """;

            uniqueRequest()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/auth/login")
                .then()
                // Pode ser 401 (usuario nao existe) mas nao 400 (validacao)
                .statusCode(401);
        }
    }

    // ====================
    // TESTES DE ERROS HTTP
    // ====================

    @Nested
    @DisplayName("Erros HTTP Gerais")
    class HttpErrorTests {

        @Test
        @DisplayName("Deve retornar erro para endpoint inexistente")
        void shouldReturnErrorForNonExistentEndpoint() {
            uniqueRequest()
                .when()
                .get("/api/v1/endpoint-inexistente")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(404), equalTo(500)));
        }

        @Test
        @DisplayName("Deve retornar erro para Content-Type invalido")
        void shouldReturnErrorForInvalidContentType() {
            uniqueRequest()
                .contentType("text/plain")
                .body("dados invalidos")
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(415), equalTo(500)));
        }

        @Test
        @DisplayName("Deve retornar erro para JSON malformado")
        void shouldReturnErrorForMalformedJson() {
            uniqueRequest()
                .contentType(ContentType.JSON)
                .body("{ json invalido }")
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(500)));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar erro para body vazio em POST")
        void shouldReturnErrorForEmptyBody() {
            uniqueRequest()
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(500)));
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar erro para metodo HTTP nao permitido")
        void shouldReturnErrorForMethodNotAllowed() {
            uniqueRequest()
                .when()
                .patch("/api/v1/artistas/1")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(405), equalTo(500)));
        }
    }

    // ====================
    // TESTES DE PAGINACAO INVALIDA
    // ====================

    @Nested
    @DisplayName("Validacao de Paginacao")
    class PaginacaoValidationTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve aceitar paginacao com valores minimos")
        void shouldAcceptMinimumPaginationValues() {
            uniqueRequest()
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve aceitar tamanho de pagina grande")
        void shouldAcceptLargePageSize() {
            uniqueRequest()
                .queryParam("page", 0)
                .queryParam("size", 1000)
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .body("size", notNullValue());
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar lista vazia para pagina alem do total")
        void shouldReturnEmptyForPageBeyondTotal() {
            uniqueRequest()
                .queryParam("page", 9999)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
        }
    }
}
