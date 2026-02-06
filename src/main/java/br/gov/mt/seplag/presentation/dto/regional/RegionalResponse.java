package br.gov.mt.seplag.presentation.dto.regional;

import br.gov.mt.seplag.domain.model.Regional;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para resposta de regional.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados da regional conforme estrutura do edital")
public class RegionalResponse {

    @Schema(description = "ID da regional (conforme API externa)", example = "1")
    private Integer id;

    @Schema(description = "Nome da regional", example = "Regional de Cuiaba")
    private String nome;

    @Schema(description = "Indica se a regional esta ativa", example = "true")
    private Boolean ativo;

    public RegionalResponse() {
    }

    public RegionalResponse(Integer id, String nome, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.ativo = ativo;
    }

    public static RegionalResponse fromEntity(Regional regional) {
        return new RegionalResponse(
            regional.getId(),
            regional.getNome(),
            regional.getAtivo()
        );
    }

    public static List<RegionalResponse> fromEntities(List<Regional> regionais) {
        return regionais.stream()
            .map(RegionalResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
