package br.gov.mt.seplag.infrastructure.exception;

import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Handler especifico para excecoes de validacao do Bean Validation.
 * Formata a mensagem incluindo o campo que falhou na validacao.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Provider
public class ValidationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
            .stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining("; "));

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse("Erro de validacao: " + message))
            .build();
    }
}
