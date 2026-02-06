package br.gov.mt.seplag.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios para MetricsService.
 *
 * @author Jean Paulo Sassi de Miranda
 */
class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordLoginSuccessShouldIncrementCounter() {
        metricsService.recordLoginSuccess();
        metricsService.recordLoginSuccess();

        Counter counter = meterRegistry.find("auth_login_total")
            .tag("status", "success")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordLoginFailureShouldIncrementCounter() {
        metricsService.recordLoginFailure();

        Counter counter = meterRegistry.find("auth_login_total")
            .tag("status", "failure")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordAlbumCreatedShouldIncrementCounter() {
        metricsService.recordAlbumCreated();
        metricsService.recordAlbumCreated();
        metricsService.recordAlbumCreated();

        Counter counter = meterRegistry.find("album_operations_total")
            .tag("operation", "create")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void recordArtistaCreatedShouldIncrementCounter() {
        metricsService.recordArtistaCreated();

        Counter counter = meterRegistry.find("artista_operations_total")
            .tag("operation", "create")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordImagemUploadShouldIncrementCounter() {
        metricsService.recordImagemUpload();
        metricsService.recordImagemUpload();

        Counter counter = meterRegistry.find("imagem_operations_total")
            .tag("operation", "upload")
            .tag("status", "success")
            .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordRegionalSyncShouldIncrementCounter() {
        metricsService.recordRegionalSync();

        Counter counter = meterRegistry.find("regional_sync_total").counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordRateLimitExceededShouldIncrementCounter() {
        metricsService.recordRateLimitExceeded();
        metricsService.recordRateLimitExceeded();
        metricsService.recordRateLimitExceeded();

        Counter counter = meterRegistry.find("security_rate_limit_exceeded_total").counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void recordLoginDurationShouldRecordTime() {
        metricsService.recordLoginDuration(150);
        metricsService.recordLoginDuration(200);

        Timer timer = meterRegistry.find("auth_login_duration_seconds").timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(350.0);
    }

    @Test
    void recordRegionalSyncDurationShouldRecordTime() {
        metricsService.recordRegionalSyncDuration(500);

        Timer timer = meterRegistry.find("regional_sync_duration_seconds").timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(500.0);
    }

    @Test
    void recordImagemUploadDurationShouldRecordTime() {
        metricsService.recordImagemUploadDuration(1000);
        metricsService.recordImagemUploadDuration(2000);

        Timer timer = meterRegistry.find("imagem_upload_duration_seconds").timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(3000.0);
    }

    @Test
    void registerWebSocketConnectionsGaugeShouldCreateGauge() {
        // Usa um holder para o valor do gauge
        int[] connectionCount = {5};
        metricsService.registerWebSocketConnectionsGauge(connectionCount, arr -> arr[0]);

        Double value = meterRegistry.find("websocket_connections_active").gauge().value();

        assertThat(value).isEqualTo(5.0);
    }
}
