package br.gov.mt.seplag.domain.exception;

/**
 * Excecao para erros de regra de negocio.
 * Lancada quando uma operacao viola alguma restricao do dominio.
 *
 * @author Jean Paulo Sassi de Miranda
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
