package br.gov.mt.seplag.domain.exception;

/**
 * Excecao para recursos nao encontrados.
 * Usada quando uma busca por ID nao retorna resultado.
 *
 * @author Jean Paulo Sassi de Miranda
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s com ID %d nao encontrado", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s com %s '%s' nao encontrado", resourceName, field, value));
    }
}
