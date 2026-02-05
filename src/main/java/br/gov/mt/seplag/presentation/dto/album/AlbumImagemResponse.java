package br.gov.mt.seplag.presentation.dto.album;

import br.gov.mt.seplag.domain.model.AlbumImagem;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para resposta de imagem de album.
 * A URL pre-assinada eh gerada pelo MinIO na hora da consulta.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Dados da imagem do album")
public class AlbumImagemResponse {

    @Schema(description = "ID da imagem", example = "1")
    private Long id;

    @Schema(description = "Nome original do arquivo", example = "capa-album.jpg")
    private String nomeOriginal;

    @Schema(description = "Tipo do conteudo", example = "image/jpeg")
    private String contentType;

    @Schema(description = "Tamanho em bytes", example = "102400")
    private Long tamanhoBytes;

    @Schema(description = "URL pre-assinada para download (valida por 30 minutos)")
    private String url;

    @Schema(description = "Data de criacao")
    private LocalDateTime createdAt;

    public AlbumImagemResponse() {
    }

    public static AlbumImagemResponse fromEntity(AlbumImagem imagem, String presignedUrl) {
        AlbumImagemResponse response = new AlbumImagemResponse();
        response.setId(imagem.getId());
        response.setNomeOriginal(imagem.getNomeOriginal());
        response.setContentType(imagem.getContentType());
        response.setTamanhoBytes(imagem.getTamanhoBytes());
        response.setUrl(presignedUrl);
        response.setCreatedAt(imagem.getCreatedAt());
        return response;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
