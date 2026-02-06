package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.domain.model.WebSocketTicket;
import br.gov.mt.seplag.infrastructure.security.WebSocketTicketService;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import br.gov.mt.seplag.presentation.dto.websocket.WebSocketTicketResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.Map;

/**
 * Resource para geracao de tickets de autenticacao WebSocket.
 *
 * Fluxo de autenticacao:
 * 1. Cliente chama POST /api/v1/ws/ticket com JWT valido
 * 2. Servidor retorna ticket temporario (30s, single-use)
 * 3. Cliente conecta ao WebSocket: ws://host/ws/albuns?ticket=xxx
 * 4. WebSocket valida e consome o ticket
 *
 * Esta abordagem e mais segura que passar o JWT na URL porque:
 * - O ticket tem vida curta (30s vs 5min do JWT)
 * - O ticket e single-use (nao pode ser reutilizado)
 * - O JWT nunca aparece em logs de URL
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/api/v1/ws")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "WebSocket", description = "Endpoints para autenticacao WebSocket")
@SecurityRequirement(name = "Bearer Authentication")
public class WebSocketTicketResource {

    private static final Logger LOG = Logger.getLogger(WebSocketTicketResource.class);

    @Inject
    WebSocketTicketService ticketService;

    @Inject
    JsonWebToken jwt;

    @Context
    UriInfo uriInfo;

    // =========================================================================
    // Geracao de Tickets
    // =========================================================================

    @POST
    @Path("/ticket")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
        summary = "Gera ticket para conexao WebSocket",
        description = "Gera um ticket temporario (30 segundos, single-use) para autenticacao no WebSocket. " +
            "O ticket deve ser usado na URL de conexao: ws://host/ws/albuns?ticket=xxx"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Ticket gerado com sucesso",
            content = @Content(schema = @Schema(implementation = WebSocketTicketResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Nao autenticado - JWT invalido ou ausente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response createTicket() {
        String username = jwt.getName();
        var roles = new HashSet<>(jwt.getGroups());

        LOG.infof("Gerando ticket WebSocket para usuario: %s", username);

        WebSocketTicket ticket = ticketService.createTicket(username, roles);

        // Constroi URL do WebSocket
        String baseUrl = uriInfo.getBaseUri().toString()
            .replace("http://", "ws://")
            .replace("https://", "wss://");
        String websocketUrl = baseUrl.replaceFirst("/api/v1/ws/?$", "") + "ws/albuns?ticket=" + ticket.getTicket();

        WebSocketTicketResponse response = WebSocketTicketResponse.builder()
            .ticket(ticket.getTicket())
            .expiresIn(ticketService.getTicketTtlSeconds())
            .websocketUrl(websocketUrl)
            .username(username)
            .build();

        return Response.ok(response).build();
    }

    // =========================================================================
    // Estatisticas (apenas ADMIN)
    // =========================================================================

    @GET
    @Path("/stats")
    @RolesAllowed({"ADMIN"})
    @Operation(
        summary = "Estatisticas do sistema de tickets",
        description = "Retorna estatisticas do servico de tickets WebSocket: " +
            "tickets ativos, criados, consumidos, expirados e rejeitados"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Estatisticas do sistema de tickets"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Sem permissao (requer role ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response getStats() {
        Map<String, Object> stats = ticketService.getStatistics();
        return Response.ok(stats).build();
    }
}
