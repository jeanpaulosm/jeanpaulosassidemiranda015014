package br.gov.mt.seplag.presentation.dto.album;

import br.gov.mt.seplag.domain.model.Album;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para resposta simplificada de album.
 * Usado quando queremos exibir o album dentro da resposta de artista, por exemplo.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados simplificados do album")
public class AlbumSimpleResponse {

    @Schema(description = "ID do album", example = "1")
    private Long id;

    @Schema(description = "Titulo do album", example = "Harakiri")
    private String titulo;

    @Schema(description = "Ano de lancamento", example = "2012")
    private Integer anoLancamento;

    public AlbumSimpleResponse() {
    }

    public static AlbumSimpleResponse fromEntity(Album album) {
        AlbumSimpleResponse response = new AlbumSimpleResponse();
        response.setId(album.getId());
        response.setTitulo(album.getTitulo());
        response.setAnoLancamento(album.getAnoLancamento());
        return response;
    }

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
}
