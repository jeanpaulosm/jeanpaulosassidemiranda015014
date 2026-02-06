package br.gov.mt.seplag.infrastructure.security;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para JwtTokenService.
 * Valida a geracao de tokens JWT conforme edital:
 * "Autenticacao JWT com expiracao a cada 5 minutos e possibilidade de renovacao"
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class JwtTokenServiceTest {

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    JWTParser jwtParser;

    @Test
    void shouldGenerateValidAccessToken() throws ParseException {
        String username = "testuser";
        String role = "USER";

        String token = jwtTokenService.generateToken(username, role);

        assertNotNull(token, "Token nao deve ser nulo");
        assertFalse(token.isEmpty(), "Token nao deve ser vazio");

        // Verifica estrutura basica do JWT (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT deve ter 3 partes separadas por ponto");
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String username = "testuser";
        String role = "ADMIN";

        String refreshToken = jwtTokenService.generateRefreshToken(username, role);

        assertNotNull(refreshToken, "Refresh token nao deve ser nulo");
        assertFalse(refreshToken.isEmpty(), "Refresh token nao deve ser vazio");

        // Verifica estrutura basica do JWT
        String[] parts = refreshToken.split("\\.");
        assertEquals(3, parts.length, "JWT deve ter 3 partes separadas por ponto");
    }

    @Test
    void shouldReturnCorrectTokenLifespan() {
        // Conforme edital: "expiracao a cada 5 minutos" = 300 segundos
        int lifespan = jwtTokenService.getTokenLifespan();
        assertEquals(300, lifespan, "Token deve expirar em 300 segundos (5 minutos) conforme edital");
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtTokenService.generateToken("user1", "USER");
        String token2 = jwtTokenService.generateToken("user2", "USER");

        assertNotEquals(token1, token2, "Tokens de usuarios diferentes devem ser diferentes");
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentRoles() {
        String tokenUser = jwtTokenService.generateToken("testuser", "USER");
        String tokenAdmin = jwtTokenService.generateToken("testuser", "ADMIN");

        assertNotEquals(tokenUser, tokenAdmin, "Tokens com roles diferentes devem ser diferentes");
    }

    @Test
    void shouldGenerateDifferentAccessAndRefreshTokens() {
        String username = "testuser";
        String role = "USER";

        String accessToken = jwtTokenService.generateToken(username, role);
        String refreshToken = jwtTokenService.generateRefreshToken(username, role);

        assertNotEquals(accessToken, refreshToken,
            "Access token e refresh token devem ser diferentes");
    }

    @Test
    void shouldGenerateUniqueTokensOnEachCall() throws InterruptedException {
        String username = "testuser";
        String role = "USER";

        String token1 = jwtTokenService.generateToken(username, role);
        // Pequena pausa para garantir timestamp diferente
        Thread.sleep(10);
        String token2 = jwtTokenService.generateToken(username, role);

        assertNotEquals(token1, token2,
            "Tokens gerados em momentos diferentes devem ser unicos");
    }

    @Test
    void accessTokenShouldExpireBeforeRefreshToken() {
        // Access token: 5 minutos (300 segundos)
        // Refresh token: 24 horas (86400 segundos)
        int accessTokenLifespan = jwtTokenService.getTokenLifespan();

        // Verifica que o access token tem expiracao menor que 24 horas
        assertTrue(accessTokenLifespan < 86400,
            "Access token deve expirar antes do refresh token");
        assertEquals(300, accessTokenLifespan,
            "Access token deve ter 5 minutos de validade conforme edital");
    }
}
