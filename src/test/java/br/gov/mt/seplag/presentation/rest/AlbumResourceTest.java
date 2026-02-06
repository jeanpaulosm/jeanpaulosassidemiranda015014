package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para AlbumResource.
 * Cobertura completa de endpoints de albuns.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("AlbumResource - Testes de Integracao")
class AlbumResourceTest {

    // ====================
    // TESTES DE AUTENTICACAO
    // ====================

    @Nested
    @DisplayName("Autenticacao")
    class AutenticacaoTests {

        @Test
        @DisplayName("Deve retornar 401 ao listar albuns sem autenticacao")
        void shouldReturn401WhenListingWithoutAuthentication() {
            given()
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar album por ID sem autenticacao")
        void shouldReturn401WhenGettingByIdWithoutAuthentication() {
            given()
                .when()
                .get("/api/v1/albuns/1")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar album sem autenticacao")
        void shouldReturn401WhenCreatingWithoutAuthentication() {
            String json = """
                {
                    "titulo": "Novo Album",
                    "anoLancamento": 2024
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(401);
        }
    }

    // ====================
    // TESTES DE AUTORIZACAO
    // ====================

    @Nested
    @DisplayName("Autorizacao")
    class AutorizacaoTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Usuario comum NAO pode criar album (403)")
        void shouldReturn403WhenUserTriesToCreate() {
            String json = """
                {
                    "titulo": "Novo Album",
                    "anoLancamento": 2024,
                    "artistaIds": [1]
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(403);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Usuario comum NAO pode atualizar album (403)")
        void shouldReturn403WhenUserTriesToUpdate() {
            String json = """
                {
                    "titulo": "Album Atualizado",
                    "anoLancamento": 2024
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/api/v1/albuns/1")
                .then()
                .statusCode(403);
        }
    }

    // ====================
    // TESTES DE LISTAGEM
    // ====================

    @Nested
    @DisplayName("Listagem de Albuns")
    class ListagemTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve listar albuns com paginacao")
        void shouldListAlbunsWithPagination() {
            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content", notNullValue())
                .body("page", equalTo(0))
                .body("size", lessThanOrEqualTo(10))
                .body("totalElements", greaterThanOrEqualTo(0));
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve filtrar albuns por titulo")
        void shouldFilterAlbunsByTitulo() {
            given()
                .queryParam("titulo", "Harakiri")
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve filtrar albuns por ano de lancamento")
        void shouldFilterAlbunsByAnoLancamento() {
            given()
                .queryParam("anoLancamento", 2012)
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve filtrar albuns por artista")
        void shouldFilterAlbunsByArtista() {
            given()
                .queryParam("artistaId", 1)
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve ordenar albuns por titulo ASC")
        void shouldSortAlbunsByTituloAsc() {
            given()
                .queryParam("sortField", "titulo")
                .queryParam("sortDir", "asc")
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve ordenar albuns por titulo DESC")
        void shouldSortAlbunsByTituloDesc() {
            given()
                .queryParam("sortField", "titulo")
                .queryParam("sortDir", "desc")
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve ordenar albuns por ano de lancamento")
        void shouldSortAlbunsByAnoLancamento() {
            given()
                .queryParam("sortField", "anoLancamento")
                .queryParam("sortDir", "desc")
                .when()
                .get("/api/v1/albuns")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }
    }

    // ====================
    // TESTES DE BUSCA POR ID
    // ====================

    @Nested
    @DisplayName("Busca por ID")
    class BuscaPorIdTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar 404 para album inexistente")
        void shouldReturn404ForNonExistentAlbum() {
            given()
                .when()
                .get("/api/v1/albuns/999999")
                .then()
                .statusCode(404);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar detalhes do album existente")
        void shouldReturnAlbumDetails() {
            given()
                .when()
                .get("/api/v1/albuns/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("titulo", notNullValue());
        }
    }

    // ====================
    // TESTES DE CRIACAO (ADMIN)
    // ====================

    @Nested
    @DisplayName("Criacao de Albuns (ADMIN)")
    class CriacaoTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Admin deve criar album com artistas")
        void shouldCreateAlbumWithArtistas() {
            String json = """
                {
                    "titulo": "Album Teste %d",
                    "anoLancamento": 2024,
                    "descricao": "Descricao do album",
                    "artistaIds": [1]
                }
                """.formatted(System.currentTimeMillis());

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("anoLancamento", equalTo(2024));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 400 ao criar album sem artistas (validacao requer pelo menos 1)")
        void shouldReturn400WhenCreatingWithoutArtistas() {
            String json = """
                {
                    "titulo": "Album Sem Artistas %d",
                    "anoLancamento": 2023,
                    "descricao": "Album solo"
                }
                """.formatted(System.currentTimeMillis());

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 400 ao criar album sem titulo")
        void shouldReturn400WhenCreatingWithoutTitulo() {
            String json = """
                {
                    "anoLancamento": 2024
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar erro ao criar album com artista inexistente")
        void shouldReturn400WhenCreatingWithInvalidArtista() {
            String json = """
                {
                    "titulo": "Album com Artista Invalido",
                    "anoLancamento": 2024,
                    "artistaIds": [999999]
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/albuns")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(404)));
        }
    }

    // ====================
    // TESTES DE ATUALIZACAO (ADMIN)
    // ====================

    @Nested
    @DisplayName("Atualizacao de Albuns (ADMIN)")
    class AtualizacaoTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 404 ao atualizar album inexistente")
        void shouldReturn404WhenUpdatingNonExistent() {
            String json = """
                {
                    "titulo": "Titulo Atualizado",
                    "anoLancamento": 2024,
                    "artistaIds": [1]
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/api/v1/albuns/999999")
                .then()
                .statusCode(404);
        }
    }

    // ====================
    // TESTES DE IMAGENS
    // ====================

    @Nested
    @DisplayName("Imagens de Album")
    class ImagensTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve listar imagens do album")
        void shouldListAlbumImages() {
            given()
                .when()
                .get("/api/v1/albuns/1/imagens")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar 404 para imagens de album inexistente")
        void shouldReturn404ForImagesOfNonExistentAlbum() {
            given()
                .when()
                .get("/api/v1/albuns/999999/imagens")
                .then()
                .statusCode(404);
        }
    }
}
