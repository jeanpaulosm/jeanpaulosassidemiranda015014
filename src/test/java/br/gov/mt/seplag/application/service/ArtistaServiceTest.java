package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import br.gov.mt.seplag.domain.repository.ArtistaRepository;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaDetailResponse;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaRequest;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
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
 * Testes unitarios para ArtistaService.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("ArtistaService - Testes Unitarios")
class ArtistaServiceTest {

    @Inject
    ArtistaService artistaService;

    @InjectMock
    ArtistaRepository artistaRepository;

    private Artista criarArtista(Long id, String nome, TipoArtista tipo) {
        Artista artista = new Artista();
        artista.setId(id);
        artista.setNome(nome);
        artista.setTipo(tipo);
        artista.setDescricao("Descricao de " + nome);
        artista.setCreatedAt(LocalDateTime.now());
        artista.setUpdatedAt(LocalDateTime.now());
        artista.setAlbuns(new HashSet<>());
        return artista;
    }

    private ArtistaRequest criarRequest(String nome, TipoArtista tipo) {
        ArtistaRequest request = new ArtistaRequest();
        request.setNome(nome);
        request.setTipo(tipo);
        request.setDescricao("Descricao de " + nome);
        return request;
    }

    // ====================
    // TESTES DE LISTAGEM
    // ====================

    @Nested
    @DisplayName("Listagem de Artistas")
    class ListagemTests {

