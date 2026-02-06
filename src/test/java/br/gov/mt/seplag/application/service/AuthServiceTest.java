package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.AuthenticationException;
import br.gov.mt.seplag.domain.model.Usuario;
import br.gov.mt.seplag.domain.repository.UsuarioRepository;
import br.gov.mt.seplag.infrastructure.security.JwtTokenService;
import br.gov.mt.seplag.infrastructure.security.PasswordEncoder;
import br.gov.mt.seplag.presentation.dto.auth.LoginRequest;
import br.gov.mt.seplag.presentation.dto.auth.LoginResponse;
import br.gov.mt.seplag.presentation.dto.auth.RefreshTokenRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para AuthService.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("AuthService - Testes Unitarios")
class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    UsuarioRepository usuarioRepository;

    @InjectMock
    JwtTokenService jwtTokenService;

    @InjectMock
    PasswordEncoder passwordEncoder;

    @InjectMock
    JWTParser jwtParser;

    private Usuario criarUsuario(String username, String password, String role) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setRole(role);
        usuario.setAtivo(true);
        usuario.setCreatedAt(LocalDateTime.now());
        return usuario;
    }

    private LoginRequest criarLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    // ====================
    // TESTES DE LOGIN
    // ====================

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void shouldLoginSuccessfully() {
            // Arrange
            String username = "admin";
            String password = "senha123";
            String hashedPassword = "$2a$12$hashedpassword";
            Usuario usuario = criarUsuario(username, hashedPassword, "ADMIN");
            LoginRequest request = criarLoginRequest(username, password);

            when(usuarioRepository.findByUsernameAndAtivo(username)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
            when(jwtTokenService.generateToken(username, "ADMIN")).thenReturn("access_token");
            when(jwtTokenService.generateRefreshToken(username, "ADMIN")).thenReturn("refresh_token");
            when(jwtTokenService.getTokenLifespan()).thenReturn(300);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access_token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
            assertThat(response.getExpiresIn()).isEqualTo(300);
            assertThat(response.getUsername()).isEqualTo(username);
            assertThat(response.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando usuario nao existe")
        void shouldThrowAuthenticationExceptionWhenUserNotExists() {
            // Arrange
            LoginRequest request = criarLoginRequest("usuario_inexistente", "senha");
            when(usuarioRepository.findByUsernameAndAtivo("usuario_inexistente")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciais invalidas");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando senha incorreta")
        void shouldThrowAuthenticationExceptionWhenPasswordIncorrect() {
            // Arrange
            String username = "admin";
            Usuario usuario = criarUsuario(username, "$2a$12$hashedpassword", "ADMIN");
            LoginRequest request = criarLoginRequest(username, "senha_errada");

            when(usuarioRepository.findByUsernameAndAtivo(username)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha_errada", "$2a$12$hashedpassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Credenciais invalidas");

            verify(jwtTokenService, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve gerar tokens para usuario USER")
        void shouldGenerateTokensForUserRole() {
            // Arrange
            String username = "user";
            String password = "senha123";
            Usuario usuario = criarUsuario(username, "$2a$12$hash", "USER");
            LoginRequest request = criarLoginRequest(username, password);

            when(usuarioRepository.findByUsernameAndAtivo(username)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(password, "$2a$12$hash")).thenReturn(true);
            when(jwtTokenService.generateToken(username, "USER")).thenReturn("user_token");
            when(jwtTokenService.generateRefreshToken(username, "USER")).thenReturn("user_refresh");
            when(jwtTokenService.getTokenLifespan()).thenReturn(300);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response.getRole()).isEqualTo("USER");
            verify(jwtTokenService).generateToken(username, "USER");
        }
    }

    // ====================
    // TESTES DE REFRESH TOKEN
    // ====================

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Deve renovar token com sucesso")
        void shouldRefreshTokenSuccessfully() throws ParseException {
            // Arrange
            String username = "admin";
            Usuario usuario = criarUsuario(username, "$2a$12$hash", "ADMIN");
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid_refresh_token");

            JsonWebToken jwt = mock(JsonWebToken.class);
            when(jwt.getClaim("type")).thenReturn("refresh");
            when(jwt.getSubject()).thenReturn(username);

            when(jwtParser.parse("valid_refresh_token")).thenReturn(jwt);
            when(usuarioRepository.findByUsernameAndAtivo(username)).thenReturn(Optional.of(usuario));
            when(jwtTokenService.generateToken(username, "ADMIN")).thenReturn("new_access_token");
            when(jwtTokenService.generateRefreshToken(username, "ADMIN")).thenReturn("new_refresh_token");
            when(jwtTokenService.getTokenLifespan()).thenReturn(300);

            // Act
            LoginResponse response = authService.refreshToken(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new_access_token");
            assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando token nao e do tipo refresh")
        void shouldThrowAuthenticationExceptionWhenTokenTypeIsNotRefresh() throws ParseException {
            // Arrange
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("access_token");

            JsonWebToken jwt = mock(JsonWebToken.class);
            when(jwt.getClaim("type")).thenReturn("access"); // Tipo errado

            when(jwtParser.parse("access_token")).thenReturn(jwt);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Token invalido para renovacao");
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando usuario nao existe mais")
        void shouldThrowAuthenticationExceptionWhenUserNoLongerExists() throws ParseException {
            // Arrange
            String username = "usuario_deletado";
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("valid_refresh_token");

            JsonWebToken jwt = mock(JsonWebToken.class);
            when(jwt.getClaim("type")).thenReturn("refresh");
            when(jwt.getSubject()).thenReturn(username);

            when(jwtParser.parse("valid_refresh_token")).thenReturn(jwt);
            when(usuarioRepository.findByUsernameAndAtivo(username)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Usuario nao encontrado ou inativo");
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando refresh token invalido")
        void shouldThrowAuthenticationExceptionWhenRefreshTokenInvalid() throws ParseException {
            // Arrange
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("invalid_token");

            when(jwtParser.parse("invalid_token")).thenThrow(new ParseException("Invalid token"));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Refresh token invalido ou expirado");
        }

        @Test
        @DisplayName("Deve lancar AuthenticationException quando refresh token expirado")
        void shouldThrowAuthenticationExceptionWhenRefreshTokenExpired() throws ParseException {
            // Arrange
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("expired_token");

            when(jwtParser.parse("expired_token")).thenThrow(new ParseException("Token expired"));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Refresh token invalido ou expirado");
        }
    }
}
