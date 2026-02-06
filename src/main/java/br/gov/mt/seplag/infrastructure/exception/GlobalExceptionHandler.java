package br.gov.mt.seplag.infrastructure.exception;

import br.gov.mt.seplag.domain.exception.AuthenticationException;
import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.presentation.dto.common.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

/**
 * Handler global de excecoes para retornar respostas padronizadas.
 * Trata todas as excecoes conhecidas da aplicacao e retorna o ErrorResponse no formato correto.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Excecao capturada: " + exception.getClass().getSimpleName(), exception);

        if (exception instanceof ResourceNotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, "Recurso nao encontrado", exception.getMessage());
        }

        if (exception instanceof AuthenticationException) {
            return buildResponse(Response.Status.UNAUTHORIZED, "Autenticacao falhou", exception.getMessage());
        }

        if (exception instanceof BusinessException) {
            return buildResponse(Response.Status.BAD_REQUEST, "Erro de negocio", exception.getMessage());
        }

        if (exception instanceof ConstraintViolationException) {
            String message = ((ConstraintViolationException) exception).getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            return buildResponse(Response.Status.BAD_REQUEST, "Erro de validacao", message);
        }

        if (exception instanceof jakarta.ws.rs.NotAuthorizedException) {
            return buildResponse(Response.Status.UNAUTHORIZED, "Nao autorizado", "Token JWT invalido ou expirado");
        }

        if (exception instanceof jakarta.ws.rs.ForbiddenException) {
            return buildResponse(Response.Status.FORBIDDEN, "Acesso negado", "Permissao insuficiente para acessar este recurso");
        }

        // Excecao generica - nao expoe detalhes internos em producao
        LOG.errorf("Excecao nao tratada: %s - %s", exception.getClass().getName(), exception.getMessage());
        return buildResponse(
            Response.Status.INTERNAL_SERVER_ERROR,
            "Erro interno",
            "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde."
        );
    }

    /**
     * Constroi resposta padronizada de erro.
     */
    private Response buildResponse(Response.Status status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
            status.getStatusCode(),
            error,
            message,
            null
        );
        return Response.status(status)
            .entity(errorResponse)
            .build();
    }
}
