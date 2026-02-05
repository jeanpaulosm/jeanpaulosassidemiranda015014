package br.gov.mt.seplag.presentation.dto.artista;

import br.gov.mt.seplag.domain.model.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para criacao/atualizacao de artista.
 * Inclui validacoes completas conforme regras de negocio.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados para criacao ou atualizacao de artista")
public class ArtistaRequest {

    @NotBlank(message = "Nome do artista e obrigatorio")
    @Size(min = 2, max = 200, message = "Nome deve ter entre 2 e 200 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s'.\\-&]+$", message = "Nome contem caracteres invalidos")
    @Schema(description = "Nome do artista", example = "Serj Tankian", required = true, minLength = 2, maxLength = 200)
    private String nome;

    @NotNull(message = "Tipo do artista e obrigatorio (CANTOR ou BANDA)")
    @Schema(description = "Tipo do artista: CANTOR para artistas solo, BANDA para grupos musicais",
            example = "CANTOR", required = true, enumeration = {"CANTOR", "BANDA"})
    private TipoArtista tipo;

    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres")
    @Schema(description = "Descricao biografica do artista", example = "Cantor e compositor armeno-americano, vocalista do System of a Down", maxLength = 2000)
    private String descricao;

    public ArtistaRequest() {
    }

    public ArtistaRequest(String nome, TipoArtista tipo, String descricao) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