        @Test
        @DisplayName("Deve listar artistas com paginacao")
        void shouldListArtistasWithPagination() {
            // Arrange
            List<Artista> artistas = List.of(
                criarArtista(1L, "Artista A", TipoArtista.CANTOR),
                criarArtista(2L, "Artista B", TipoArtista.BANDA)
            );
            when(artistaRepository.findWithFilters(any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(artistas);
            when(artistaRepository.countWithFilters(any(), any())).thenReturn(2L);

            // Act
            PageResponse<ArtistaResponse> result = artistaService.listar(null, null, "nome", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Deve filtrar artistas por nome")
        void shouldFilterArtistasByNome() {
            // Arrange
            Artista artista = criarArtista(1L, "Beatles", TipoArtista.BANDA);
            when(artistaRepository.findWithFilters(eq("Beatles"), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(artista));
            when(artistaRepository.countWithFilters(eq("Beatles"), any())).thenReturn(1L);

            // Act
            PageResponse<ArtistaResponse> result = artistaService.listar("Beatles", null, "nome", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNome()).isEqualTo("Beatles");
        }

        @Test
        @DisplayName("Deve filtrar artistas por tipo")
        void shouldFilterArtistasByTipo() {
            // Arrange
            Artista artista = criarArtista(1L, "Solo Artist", TipoArtista.CANTOR);
            when(artistaRepository.findWithFilters(any(), eq(TipoArtista.CANTOR), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(artista));
            when(artistaRepository.countWithFilters(any(), eq(TipoArtista.CANTOR))).thenReturn(1L);

            // Act
            PageResponse<ArtistaResponse> result = artistaService.listar(null, TipoArtista.CANTOR, "nome", "asc", 0, 10);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTipo()).isEqualTo(TipoArtista.CANTOR);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha artistas")
        void shouldReturnEmptyListWhenNoArtistas() {
            // Arrange
            when(artistaRepository.findWithFilters(any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(artistaRepository.countWithFilters(any(), any())).thenReturn(0L);

            // Act
            PageResponse<ArtistaResponse> result = artistaService.listar(null, null, "nome", "asc", 0, 10);

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
        @DisplayName("Deve retornar artista quando encontrado")
        void shouldReturnArtistaWhenFound() {
            // Arrange
            Artista artista = criarArtista(1L, "The Beatles", TipoArtista.BANDA);
            when(artistaRepository.findByIdWithAlbuns(1L)).thenReturn(Optional.of(artista));

            // Act
            ArtistaDetailResponse result = artistaService.buscarPorId(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNome()).isEqualTo("The Beatles");
            assertThat(result.getTipo()).isEqualTo(TipoArtista.BANDA);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando artista nao encontrado")
        void shouldThrowResourceNotFoundExceptionWhenArtistaNotFound() {
            // Arrange
            when(artistaRepository.findByIdWithAlbuns(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artista")
                .hasMessageContaining("999");
        }
    }

    // ====================
    // TESTES DE CRIACAO
    // ====================

    @Nested
    @DisplayName("Criacao de Artista")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar artista com sucesso")
        void shouldCreateArtistaSuccessfully() {
            // Arrange
            ArtistaRequest request = criarRequest("Novo Artista", TipoArtista.CANTOR);
            when(artistaRepository.findByNome("Novo Artista")).thenReturn(Optional.empty());
            doNothing().when(artistaRepository).persist(any(Artista.class));

            // Act
            ArtistaResponse result = artistaService.criar(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNome()).isEqualTo("Novo Artista");
            assertThat(result.getTipo()).isEqualTo(TipoArtista.CANTOR);

            ArgumentCaptor<Artista> captor = ArgumentCaptor.forClass(Artista.class);
            verify(artistaRepository).persist(captor.capture());
            assertThat(captor.getValue().getNome()).isEqualTo("Novo Artista");
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando nome ja existe")
        void shouldThrowBusinessExceptionWhenNomeAlreadyExists() {
            // Arrange
            ArtistaRequest request = criarRequest("Artista Existente", TipoArtista.BANDA);
            Artista existente = criarArtista(1L, "Artista Existente", TipoArtista.BANDA);
            when(artistaRepository.findByNome("Artista Existente")).thenReturn(Optional.of(existente));

            // Act & Assert
            assertThatThrownBy(() -> artistaService.criar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe um artista com o nome");

            verify(artistaRepository, never()).persist(any(Artista.class));
        }

        @Test
        @DisplayName("Deve criar artista do tipo BANDA")
        void shouldCreateArtistaTipoBanda() {
            // Arrange
            ArtistaRequest request = criarRequest("Banda Rock", TipoArtista.BANDA);
            when(artistaRepository.findByNome("Banda Rock")).thenReturn(Optional.empty());
            doNothing().when(artistaRepository).persist(any(Artista.class));

            // Act
            ArtistaResponse result = artistaService.criar(request);

            // Assert
            assertThat(result.getTipo()).isEqualTo(TipoArtista.BANDA);
        }
    }

    // ====================
    // TESTES DE ATUALIZACAO
    // ====================

    @Nested
    @DisplayName("Atualizacao de Artista")
    class AtualizacaoTests {

        @Test
        @DisplayName("Deve atualizar artista com sucesso")
        void shouldUpdateArtistaSuccessfully() {
            // Arrange
            Artista artista = criarArtista(1L, "Nome Antigo", TipoArtista.CANTOR);
            ArtistaRequest request = criarRequest("Nome Novo", TipoArtista.BANDA);

            when(artistaRepository.findById(1L)).thenReturn(artista);
            when(artistaRepository.findByNome("Nome Novo")).thenReturn(Optional.empty());
            doNothing().when(artistaRepository).persist(any(Artista.class));

            // Act
            ArtistaResponse result = artistaService.atualizar(1L, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNome()).isEqualTo("Nome Novo");
            assertThat(result.getTipo()).isEqualTo(TipoArtista.BANDA);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando artista nao existe")
        void shouldThrowResourceNotFoundExceptionWhenArtistaNotExists() {
            // Arrange
            ArtistaRequest request = criarRequest("Qualquer", TipoArtista.CANTOR);
            when(artistaRepository.findById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> artistaService.atualizar(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando novo nome ja existe em outro artista")
        void shouldThrowBusinessExceptionWhenNewNomeAlreadyExistsInOtherArtista() {
            // Arrange
            Artista artista = criarArtista(1L, "Artista 1", TipoArtista.CANTOR);
            Artista outroArtista = criarArtista(2L, "Artista 2", TipoArtista.BANDA);
            ArtistaRequest request = criarRequest("Artista 2", TipoArtista.CANTOR);

            when(artistaRepository.findById(1L)).thenReturn(artista);
            when(artistaRepository.findByNome("Artista 2")).thenReturn(Optional.of(outroArtista));

            // Act & Assert
            assertThatThrownBy(() -> artistaService.atualizar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe outro artista com o nome");
        }

        @Test
        @DisplayName("Deve permitir atualizar mantendo o mesmo nome")
        void shouldAllowUpdateKeepingSameName() {
            // Arrange
            Artista artista = criarArtista(1L, "Mesmo Nome", TipoArtista.CANTOR);
            ArtistaRequest request = criarRequest("Mesmo Nome", TipoArtista.BANDA); // Mudando apenas o tipo

            when(artistaRepository.findById(1L)).thenReturn(artista);
            when(artistaRepository.findByNome("Mesmo Nome")).thenReturn(Optional.of(artista)); // Retorna o mesmo
            doNothing().when(artistaRepository).persist(any(Artista.class));

            // Act
            ArtistaResponse result = artistaService.atualizar(1L, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNome()).isEqualTo("Mesmo Nome");
            assertThat(result.getTipo()).isEqualTo(TipoArtista.BANDA);
        }
    }

    // ====================
    // TESTES DE REMOCAO
    // ====================

    @Nested
    @DisplayName("Remocao de Artista")
    class RemocaoTests {

        @Test
        @DisplayName("Deve remover artista sem albuns associados")
        void shouldRemoveArtistaWithoutAlbuns() {
            // Arrange
            Artista artista = criarArtista(1L, "Artista Sem Albuns", TipoArtista.CANTOR);
            artista.setAlbuns(new HashSet<>()); // Sem albuns

            when(artistaRepository.findByIdWithAlbuns(1L)).thenReturn(Optional.of(artista));
            doNothing().when(artistaRepository).delete(artista);

            // Act
            artistaService.remover(1L);

            // Assert
            verify(artistaRepository).delete(artista);
        }

        @Test
        @DisplayName("Deve lancar BusinessException quando artista possui albuns")
        void shouldThrowBusinessExceptionWhenArtistaHasAlbuns() {
            // Arrange
            Artista artista = criarArtista(1L, "Artista Com Albuns", TipoArtista.BANDA);
            Album album = new Album();
            album.setId(1L);
            album.setTitulo("Album Teste");
            artista.setAlbuns(new HashSet<>(Set.of(album)));

            when(artistaRepository.findByIdWithAlbuns(1L)).thenReturn(Optional.of(artista));

            // Act & Assert
            assertThatThrownBy(() -> artistaService.remover(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nao e possivel remover o artista")
                .hasMessageContaining("album(ns) associado(s)");

            verify(artistaRepository, never()).delete(any(Artista.class));
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando artista nao existe")
        void shouldThrowResourceNotFoundExceptionWhenRemovingNonExistentArtista() {
            // Arrange
            when(artistaRepository.findByIdWithAlbuns(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.remover(999L))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(artistaRepository, never()).delete(any(Artista.class));
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
            Artista artista = criarArtista(1L, "Artista", TipoArtista.CANTOR);
            when(artistaRepository.findById(1L)).thenReturn(artista);

            // Act
            Artista result = artistaService.buscarEntidadePorId(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando entidade nao encontrada")
        void shouldThrowResourceNotFoundExceptionWhenEntityNotFound() {
            // Arrange
            when(artistaRepository.findById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> artistaService.buscarEntidadePorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
