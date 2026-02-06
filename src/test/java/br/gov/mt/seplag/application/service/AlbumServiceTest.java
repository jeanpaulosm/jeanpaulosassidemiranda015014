package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.domain.model.AlbumImagem;
import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import br.gov.mt.seplag.domain.repository.AlbumRepository;
import br.gov.mt.seplag.domain.repository.ArtistaRepository;
import br.gov.mt.seplag.infrastructure.storage.StorageService;
import br.gov.mt.seplag.presentation.dto.album.AlbumDetailResponse;
import br.gov.mt.seplag.presentation.dto.album.AlbumRequest;
import br.gov.mt.seplag.presentation.dto.album.AlbumResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
import br.gov.mt.seplag.presentation.websocket.AlbumWebSocket;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para AlbumService.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("AlbumService - Testes Unitarios")
class AlbumServiceTest {

    @Inject
    AlbumService albumService;

    @InjectMock
    AlbumRepository albumRepository;

    @InjectMock
    ArtistaRepository artistaRepository;

    @InjectMock
    StorageService storageService;

    @InjectMock
    AlbumWebSocket albumWebSocket;

    private Album criarAlbum(Long id, String titulo, Integer anoLancamento) {
        Album album = new Album();
        album.setId(id);
        album.setTitulo(titulo);
        album.setAnoLancamento(anoLancamento);
        album.setDescricao("Descricao do " + titulo);
        album.setCreatedAt(LocalDateTime.now());
        album.setUpdatedAt(LocalDateTime.now());
        album.setArtistas(new HashSet<>());
        album.setImagens(new ArrayList<>());
        return album;
    }

    private Artista criarArtista(Long id, String nome) {
        Artista artista = new Artista();
        artista.setId(id);
        artista.setNome(nome);
        artista.setTipo(TipoArtista.CANTOR);
        artista.setAlbuns(new HashSet<>());
        return artista;
    }

    private AlbumRequest criarRequest(String titulo, Integer anoLancamento, List<Long> artistaIds) {
        AlbumRequest request = new AlbumRequest();
        request.setTitulo(titulo);
        request.setAnoLancamento(anoLancamento);
        request.setDescricao("Descricao do " + titulo);
        request.setArtistaIds(artistaIds);
        return request;
    }

    // ====================
    // TESTES DE LISTAGEM
    // ====================

    @Nested
    @DisplayName("Listagem de Albuns")
    class ListagemTests {

