package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.application.service.ArtistaService;
import br.gov.mt.seplag.domain.model.TipoArtista;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaDetailResponse;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaRequest;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaResponse;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Resource REST para gerenciamento de artistas.
 * Todos os endpoints requerem autenticacao JWT.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/api/v1/artistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artistas", description = "Endpoints para gerenciamento de artistas")
@SecurityRequirement(name = "Bearer Authentication")
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Lista artistas", description = "Retorna lista paginada de artistas com filtros e ordenacao")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de artistas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response listar(
        @Parameter(description = "Filtrar por nome (parcial)")
        @QueryParam("nome") String nome,

        @Parameter(description = "Filtrar por tipo")
        @QueryParam("tipo") TipoArtista tipo,

        @Parameter(description = "Campo para ordenacao", example = "nome")
        @QueryParam("sortField") @DefaultValue("nome") String sortField,

        @Parameter(description = "Direcao da ordenacao (asc/desc)", example = "asc")
        @QueryParam("sortDir") @DefaultValue("asc") String sortDir,

        @Parameter(description = "Numero da pagina (0-based)", example = "0")
        @QueryParam("page") @DefaultValue("0") int page,

        @Parameter(description = "Tamanho da pagina", example = "10")
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        PageResponse<ArtistaResponse> response = artistaService.listar(nome, tipo, sortField, sortDir, page, size);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Busca artista por ID", description = "Retorna detalhes do artista")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Artista encontrado",
            content = @Content(schema = @Schema(implementation = ArtistaDetailResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Artista nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response buscarPorId(
        @Parameter(description = "ID do artista", required = true)
        @PathParam("id") Long id
    ) {
        ArtistaDetailResponse response = artistaService.buscarPorId(id);
        return Response.ok(response).build();
    }

    @POST
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Cria artista", description = "Cria um novo artista no catalogo. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Artista criado com sucesso",
            content = @Content(schema = @Schema(implementation = ArtistaResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response criar(@Valid ArtistaRequest request) {
        ArtistaResponse response = artistaService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Atualiza artista", description = "Atualiza um artista existente. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Artista atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = ArtistaResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Artista nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response atualizar(
        @Parameter(description = "ID do artista", required = true)
        @PathParam("id") Long id,
        @Valid ArtistaRequest request
    ) {
        ArtistaResponse response = artistaService.atualizar(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Remove artista", description = "Remove um artista do catalogo. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Artista removido com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Artista nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response remover(
        @Parameter(description = "ID do artista", required = true)
        @PathParam("id") Long id
    ) {
        artistaService.remover(id);
        return Response.noContent().build();
    }
}
