-- =============================================================================
-- V2: Criacao da tabela de albuns
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- =============================================================================

CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(300) NOT NULL,
    ano_lancamento INTEGER,
    descricao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_album_titulo ON album(titulo);
CREATE INDEX idx_album_ano_lancamento ON album(ano_lancamento);

COMMENT ON TABLE album IS 'Tabela de albuns musicais';
COMMENT ON COLUMN album.ano_lancamento IS 'Ano de lancamento do album';
