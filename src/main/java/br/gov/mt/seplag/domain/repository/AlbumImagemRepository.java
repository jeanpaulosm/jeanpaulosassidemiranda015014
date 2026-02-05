package br.gov.mt.seplag.domain.repository;

import br.gov.mt.seplag.domain.model.AlbumImagem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para a entidade AlbumImagem.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class AlbumImagemRepository implements PanacheRepository<AlbumImagem> {

    /**
     * Busca imagens por album ID.
     */
    public List<AlbumImagem> findByAlbumId(Long albumId) {
        return list("album.id", albumId);
    }

    /**
     * Busca imagem por object key.
     */
    public Optional<AlbumImagem> findByObjectKey(String objectKey) {
        return find("objectKey", objectKey).firstResultOptional();
    }

    /**
     * Deleta todas as imagens de um album.
     */
    public long deleteByAlbumId(Long albumId) {
        return delete("album.id", albumId);
    }

    /**
     * Conta imagens de um album.
     */
    public long countByAlbumId(Long albumId) {
        return count("album.id", albumId);
    }
}
