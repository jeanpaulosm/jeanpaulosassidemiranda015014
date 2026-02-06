package br.gov.mt.seplag.presentation.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integracao para o endpoint DELETE de Artistas.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class ArtistaDeleteResourceTest {

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        given()
            .when()
            .delete("/api/v1/artistas/999")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void shouldReturn403WhenUserTriesToDelete() {
        given()
            .when()
            .delete("/api/v1/artistas/999")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldReturn404WhenArtistNotFound() {
        given()
            .when()
            .delete("/api/v1/artistas/99999")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldReturn400WhenArtistHasAlbuns() {
        // Os artistas iniciais (Serj Tankian, Mike Shinoda, etc) possuem albuns associados
        // ID 1 = Serj Tankian (tem albuns)
        given()
            .when()
            .delete("/api/v1/artistas/1")
            .then()
            .statusCode(400)
            .body("message", containsString("album"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void shouldDeleteArtistWithoutAlbuns() {
        // Primeiro, cria um artista sem albuns
        String json = """
            {
                "nome": "Artista Temporario Para Deletar",
                "tipo": "CANTOR",
                "descricao": "Sera deletado"
            }
            """;

        Integer artistaId = given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post("/api/v1/artistas")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Agora deleta o artista
        given()
            .when()
            .delete("/api/v1/artistas/" + artistaId)
            .then()
            .statusCode(204);

        // Verifica que foi deletado
        given()
            .when()
            .get("/api/v1/artistas/" + artistaId)
            .then()
            .statusCode(404);
    }
}
