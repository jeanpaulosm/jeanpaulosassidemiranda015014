package br.gov.mt.seplag.infrastructure.storage;

import br.gov.mt.seplag.domain.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * Servico para armazenamento de arquivos no MinIO/S3.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class StorageService {

    private static final Logger LOG = Logger.getLogger(StorageService.class);

    @Inject
    S3Client s3Client;

    @ConfigProperty(name = "app.storage.bucket-name", defaultValue = "albuns-capas")
    String bucketName;

    @ConfigProperty(name = "app.storage.presigned-url-expiration-minutes", defaultValue = "30")
    int presignedUrlExpirationMinutes;

    @ConfigProperty(name = "quarkus.s3.endpoint-override")
    String endpoint;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String accessKey;

    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String secretKey;

    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;

    @PostConstruct
    void init() {
        try {
            createBucketIfNotExists();
        } catch (Exception e) {
            LOG.warn("Nao foi possivel criar o bucket na inicializacao. Sera criado no primeiro upload.", e);
        }
    }

    /**
     * Cria o bucket se nao existir.
     */
    private void createBucketIfNotExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
            s3Client.headBucket(headBucketRequest);
            LOG.infof("Bucket '%s' ja existe", bucketName);
        } catch (NoSuchBucketException e) {
            LOG.infof("Criando bucket '%s'", bucketName);
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
            s3Client.createBucket(createBucketRequest);
            LOG.infof("Bucket '%s' criado com sucesso", bucketName);
        }
    }

    /**
     * Faz upload de um arquivo.
     *
     * @param inputStream conteudo do arquivo
     * @param contentType tipo do conteudo
     * @param originalFileName nome original do arquivo
     * @param size tamanho em bytes
     * @return chave do objeto no S3
     */
    public String upload(InputStream inputStream, String contentType, String originalFileName, long size) {
        try {
            createBucketIfNotExists();

            String objectKey = generateObjectKey(originalFileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));

            LOG.infof("Arquivo '%s' enviado com sucesso. Object key: %s", originalFileName, objectKey);
            return objectKey;
        } catch (Exception e) {
            LOG.error("Erro ao fazer upload do arquivo", e);
            throw new BusinessException("Erro ao fazer upload do arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Gera uma URL pre-assinada para download do arquivo.
     *
     * @param objectKey chave do objeto no S3
     * @return URL pre-assinada
     */
    public String getPresignedUrl(String objectKey) {
        try {
            S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(() -> software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey))
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            presigner.close();

            LOG.debugf("URL pre-assinada gerada para object key: %s", objectKey);
            return url;
        } catch (Exception e) {
            LOG.error("Erro ao gerar URL pre-assinada", e);
            throw new BusinessException("Erro ao gerar URL de download: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um arquivo.
     *
     * @param objectKey chave do objeto no S3
     */
    public void delete(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            s3Client.deleteObject(deleteRequest);
            LOG.infof("Arquivo deletado com sucesso. Object key: %s", objectKey);
        } catch (Exception e) {
            LOG.error("Erro ao deletar arquivo", e);
            throw new BusinessException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Gera uma chave unica para o objeto.
     */
    private String generateObjectKey(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        return "capas/" + UUID.randomUUID() + extension;
    }
}
