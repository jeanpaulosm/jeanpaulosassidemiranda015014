package br.gov.mt.seplag.infrastructure.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Filtro para adicionar headers de rate limit na resposta.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Provider
public class RateLimitResponseFilter implements ContainerResponseFilter {

    @Inject
    RateLimitService rateLimitService;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        String userId = (String) requestContext.getProperty("rate-limit-user");
        if (userId != null) {
            int remaining = rateLimitService.getRemainingRequests(userId);
            responseContext.getHeaders().add("X-RateLimit-Limit", rateLimitService.getMaxRequests());
            responseContext.getHeaders().add("X-RateLimit-Remaining", remaining);
            responseContext.getHeaders().add("X-RateLimit-Window", rateLimitService.getWindowSeconds());
        }
    }
}
