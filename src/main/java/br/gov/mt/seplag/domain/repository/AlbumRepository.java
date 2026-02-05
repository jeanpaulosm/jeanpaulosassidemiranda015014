package br.gov.mt.seplag.domain.repository;

import br.gov.mt.seplag.domain.model.Album;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para a entidade Album.
 * Aqui precisei usar EntityManager direto em alguns metodos por causa
 * dos joins com a tabela de relacionamento N:N.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AlbumRepository implements PanacheRepository<Album> {

    /**
     * Busca albuns com paginacao e filtros.
     * A query eh montada dinamicamente pra suportar combinacao de filtros opcionais.
     *
     * @param titulo        filtro por titulo (parcial, case-insensitive)
     * @param anoLancamento filtro por ano de lancamento
     * @param artistaId     filtro por ID do artista
     * @param sortField     campo para ordenacao
     * @param sortDir       direcao da ordenacao
     * @param page          numero da pagina
     * @param size          tamanho da pagina
     * @return lista de albuns
     */
    public List<Album> findWithFilters(String titulo, Integer anoLancamento, Long artistaId,
                                        String sortField, String sortDir, int page, int size) {
        StringBuilder query = new StringBuilder("SELECT DISTINCT al FROM Album al LEFT JOIN al.artistas ar WHERE 1=1");

        if (titulo != null && !titulo.isBlank()) {
            query.append(" AND lower(al.titulo) LIKE lower(concat('%', :titulo, '%'))");
        }
        if (anoLancamento != null) {
            query.append(" AND al.anoLancamento = :anoLancamento");
        }
        if (artistaId != null) {
            query.append(" AND ar.id = :artistaId");
        }

        String orderField = sortField != null ? "al." + sortField : "al.titulo";
        String orderDir = sortDir != null && sortDir.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        query.append(" ORDER BY ").append(orderField).append(" ").append(orderDir);

        var panacheQuery = getEntityManager().createQuery(query.toString(), Album.class);

        if (titulo != null && !titulo.isBlank()) {
            panacheQuery.setParameter("titulo", titulo);
        }
        if (anoLancamento != null) {
            panacheQuery.setParameter("anoLancamento", anoLancamento);
        }
        if (artistaId != null) {
            panacheQuery.setParameter("artistaId", artistaId);
        }

        return panacheQuery
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }

    /**
     * Conta albuns com filtros (pra paginacao).
     */
    public long countWithFilters(String titulo, Integer anoLancamento, Long artistaId) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT al) FROM Album al LEFT JOIN al.artistas ar WHERE 1=1");

        if (titulo != null && !titulo.isBlank()) {
            query.append(" AND lower(al.titulo) LIKE lower(concat('%', :titulo, '%'))");
        }
        if (anoLancamento != null) {
            query.append(" AND al.anoLancamento = :anoLancamento");
        }
        if (artistaId != null) {
            query.append(" AND ar.id = :artistaId");
        }

        var panacheQuery = getEntityManager().createQuery(query.toString(), Long.class);

        if (titulo != null && !titulo.isBlank()) {
            panacheQuery.setParameter("titulo", titulo);
        }
        if (anoLancamento != null) {
            panacheQuery.setParameter("anoLancamento", anoLancamento);
        }
        if (artistaId != null) {
            panacheQuery.setParameter("artistaId", artistaId);
        }

        return panacheQuery.getSingleResult();
    }

    /**
     * Busca album por ID com artistas e imagens carregados.
     * Dois fetch joins de uma vez pra evitar o N+1.
     */
    public Optional<Album> findByIdWithDetails(Long id) {
        return find("SELECT DISTINCT a FROM Album a " +
                    "LEFT JOIN FETCH a.artistas " +
                    "LEFT JOIN FETCH a.imagens " +
                    "WHERE a.id = ?1", id)
            .firstResultOptional();
    }

    /**
     * Busca albuns por artista.
     */
    public List<Album> findByArtistaId(Long artistaId) {
        return list("SELECT DISTINCT a FROM Album a JOIN a.artistas ar WHERE ar.id = ?1", artistaId);
    }

    /**
     * Busca album por titulo.
     */
    public Optional<Album> findByTitulo(String titulo) {
        return find("titulo", titulo).firstResultOptional();
    }
}
