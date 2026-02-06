package br.gov.mt.seplag.infrastructure.security;

import br.gov.mt.seplag.domain.exception.RateLimitExceededException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.Principal;

/**
 * Filtro para aplicar rate limiting nas requisicoes.
 *
 * Estrategia de identificacao do cliente:
 * 1. Usuario autenticado: usa o username do JWT (rate limit por usuario)
 * 2. Usuario nao autenticado: usa o IP do cliente (rate limit por IP)
 *
 * Isso garante que:
 * - Usuarios autenticados tem seu proprio limite individual
 * - Requisicoes anonimas sao limitadas por IP, evitando abuso
 * - Nao ha mais um bucket "anonymous" compartilhado
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RateLimitFilter.class);
    private static final String RATE_LIMIT_USER_PROPERTY = "rate-limit-user";
    private static final String RATE_LIMIT_TYPE_PROPERTY = "rate-limit-type";

    @Inject
    RateLimitService rateLimitService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Ignora endpoints de health, metricas e swagger
        String path = requestContext.getUriInfo().getPath();
        if (isExcludedPath(path)) {
            return;
        }

        // Identifica o cliente pela autenticacao ou IP
        ClientIdentification clientId = identifyClient(requestContext);

        try {
            rateLimitService.checkRateLimit(clientId.identifier());

            // Adiciona headers de rate limit na resposta (via filter de resposta)
            requestContext.setProperty(RATE_LIMIT_USER_PROPERTY, clientId.identifier());
            requestContext.setProperty(RATE_LIMIT_TYPE_PROPERTY, clientId.type());

            LOG.debugf("Rate limit check passed - Type: %s, Identifier: %s, Remaining: %d",
                clientId.type(), clientId.identifier(),
                rateLimitService.getRemainingRequests(clientId.identifier()));

        } catch (RateLimitExceededException e) {
            LOG.warnf("Rate limit exceeded - Type: %s, Identifier: %s",
                clientId.type(), clientId.identifier());

            requestContext.abortWith(Response
                .status(Response.Status.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", rateLimitService.getMaxRequests())
                .header("X-RateLimit-Remaining", 0)
                .header("X-RateLimit-Window", rateLimitService.getWindowSeconds())
                .header("X-RateLimit-Type", clientId.type())
                .header("Retry-After", rateLimitService.getWindowSeconds())
                .entity(new ErrorResponse(429, e.getMessage(), clientId.type()))
                .build());
        }
    }

    /**
     * Verifica se o path deve ser excluido do rate limiting.
     */
    private boolean isExcludedPath(String path) {
        return path.startsWith("q/") ||
               path.startsWith("openapi") ||
               path.equals("ws/albuns");
    }

    /**
     * Identifica o cliente para rate limiting.
     *
     * Estrategia:
     * 1. Se autenticado (JWT valido): usa username
     * 2. Se nao autenticado: usa IP do cliente
     *
     * @return ClientIdentification com tipo e identificador
     */
    private ClientIdentification identifyClient(ContainerRequestContext requestContext) {
        // Tenta obter usuario autenticado
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext != null) {
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
                return new ClientIdentification("user", principal.getName());
            }
        }

        // Fallback para IP do cliente
        String clientIp = extractClientIp(requestContext);
        return new ClientIdentification("ip", clientIp);
    }

    /**
     * Extrai o IP real do cliente, considerando proxies reversos.
     *
     * Ordem de verificacao:
     * 1. X-Forwarded-For (primeiro IP da lista)
     * 2. X-Real-IP
     * 3. IP da conexao direta
     */
    private String extractClientIp(ContainerRequestContext requestContext) {
        // X-Forwarded-For: client, proxy1, proxy2
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String clientIp = forwardedFor.split(",")[0].trim();
            if (isValidIp(clientIp)) {
                return "ip:" + clientIp;
            }
        }

        // X-Real-IP (comum em Nginx)
        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank() && isValidIp(realIp.trim())) {
            return "ip:" + realIp.trim();
        }

        // Fallback: IP generico para casos onde nao conseguimos identificar
        // Isso pode acontecer em ambientes de teste ou configuracoes especificas
        return "ip:unknown";
    }

    /**
     * Valida formato basico de IP (IPv4 ou IPv6).
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        // Validacao simples: contem . (IPv4) ou : (IPv6)
        return ip.contains(".") || ip.contains(":");
    }

    /**
     * Record para identificacao do cliente.
     */
    public record ClientIdentification(String type, String identifier) {}

    /**
     * Classe para resposta de erro padronizada.
     */
    public static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public String rateLimitType;
        public String hint;

        public ErrorResponse(int status, String message, String rateLimitType) {
            this.status = status;
            this.error = "TOO_MANY_REQUESTS";
            this.message = message;
            this.rateLimitType = rateLimitType;
            this.hint = "user".equals(rateLimitType)
                ? "Rate limit por usuario excedido. Aguarde a janela de tempo expirar."
                : "Rate limit por IP excedido. Autentique-se para ter seu proprio limite.";
        }
    }
}
