package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.domain.repository.AlbumImagemRepository;
import br.gov.mt.seplag.domain.repository.AlbumRepository;
import br.gov.mt.seplag.infrastructure.metrics.MetricsService;
import br.gov.mt.seplag.infrastructure.storage.StorageService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes de validacao especificos para upload de imagens.
 * Cobertura de cenarios de erro: magic numbers, tamanho, tipos invalidos.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("AlbumImagemService - Testes de Validacao de Upload")
class AlbumImagemValidationTest {

    @Inject
    AlbumImagemService albumImagemService;

    @InjectMock
    AlbumRepository albumRepository;

    @InjectMock
    AlbumImagemRepository albumImagemRepository;

    @InjectMock
    StorageService storageService;

    @InjectMock
    MetricsService metricsService;

    private Album albumMock;

    @BeforeEach
    void setUp() {
        albumMock = new Album();
        albumMock.setId(1L);
        albumMock.setTitulo("Album Teste");
    }

    // ====================
    // TESTES DE MAGIC NUMBER INVALIDO
    // ====================

    @Nested
    @DisplayName("Validacao de Magic Numbers")
    class MagicNumberValidationTests {

        @Test
        @DisplayName("Deve rejeitar arquivo JPEG com magic number invalido (arquivo texto disfarado)")
        void shouldRejectJpegWithInvalidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo texto disfarado de JPEG
            Path tempFile = Files.createTempFile("fake", ".jpg");
            Files.write(tempFile, "Este nao e um arquivo JPEG real".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/jpeg");
            when(mockFile.fileName()).thenReturn("fake-image.jpg");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo do arquivo nao corresponde ao tipo declarado");

            // Verifica que a metrica foi registrada
            verify(metricsService).recordInvalidMagicNumber();

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo PNG com magic number invalido")
        void shouldRejectPngWithInvalidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo com conteudo invalido para PNG
            Path tempFile = Files.createTempFile("fake", ".png");
            Files.write(tempFile, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/png");
            when(mockFile.fileName()).thenReturn("fake-image.png");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo do arquivo nao corresponde ao tipo declarado");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo GIF com magic number invalido")
        void shouldRejectGifWithInvalidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo com conteudo invalido para GIF
            Path tempFile = Files.createTempFile("fake", ".gif");
            Files.write(tempFile, "Not a GIF file content".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/gif");
            when(mockFile.fileName()).thenReturn("fake-image.gif");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo do arquivo nao corresponde ao tipo declarado");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo WebP com magic number invalido")
        void shouldRejectWebpWithInvalidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo com conteudo invalido para WebP
            Path tempFile = Files.createTempFile("fake", ".webp");
            Files.write(tempFile, "Not a WebP file".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/webp");
            when(mockFile.fileName()).thenReturn("fake-image.webp");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo do arquivo nao corresponde ao tipo declarado");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve aceitar arquivo JPEG com magic number valido")
        void shouldAcceptJpegWithValidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo JPEG valido (magic number: FF D8 FF)
            Path tempFile = Files.createTempFile("valid", ".jpg");
            byte[] jpegContent = new byte[100];
            jpegContent[0] = (byte) 0xFF;
            jpegContent[1] = (byte) 0xD8;
            jpegContent[2] = (byte) 0xFF;
            Files.write(tempFile, jpegContent);

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/jpeg");
            when(mockFile.fileName()).thenReturn("valid-image.jpg");

            when(storageService.upload(any(), eq("image/jpeg"), eq("valid-image.jpg"), anyLong()))
                .thenReturn("albums/1/uuid-valid-image.jpg");
            when(storageService.getPresignedUrl(anyString())).thenReturn("https://minio.local/presigned-url");

            // Act - should not throw
            albumImagemService.uploadImagens(1L, List.of(mockFile));

            // Assert - metrica de magic number invalido NAO deve ser chamada
            verify(metricsService, never()).recordInvalidMagicNumber();

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve aceitar arquivo PNG com magic number valido")
        void shouldAcceptPngWithValidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo PNG valido (magic number: 89 50 4E 47 0D 0A 1A 0A)
            Path tempFile = Files.createTempFile("valid", ".png");
            byte[] pngContent = new byte[100];
            pngContent[0] = (byte) 0x89;
            pngContent[1] = 0x50;  // P
            pngContent[2] = 0x4E;  // N
            pngContent[3] = 0x47;  // G
            pngContent[4] = 0x0D;
            pngContent[5] = 0x0A;
            pngContent[6] = 0x1A;
            pngContent[7] = 0x0A;
            Files.write(tempFile, pngContent);

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/png");
            when(mockFile.fileName()).thenReturn("valid-image.png");

            when(storageService.upload(any(), eq("image/png"), eq("valid-image.png"), anyLong()))
                .thenReturn("albums/1/uuid-valid-image.png");
            when(storageService.getPresignedUrl(anyString())).thenReturn("https://minio.local/presigned-url");

            // Act - should not throw
            albumImagemService.uploadImagens(1L, List.of(mockFile));

            // Assert
            verify(metricsService, never()).recordInvalidMagicNumber();

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve aceitar arquivo GIF87a com magic number valido")
        void shouldAcceptGif87aWithValidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo GIF87a valido
            Path tempFile = Files.createTempFile("valid", ".gif");
            byte[] gifContent = new byte[100];
            gifContent[0] = 0x47;  // G
            gifContent[1] = 0x49;  // I
            gifContent[2] = 0x46;  // F
            gifContent[3] = 0x38;  // 8
            gifContent[4] = 0x37;  // 7
            gifContent[5] = 0x61;  // a
            Files.write(tempFile, gifContent);

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/gif");
            when(mockFile.fileName()).thenReturn("valid-image.gif");

            when(storageService.upload(any(), eq("image/gif"), eq("valid-image.gif"), anyLong()))
                .thenReturn("albums/1/uuid-valid-image.gif");
            when(storageService.getPresignedUrl(anyString())).thenReturn("https://minio.local/presigned-url");

            // Act - should not throw
            albumImagemService.uploadImagens(1L, List.of(mockFile));

            // Assert
            verify(metricsService, never()).recordInvalidMagicNumber();

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve aceitar arquivo GIF89a com magic number valido")
        void shouldAcceptGif89aWithValidMagicNumber() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo GIF89a valido
            Path tempFile = Files.createTempFile("valid", ".gif");
            byte[] gifContent = new byte[100];
            gifContent[0] = 0x47;  // G
            gifContent[1] = 0x49;  // I
            gifContent[2] = 0x46;  // F
            gifContent[3] = 0x38;  // 8
            gifContent[4] = 0x39;  // 9
            gifContent[5] = 0x61;  // a
            Files.write(tempFile, gifContent);

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/gif");
            when(mockFile.fileName()).thenReturn("valid-image.gif");

            when(storageService.upload(any(), eq("image/gif"), eq("valid-image.gif"), anyLong()))
                .thenReturn("albums/1/uuid-valid-image.gif");
            when(storageService.getPresignedUrl(anyString())).thenReturn("https://minio.local/presigned-url");

            // Act - should not throw
            albumImagemService.uploadImagens(1L, List.of(mockFile));

            // Assert
            verify(metricsService, never()).recordInvalidMagicNumber();

            // Cleanup
            Files.deleteIfExists(tempFile);
        }
    }

