package br.gov.mt.seplag.presentation.dto.album;

import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaSimpleResponse;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para resposta detalhada de album, incluindo imagens.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados detalhados do album")
public class AlbumDetailResponse {

    @Schema(description = "ID do album", example = "1")
    private Long id;

    @Schema(description = "Titulo do album", example = "Harakiri")
    private String titulo;

    @Schema(description = "Ano de lancamento", example = "2012")
    private Integer anoLancamento;

    @Schema(description = "Descricao do album")
    private String descricao;

    @Schema(description = "Artistas do album")
    private List<ArtistaSimpleResponse> artistas;

    @Schema(description = "Imagens do album com URLs pre-assinadas")
    private List<AlbumImagemResponse> imagens;

    @Schema(description = "Data de criacao")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualizacao")
    private LocalDateTime updatedAt;

    public AlbumDetailResponse() {
    }

    public static AlbumDetailResponse fromEntity(Album album, List<AlbumImagemResponse> imagensComUrls) {
        AlbumDetailResponse response = new AlbumDetailResponse();
        response.setId(album.getId());
        response.setTitulo(album.getTitulo());
        response.setAnoLancamento(album.getAnoLancamento());
        response.setDescricao(album.getDescricao());
        response.setCreatedAt(album.getCreatedAt());
        response.setUpdatedAt(album.getUpdatedAt());
        response.setImagens(imagensComUrls);

        if (album.getArtistas() != null) {
            response.setArtistas(album.getArtistas().stream()
                .map(ArtistaSimpleResponse::fromEntity)
                .collect(Collectors.toList()));
        }

        return response;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Integer anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<ArtistaSimpleResponse> getArtistas() {
        return artistas;
    }

    public void setArtistas(List<ArtistaSimpleResponse> artistas) {
        this.artistas = artistas;
    }

    public List<AlbumImagemResponse> getImagens() {
        return imagens;
    }

    public void setImagens(List<AlbumImagemResponse> imagens) {
        this.imagens = imagens;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
