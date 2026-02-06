package br.gov.mt.seplag.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

/**
 * Servico centralizado de metricas da aplicacao.
 * Fornece metricas customizadas para monitoramento via Prometheus.
 *
 * Metricas disponiveis:
 * - Autenticacao: logins bem sucedidos/falhos, duracao
 * - Entidades: criacao, atualizacao e delecao de artistas e albuns
 * - Imagens: uploads, delecoes, tamanho de arquivos
 * - Regionais: sincronizacoes, inseridas, alteradas, inativadas
 * - Seguranca: rate limit, magic numbers invalidos
 * - WebSocket: conexoes ativas
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class MetricsService {

    private final MeterRegistry registry;

    // Contadores de Autenticacao
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;
    private final Counter tokenRefreshCounter;
    private final Counter tokenExpiredCounter;

    // Contadores de Artistas
    private final Counter artistaCreatedCounter;
    private final Counter artistaUpdatedCounter;
    private final Counter artistaDeletedCounter;

    // Contadores de Albuns
    private final Counter albumCreatedCounter;
    private final Counter albumUpdatedCounter;
    private final Counter albumDeletedCounter;

    // Contadores de Imagens
    private final Counter imagemUploadCounter;
    private final Counter imagemDeletedCounter;
    private final Counter imagemUploadFailedCounter;

    // Contadores de Regionais
    private final Counter regionalSyncCounter;
    private final Counter regionalSyncInseridaCounter;
    private final Counter regionalSyncAlteradaCounter;
    private final Counter regionalSyncInativadaCounter;

    // Contadores de Seguranca
    private final Counter rateLimitExceededCounter;
    private final Counter invalidMagicNumberCounter;
    private final Counter validationErrorCounter;

    // Contadores de WebSocket Auth
    private final Counter wsAuthSuccessCounter;
    private final Counter wsAuthFailureEmptyCounter;
    private final Counter wsAuthFailureNotFoundCounter;
    private final Counter wsAuthFailureExpiredCounter;
    private final Counter wsAuthFailureUsedCounter;

    // Timers
    private final Timer authTimer;
    private final Timer regionalSyncTimer;
    private final Timer imagemUploadTimer;
    private final Timer albumOperationTimer;
    private final Timer artistaOperationTimer;

    // Distribution Summaries (Histogramas)
    private final DistributionSummary imagemSizeSummary;

    @Inject
    public MetricsService(MeterRegistry registry) {
        this.registry = registry;

        // =====================
        // Contadores de Autenticacao
        // =====================
        this.authSuccessCounter = Counter.builder("auth_login_total")
            .tag("status", "success")
            .description("Total de logins bem sucedidos")
            .register(registry);

        this.authFailureCounter = Counter.builder("auth_login_total")
            .tag("status", "failure")
            .description("Total de logins falhos")
            .register(registry);

        this.tokenRefreshCounter = Counter.builder("auth_token_refresh_total")
            .description("Total de tokens renovados via refresh token")
            .register(registry);

        this.tokenExpiredCounter = Counter.builder("auth_token_expired_total")
            .description("Total de requisicoes com token expirado")
            .register(registry);

        // =====================
        // Contadores de Artistas
        // =====================
        this.artistaCreatedCounter = Counter.builder("artista_operations_total")
            .tag("operation", "create")
            .description("Total de artistas criados")
            .register(registry);

        this.artistaUpdatedCounter = Counter.builder("artista_operations_total")
            .tag("operation", "update")
            .description("Total de artistas atualizados")
            .register(registry);

        this.artistaDeletedCounter = Counter.builder("artista_operations_total")
            .tag("operation", "delete")
            .description("Total de artistas deletados")
            .register(registry);

        // =====================
        // Contadores de Albuns
        // =====================
        this.albumCreatedCounter = Counter.builder("album_operations_total")
            .tag("operation", "create")
            .description("Total de albuns criados")
            .register(registry);

        this.albumUpdatedCounter = Counter.builder("album_operations_total")
            .tag("operation", "update")
            .description("Total de albuns atualizados")
            .register(registry);

        this.albumDeletedCounter = Counter.builder("album_operations_total")
            .tag("operation", "delete")
            .description("Total de albuns deletados")
            .register(registry);

        // =====================
        // Contadores de Imagens
        // =====================
        this.imagemUploadCounter = Counter.builder("imagem_operations_total")
            .tag("operation", "upload")
            .tag("status", "success")
            .description("Total de imagens enviadas com sucesso")
            .register(registry);

        this.imagemDeletedCounter = Counter.builder("imagem_operations_total")
            .tag("operation", "delete")
            .description("Total de imagens deletadas")
            .register(registry);

        this.imagemUploadFailedCounter = Counter.builder("imagem_operations_total")
            .tag("operation", "upload")
            .tag("status", "failed")
            .description("Total de uploads de imagens que falharam")
            .register(registry);

        // =====================
        // Contadores de Regionais
        // =====================
        this.regionalSyncCounter = Counter.builder("regional_sync_total")
            .description("Total de sincronizacoes de regionais")
            .register(registry);

        this.regionalSyncInseridaCounter = Counter.builder("regional_sync_operations_total")
            .tag("operation", "insert")
            .description("Total de regionais inseridas na sincronizacao")
            .register(registry);

        this.regionalSyncAlteradaCounter = Counter.builder("regional_sync_operations_total")
            .tag("operation", "update")
            .description("Total de regionais alteradas na sincronizacao")
            .register(registry);

        this.regionalSyncInativadaCounter = Counter.builder("regional_sync_operations_total")
            .tag("operation", "inactivate")
            .description("Total de regionais inativadas na sincronizacao")
            .register(registry);

        // =====================
        // Contadores de Seguranca
        // =====================
        this.rateLimitExceededCounter = Counter.builder("security_rate_limit_exceeded_total")
            .description("Total de requisicoes bloqueadas por rate limit")
            .register(registry);

        this.invalidMagicNumberCounter = Counter.builder("security_invalid_magic_number_total")
            .description("Total de uploads rejeitados por magic number invalido")
            .register(registry);

        this.validationErrorCounter = Counter.builder("security_validation_error_total")
            .description("Total de erros de validacao de entrada")
            .register(registry);

        // =====================
        // Contadores de WebSocket Auth
        // =====================
        this.wsAuthSuccessCounter = Counter.builder("websocket_auth_total")
            .tag("status", "success")
            .description("Total de autenticacoes WebSocket bem sucedidas")
            .register(registry);

        this.wsAuthFailureEmptyCounter = Counter.builder("websocket_auth_total")
            .tag("status", "failure")
            .tag("reason", "empty_ticket")
            .description("Total de falhas WebSocket por ticket vazio")
            .register(registry);

        this.wsAuthFailureNotFoundCounter = Counter.builder("websocket_auth_total")
            .tag("status", "failure")
            .tag("reason", "not_found")
            .description("Total de falhas WebSocket por ticket nao encontrado")
            .register(registry);

        this.wsAuthFailureExpiredCounter = Counter.builder("websocket_auth_total")
            .tag("status", "failure")
            .tag("reason", "expired")
            .description("Total de falhas WebSocket por ticket expirado")
            .register(registry);

        this.wsAuthFailureUsedCounter = Counter.builder("websocket_auth_total")
            .tag("status", "failure")
            .tag("reason", "already_used")
            .description("Total de falhas WebSocket por ticket ja utilizado")
            .register(registry);

        // =====================
        // Timers
        // =====================
        this.authTimer = Timer.builder("auth_login_duration_seconds")
            .description("Duracao do processo de login em segundos")
            .register(registry);

        this.regionalSyncTimer = Timer.builder("regional_sync_duration_seconds")
            .description("Duracao da sincronizacao de regionais em segundos")
            .register(registry);

        this.imagemUploadTimer = Timer.builder("imagem_upload_duration_seconds")
            .description("Duracao do upload de imagem em segundos")
            .register(registry);

        this.albumOperationTimer = Timer.builder("album_operation_duration_seconds")
            .description("Duracao de operacoes em albuns em segundos")
            .register(registry);

        this.artistaOperationTimer = Timer.builder("artista_operation_duration_seconds")
            .description("Duracao de operacoes em artistas em segundos")
            .register(registry);

        // =====================
        // Distribution Summaries
        // =====================
        this.imagemSizeSummary = DistributionSummary.builder("imagem_size_bytes")
            .description("Distribuicao de tamanho de imagens enviadas em bytes")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(registry);
    }

    // =====================
    // Metodos de Autenticacao
    // =====================
    public void recordLoginSuccess() {
        authSuccessCounter.increment();
    }

    public void recordLoginFailure() {
        authFailureCounter.increment();
    }

    public void recordLoginDuration(long durationMs) {
        authTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordTokenRefresh() {
        tokenRefreshCounter.increment();
    }

    public void recordTokenExpired() {
        tokenExpiredCounter.increment();
    }

    // =====================
    // Metodos de Artistas
    // =====================
    public void recordArtistaCreated() {
        artistaCreatedCounter.increment();
    }

    public void recordArtistaUpdated() {
        artistaUpdatedCounter.increment();
    }

    public void recordArtistaDeleted() {
        artistaDeletedCounter.increment();
    }

    public void recordArtistaOperationDuration(long durationMs) {
        artistaOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    // =====================
    // Metodos de Albuns
    // =====================
    public void recordAlbumCreated() {
        albumCreatedCounter.increment();
    }

    public void recordAlbumUpdated() {
        albumUpdatedCounter.increment();
    }

    public void recordAlbumDeleted() {
        albumDeletedCounter.increment();
    }

    public void recordAlbumOperationDuration(long durationMs) {
        albumOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    // =====================
    // Metodos de Imagens
    // =====================
    public void recordImagemUpload() {
        imagemUploadCounter.increment();
    }

    public void recordImagemUploadFailed() {
        imagemUploadFailedCounter.increment();
    }

    public void recordImagemDeleted() {
        imagemDeletedCounter.increment();
    }

    public void recordImagemUploadDuration(long durationMs) {
        imagemUploadTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordImagemSize(long sizeBytes) {
        imagemSizeSummary.record(sizeBytes);
    }

    // =====================
    // Metodos de Regionais
    // =====================
    public void recordRegionalSync() {
        regionalSyncCounter.increment();
    }

    public void recordRegionalSyncDuration(long durationMs) {
        regionalSyncTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordRegionalSyncResults(int inseridas, int alteradas, int inativadas) {
        for (int i = 0; i < inseridas; i++) {
            regionalSyncInseridaCounter.increment();
        }
        for (int i = 0; i < alteradas; i++) {
            regionalSyncAlteradaCounter.increment();
        }
        for (int i = 0; i < inativadas; i++) {
            regionalSyncInativadaCounter.increment();
        }
    }

    // =====================
    // Metodos de Seguranca
    // =====================
    public void recordRateLimitExceeded() {
        rateLimitExceededCounter.increment();
    }

    public void recordInvalidMagicNumber() {
        invalidMagicNumberCounter.increment();
    }

    public void recordValidationError() {
        validationErrorCounter.increment();
    }

    // =====================
    // Metodos de WebSocket Auth
    // =====================
    public void recordWebSocketAuthSuccess() {
        wsAuthSuccessCounter.increment();
    }

    public void recordWebSocketAuthFailure(String reason) {
        switch (reason) {
            case "empty_ticket" -> wsAuthFailureEmptyCounter.increment();
            case "not_found" -> wsAuthFailureNotFoundCounter.increment();
            case "expired" -> wsAuthFailureExpiredCounter.increment();
            case "already_used" -> wsAuthFailureUsedCounter.increment();
            default -> wsAuthFailureNotFoundCounter.increment();
        }
    }

    // =====================
    // Gauges Dinamicos
    // =====================
    public <T> void registerWebSocketConnectionsGauge(T stateObject, java.util.function.ToDoubleFunction<T> valueFunction) {
        registry.gauge("websocket_connections_active", stateObject, valueFunction);
    }
}
