package br.gov.mt.seplag.domain.repository;

import br.gov.mt.seplag.domain.model.Regional;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repositorio para a entidade Regional.
 * O ID corresponde ao identificador da regional na API externa (Integer, nao Long).
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class RegionalRepository implements PanacheRepositoryBase<Regional, Integer> {

    /**
     * Busca todas as regionais ativas ordenadas por nome.
     */
    public List<Regional> findAllAtivas() {
        return list("ativo = true ORDER BY nome");
    }

    /**
     * Busca todas as regionais inativas ordenadas por nome.
     */
    public List<Regional> findAllInativas() {
        return list("ativo = false ORDER BY nome");
    }

    /**
     * Busca todas as regionais ordenadas por nome.
     */
    public List<Regional> findAllOrdenadas() {
        return list("ORDER BY nome");
    }

    /**
     * Busca regional por ID.
     */
    public Optional<Regional> findByIdOptional(Integer id) {
        return find("id", id).firstResultOptional();
    }

    /**
     * Busca regional ativa por ID.
     */
    public Optional<Regional> findAtivaById(Integer id) {
        return find("id = ?1 AND ativo = true", id).firstResultOptional();
    }

    /**
     * Retorna um mapa de todas as regionais indexadas por ID.
     * Usado na sincronizacao para lookups O(1).
     */
    public Map<Integer, Regional> findAllAsMap() {
        return listAll().stream()
            .collect(Collectors.toMap(Regional::getId, r -> r, (r1, r2) -> r1));
    }

    /**
     * Inativa todas as regionais cujos IDs nao estao no conjunto fornecido.
     * Usado para inativar regionais ausentes no endpoint externo.
     */
    public int inativarAusentes(Set<Integer> idsPresentes) {
        if (idsPresentes == null || idsPresentes.isEmpty()) {
            return update("ativo = false WHERE ativo = true");
        }
        return update("ativo = false WHERE ativo = true AND id NOT IN ?1", idsPresentes);
    }

    /**
     * Conta o total de regionais ativas.
     */
    public long countAtivas() {
        return count("ativo", true);
    }

    /**
     * Conta o total de regionais inativas.
     */
    public long countInativas() {
        return count("ativo", false);
    }
}
