package br.gov.mt.seplag.domain.repository;

import br.gov.mt.seplag.domain.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repositorio para a entidade Usuario.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    /**
     * Busca um usuario pelo username.
     */
    public Optional<Usuario> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    /**
     * Busca um usuario ativo pelo username.
     * Usado no login pra garantir que o usuario nao esta desativado.
     */
    public Optional<Usuario> findByUsernameAndAtivo(String username) {
        return find("username = ?1 and ativo = true", username).firstResultOptional();
    }

    /**
     * Verifica se existe um usuario com o username informado.
     */
    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }
}
