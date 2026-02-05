package br.gov.mt.seplag.presentation.dto.artista;

import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para resposta simplificada de artista.
 * Usado quando queremos exibir o artista dentro da resposta de album, por exemplo.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados simplificados do artista")
public class ArtistaSimpleResponse {

    @Schema(description = "ID do artista", example = "1")
    private Long id;

    @Schema(description = "Nome do artista", example = "Serj Tankian")
    private String nome;

    @Schema(description = "Tipo do artista", example = "CANTOR")
    private TipoArtista tipo;

    public ArtistaSimpleResponse() {
    }

    public static ArtistaSimpleResponse fromEntity(Artista artista) {
        ArtistaSimpleResponse response = new ArtistaSimpleResponse();
        response.setId(artista.getId());
        response.setNome(artista.getNome());
        response.setTipo(artista.getTipo());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoArtista getTipo() {
        return tipo;
    }

    public void setTipo(TipoArtista tipo) {
        this.tipo = tipo;
    }
}
