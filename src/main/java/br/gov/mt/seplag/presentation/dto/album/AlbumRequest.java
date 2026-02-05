package br.gov.mt.seplag.presentation.dto.album;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * DTO para criacao/atualizacao de album.
 * Inclui validacoes completas conforme regras de negocio.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados para criacao ou atualizacao de album")
public class AlbumRequest {

    @NotBlank(message = "Titulo do album e obrigatorio")
    @Size(min = 1, max = 300, message = "Titulo deve ter entre 1 e 300 caracteres")
    @Schema(description = "Titulo do album", example = "Harakiri", required = true, minLength = 1, maxLength = 300)
    private String titulo;

    @Min(value = 1900, message = "Ano de lancamento deve ser maior ou igual a 1900")
    @Max(value = 2100, message = "Ano de lancamento deve ser menor ou igual a 2100")
    @Schema(description = "Ano de lancamento do album", example = "2012", minimum = "1900", maximum = "2100")
    private Integer anoLancamento;

    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres")
    @Schema(description = "Descricao do album", example = "Terceiro album de estudio de Serj Tankian", maxLength = 2000)
    private String descricao;

    @NotEmpty(message = "Pelo menos um artista deve ser associado ao album")
    @Size(max = 50, message = "Um album pode ter no maximo 50 artistas")
    @Schema(description = "IDs dos artistas do album. Pelo menos um artista e obrigatorio.", example = "[1, 2]", required = true)
    private List<Long> artistaIds;

    public AlbumRequest() {
    }

    public AlbumRequest(String titulo, Integer anoLancamento, String descricao, List<Long> artistaIds) {
        this.titulo = titulo;
        this.anoLancamento = anoLancamento;
        this.descricao = descricao;
        this.artistaIds = artistaIds;
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

    public List<Long> getArtistaIds() {
        return artistaIds;
    }

    public void setArtistaIds(List<Long> artistaIds) {
        this.artistaIds = artistaIds;
    }
}
