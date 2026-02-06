package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.domain.model.AlbumImagem;
import br.gov.mt.seplag.domain.repository.AlbumImagemRepository;
import br.gov.mt.seplag.domain.repository.AlbumRepository;
import br.gov.mt.seplag.infrastructure.storage.StorageService;
import br.gov.mt.seplag.presentation.dto.album.AlbumImagemResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Servico para gerenciamento de imagens de albuns.
 *
 * Validacoes implementadas:
 * 1. Content-Type (MIME type declarado)
 * 2. Magic Numbers (assinatura real do arquivo)
 * 3. Tamanho maximo do arquivo
 *
 * Tipos suportados: JPEG, PNG, GIF, WebP
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AlbumImagemService {

    private static final Logger LOG = Logger.getLogger(AlbumImagemService.class);

    /**
     * Content-Types permitidos para upload.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Tamanho maximo do arquivo: 10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Magic numbers (assinaturas de arquivo) para validacao de tipo real.
     * Previne ataques onde o content-type e falsificado.
     */
    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.of(
        "image/jpeg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        },
        "image/jpg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        },
        "image/png", new byte[][]{
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        },
        "image/gif", new byte[][]{
            {0x47, 0x49, 0x46, 0x38, 0x37, 0x61},  // GIF87a
            {0x47, 0x49, 0x46, 0x38, 0x39, 0x61}   // GIF89a
        },
        "image/webp", new byte[][]{
            {0x52, 0x49, 0x46, 0x46}  // RIFF (WebP comeca com RIFF)
        }
    );

    @Inject
    AlbumImagemRepository albumImagemRepository;

    @Inject
    AlbumRepository albumRepository;

    @Inject
    StorageService storageService;

    /**
     * Faz upload de uma ou mais imagens para um album.
     */
    @Transactional
    public List<AlbumImagemResponse> uploadImagens(Long albumId, List<FileUpload> files) {
        LOG.infof("Upload de %d imagens para album ID: %d", files.size(), albumId);

        Album album = albumRepository.findById(albumId);
        if (album == null) {
            throw new ResourceNotFoundException("Album", albumId);
        }

        List<AlbumImagemResponse> responses = new ArrayList<>();

        for (FileUpload file : files) {
            validateFile(file);

            try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
                String contentType = file.contentType();
                String originalFileName = file.fileName();
                long size = Files.size(file.uploadedFile());

                // Upload para o MinIO
                String objectKey = storageService.upload(inputStream, contentType, originalFileName, size);

                // Salva metadados no banco
                AlbumImagem imagem = new AlbumImagem();
                imagem.setAlbum(album);
                imagem.setObjectKey(objectKey);
                imagem.setNomeOriginal(originalFileName);
                imagem.setContentType(contentType);
                imagem.setTamanhoBytes(size);

                albumImagemRepository.persist(imagem);

                // Gera URL pre-assinada
                String presignedUrl = storageService.getPresignedUrl(objectKey);
                responses.add(AlbumImagemResponse.fromEntity(imagem, presignedUrl));

                LOG.infof("Imagem '%s' enviada com sucesso para album ID: %d", originalFileName, albumId);
            } catch (IOException e) {
                LOG.error("Erro ao processar arquivo de upload", e);
                throw new BusinessException("Erro ao processar arquivo: " + e.getMessage());
            }
        }

        return responses;
    }

    /**
     * Lista imagens de um album com URLs pre-assinadas.
     */
    public List<AlbumImagemResponse> listarImagens(Long albumId) {
        LOG.debugf("Listando imagens do album ID: %d", albumId);

        if (albumRepository.findById(albumId) == null) {
            throw new ResourceNotFoundException("Album", albumId);
        }

        List<AlbumImagem> imagens = albumImagemRepository.findByAlbumId(albumId);
        List<AlbumImagemResponse> responses = new ArrayList<>();

        for (AlbumImagem imagem : imagens) {
            String presignedUrl = storageService.getPresignedUrl(imagem.getObjectKey());
            responses.add(AlbumImagemResponse.fromEntity(imagem, presignedUrl));
        }

        return responses;
    }

    /**
     * Deleta uma imagem.
     */
    @Transactional
    public void deletarImagem(Long imagemId) {
        LOG.infof("Deletando imagem ID: %d", imagemId);

        AlbumImagem imagem = albumImagemRepository.findById(imagemId);
        if (imagem == null) {
            throw new ResourceNotFoundException("Imagem", imagemId);
        }

        // Deleta do MinIO
        storageService.delete(imagem.getObjectKey());

        // Deleta do banco
        albumImagemRepository.delete(imagem);

        LOG.infof("Imagem ID: %d deletada com sucesso", imagemId);
    }

    /**
     * Valida o arquivo de upload.
     *
     * Validacoes:
     * 1. Arquivo nao nulo
     * 2. Content-Type permitido
     * 3. Tamanho maximo
     * 4. Magic numbers (assinatura real do arquivo)
     */
    private void validateFile(FileUpload file) {
        if (file == null || file.uploadedFile() == null) {
            throw new BusinessException("Arquivo nao pode ser nulo");
        }

        String contentType = file.contentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Tipo de arquivo nao permitido. Tipos aceitos: " + ALLOWED_CONTENT_TYPES);
        }

        try {
            long size = Files.size(file.uploadedFile());
            if (size > MAX_FILE_SIZE) {
                throw new BusinessException("Arquivo muito grande. Tamanho maximo permitido: 10MB");
            }

            // Validacao de magic numbers para prevenir falsificacao de content-type
            validateMagicNumbers(file.uploadedFile(), contentType.toLowerCase());

        } catch (IOException e) {
            throw new BusinessException("Erro ao verificar arquivo: " + e.getMessage());
        }
    }

    /**
     * Valida se os magic numbers do arquivo correspondem ao content-type declarado.
     * Previne ataques onde um arquivo malicioso e enviado com content-type falsificado.
     *
     * @param filePath caminho do arquivo temporario
     * @param contentType tipo MIME declarado
     * @throws IOException se houver erro ao ler o arquivo
     * @throws BusinessException se os magic numbers nao corresponderem
     */
    private void validateMagicNumbers(java.nio.file.Path filePath, String contentType) throws IOException {
        byte[][] expectedMagicNumbers = MAGIC_NUMBERS.get(contentType);
        if (expectedMagicNumbers == null) {
            LOG.warnf("Nenhum magic number definido para content-type: %s", contentType);
            return;
        }

        // Encontra o maior magic number para determinar quantos bytes ler
        int maxLength = 0;
        for (byte[] magic : expectedMagicNumbers) {
            maxLength = Math.max(maxLength, magic.length);
        }

        // Le os primeiros bytes do arquivo
        byte[] fileHeader = new byte[maxLength];
        try (InputStream is = Files.newInputStream(filePath)) {
            int bytesRead = is.read(fileHeader);
            if (bytesRead < maxLength) {
                throw new BusinessException("Arquivo muito pequeno para ser uma imagem valida");
            }
        }

        // Verifica se algum dos magic numbers esperados corresponde
        boolean isValid = false;
        for (byte[] expectedMagic : expectedMagicNumbers) {
            if (startsWithMagicNumber(fileHeader, expectedMagic)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            LOG.warnf("Magic number invalido para content-type %s. Possivel tentativa de falsificacao.", contentType);
            throw new BusinessException("Conteudo do arquivo nao corresponde ao tipo declarado. " +
                "Verifique se o arquivo e realmente uma imagem do tipo " + contentType);
        }

        LOG.debugf("Magic number validado com sucesso para content-type: %s", contentType);
    }

    /**
     * Verifica se o cabecalho do arquivo comeca com o magic number esperado.
     */
    private boolean startsWithMagicNumber(byte[] fileHeader, byte[] magicNumber) {
        if (fileHeader.length < magicNumber.length) {
            return false;
        }
        for (int i = 0; i < magicNumber.length; i++) {
            if (fileHeader[i] != magicNumber[i]) {
                return false;
            }
        }
        return true;
    }
}
