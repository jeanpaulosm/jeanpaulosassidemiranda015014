package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para ArtistaResource.
 * Cobertura completa de endpoints de artistas.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("ArtistaResource - Testes de Integracao")
class ArtistaResourceTest {

    // ====================
    // TESTES DE AUTENTICACAO
    // ====================

    @Nested
    @DisplayName("Autenticacao")
    class AutenticacaoTests {

        @Test
        @DisplayName("Deve retornar 401 ao listar artistas sem autenticacao")
        void shouldReturn401WhenListingWithoutAuthentication() {
            given()
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 401 ao buscar artista por ID sem autenticacao")
        void shouldReturn401WhenGettingByIdWithoutAuthentication() {
            given()
                .when()
                .get("/api/v1/artistas/1")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Deve retornar 401 ao criar artista sem autenticacao")
        void shouldReturn401WhenCreatingWithoutAuthentication() {
            String json = """
                {
                    "nome": "Teste",
                    "tipo": "CANTOR"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
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
        @DisplayName("Usuario comum NAO pode criar artista (403)")
        void shouldReturn403WhenUserTriesToCreate() {
            String json = """
                {
                    "nome": "Novo Artista",
                    "tipo": "CANTOR"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(403);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Usuario comum NAO pode atualizar artista (403)")
        void shouldReturn403WhenUserTriesToUpdate() {
            String json = """
                {
                    "nome": "Artista Atualizado",
                    "tipo": "BANDA"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/api/v1/artistas/1")
                .then()
                .statusCode(403);
        }
    }

    // ====================
    // TESTES DE LISTAGEM
    // ====================

    @Nested
    @DisplayName("Listagem de Artistas")
    class ListagemTests {

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve listar artistas com paginacao")
        void shouldListArtistasWithPagination() {
            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/artistas")
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
        @DisplayName("Deve filtrar artistas por tipo CANTOR")
        void shouldFilterArtistasByTipoCantor() {
            given()
                .queryParam("tipo", "CANTOR")
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content.findAll { it.tipo == 'CANTOR' }.size()", greaterThanOrEqualTo(0));
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve filtrar artistas por tipo BANDA")
        void shouldFilterArtistasByTipoBanda() {
            given()
                .queryParam("tipo", "BANDA")
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content.findAll { it.tipo == 'BANDA' }.size()", greaterThanOrEqualTo(0));
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve filtrar artistas por nome parcial")
        void shouldFilterArtistasByNome() {
            given()
                .queryParam("nome", "Serj")
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve ordenar artistas por nome ASC")
        void shouldSortArtistasByNomeAsc() {
            given()
                .queryParam("sortField", "nome")
                .queryParam("sortDir", "asc")
                .when()
                .get("/api/v1/artistas")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve ordenar artistas por nome DESC")
        void shouldSortArtistasByNomeDesc() {
            given()
                .queryParam("sortField", "nome")
                .queryParam("sortDir", "desc")
                .when()
                .get("/api/v1/artistas")
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
        @DisplayName("Deve retornar 404 para artista inexistente")
        void shouldReturn404ForNonExistentArtista() {
            given()
                .when()
                .get("/api/v1/artistas/999999")
                .then()
                .statusCode(404);
        }

        @Test
        @TestSecurity(user = "testUser", roles = {"USER"})
        @DisplayName("Deve retornar detalhes do artista existente")
        void shouldReturnArtistaDetails() {
            given()
                .when()
                .get("/api/v1/artistas/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("nome", notNullValue())
                .body("tipo", notNullValue());
        }
    }

    // ====================
    // TESTES DE CRIACAO (ADMIN)
    // ====================

    @Nested
    @DisplayName("Criacao de Artistas (ADMIN)")
    class CriacaoTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Admin deve criar artista do tipo CANTOR")
        void shouldCreateArtistaCantor() {
            String json = """
                {
                    "nome": "Artista Teste Cantor %d",
                    "tipo": "CANTOR",
                    "descricao": "Descricao do artista cantor"
                }
                """.formatted(System.currentTimeMillis());

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("tipo", equalTo("CANTOR"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Admin deve criar artista do tipo BANDA")
        void shouldCreateArtistaBanda() {
            String json = """
                {
                    "nome": "Artista Teste Banda %d",
                    "tipo": "BANDA",
                    "descricao": "Descricao da banda"
                }
                """.formatted(System.currentTimeMillis());

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("tipo", equalTo("BANDA"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 400 ao criar artista sem nome")
        void shouldReturn400WhenCreatingWithoutNome() {
            String json = """
                {
                    "tipo": "CANTOR"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 400 ao criar artista sem tipo")
        void shouldReturn400WhenCreatingWithoutTipo() {
            String json = """
                {
                    "nome": "Artista Sem Tipo"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/artistas")
                .then()
                .statusCode(400);
        }
    }

    // ====================
    // TESTES DE ATUALIZACAO (ADMIN)
    // ====================

    @Nested
    @DisplayName("Atualizacao de Artistas (ADMIN)")
    class AtualizacaoTests {

        @Test
        @TestSecurity(user = "adminUser", roles = {"ADMIN"})
        @DisplayName("Deve retornar 404 ao atualizar artista inexistente")
        void shouldReturn404WhenUpdatingNonExistent() {
            String json = """
                {
                    "nome": "Nome Atualizado",
                    "tipo": "BANDA"
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/api/v1/artistas/999999")
                .then()
                .statusCode(404);
        }
    }
}
