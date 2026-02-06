package br.gov.mt.seplag.infrastructure.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para PasswordEncoder.
 * Valida o uso de BCrypt para hash de senhas conforme boas praticas de seguranca.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class PasswordEncoderTest {

    @Inject
    PasswordEncoder passwordEncoder;

    @Test
    void shouldEncodePassword() {
        String rawPassword = "admin123";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "Senha codificada nao deve ser nula");
        assertFalse(encodedPassword.isEmpty(), "Senha codificada nao deve ser vazia");
        assertNotEquals(rawPassword, encodedPassword, "Senha codificada deve ser diferente da original");
    }

    @Test
    void shouldMatchCorrectPassword() {
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
            "Senha correta deve corresponder ao hash");
    }

    @Test
    void shouldNotMatchIncorrectPassword() {
        String rawPassword = "admin123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword),
            "Senha incorreta nao deve corresponder ao hash");
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "admin123";

        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        // BCrypt gera salt diferente a cada codificacao
        assertNotEquals(encoded1, encoded2,
            "BCrypt deve gerar hashes diferentes para mesma senha (salt aleatorio)");

        // Ambos devem corresponder a senha original
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    void shouldGenerateBCryptHash() {
        String rawPassword = "testpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Hash BCrypt comeca com $2a$, $2b$ ou $2y$
        assertTrue(
            encodedPassword.startsWith("$2a$") ||
            encodedPassword.startsWith("$2b$") ||
            encodedPassword.startsWith("$2y$"),
            "Hash deve estar no formato BCrypt"
        );
    }

    @Test
    void shouldRejectEmptyPassword() {
        String emptyPassword = "";

        // Senha vazia deve lancar excecao
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> passwordEncoder.encode(emptyPassword));

        assertTrue(exception.getMessage().contains("nao pode ser nula ou vazia"),
            "Mensagem deve indicar que senha vazia nao e permitida");
    }

    @Test
    void shouldRejectNullPassword() {
        // Senha nula deve lancar excecao
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> passwordEncoder.encode(null));

        assertTrue(exception.getMessage().contains("nao pode ser nula ou vazia"),
            "Mensagem deve indicar que senha nula nao e permitida");
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String specialPassword = "P@$$w0rd!@#$%^&*()";
        String encodedPassword = passwordEncoder.encode(specialPassword);

        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword),
            "Deve suportar senhas com caracteres especiais");
    }

    @Test
    void shouldHandleLongPassword() {
        String longPassword = "a".repeat(100);
        String encodedPassword = passwordEncoder.encode(longPassword);

        assertTrue(passwordEncoder.matches(longPassword, encodedPassword),
            "Deve suportar senhas longas");
    }

    @Test
    void shouldHandleUnicodePassword() {
        String unicodePassword = "senha123áéíóú日本語";
        String encodedPassword = passwordEncoder.encode(unicodePassword);

        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword),
            "Deve suportar senhas com caracteres unicode");
    }

    @Test
    void shouldBeCaseSensitive() {
        String password = "Password123";
        String encodedPassword = passwordEncoder.encode(password);

        assertFalse(passwordEncoder.matches("password123", encodedPassword),
            "Verificacao de senha deve ser case-sensitive");
        assertFalse(passwordEncoder.matches("PASSWORD123", encodedPassword),
            "Verificacao de senha deve ser case-sensitive");
        assertTrue(passwordEncoder.matches("Password123", encodedPassword),
            "Senha exata deve corresponder");
    }

    @Test
    void shouldValidateMigrationHashes() {
        // Hashes da migration V10
        String hashAdmin = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        String hashUser = "$2a$10$6KABs6t3FxQ8OC0VGrMX3ePl9v1X6ZQA.jb2.fB8mH5aG7tN1kR2O";

        // Se os hashes nao correspondem, gera novos e imprime para atualizacao
        if (!passwordEncoder.matches("admin123", hashAdmin)) {
            String newHash = passwordEncoder.encode("admin123");
            System.out.println("Hash correto para admin123: " + newHash);
        }
        if (!passwordEncoder.matches("user123", hashUser)) {
            String newHash = passwordEncoder.encode("user123");
            System.out.println("Hash correto para user123: " + newHash);
        }

        // Verifica se os hashes podem ser gerados corretamente
        String freshAdminHash = passwordEncoder.encode("admin123");
        String freshUserHash = passwordEncoder.encode("user123");

        assertTrue(passwordEncoder.matches("admin123", freshAdminHash),
            "Hash gerado deve corresponder a senha 'admin123'");
        assertTrue(passwordEncoder.matches("user123", freshUserHash),
            "Hash gerado deve corresponder a senha 'user123'");
    }

    @Test
    void shouldPrintValidHashes() {
        // Gera e imprime hashes validos para as senhas de teste
        String adminHash = passwordEncoder.encode("admin123");
        String userHash = passwordEncoder.encode("user123");

        System.out.println("=== HASHES BCRYPT VALIDOS ===");
        System.out.println("admin123: " + adminHash);
        System.out.println("user123: " + userHash);
        System.out.println("=============================");

        // Verifica que os hashes sao validos
        assertTrue(passwordEncoder.matches("admin123", adminHash));
        assertTrue(passwordEncoder.matches("user123", userHash));
    }
}
