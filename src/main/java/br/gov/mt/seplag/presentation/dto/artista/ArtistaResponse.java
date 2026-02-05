package br.gov.mt.seplag.presentation.dto.artista;

import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para resposta de artista.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados do artista")
public class ArtistaResponse {

    @Schema(description = "ID do artista", example = "1")
    private Long id;

    @Schema(description = "Nome do artista", example = "Serj Tankian")
    private String nome;

    @Schema(description = "Tipo do artista", example = "CANTOR")
    private TipoArtista tipo;

    @Schema(description = "Descricao do artista", example = "Cantor e compositor armeno-americano")
    private String descricao;

    @Schema(description = "Data de criacao")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualizacao")
    private LocalDateTime updatedAt;

    public ArtistaResponse() {
    }

    public static ArtistaResponse fromEntity(Artista artista) {
        ArtistaResponse response = new ArtistaResponse();
        response.setId(artista.getId());
        response.setNome(artista.getNome());
        response.setTipo(artista.getTipo());
        response.setDescricao(artista.getDescricao());
        response.setCreatedAt(artista.getCreatedAt());
        response.setUpdatedAt(artista.getUpdatedAt());
        return response;
    }

    public static List<ArtistaResponse> fromEntities(List<Artista> artistas) {
        return artistas.stream()
            .map(ArtistaResponse::fromEntity)
            .collect(Collectors.toList());
    }

    // Getters e Setters
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
