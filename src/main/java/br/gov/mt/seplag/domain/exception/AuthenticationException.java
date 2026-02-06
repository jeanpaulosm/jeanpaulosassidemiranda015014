package br.gov.mt.seplag.domain.exception;

/**
 * Excecao para erros de autenticacao (login invalido, token expirado, etc).
 *
 * @author Jean Paulo Sassi de Miranda
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
