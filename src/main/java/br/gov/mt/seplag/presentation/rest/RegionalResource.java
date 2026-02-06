package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.application.service.RegionalService;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import br.gov.mt.seplag.presentation.dto.regional.RegionalResponse;
import br.gov.mt.seplag.presentation.dto.regional.SincronizacaoResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * Resource para gerenciamento de regionais.
 * Sincronizacao com API externa conforme regras do edital.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/api/v1/regionais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Regionais", description = "Endpoints para gerenciamento de regionais da Policia Civil")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionalResource {

    @Inject
    RegionalService regionalService;

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
        summary = "Lista regionais",
        description = "Retorna lista de regionais sincronizadas com a API externa. Por padrao retorna apenas ativas."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de regionais",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = RegionalResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Nao autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response listar(
        @Parameter(description = "Filtrar apenas regionais ativas (default: true)")
        @QueryParam("apenasAtivas") @DefaultValue("true") Boolean apenasAtivas
    ) {
        List<RegionalResponse> response = regionalService.listar(apenasAtivas);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
        summary = "Busca regional por ID",
        description = "Retorna uma regional especifica pelo seu ID (conforme API externa)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Regional encontrada",
            content = @Content(schema = @Schema(implementation = RegionalResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Regional nao encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response buscarPorId(
        @Parameter(description = "ID da regional", required = true)
        @PathParam("id") Integer id
    ) {
        RegionalResponse response = regionalService.buscarPorId(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/estatisticas")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(
        summary = "Estatisticas das regionais",
        description = "Retorna estatisticas sobre as regionais (total, ativas, inativas)"
    )
    @APIResponse(
        responseCode = "200",
        description = "Estatisticas das regionais"
    )
    public Response estatisticas() {
        Map<String, Long> stats = regionalService.getEstatisticas();
        return Response.ok(stats).build();
    }

    @POST
    @Path("/sincronizar")
    @RolesAllowed({"ADMIN"})
    @Operation(
        summary = "Sincroniza regionais com API externa",
        description = "Sincroniza conforme edital: " +
            "1) Novo no endpoint -> INSERT; " +
            "2) Ausente no endpoint -> UPDATE ativo=false; " +
            "3) Atributo alterado -> UPDATE nome."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Sincronizacao realizada com sucesso",
            content = @Content(schema = @Schema(implementation = SincronizacaoResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro na sincronizacao",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "403",
            description = "Sem permissao (requer role ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response sincronizar() {
        SincronizacaoResponse response = regionalService.sincronizar();
        return Response.ok(response).build();
    }
}
