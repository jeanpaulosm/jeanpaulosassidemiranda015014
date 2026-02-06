-- =============================================================================
-- V8: Criacao de indices para otimizacao de consultas
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- Autor: Jean Paulo Sassi de Miranda
-- =============================================================================

-- Indices para tabela artista
CREATE INDEX IF NOT EXISTS idx_artista_nome ON artista(nome);
CREATE INDEX IF NOT EXISTS idx_artista_tipo ON artista(tipo);
CREATE INDEX IF NOT EXISTS idx_artista_nome_lower ON artista(LOWER(nome));

-- Indices para tabela album
CREATE INDEX IF NOT EXISTS idx_album_titulo ON album(titulo);
CREATE INDEX IF NOT EXISTS idx_album_ano_lancamento ON album(ano_lancamento);
CREATE INDEX IF NOT EXISTS idx_album_titulo_lower ON album(LOWER(titulo));
CREATE INDEX IF NOT EXISTS idx_album_created_at ON album(created_at DESC);

-- Indices para tabela regional
CREATE INDEX IF NOT EXISTS idx_regional_ativo ON regional(ativo);
CREATE INDEX IF NOT EXISTS idx_regional_id_ativo ON regional(id, ativo);

-- Indices para tabela album_imagem
CREATE INDEX IF NOT EXISTS idx_album_imagem_album_id ON album_imagem(album_id);

-- Indices para tabela artista_album (chaves compostas para joins)
CREATE INDEX IF NOT EXISTS idx_artista_album_artista_id ON artista_album(artista_id);
CREATE INDEX IF NOT EXISTS idx_artista_album_album_id ON artista_album(album_id);
