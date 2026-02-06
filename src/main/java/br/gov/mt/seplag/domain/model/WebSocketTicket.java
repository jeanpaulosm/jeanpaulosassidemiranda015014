package br.gov.mt.seplag.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Representa um ticket temporario para autenticacao WebSocket.
 * Single-use, curta duracao (30s), vinculado ao usuario.
 *
 * @author Jean Paulo Sassi de Miranda
 */
public class WebSocketTicket {

    private final String ticket;
    private final String username;
    private final Set<String> roles;
    private final Instant createdAt;
    private final Instant expiresAt;
    private volatile boolean used;

    public WebSocketTicket(String username, Set<String> roles, int ttlSeconds) {
        this.ticket = UUID.randomUUID().toString();
        this.username = username;
        this.roles = Set.copyOf(roles);
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plusSeconds(ttlSeconds);
        this.used = false;
    }

    /**
     * Verifica se o ticket e valido (nao expirado e nao usado).
     */
    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    /**
     * Verifica se o ticket expirou.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Marca o ticket como usado.
     */
    public void markAsUsed() {
        this.used = true;
    }

    /**
     * Tenta consumir o ticket de forma atomica.
     * Retorna true apenas se o ticket era valido e foi marcado como usado.
     */
    public synchronized boolean tryConsume() {
        if (isValid()) {
            this.used = true;
            return true;
        }
        return false;
    }

    public long getSecondsUntilExpiration() {
        long seconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, seconds);
    }

    public String getTicket() {
        return ticket;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    @Override
    public String toString() {
        return "WebSocketTicket{" +
            "ticket='" + ticket.substring(0, 8) + "...'" +
            ", username='" + username + '\'' +
            ", roles=" + roles +
            ", expiresAt=" + expiresAt +
            ", used=" + used +
            '}';
    }
}
