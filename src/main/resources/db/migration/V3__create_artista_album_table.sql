-- =============================================================================
-- V3: Criacao da tabela de relacionamento N:N entre artistas e albuns
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- =============================================================================

CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (artista_id, album_id),
    CONSTRAINT fk_artista_album_artista FOREIGN KEY (artista_id) REFERENCES artista(id) ON DELETE CASCADE,
    CONSTRAINT fk_artista_album_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

CREATE INDEX idx_artista_album_artista_id ON artista_album(artista_id);
CREATE INDEX idx_artista_album_album_id ON artista_album(album_id);

COMMENT ON TABLE artista_album IS 'Tabela de relacionamento N:N entre artistas e albuns';
