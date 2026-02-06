package br.gov.mt.seplag.presentation.rest;

import br.gov.mt.seplag.application.service.AlbumImagemService;
import br.gov.mt.seplag.application.service.AlbumService;
import br.gov.mt.seplag.presentation.dto.album.*;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;

/**
 * Resource para gerenciamento de albuns.
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

    @Inject
    AlbumImagemService albumImagemService;

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
    @Operation(summary = "Busca album por ID", description = "Retorna detalhes do album incluindo artistas e imagens com URLs pre-assinadas")
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

    @POST
    @Path("/{id}/imagens")
    @RolesAllowed({"ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload de imagens",
               description = "Faz upload de imagens para o album. Aceita JPEG, PNG, GIF e WebP. Tamanho maximo: 10MB por arquivo. Valida magic numbers para prevenir falsificacao de content-type. Requer role ADMIN.")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Imagens enviadas com sucesso",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = AlbumImagemResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Erro no upload - tipo nao permitido, arquivo muito grande ou magic number invalido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response uploadImagens(
        @Parameter(description = "ID do album", required = true)
        @PathParam("id") Long id,
        @RestForm("files") List<FileUpload> files
    ) {
        List<AlbumImagemResponse> response = albumImagemService.uploadImagens(id, files);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}/imagens")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Lista imagens do album", description = "Retorna lista de imagens com presigned URLs (30 min expiracao)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de imagens",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = AlbumImagemResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response listarImagens(
        @Parameter(description = "ID do album", required = true)
        @PathParam("id") Long id
    ) {
        List<AlbumImagemResponse> response = albumImagemService.listarImagens(id);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Remove album", description = "Remove um album existente, incluindo suas imagens do MinIO. Requer role ADMIN.")
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

    @DELETE
    @Path("/{albumId}/imagens/{imagemId}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Remove imagem do album", description = "Remove uma imagem especifica do album")
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "Imagem removida com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Imagem nao encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response removerImagem(
        @Parameter(description = "ID do album", required = true)
        @PathParam("albumId") Long albumId,
        @Parameter(description = "ID da imagem", required = true)
        @PathParam("imagemId") Long imagemId
    ) {
        albumImagemService.deletarImagem(imagemId);
        return Response.noContent().build();
    }
}
