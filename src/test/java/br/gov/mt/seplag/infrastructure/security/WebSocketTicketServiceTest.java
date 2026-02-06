package br.gov.mt.seplag.infrastructure.security;

import br.gov.mt.seplag.domain.model.WebSocketTicket;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o servico de tickets WebSocket.
 *
 * Valida:
 * - Criacao de tickets com dados corretos
 * - Validacao e consumo de tickets (single-use)
 * - Rejeicao de tickets invalidos, expirados ou ja usados
 * - Thread-safety em cenarios de concorrencia
 * - Estatisticas do servico
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class WebSocketTicketServiceTest {

    @Inject
    WebSocketTicketService ticketService;

    private static final String TEST_USERNAME = "testuser";
    private static final Set<String> TEST_ROLES = Set.of("USER", "ADMIN");

    @BeforeEach
    void setUp() {
        // Limpa tickets antigos entre testes
        // O cleanup automatico roda a cada 30s, mas podemos ter tickets de testes anteriores
    }

    // =========================================================================
    // Testes de Criacao de Tickets
    // =========================================================================

    @Test
    @DisplayName("Deve criar ticket com dados corretos")
    void shouldCreateTicketWithCorrectData() {
        // Act
        WebSocketTicket ticket = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);

        // Assert
        assertNotNull(ticket);
        assertNotNull(ticket.getTicket());
        assertEquals(36, ticket.getTicket().length()); // UUID tem 36 caracteres
        assertEquals(TEST_USERNAME, ticket.getUsername());
        assertEquals(TEST_ROLES, ticket.getRoles());
        assertTrue(ticket.isValid());
        assertFalse(ticket.isUsed());
        assertFalse(ticket.isExpired());
    }

    @Test
    @DisplayName("Deve criar tickets unicos para cada chamada")
    void shouldCreateUniqueTickets() {
        // Act
        WebSocketTicket ticket1 = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);
        WebSocketTicket ticket2 = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);
        WebSocketTicket ticket3 = ticketService.createTicket("otheruser", Set.of("USER"));

        // Assert
        assertNotEquals(ticket1.getTicket(), ticket2.getTicket());
        assertNotEquals(ticket2.getTicket(), ticket3.getTicket());
        assertNotEquals(ticket1.getTicket(), ticket3.getTicket());
    }

    // =========================================================================
    // Testes de Validacao e Consumo
    // =========================================================================

    @Test
    @DisplayName("Deve validar e consumir ticket valido")
    void shouldValidateAndConsumeValidTicket() {
        // Arrange
        WebSocketTicket created = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);
        String ticketId = created.getTicket();

        // Act
        Optional<WebSocketTicket> consumed = ticketService.validateAndConsume(ticketId);

        // Assert
        assertTrue(consumed.isPresent());
        assertEquals(TEST_USERNAME, consumed.get().getUsername());
        assertEquals(TEST_ROLES, consumed.get().getRoles());
    }

    @Test
    @DisplayName("Deve rejeitar ticket null")
    void shouldRejectNullTicket() {
        // Act
        Optional<WebSocketTicket> result = ticketService.validateAndConsume(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar ticket vazio")
    void shouldRejectEmptyTicket() {
        // Act
        Optional<WebSocketTicket> result = ticketService.validateAndConsume("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar ticket com espacos em branco")
    void shouldRejectBlankTicket() {
        // Act
        Optional<WebSocketTicket> result = ticketService.validateAndConsume("   ");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar ticket inexistente")
    void shouldRejectNonExistentTicket() {
        // Act
        Optional<WebSocketTicket> result = ticketService.validateAndConsume("non-existent-ticket-id");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar ticket ja consumido (single-use)")
    void shouldRejectAlreadyConsumedTicket() {
        // Arrange
        WebSocketTicket created = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);
        String ticketId = created.getTicket();

        // Primeiro consumo - deve funcionar
        Optional<WebSocketTicket> firstConsume = ticketService.validateAndConsume(ticketId);
        assertTrue(firstConsume.isPresent());

        // Act - Segundo consumo - deve falhar
        Optional<WebSocketTicket> secondConsume = ticketService.validateAndConsume(ticketId);

        // Assert
        assertTrue(secondConsume.isEmpty());
    }

    // =========================================================================
    // Testes de Verificacao de Validade
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true para ticket valido via isValid")
    void shouldReturnTrueForValidTicket() {
        // Arrange
        WebSocketTicket created = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);

        // Act & Assert
        assertTrue(ticketService.isValid(created.getTicket()));
    }

    @Test
    @DisplayName("Deve retornar false para ticket null via isValid")
    void shouldReturnFalseForNullTicket() {
        // Act & Assert
        assertFalse(ticketService.isValid(null));
    }

    @Test
    @DisplayName("Deve retornar false para ticket inexistente via isValid")
    void shouldReturnFalseForNonExistentTicket() {
        // Act & Assert
        assertFalse(ticketService.isValid("non-existent"));
    }

    // =========================================================================
    // Testes de Concorrencia (Thread-Safety)
    // =========================================================================

    @Test
    @DisplayName("Deve garantir single-use em cenario de concorrencia")
    void shouldEnsureSingleUseUnderConcurrency() throws InterruptedException {
        // Arrange
        WebSocketTicket created = ticketService.createTicket(TEST_USERNAME, TEST_ROLES);
        String ticketId = created.getTicket();

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Act - Todas as threads tentam consumir o mesmo ticket simultaneamente
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Espera todas as threads estarem prontas
                    Optional<WebSocketTicket> result = ticketService.validateAndConsume(ticketId);
                    if (result.isPresent()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Libera todas as threads
        doneLatch.await(); // Espera todas terminarem
        executor.shutdown();

        // Assert - Apenas uma thread deve ter conseguido consumir
        assertEquals(1, successCount.get(), "Apenas uma thread deve conseguir consumir o ticket");
    }

    @Test
    @DisplayName("Deve suportar criacao concorrente de multiplos tickets")
    void shouldSupportConcurrentTicketCreation() throws InterruptedException {
        // Arrange
        int numThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        Set<String> createdTickets = ConcurrentHashMap.newKeySet();

        // Act
        for (int i = 0; i < numThreads; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    WebSocketTicket ticket = ticketService.createTicket("user" + userId, Set.of("USER"));
                    createdTickets.add(ticket.getTicket());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Assert - Todos os tickets devem ser unicos
        assertEquals(numThreads, createdTickets.size(), "Todos os tickets devem ser unicos");
    }

    // =========================================================================
    // Testes de Estatisticas
    // =========================================================================

    @Test
    @DisplayName("Deve retornar estatisticas corretas")
    void shouldReturnCorrectStatistics() {
        // Arrange
        int initialActive = ticketService.getActiveTicketsCount();

        // Cria alguns tickets
        WebSocketTicket ticket1 = ticketService.createTicket("user1", Set.of("USER"));
        WebSocketTicket ticket2 = ticketService.createTicket("user2", Set.of("ADMIN"));

        // Act
        Map<String, Object> stats = ticketService.getStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue((int) stats.get("activeTickets") >= 2);
        assertTrue((long) stats.get("totalCreated") >= 2);
        assertEquals(30, stats.get("ticketTtlSeconds")); // Valor padrao

        // Cleanup - consome os tickets criados
        ticketService.validateAndConsume(ticket1.getTicket());
        ticketService.validateAndConsume(ticket2.getTicket());
    }

    @Test
    @DisplayName("Deve retornar TTL configurado")
    void shouldReturnConfiguredTtl() {
        // Act
        int ttl = ticketService.getTicketTtlSeconds();

        // Assert
        assertEquals(30, ttl); // Valor padrao configurado
    }

    // =========================================================================
    // Classe auxiliar para ConcurrentHashMap.newKeySet()
    // =========================================================================
    private static class ConcurrentHashMap {
        static <T> Set<T> newKeySet() {
            return java.util.concurrent.ConcurrentHashMap.newKeySet();
        }
    }
}
