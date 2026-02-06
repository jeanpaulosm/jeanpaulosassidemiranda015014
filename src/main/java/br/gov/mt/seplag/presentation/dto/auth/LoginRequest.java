package br.gov.mt.seplag.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para requisicao de login.
 * Inclui validacoes de seguranca para prevenir ataques.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Credenciais para autenticacao na API")
public class LoginRequest {

    @NotBlank(message = "Nome de usuario e obrigatorio")
    @Size(min = 3, max = 50, message = "Nome de usuario deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Nome de usuario contem caracteres invalidos")
    @Schema(description = "Nome de usuario para autenticacao", example = "admin", required = true, minLength = 3, maxLength = 50)
    private String username;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Schema(description = "Senha do usuario", example = "admin123", required = true, minLength = 6, maxLength = 100)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
