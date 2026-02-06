package br.gov.mt.seplag.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para requisicao de renovacao de token.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados para renovacao do token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token e obrigatorio")
    @Schema(description = "Token de renovacao", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
