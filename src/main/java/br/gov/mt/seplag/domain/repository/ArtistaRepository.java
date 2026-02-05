package br.gov.mt.seplag.domain.repository;

import br.gov.mt.seplag.domain.model.Artista;
import br.gov.mt.seplag.domain.model.TipoArtista;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para a entidade Artista.
 * Panache simplifica bastante o acesso a dados comparado com o JPA puro.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class ArtistaRepository implements PanacheRepository<Artista> {

    /**
     * Busca artistas com filtros e ordenacao.
     * Precisei montar a query dinamicamente por causa da combinacao opcional de filtros.
     *
     * @param nome      filtro por nome (parcial, case-insensitive)
     * @param tipo      filtro por tipo (CANTOR ou BANDA)
     * @param sortField campo para ordenacao
     * @param sortDir   direcao da ordenacao (asc ou desc)
     * @param page      numero da pagina (0-based)
     * @param size      tamanho da pagina
     * @return lista de artistas
     */
    public List<Artista> findWithFilters(String nome, TipoArtista tipo, String sortField, String sortDir, int page, int size) {
        StringBuilder query = new StringBuilder("1=1");

        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(nome) like lower(concat('%', ?1, '%'))");
        }
        if (tipo != null) {
            query.append(" and tipo = ?2");
        }

        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortField != null ? sortField : "nome").descending()
            : Sort.by(sortField != null ? sortField : "nome").ascending();

        if (nome != null && !nome.isBlank() && tipo != null) {
            return find(query.toString(), sort, nome, tipo).page(Page.of(page, size)).list();
        } else if (nome != null && !nome.isBlank()) {
            return find(query.toString(), sort, nome).page(Page.of(page, size)).list();
        } else if (tipo != null) {
            return find("tipo = ?1", sort, tipo).page(Page.of(page, size)).list();
        } else {
            return findAll(sort).page(Page.of(page, size)).list();
        }
    }

    /**
     * Conta artistas com filtros (necessario para paginacao).
     */
    public long countWithFilters(String nome, TipoArtista tipo) {
        if (nome != null && !nome.isBlank() && tipo != null) {
            return count("lower(nome) like lower(concat('%', ?1, '%')) and tipo = ?2", nome, tipo);
        } else if (nome != null && !nome.isBlank()) {
            return count("lower(nome) like lower(concat('%', ?1, '%'))", nome);
        } else if (tipo != null) {
            return count("tipo", tipo);
        } else {
            return count();
        }
    }

    /**
     * Busca artistas por nome (exato).
     * Usado para validar duplicidade antes de inserir.
     */
    public Optional<Artista> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }

    /**
     * Busca artistas por tipo.
     */
    public List<Artista> findByTipo(TipoArtista tipo) {
        return list("tipo", tipo);
    }
}
