package br.gov.mt.seplag.infrastructure.security;

import br.gov.mt.seplag.domain.model.WebSocketTicket;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servico para gerenciamento de tickets temporarios de autenticacao WebSocket.
 *
 * Fluxo:
 * 1. Cliente obtem ticket via REST (autenticado com JWT)
 * 2. Cliente conecta ao WebSocket usando ticket na URL
 * 3. Ticket e validado e consumido (single-use, 30s TTL)
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class WebSocketTicketService {

    private static final Logger LOG = Logger.getLogger(WebSocketTicketService.class);

    private final Map<String, WebSocketTicket> activeTickets = new ConcurrentHashMap<>();

    private final AtomicLong ticketsCreated = new AtomicLong(0);
    private final AtomicLong ticketsConsumed = new AtomicLong(0);
    private final AtomicLong ticketsExpired = new AtomicLong(0);
    private final AtomicLong ticketsRejected = new AtomicLong(0);

    @ConfigProperty(name = "app.websocket.ticket.ttl-seconds", defaultValue = "30")
    int ticketTtlSeconds;

    /**
     * Cria um novo ticket para o usuario autenticado.
     */
    public WebSocketTicket createTicket(String username, Set<String> roles) {
        WebSocketTicket ticket = new WebSocketTicket(username, roles, ticketTtlSeconds);
        activeTickets.put(ticket.getTicket(), ticket);
        ticketsCreated.incrementAndGet();

        LOG.infof("Ticket WebSocket criado - User: %s, Ticket: %s..., Expira em: %ds",
            username, ticket.getTicket().substring(0, 8), ticketTtlSeconds);

        return ticket;
    }

    /**
     * Valida e consome um ticket de forma atomica.
     * O ticket so pode ser usado uma vez (single-use).
     */
    public Optional<WebSocketTicket> validateAndConsume(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            LOG.debug("Tentativa de validacao com ticket nulo ou vazio");
            ticketsRejected.incrementAndGet();
            return Optional.empty();
        }

        WebSocketTicket ticket = activeTickets.get(ticketId);

        if (ticket == null) {
            LOG.warnf("Ticket nao encontrado: %s...", ticketId.substring(0, Math.min(8, ticketId.length())));
            ticketsRejected.incrementAndGet();
            return Optional.empty();
        }

        // Tenta consumir de forma atomica
        if (ticket.tryConsume()) {
            activeTickets.remove(ticketId);
            ticketsConsumed.incrementAndGet();

            LOG.infof("Ticket WebSocket consumido - User: %s, Ticket: %s...",
                ticket.getUsername(), ticketId.substring(0, 8));

            return Optional.of(ticket);
        }

        // Ticket invalido (expirado ou ja usado)
        String reason = ticket.isExpired() ? "expirado" : "ja utilizado";
        LOG.warnf("Ticket invalido (%s) - Ticket: %s...", reason, ticketId.substring(0, 8));
        ticketsRejected.incrementAndGet();
        activeTickets.remove(ticketId);

        return Optional.empty();
    }

    /**
     * Verifica se um ticket existe e e valido (sem consumi-lo).
     */
    public boolean isValid(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return false;
        }
        WebSocketTicket ticket = activeTickets.get(ticketId);
        return ticket != null && ticket.isValid();
    }

    /**
     * Remove tickets expirados. Executado a cada 30 segundos.
     */
    @Scheduled(every = "30s")
    void cleanupExpiredTickets() {
        int initialSize = activeTickets.size();
        int removed = 0;

        var iterator = activeTickets.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
                ticketsExpired.incrementAndGet();
            }
        }

        if (removed > 0) {
            LOG.infof("Cleanup de tickets: %d expirados removidos (ativos: %d)", removed, activeTickets.size());
        }
    }

    /**
     * Retorna estatisticas do servico de tickets.
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "activeTickets", activeTickets.size(),
            "totalCreated", ticketsCreated.get(),
            "totalConsumed", ticketsConsumed.get(),
            "totalExpired", ticketsExpired.get(),
            "totalRejected", ticketsRejected.get(),
            "ticketTtlSeconds", ticketTtlSeconds
        );
    }

    public int getActiveTicketsCount() {
        return activeTickets.size();
    }

    public int getTicketTtlSeconds() {
        return ticketTtlSeconds;
    }
}
