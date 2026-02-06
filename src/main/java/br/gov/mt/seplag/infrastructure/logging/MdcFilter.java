package br.gov.mt.seplag.infrastructure.logging;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

/**
 * Filtro para adicionar informacoes de rastreabilidade ao MDC (Mapped Diagnostic Context).
 *
 * Informacoes adicionadas:
 * - requestId: UUID unico para cada requisicao
 * - user: username do usuario autenticado (ou "anonymous")
 * - clientIp: IP do cliente (considerando proxies)
 * - method: metodo HTTP (GET, POST, etc.)
 * - path: caminho da requisicao
 *
 * Estas informacoes aparecerao em todos os logs gerados durante a requisicao,
 * facilitando o rastreamento e debug em ambientes de producao.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Provider
@Priority(Priorities.USER - 100) // Executa bem no inicio para capturar todos os logs
public class MdcFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String REQUEST_ID = "requestId";
    public static final String USER = "user";
    public static final String CLIENT_IP = "clientIp";
    public static final String METHOD = "method";
    public static final String PATH = "path";

    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Gera ou reutiliza request ID
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Extrai informacoes do usuario
        String user = "anonymous";
        if (requestContext.getSecurityContext() != null) {
            Principal principal = requestContext.getSecurityContext().getUserPrincipal();
            if (principal != null && principal.getName() != null) {
                user = principal.getName();
            }
        }

        // Extrai IP do cliente
        String clientIp = extractClientIp(requestContext);

        // Extrai metodo e path
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        // Adiciona ao MDC
        MDC.put(REQUEST_ID, requestId);
        MDC.put(USER, user);
        MDC.put(CLIENT_IP, clientIp);
        MDC.put(METHOD, method);
        MDC.put(PATH, path);

        // Armazena o requestId para adicionar ao header de resposta
        requestContext.setProperty(REQUEST_ID, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        try {
            // Adiciona o request ID no header de resposta para rastreabilidade
            String requestId = (String) requestContext.getProperty(REQUEST_ID);
            if (requestId != null) {
                responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId);
            }
        } finally {
            // Limpa o MDC ao final da requisicao
            MDC.clear();
        }
    }

    /**
     * Extrai o IP real do cliente, considerando proxies reversos.
     */
    private String extractClientIp(ContainerRequestContext requestContext) {
        // X-Forwarded-For: client, proxy1, proxy2
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        // X-Real-IP (comum em Nginx)
        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        // Fallback
        return "unknown";
    }
}
