package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.application.service.AlbumService;
import br.gov.mt.seplag.presentation.dto.album.AlbumDetailResponse;
import br.gov.mt.seplag.presentation.dto.album.AlbumRequest;
import br.gov.mt.seplag.presentation.dto.album.AlbumResponse;
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
 * Resource REST para gerenciamento de albuns.
 * Todos os endpoints requerem autenticacao JWT.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/api/v1/albuns")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Albuns", description = "Endpoints para gerenciamento de albuns")
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Lista albuns", description = "Retorna lista paginada de albuns com filtros e ordenacao")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de albuns retornada com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response listar(
        @Parameter(description = "Filtrar por titulo (parcial)")
        @QueryParam("titulo") String titulo,

        @Parameter(description = "Filtrar por ano de lancamento")
        @QueryParam("anoLancamento") Integer anoLancamento,

        @Parameter(description = "Filtrar por ID do artista")
        @QueryParam("artistaId") Long artistaId,

        @Parameter(description = "Campo para ordenacao", example = "titulo")
        @QueryParam("sortField") @DefaultValue("titulo") String sortField,

        @Parameter(description = "Direcao da ordenacao (asc/desc)", example = "asc")
        @QueryParam("sortDir") @DefaultValue("asc") String sortDir,

        @Parameter(description = "Numero da pagina (0-based)", example = "0")
        @QueryParam("page") @DefaultValue("0") int page,

        @Parameter(description = "Tamanho da pagina", example = "10")
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        PageResponse<AlbumResponse> response = albumService.listar(titulo, anoLancamento, artistaId, sortField, sortDir, page, size);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Busca album por ID", description = "Retorna detalhes do album incluindo artistas e imagens")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Album encontrado",
            content = @Content(schema = @Schema(implementation = AlbumDetailResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response buscarPorId(
        @Parameter(description = "ID do album", required = true)
        @PathParam("id") Long id
    ) {
        AlbumDetailResponse response = albumService.buscarPorId(id);
        return Response.ok(response).build();
    }

    @POST
    @RolesAllowed({"ADMIN"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cria album", description = "Cria um novo album. Requer pelo menos um artista associado. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Album criado com sucesso",
            content = @Content(schema = @Schema(implementation = AlbumResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response criar(@Valid AlbumRequest request) {
        AlbumResponse response = albumService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza album", description = "Atualiza um album existente. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Album atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = AlbumResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response atualizar(
        @Parameter(description = "ID do album", required = true)
        @PathParam("id") Long id,
        @Valid AlbumRequest request
    ) {
        AlbumResponse response = albumService.atualizar(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Remove album", description = "Remove um album e seus relacionamentos. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Album removido com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response remover(
        @Parameter(description = "ID do album", required = true)
        @PathParam("id") Long id
    ) {
        albumService.remover(id);
        return Response.noContent().build();
    }
}
