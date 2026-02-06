-- =============================================================================
-- V6: Criacao da tabela de regionais
-- Estrutura conforme edital: regional (id integer, nome varchar(200), ativo boolean)
-- O campo 'id' corresponde ao ID da regional na API externa.
-- =============================================================================

CREATE TABLE regional (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Indices para consultas frequentes
CREATE INDEX idx_regional_nome ON regional(nome);
CREATE INDEX idx_regional_ativo ON regional(ativo);

COMMENT ON TABLE regional IS 'Regionais da Policia Civil - sincronizada com API externa';
COMMENT ON COLUMN regional.id IS 'ID da regional conforme API externa';
COMMENT ON COLUMN regional.nome IS 'Nome da regional';
COMMENT ON COLUMN regional.ativo IS 'Indica se a regional esta ativa (false = inativada durante sincronizacao)';
