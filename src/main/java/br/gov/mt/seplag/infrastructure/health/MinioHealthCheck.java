package br.gov.mt.seplag.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * Health Check customizado para verificar conectividade com MinIO/S3.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    @Inject
    S3Client s3Client;

    @ConfigProperty(name = "app.storage.bucket-name", defaultValue = "albuns-capas")
    String bucketName;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
            .named("MinIO/S3 Storage")
            .withData("bucket", bucketName);

        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

            s3Client.headBucket(headBucketRequest);

            return responseBuilder
                .withData("status", "Bucket acessivel")
                .up()
                .build();
        } catch (Exception e) {
            return responseBuilder
                .withData("error", e.getMessage())
                .down()
                .build();
        }
    }
}
