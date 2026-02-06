package br.gov.mt.seplag.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Servico para geracao de tokens JWT.
 * Gera access tokens (curta duracao) e refresh tokens (24h).
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class JwtTokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "300")
    int tokenLifespan;

    /**
     * Gera um token JWT para o usuario.
     *
     * @param username o username do usuario
     * @param role     o papel do usuario
     * @return o token JWT assinado
     */
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofSeconds(tokenLifespan));

        return Jwt.issuer(issuer)
            .subject(username)
            .groups(Set.of(role))
            .claim("username", username)
            .issuedAt(now)
            .expiresAt(expiry)
            .sign();
    }

    /**
     * Gera um refresh token com duracao de 24 horas.
     *
     * @param username o username do usuario
     * @param role     o papel do usuario
     * @return o refresh token assinado
     */
    public String generateRefreshToken(String username, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofHours(24));

        return Jwt.issuer(issuer)
            .subject(username)
            .groups(Set.of(role))
            .claim("username", username)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiresAt(expiry)
            .sign();
    }

    /**
     * Retorna o tempo de expiracao do token em segundos.
     */
    public int getTokenLifespan() {
        return tokenLifespan;
    }
}
