package br.gov.mt.seplag.infrastructure.audit;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios para AuditService.
 *
 * @author Jean Paulo Sassi de Miranda
 */
class AuditServiceTest {

    private AuditService auditService;

    @Mock
    private JsonWebToken jwt;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        auditService = new AuditService();

        // Injeta o mock do JWT
        Field jwtField = AuditService.class.getDeclaredField("jwt");
        jwtField.setAccessible(true);
        jwtField.set(auditService, jwt);
    }

    @Test
    void auditCreateShouldLogSuccessfully() {
        when(jwt.getName()).thenReturn("admin");

        assertThatNoException().isThrownBy(() ->
            auditService.auditCreate("Album", 1L, "Album criado: Test Album")
        );
    }

    @Test
    void auditUpdateShouldLogSuccessfully() {
        when(jwt.getName()).thenReturn("admin");

        assertThatNoException().isThrownBy(() ->
            auditService.auditUpdate("Artista", 2L, "Artista atualizado")
        );
    }

    @Test
    void auditDeleteShouldLogSuccessfully() {
        when(jwt.getName()).thenReturn("admin");

        assertThatNoException().isThrownBy(() ->
            auditService.auditDelete("Album", 3L, "Album removido")
        );
    }

    @Test
    void auditReadShouldLogSuccessfully() {
        when(jwt.getName()).thenReturn("user");

        assertThatNoException().isThrownBy(() ->
            auditService.auditRead("Artista", 1L, "Consulta de artista")
        );
    }

    @Test
    void auditLoginSuccessShouldLogSuccessfully() {
        assertThatNoException().isThrownBy(() ->
            auditService.auditLogin("admin", true, "Login bem sucedido")
        );
    }

    @Test
    void auditLoginFailureShouldLogSuccessfully() {
        assertThatNoException().isThrownBy(() ->
            auditService.auditLogin("hacker", false, "Senha incorreta")
        );
    }

    @Test
    void auditSyncShouldLogSuccessfully() {
        when(jwt.getName()).thenReturn("admin");

        assertThatNoException().isThrownBy(() ->
            auditService.auditSync("Regional", 50, "Sincronizacao concluida: 10 novos, 5 atualizados")
        );
    }

    @Test
    void auditShouldHandleNullJwt() throws Exception {
        // Simula JWT null (requisicao anonima)
        Field jwtField = AuditService.class.getDeclaredField("jwt");
        jwtField.setAccessible(true);
        jwtField.set(auditService, null);

        assertThatNoException().isThrownBy(() ->
            auditService.auditCreate("Album", 1L, "Album criado anonimamente")
        );
    }

    @Test
    void auditShouldHandleJwtWithNullName() {
        when(jwt.getName()).thenReturn(null);

        assertThatNoException().isThrownBy(() ->
            auditService.auditCreate("Album", 1L, "Album criado")
        );
    }

    @Test
    void auditShouldHandleNullEntityId() {
        when(jwt.getName()).thenReturn("admin");

        assertThatNoException().isThrownBy(() ->
            auditService.audit(AuditService.AuditOperation.CREATE, "Album", null, "Teste")
        );
    }
}