    // ====================
    // TESTES DE TAMANHO DE ARQUIVO
    // ====================

    @Nested
    @DisplayName("Validacao de Tamanho de Arquivo")
    class FileSizeValidationTests {

        @Test
        @DisplayName("Deve rejeitar arquivo muito pequeno para ser imagem valida")
        void shouldRejectFileTooSmallToBeValidImage() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Cria arquivo muito pequeno (menos bytes que o magic number)
            Path tempFile = Files.createTempFile("tiny", ".jpg");
            Files.write(tempFile, new byte[]{(byte) 0xFF, (byte) 0xD8}); // Apenas 2 bytes

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/jpeg");
            when(mockFile.fileName()).thenReturn("tiny-image.jpg");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("muito pequeno");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }
    }

    // ====================
    // TESTES DE CONTENT-TYPE INVALIDO
    // ====================

    @Nested
    @DisplayName("Validacao de Content-Type")
    class ContentTypeValidationTests {

        @Test
        @DisplayName("Deve rejeitar arquivo PDF")
        void shouldRejectPdfFile() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".pdf");
            Files.write(tempFile, "PDF content".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("application/pdf");
            when(mockFile.fileName()).thenReturn("document.pdf");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo executavel")
        void shouldRejectExecutableFile() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".exe");
            Files.write(tempFile, "MZ".getBytes()); // Magic number de executavel Windows

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("application/octet-stream");
            when(mockFile.fileName()).thenReturn("program.exe");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo JavaScript")
        void shouldRejectJavaScriptFile() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".js");
            Files.write(tempFile, "alert('xss');".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("application/javascript");
            when(mockFile.fileName()).thenReturn("script.js");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo HTML")
        void shouldRejectHtmlFile() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".html");
            Files.write(tempFile, "<html><script>alert('xss')</script></html>".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("text/html");
            when(mockFile.fileName()).thenReturn("page.html");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo com content-type nulo")
        void shouldRejectFileWithNullContentType() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".unknown");
            Files.write(tempFile, "some content".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn(null);
            when(mockFile.fileName()).thenReturn("file.unknown");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("Deve rejeitar arquivo SVG (risco de XSS)")
        void shouldRejectSvgFile() throws IOException {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            Path tempFile = Files.createTempFile("test", ".svg");
            Files.write(tempFile, "<svg onload='alert(1)'></svg>".getBytes());

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(tempFile);
            when(mockFile.contentType()).thenReturn("image/svg+xml");
            when(mockFile.fileName()).thenReturn("image.svg");

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de arquivo nao permitido");

            // Cleanup
            Files.deleteIfExists(tempFile);
        }
    }

    // ====================
    // TESTES DE ARQUIVO NULO
    // ====================

    @Nested
    @DisplayName("Validacao de Arquivo Nulo")
    class NullFileValidationTests {

        @Test
        @DisplayName("Deve rejeitar FileUpload nulo")
        void shouldRejectNullFileUpload() {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of((FileUpload) null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nulo");
        }

        @Test
        @DisplayName("Deve rejeitar FileUpload com path nulo")
        void shouldRejectFileUploadWithNullPath() {
            // Arrange
            when(albumRepository.findById(1L)).thenReturn(albumMock);

            FileUpload mockFile = mock(FileUpload.class);
            when(mockFile.uploadedFile()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> albumImagemService.uploadImagens(1L, List.of(mockFile)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nulo");
        }
    }
}
