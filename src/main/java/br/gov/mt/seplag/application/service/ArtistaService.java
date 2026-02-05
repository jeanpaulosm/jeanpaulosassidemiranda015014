package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.exception.ResourceNotFoundException;
import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import br.gov.mt.seplag.domain.repository.ArtistaRepository;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaDetailResponse;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaRequest;
import br.gov.mt.seplag.presentation.dto.artista.ArtistaResponse;
import br.gov.mt.seplag.presentation.dto.common.PageResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Servico de artistas - contem a logica de negocio do CRUD.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class ArtistaService {

    private static final Logger LOG = Logger.getLogger(ArtistaService.class);

    @Inject
    ArtistaRepository artistaRepository;

    /**
     * Lista artistas com filtros e paginacao.
     */
    public PageResponse<ArtistaResponse> listar(String nome, TipoArtista tipo, String sortField,
                                                 String sortDir, int page, int size) {
        LOG.debugf("Listando artistas - nome: %s, tipo: %s, page: %d, size: %d", nome, tipo, page, size);

        List<Artista> artistas = artistaRepository.findWithFilters(nome, tipo, sortField, sortDir, page, size);
        long total = artistaRepository.countWithFilters(nome, tipo);

        List<ArtistaResponse> content = ArtistaResponse.fromEntities(artistas);
        return PageResponse.of(content, page, size, total);
    }

    /**
     * Busca artista por ID com detalhes.
     */
    public ArtistaDetailResponse buscarPorId(Long id) {
        LOG.debugf("Buscando artista por ID: %d", id);

        Artista artista = artistaRepository.findById(id);
        if (artista == null) {
            throw new ResourceNotFoundException("Artista", id);
        }

        return ArtistaDetailResponse.fromEntity(artista);
    }

    /**
     * Cria um novo artista.
     * Verifica duplicidade de nome antes de inserir.
     */
    @Transactional
    public ArtistaResponse criar(ArtistaRequest request) {
        LOG.infof("Criando artista: %s", request.getNome());

        // Nao permite dois artistas com o mesmo nome
        if (artistaRepository.findByNome(request.getNome()).isPresent()) {
            throw new BusinessException("Ja existe um artista com o nome: " + request.getNome());
        }

        Artista artista = new Artista();
        artista.setNome(request.getNome());
        artista.setTipo(request.getTipo());
        artista.setDescricao(request.getDescricao());

        artistaRepository.persist(artista);

        LOG.infof("Artista criado com sucesso - ID: %d", artista.getId());
        return ArtistaResponse.fromEntity(artista);
    }

    /**
     * Atualiza um artista existente.
     */
    @Transactional
    public ArtistaResponse atualizar(Long id, ArtistaRequest request) {
        LOG.infof("Atualizando artista ID: %d", id);

        Artista artista = artistaRepository.findById(id);
        if (artista == null) {
            throw new ResourceNotFoundException("Artista", id);
        }

        // Verifica se ja existe outro artista com o mesmo nome
        artistaRepository.findByNome(request.getNome())
            .filter(a -> !a.getId().equals(id))
            .ifPresent(a -> {
                throw new BusinessException("Ja existe outro artista com o nome: " + request.getNome());
            });

        artista.setNome(request.getNome());
        artista.setTipo(request.getTipo());
        artista.setDescricao(request.getDescricao());

        artistaRepository.persist(artista);

        LOG.infof("Artista atualizado com sucesso - ID: %d", artista.getId());
        return ArtistaResponse.fromEntity(artista);
    }

    /**
     * Busca artista por ID (retorna entidade, para uso interno).
     */
    public Artista buscarEntidadePorId(Long id) {
        Artista artista = artistaRepository.findById(id);
        if (artista == null) {
            throw new ResourceNotFoundException("Artista", id);
        }
        return artista;
    }

    /**
     * Remove um artista pelo ID.
     */
    @Transactional
    public void remover(Long id) {
        LOG.infof("Removendo artista ID: %d", id);

        Artista artista = artistaRepository.findById(id);
        if (artista == null) {
            throw new ResourceNotFoundException("Artista", id);
        }

        artistaRepository.delete(artista);
        LOG.infof("Artista removido com sucesso - ID: %d", id);
    }
}
