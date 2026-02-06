package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.application.service.AuthService;
import br.gov.mt.seplag.presentation.dto.auth.LoginRequest;
import br.gov.mt.seplag.presentation.dto.auth.LoginResponse;
import br.gov.mt.seplag.presentation.dto.auth.RefreshTokenRequest;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Resource para autenticacao.
 * Endpoints publicos (sem necessidade de token) para login e renovacao.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticacao", description = "Endpoints de autenticacao JWT")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Operation(summary = "Realiza login",
               description = "Autentica o usuario e retorna tokens JWT. O access token tem validade de 5 minutos e o refresh token de 24 horas.")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados de entrada invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Credenciais invalidas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Renova token",
               description = "Renova o token de acesso usando o refresh token. O refresh token tem validade de 24 horas.")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Token renovado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados de entrada invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Refresh token invalido ou expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response refreshToken(@Valid RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return Response.ok(response).build();
    }
}
