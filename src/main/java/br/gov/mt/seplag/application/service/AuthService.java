package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.AuthenticationException;
import br.gov.mt.seplag.domain.model.Usuario;
import br.gov.mt.seplag.domain.repository.UsuarioRepository;
import br.gov.mt.seplag.infrastructure.security.JwtTokenService;
import br.gov.mt.seplag.infrastructure.security.PasswordEncoder;
import br.gov.mt.seplag.presentation.dto.auth.LoginRequest;
import br.gov.mt.seplag.presentation.dto.auth.LoginResponse;
import br.gov.mt.seplag.presentation.dto.auth.RefreshTokenRequest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * Servico de autenticacao.
 * Gerencia login e renovacao de tokens JWT.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    PasswordEncoder passwordEncoder;

    @Inject
    JWTParser jwtParser;

    /**
     * Realiza o login do usuario.
     *
     * @param request dados de login
     * @return resposta com tokens
     * @throws AuthenticationException se as credenciais forem invalidas
     */
    public LoginResponse login(LoginRequest request) {
        LOG.infof("Tentativa de login para usuario: %s", request.getUsername());

        Usuario usuario = usuarioRepository.findByUsernameAndAtivo(request.getUsername())
            .orElseThrow(() -> new AuthenticationException("Credenciais invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            LOG.warnf("Senha incorreta para usuario: %s", request.getUsername());
            throw new AuthenticationException("Credenciais invalidas");
        }

        String accessToken = jwtTokenService.generateToken(usuario.getUsername(), usuario.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(usuario.getUsername(), usuario.getRole());

        LOG.infof("Login realizado com sucesso para usuario: %s", request.getUsername());

        return new LoginResponse(
            accessToken,
            refreshToken,
            jwtTokenService.getTokenLifespan(),
            usuario.getUsername(),
            usuario.getRole()
        );
    }

    /**
     * Renova o token de acesso usando o refresh token.
     *
     * @param request dados do refresh token
     * @return resposta com novo token
     * @throws AuthenticationException se o refresh token for invalido
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            JsonWebToken jwt = jwtParser.parse(request.getRefreshToken());

            String tokenType = jwt.getClaim("type");
            if (!"refresh".equals(tokenType)) {
                throw new AuthenticationException("Token invalido para renovacao");
            }

            String username = jwt.getSubject();
            Usuario usuario = usuarioRepository.findByUsernameAndAtivo(username)
                .orElseThrow(() -> new AuthenticationException("Usuario nao encontrado ou inativo"));

            String accessToken = jwtTokenService.generateToken(usuario.getUsername(), usuario.getRole());
            String newRefreshToken = jwtTokenService.generateRefreshToken(usuario.getUsername(), usuario.getRole());

            LOG.infof("Token renovado com sucesso para usuario: %s", username);

            return new LoginResponse(
                accessToken,
                newRefreshToken,
                jwtTokenService.getTokenLifespan(),
                usuario.getUsername(),
                usuario.getRole()
            );
        } catch (ParseException e) {
            LOG.error("Erro ao fazer parse do refresh token", e);
            throw new AuthenticationException("Refresh token invalido ou expirado");
        }
    }
}
