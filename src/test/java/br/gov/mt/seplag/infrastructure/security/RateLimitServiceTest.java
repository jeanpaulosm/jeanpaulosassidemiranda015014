package br.gov.mt.seplag.infrastructure.security;

import br.gov.mt.seplag.domain.exception.RateLimitExceededException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para RateLimitService.
 * Valida o controle de taxa de requisicoes por usuario conforme edital:
 * "Rate limit: ate 10 requisicoes por minuto por usuario"
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class RateLimitServiceTest {

    @Inject
    RateLimitService rateLimitService;

    private String testUserId;

    @BeforeEach
    void setUp() {
        // Usa um ID unico por teste para evitar interferencia entre testes
        testUserId = "test-user-" + System.nanoTime();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        // Deve permitir ate 10 requisicoes
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> rateLimitService.checkRateLimit(testUserId));
        }
    }

    @Test
    void shouldBlockRequestsExceedingLimit() {
        // Faz 10 requisicoes (limite)
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkRateLimit(testUserId);
        }

        // A 11a requisicao deve ser bloqueada
        assertThrows(RateLimitExceededException.class,
            () -> rateLimitService.checkRateLimit(testUserId));
    }

    @Test
    void shouldReturnCorrectRemainingRequests() {
        // Inicialmente deve ter 10 requisicoes disponiveis
        assertEquals(10, rateLimitService.getRemainingRequests(testUserId));

        // Apos 3 requisicoes, deve restar 7
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkRateLimit(testUserId);
        }
        assertEquals(7, rateLimitService.getRemainingRequests(testUserId));

        // Apos mais 7 requisicoes, deve restar 0
        for (int i = 0; i < 7; i++) {
            rateLimitService.checkRateLimit(testUserId);
        }
        assertEquals(0, rateLimitService.getRemainingRequests(testUserId));
    }

    @Test
    void shouldTrackDifferentUsersIndependently() {
        String user1 = "user1-" + System.nanoTime();
        String user2 = "user2-" + System.nanoTime();

        // User1 faz 10 requisicoes (atinge o limite)
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkRateLimit(user1);
        }

        // User1 deve estar bloqueado
        assertThrows(RateLimitExceededException.class,
            () -> rateLimitService.checkRateLimit(user1));

        // User2 deve ter todas as requisicoes disponiveis
        assertEquals(10, rateLimitService.getRemainingRequests(user2));
        assertDoesNotThrow(() -> rateLimitService.checkRateLimit(user2));
    }

    @Test
    void shouldReturnMaxRequestsForNewUser() {
        String newUser = "new-user-" + System.nanoTime();
        assertEquals(10, rateLimitService.getRemainingRequests(newUser));
    }

    @Test
    void shouldHaveCorrectConfiguration() {
        // Valida que a configuracao esta de acordo com o edital
        assertEquals(10, rateLimitService.getMaxRequests(),
            "Rate limit deve ser 10 requisicoes conforme edital");
        assertEquals(60, rateLimitService.getWindowSeconds(),
            "Janela de tempo deve ser 60 segundos (1 minuto) conforme edital");
    }

    @Test
    void shouldThrowRateLimitExceededExceptionWithCorrectMessage() {
        // Atinge o limite
        for (int i = 0; i < 10; i++) {
            rateLimitService.checkRateLimit(testUserId);
        }

        // Verifica a mensagem da excecao
        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> rateLimitService.checkRateLimit(testUserId)
        );

        assertTrue(exception.getMessage().contains("10"),
            "Mensagem deve conter o limite de requisicoes");
        assertTrue(exception.getMessage().contains("60") || exception.getMessage().contains("minuto"),
            "Mensagem deve conter a janela de tempo");
    }
}
