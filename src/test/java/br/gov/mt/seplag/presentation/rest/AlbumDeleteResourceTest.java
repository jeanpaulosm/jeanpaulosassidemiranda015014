package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para o endpoint DELETE de Albuns.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class AlbumDeleteResourceTest {

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        given()
            .when()
            .delete("/api/v1/albuns/999")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void shouldReturn403WhenUserTriesToDelete() {
        given()
            .when()
            .delete("/api/v1/albuns/999")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldReturn404WhenAlbumNotFound() {
        given()
            .when()
            .delete("/api/v1/albuns/99999")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldDeleteAlbumSuccessfully() {
        // Primeiro, cria um album para deletar
        String json = """
            {
                "titulo": "Album Temporario Para Deletar",
                "anoLancamento": 2024,
                "descricao": "Sera deletado",
                "artistaIds": [1]
            }
            """;

        Integer albumId = given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/albuns")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deleta o album
        given()
            .when()
            .delete("/api/v1/albuns/" + albumId)
            .then()
            .statusCode(204);

        // Verifica que foi deletado
        given()
            .when()
            .get("/api/v1/albuns/" + albumId)
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldDeleteAlbumAndUnlinkFromArtists() {
        // Cria um album vinculado a um artista
        String json = """
            {
                "titulo": "Album Para Teste de Desvinculacao",
                "anoLancamento": 2024,
                "artistaIds": [1]
            }
            """;

        Integer albumId = given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/albuns")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Deleta o album
        given()
            .when()
            .delete("/api/v1/albuns/" + albumId)
            .then()
            .statusCode(204);

        // O artista deve continuar existindo
        given()
            .when()
            .get("/api/v1/artistas/1")
            .then()
            .statusCode(200);
    }
}
