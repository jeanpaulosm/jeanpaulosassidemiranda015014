package br.gov.mt.seplag.presentation.dto.common;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para resposta de erro padronizada.
 * Todas as excecoes da API devolvem nesse formato pra facilitar o consumo pelo frontend.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Resposta de erro")
public class ErrorResponse {

    @Schema(description = "Codigo HTTP do erro", example = "400")
    private int status;

    @Schema(description = "Tipo do erro", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem do erro", example = "Dados invalidos")
    private String message;

    @Schema(description = "Caminho da requisicao", example = "/api/v1/artistas")
    private String path;

    @Schema(description = "Data e hora do erro")
    private LocalDateTime timestamp;

    @Schema(description = "Lista de erros de validacao")
    private List<FieldError> fieldErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this(status, error, message, path);
        this.fieldErrors = fieldErrors;
    }

    // Getters e Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    @Schema(description = "Erro de campo de validacao")
    public static class FieldError {

        @Schema(description = "Nome do campo", example = "nome")
        private String field;

        @Schema(description = "Mensagem de erro", example = "Nome e obrigatorio")
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
