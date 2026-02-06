package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.domain.model.Album;
import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.repository.AlbumRepository;
import br.gov.mt.seplag.domain.repository.ArtistaRepository;
import br.gov.mt.seplag.presentation.dto.album.AlbumDetailResponse;
import br.gov.mt.seplag.presentation.dto.album.AlbumImagemResponse;
import br.gov.mt.seplag.presentation.dto.album.AlbumRequest;
import br.gov.mt.seplag.presentation.dto.album.AlbumResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
import br.gov.mt.seplag.infrastructure.storage.StorageService;
import br.gov.mt.seplag.presentation.websocket.AlbumWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servico de albuns.
 * Concentra a logica de negocio relacionada a criacao, atualizacao e consulta de albuns.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AlbumService {

    private static final Logger LOG = Logger.getLogger(AlbumService.class);

    @Inject
    AlbumRepository albumRepository;

    @Inject
    ArtistaRepository artistaRepository;

    @Inject
    StorageService storageService;

    @Inject
    AlbumWebSocket albumWebSocket;

    /**
     * Lista albuns com filtros e paginacao.
     */
    public PageResponse<AlbumResponse> listar(String titulo, Integer anoLancamento, Long artistaId,
                                               String sortField, String sortDir, int page, int size) {
        LOG.debugf("Listando albuns - titulo: %s, anoLancamento: %s, artistaId: %s, page: %d, size: %d",
            titulo, anoLancamento, artistaId, page, size);

        List<Album> albuns = albumRepository.findWithFilters(titulo, anoLancamento, artistaId, sortField, sortDir, page, size);
        long total = albumRepository.countWithFilters(titulo, anoLancamento, artistaId);

        List<AlbumResponse> content = AlbumResponse.fromEntities(albuns);
        return PageResponse.of(content, page, size, total);
    }

    /**
     * Busca album por ID com detalhes e imagens.
     */
    public AlbumDetailResponse buscarPorId(Long id) {
        LOG.debugf("Buscando album por ID: %d", id);

        Album album = albumRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Album", id));

        // Gera URLs pre-assinadas para as imagens
        List<AlbumImagemResponse> imagensComUrls = new ArrayList<>();
        if (album.getImagens() != null) {
            album.getImagens().forEach(imagem -> {
                String presignedUrl = storageService.getPresignedUrl(imagem.getObjectKey());
                imagensComUrls.add(AlbumImagemResponse.fromEntity(imagem, presignedUrl));
            });
        }

        return AlbumDetailResponse.fromEntity(album, imagensComUrls);
    }

    /**
     * Cria um novo album.
     */
    @Transactional
    public AlbumResponse criar(AlbumRequest request) {
        LOG.infof("Criando album: %s", request.getTitulo());

        Album album = new Album();
        album.setTitulo(request.getTitulo());
        album.setAnoLancamento(request.getAnoLancamento());
        album.setDescricao(request.getDescricao());

        // Vincula artistas
        if (request.getArtistaIds() != null && !request.getArtistaIds().isEmpty()) {
            Set<Artista> artistas = new HashSet<>();
            for (Long artistaId : request.getArtistaIds()) {
                Artista artista = artistaRepository.findById(artistaId);
                if (artista == null) {
                    throw new BusinessException("Artista com ID " + artistaId + " nao encontrado");
                }
                artistas.add(artista);
                artista.getAlbuns().add(album);
            }
            album.setArtistas(artistas);
        }

        albumRepository.persist(album);

        LOG.infof("Album criado com sucesso - ID: %d", album.getId());

        // Notifica via WebSocket
        AlbumResponse response = AlbumResponse.fromEntity(album);
        albumWebSocket.notifyNewAlbum(response);

        return response;
    }

    /**
     * Atualiza um album existente.
     */
    @Transactional
    public AlbumResponse atualizar(Long id, AlbumRequest request) {
        LOG.infof("Atualizando album ID: %d", id);

        Album album = albumRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Album", id));

        album.setTitulo(request.getTitulo());
        album.setAnoLancamento(request.getAnoLancamento());
        album.setDescricao(request.getDescricao());

        // Atualiza artistas - remove os antigos e vincula os novos
        if (request.getArtistaIds() != null) {
            for (Artista artista : album.getArtistas()) {
                artista.getAlbuns().remove(album);
            }
            album.getArtistas().clear();

            for (Long artistaId : request.getArtistaIds()) {
                Artista artista = artistaRepository.findById(artistaId);
                if (artista == null) {
                    throw new BusinessException("Artista com ID " + artistaId + " nao encontrado");
                }
                album.getArtistas().add(artista);
                artista.getAlbuns().add(album);
            }
        }

        albumRepository.persist(album);

        LOG.infof("Album atualizado com sucesso - ID: %d", album.getId());
        return AlbumResponse.fromEntity(album);
    }

    /**
     * Busca album por ID (entidade).
     */
    public Album buscarEntidadePorId(Long id) {
        return albumRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Album", id));
    }

    /**
     * Remove um album pelo ID.
     * Remove tambem as imagens associadas do MinIO e do banco.
     */
    @Transactional
    public void remover(Long id) {
        LOG.infof("Removendo album ID: %d", id);

        Album album = albumRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Album", id));

        // Remove imagens do MinIO
        if (album.getImagens() != null && !album.getImagens().isEmpty()) {
            LOG.debugf("Removendo %d imagens do album ID: %d", album.getImagens().size(), id);
            album.getImagens().forEach(imagem -> {
                try {
                    storageService.delete(imagem.getObjectKey());
                } catch (Exception e) {
                    LOG.warnf("Erro ao remover imagem %s do MinIO: %s", imagem.getObjectKey(), e.getMessage());
                }
            });
        }

        // Remove os vinculos com artistas
        if (album.getArtistas() != null) {
            for (Artista artista : album.getArtistas()) {
                artista.getAlbuns().remove(album);
            }
            album.getArtistas().clear();
        }

        albumRepository.delete(album);
        LOG.infof("Album removido com sucesso - ID: %d", id);
    }
}
