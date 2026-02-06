package br.gov.mt.seplag.infrastructure.audit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Optional;

/**
 * Servico de auditoria para operacoes criticas.
 * Registra logs estruturados para rastreabilidade e compliance.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AuditService {

    private static final Logger LOG = Logger.getLogger(AuditService.class);

    @Inject
    JsonWebToken jwt;

    /**
     * Registra uma operacao de auditoria.
     *
     * @param operation tipo da operacao
     * @param entity    entidade afetada
     * @param entityId  ID da entidade
     * @param details   detalhes adicionais
     */
    public void audit(AuditOperation operation, String entity, Object entityId, String details) {
        String username = extractUsername();
        AuditEvent event = new AuditEvent(
            Instant.now(),
            username,
            operation,
            entity,
            entityId != null ? entityId.toString() : null,
            details
        );

        LOG.infof("AUDIT | %s | user=%s | entity=%s | entityId=%s | details=%s",
            operation,
            username,
            entity,
            entityId,
            details
        );
    }

    /**
     * Registra operacao de criacao.
     */
    public void auditCreate(String entity, Object entityId, String details) {
        audit(AuditOperation.CREATE, entity, entityId, details);
    }

    /**
     * Registra operacao de atualizacao.
     */
    public void auditUpdate(String entity, Object entityId, String details) {
        audit(AuditOperation.UPDATE, entity, entityId, details);
    }

    /**
     * Registra operacao de exclusao.
     */
    public void auditDelete(String entity, Object entityId, String details) {
        audit(AuditOperation.DELETE, entity, entityId, details);
    }

    /**
     * Registra operacao de leitura sensivel.
     */
    public void auditRead(String entity, Object entityId, String details) {
        audit(AuditOperation.READ, entity, entityId, details);
    }

    /**
     * Registra operacao de login.
     */
    public void auditLogin(String username, boolean success, String details) {
        AuditOperation operation = success ? AuditOperation.LOGIN_SUCCESS : AuditOperation.LOGIN_FAILURE;
        AuditEvent event = new AuditEvent(
            Instant.now(),
            username,
            operation,
            "AUTH",
            null,
            details
        );

        if (success) {
            LOG.infof("AUDIT | %s | user=%s | details=%s", operation, username, details);
        } else {
            LOG.warnf("AUDIT | %s | user=%s | details=%s", operation, username, details);
        }
    }

    /**
     * Registra operacao de sincronizacao.
     */
    public void auditSync(String entity, int recordsProcessed, String details) {
        audit(AuditOperation.SYNC, entity, recordsProcessed, details);
    }

    /**
     * Extrai o username do token JWT atual, se disponivel.
     */
    private String extractUsername() {
        try {
            if (jwt != null && jwt.getName() != null) {
                return jwt.getName();
            }
        } catch (Exception e) {
            // Token nao disponivel (requisicao anonima)
        }
        return "anonymous";
    }

    /**
     * Operacoes de auditoria suportadas.
     */
    public enum AuditOperation {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        SYNC,
        UPLOAD,
        DOWNLOAD
    }

    /**
     * Evento de auditoria.
     */
    public record AuditEvent(
        Instant timestamp,
        String username,
        AuditOperation operation,
        String entity,
        String entityId,
        String details
    ) {}
}
