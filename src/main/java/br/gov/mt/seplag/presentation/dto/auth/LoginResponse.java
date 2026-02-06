package br.gov.mt.seplag.presentation.dto.auth;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para resposta de login com tokens JWT.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Resposta de autenticacao com tokens")
public class LoginResponse {

    @Schema(description = "Token de acesso JWT", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token para renovacao", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "Tipo do token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Tempo de expiracao em segundos", example = "300")
    private int expiresIn;

    @Schema(description = "Username do usuario autenticado", example = "admin")
    private String username;

    @Schema(description = "Papel do usuario", example = "ADMIN")
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, int expiresIn, String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
