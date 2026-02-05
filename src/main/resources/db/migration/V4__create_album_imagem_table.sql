-- =============================================================================
-- V4: Criacao da tabela de imagens de albuns
-- Imagens ficam no MinIO, aqui so guardamos os metadados
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- =============================================================================

CREATE TABLE album_imagem (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    nome_original VARCHAR(300) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_imagem_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

CREATE INDEX idx_album_imagem_album_id ON album_imagem(album_id);
CREATE INDEX idx_album_imagem_object_key ON album_imagem(object_key);

COMMENT ON TABLE album_imagem IS 'Tabela de imagens de capas de albuns armazenadas no MinIO';
COMMENT ON COLUMN album_imagem.object_key IS 'Chave do objeto no MinIO/S3';
