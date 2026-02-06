package br.gov.mt.seplag.infrastructure.security;

import br.gov.mt.seplag.domain.exception.RateLimitExceededException;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servico de rate limiting por usuario.
 * Limita o numero de requisicoes por usuario em uma janela de tempo.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class RateLimitService {

    @ConfigProperty(name = "app.rate-limit.max-requests", defaultValue = "10")
    int maxRequests;

    @ConfigProperty(name = "app.rate-limit.window-seconds", defaultValue = "60")
    int windowSeconds;

    private final Map<String, UserRequestCounter> requestCounts = new ConcurrentHashMap<>();

    /**
     * Verifica se o usuario pode fazer mais requisicoes.
     *
     * @param userId identificador do usuario (username ou IP)
     * @throws RateLimitExceededException se o limite foi excedido
     */
    public void checkRateLimit(String userId) {
        UserRequestCounter counter = requestCounts.computeIfAbsent(userId,
            k -> new UserRequestCounter(Instant.now()));

        synchronized (counter) {
            Instant now = Instant.now();

            // Se a janela expirou, reseta o contador
            if (now.isAfter(counter.windowStart.plusSeconds(windowSeconds))) {
                counter.windowStart = now;
                counter.count.set(0);
            }

            // Verifica se excedeu o limite
            if (counter.count.get() >= maxRequests) {
                throw new RateLimitExceededException(maxRequests, windowSeconds);
            }

            // Incrementa o contador
            counter.count.incrementAndGet();
        }
    }

    /**
     * Retorna o numero de requisicoes restantes para o usuario.
     */
    public int getRemainingRequests(String userId) {
        UserRequestCounter counter = requestCounts.get(userId);
        if (counter == null) {
            return maxRequests;
        }

        synchronized (counter) {
            Instant now = Instant.now();
            if (now.isAfter(counter.windowStart.plusSeconds(windowSeconds))) {
                return maxRequests;
            }
            return Math.max(0, maxRequests - counter.count.get());
        }
    }

    /**
     * Limpa contadores expirados a cada minuto.
     */
    @Scheduled(every = "1m")
    void cleanupExpiredCounters() {
        Instant threshold = Instant.now().minusSeconds(windowSeconds * 2L);
        requestCounts.entrySet().removeIf(entry ->
            entry.getValue().windowStart.isBefore(threshold));
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    private static class UserRequestCounter {
        Instant windowStart;
        AtomicInteger count;

        UserRequestCounter(Instant windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }
    }
}
