-- =============================================================================
-- V1: Criacao da tabela de artistas
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- =============================================================================

CREATE TYPE tipo_artista AS ENUM ('CANTOR', 'BANDA');

CREATE TABLE artista (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    tipo tipo_artista NOT NULL,
    descricao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_artista_nome ON artista(nome);
CREATE INDEX idx_artista_tipo ON artista(tipo);

COMMENT ON TABLE artista IS 'Tabela de artistas (cantores e bandas)';
COMMENT ON COLUMN artista.tipo IS 'Tipo do artista: CANTOR ou BANDA';