        @Test
        @DisplayName("Deve listar albuns com paginacao")
        void shouldListAlbunsWithPagination() {
            // Arrange
            List<Album> albuns = List.of(
                criarAlbum(1L, "Album A", 2020),
                criarAlbum(2L, "Album B", 2021)
            );
            when(albumRepository.findWithFilters(any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(albuns);
            when(albumRepository.countWithFilters(any(), any(), any())).thenReturn(2L);

            // Act
            PageResponse<AlbumResponse> result = albumService.listar(null, null, null, "titulo", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve filtrar albuns por titulo")
        void shouldFilterAlbunsByTitulo() {
            // Arrange
            Album album = criarAlbum(1L, "Abbey Road", 1969);
            when(albumRepository.findWithFilters(eq("Abbey"), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(album));
            when(albumRepository.countWithFilters(eq("Abbey"), any(), any())).thenReturn(1L);

            // Act
            PageResponse<AlbumResponse> result = albumService.listar("Abbey", null, null, "titulo", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitulo()).isEqualTo("Abbey Road");
        }

        @Test
        @DisplayName("Deve filtrar albuns por ano de lancamento")
        void shouldFilterAlbunsByAnoLancamento() {
            // Arrange
            Album album = criarAlbum(1L, "Album 2020", 2020);
            when(albumRepository.findWithFilters(any(), eq(2020), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(album));
            when(albumRepository.countWithFilters(any(), eq(2020), any())).thenReturn(1L);

            // Act
            PageResponse<AlbumResponse> result = albumService.listar(null, 2020, null, "titulo", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAnoLancamento()).isEqualTo(2020);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha albuns")
        void shouldReturnEmptyListWhenNoAlbuns() {
            // Arrange
            when(albumRepository.findWithFilters(any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(albumRepository.countWithFilters(any(), any(), any())).thenReturn(0L);

            // Act
            PageResponse<AlbumResponse> result = albumService.listar(null, null, null, "titulo", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // ====================
    // TESTES DE BUSCA POR ID
    // ====================

    @Nested
    @DisplayName("Busca por ID")
    class BuscaPorIdTests {

        @Test
        @DisplayName("Deve retornar album quando encontrado")
        void shouldReturnAlbumWhenFound() {
            // Arrange
            Album album = criarAlbum(1L, "Album Teste", 2020);
            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));

            // Act
            AlbumDetailResponse result = albumService.buscarPorId(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitulo()).isEqualTo("Album Teste");
        }

        @Test
        @DisplayName("Deve retornar album com presigned URLs para imagens")
        void shouldReturnAlbumWithPresignedUrlsForImages() {
            // Arrange
            Album album = criarAlbum(1L, "Album Com Imagem", 2020);
            AlbumImagem imagem = new AlbumImagem();
            imagem.setId(1L);
            imagem.setObjectKey("capas/test.jpg");
            imagem.setNomeOriginal("test.jpg");
            imagem.setContentType("image/jpeg");
            album.setImagens(List.of(imagem));

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            when(storageService.getPresignedUrl("capas/test.jpg")).thenReturn("http://presigned-url.com/test.jpg");

            // Act
            AlbumDetailResponse result = albumService.buscarPorId(1L);

            // Assert
            assertThat(result.getImagens()).hasSize(1);
            assertThat(result.getImagens().get(0).getUrl()).isEqualTo("http://presigned-url.com/test.jpg");
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando album nao encontrado")
        void shouldThrowResourceNotFoundExceptionWhenAlbumNotFound() {
            // Arrange
            when(albumRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Album")
                .hasMessageContaining("999");
        }
    }

    // ====================
    // TESTES DE CRIACAO
    // ====================

    @Nested
    @DisplayName("Criacao de Album")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar album com sucesso")
        void shouldCreateAlbumSuccessfully() {
            // Arrange
            AlbumRequest request = criarRequest("Novo Album", 2024, List.of(1L));
            Artista artista = criarArtista(1L, "Artista Teste");

            when(artistaRepository.findById(1L)).thenReturn(artista);
            doNothing().when(albumRepository).persist(any(Album.class));
            doNothing().when(albumWebSocket).notifyNewAlbum(any(AlbumResponse.class));

            // Act
            AlbumResponse result = albumService.criar(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitulo()).isEqualTo("Novo Album");
            assertThat(result.getAnoLancamento()).isEqualTo(2024);

            verify(albumWebSocket).notifyNewAlbum(any(AlbumResponse.class));
        }

        @Test
        @DisplayName("Deve criar album com multiplos artistas")
        void shouldCreateAlbumWithMultipleArtistas() {
            // Arrange
            AlbumRequest request = criarRequest("Album Colaboracao", 2024, List.of(1L, 2L));
            Artista artista1 = criarArtista(1L, "Artista 1");
            Artista artista2 = criarArtista(2L, "Artista 2");

            when(artistaRepository.findById(1L)).thenReturn(artista1);
            when(artistaRepository.findById(2L)).thenReturn(artista2);
            doNothing().when(albumRepository).persist(any(Album.class));
            doNothing().when(albumWebSocket).notifyNewAlbum(any(AlbumResponse.class));

            // Act
            AlbumResponse result = albumService.criar(request);

            // Assert
            assertThat(result).isNotNull();
            verify(artistaRepository, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando artista nao encontrado")
        void shouldThrowBusinessExceptionWhenArtistaNotFound() {
            // Arrange
            AlbumRequest request = criarRequest("Album Invalido", 2024, List.of(999L));
            when(artistaRepository.findById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> albumService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Artista com ID 999 nao encontrado");

            verify(albumRepository, never()).persist(any(Album.class));
            verify(albumWebSocket, never()).notifyNewAlbum(any(AlbumResponse.class));
        }

        @Test
        @DisplayName("Deve notificar WebSocket apos criar album")
        void shouldNotifyWebSocketAfterCreatingAlbum() {
            // Arrange
            AlbumRequest request = criarRequest("Album Notificado", 2024, List.of(1L));
            Artista artista = criarArtista(1L, "Artista");

            when(artistaRepository.findById(1L)).thenReturn(artista);
            doNothing().when(albumRepository).persist(any(Album.class));
            doNothing().when(albumWebSocket).notifyNewAlbum(any(AlbumResponse.class));

            // Act
            albumService.criar(request);

            // Assert
            ArgumentCaptor<AlbumResponse> captor = ArgumentCaptor.forClass(AlbumResponse.class);
            verify(albumWebSocket).notifyNewAlbum(captor.capture());
            assertThat(captor.getValue().getTitulo()).isEqualTo("Album Notificado");
        }
    }

    // ====================
    // TESTES DE ATUALIZACAO
    // ====================

    @Nested
    @DisplayName("Atualizacao de Album")
    class AtualizacaoTests {

        @Test
        @DisplayName("Deve atualizar album com sucesso")
        void shouldUpdateAlbumSuccessfully() {
            // Arrange
            Album album = criarAlbum(1L, "Titulo Antigo", 2020);
            AlbumRequest request = criarRequest("Titulo Novo", 2024, List.of(1L));
            Artista artista = criarArtista(1L, "Artista");

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            when(artistaRepository.findById(1L)).thenReturn(artista);
            doNothing().when(albumRepository).persist(any(Album.class));

            // Act
            AlbumResponse result = albumService.atualizar(1L, request);

            // Assert
            assertThat(result.getTitulo()).isEqualTo("Titulo Novo");
            assertThat(result.getAnoLancamento()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando album nao existe")
        void shouldThrowResourceNotFoundExceptionWhenAlbumNotExists() {
            // Arrange
            AlbumRequest request = criarRequest("Qualquer", 2024, List.of(1L));
            when(albumRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.atualizar(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Deve atualizar artistas do album")
        void shouldUpdateAlbumArtistas() {
            // Arrange
            Artista artistaAntigo = criarArtista(1L, "Artista Antigo");
            Album album = criarAlbum(1L, "Album", 2020);
            album.getArtistas().add(artistaAntigo);
            artistaAntigo.getAlbuns().add(album);

            Artista artistaNovo = criarArtista(2L, "Artista Novo");
            AlbumRequest request = criarRequest("Album", 2020, List.of(2L));

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            when(artistaRepository.findById(2L)).thenReturn(artistaNovo);
            doNothing().when(albumRepository).persist(any(Album.class));

            // Act
            albumService.atualizar(1L, request);

            // Assert
            verify(artistaRepository).findById(2L);
        }
    }

    // ====================
    // TESTES DE REMOCAO
    // ====================

    @Nested
    @DisplayName("Remocao de Album")
    class RemocaoTests {

        @Test
        @DisplayName("Deve remover album sem imagens")
        void shouldRemoveAlbumWithoutImages() {
            // Arrange
            Album album = criarAlbum(1L, "Album Sem Imagens", 2020);

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            doNothing().when(albumRepository).delete(album);

            // Act
            albumService.remover(1L);

            // Assert
            verify(albumRepository).delete(album);
            verify(storageService, never()).delete(anyString());
        }

        @Test
        @DisplayName("Deve remover album com imagens do MinIO")
        void shouldRemoveAlbumWithImagesFromMinIO() {
            // Arrange
            Album album = criarAlbum(1L, "Album Com Imagens", 2020);
            AlbumImagem imagem1 = new AlbumImagem();
            imagem1.setObjectKey("capas/img1.jpg");
            AlbumImagem imagem2 = new AlbumImagem();
            imagem2.setObjectKey("capas/img2.jpg");
            album.setImagens(List.of(imagem1, imagem2));

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            doNothing().when(storageService).delete(anyString());
            doNothing().when(albumRepository).delete(album);

            // Act
            albumService.remover(1L);

            // Assert
            verify(storageService).delete("capas/img1.jpg");
            verify(storageService).delete("capas/img2.jpg");
            verify(albumRepository).delete(album);
        }

        @Test
        @DisplayName("Deve remover vinculos com artistas ao remover album")
        void shouldRemoveArtistaLinksWhenRemovingAlbum() {
            // Arrange
            Artista artista = criarArtista(1L, "Artista");
            Album album = criarAlbum(1L, "Album", 2020);
            album.getArtistas().add(artista);
            artista.getAlbuns().add(album);

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            doNothing().when(albumRepository).delete(album);

            // Act
            albumService.remover(1L);

            // Assert
            assertThat(artista.getAlbuns()).doesNotContain(album);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando album nao existe")
        void shouldThrowResourceNotFoundExceptionWhenRemovingNonExistentAlbum() {
            // Arrange
            when(albumRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.remover(999L))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(albumRepository, never()).delete(any(Album.class));
        }

        @Test
        @DisplayName("Deve continuar remocao mesmo se falhar ao deletar imagem do MinIO")
        void shouldContinueRemovalEvenIfMinIODeleteFails() {
            // Arrange
            Album album = criarAlbum(1L, "Album", 2020);
            AlbumImagem imagem = new AlbumImagem();
            imagem.setObjectKey("capas/img.jpg");
            album.setImagens(List.of(imagem));

            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));
            doThrow(new RuntimeException("MinIO error")).when(storageService).delete(anyString());
            doNothing().when(albumRepository).delete(album);

            // Act - Nao deve lancar excecao
            albumService.remover(1L);

            // Assert
            verify(albumRepository).delete(album);
        }
    }

    // ====================
    // TESTES DE BUSCAR ENTIDADE
    // ====================

    @Nested
    @DisplayName("Buscar Entidade por ID")
    class BuscarEntidadeTests {

        @Test
        @DisplayName("Deve retornar entidade quando encontrada")
        void shouldReturnEntityWhenFound() {
            // Arrange
            Album album = criarAlbum(1L, "Album", 2020);
            when(albumRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(album));

            // Act
            Album result = albumService.buscarEntidadePorId(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando entidade nao encontrada")
        void shouldThrowResourceNotFoundExceptionWhenEntityNotFound() {
            // Arrange
            when(albumRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.buscarEntidadePorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
