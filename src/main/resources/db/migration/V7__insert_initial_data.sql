-- =============================================================================
-- V7: Insercao de dados iniciais (exemplos do edital)
-- PSS Backend MT - Processo Seletivo Simplificado 2026
-- =============================================================================

-- Artistas
INSERT INTO artista (nome, tipo, descricao) VALUES
('Serj Tankian', 'CANTOR', 'Cantor e compositor armeno-americano, vocalista da banda System of a Down'),
('Mike Shinoda', 'CANTOR', 'Cantor, compositor e produtor americano, co-vocalista do Linkin Park'),
('Michel Telo', 'CANTOR', 'Cantor e compositor brasileiro de musica sertaneja'),
('Guns N'' Roses', 'BANDA', 'Banda americana de hard rock formada em Los Angeles em 1985');

-- Albuns de Serj Tankian
INSERT INTO album (titulo, ano_lancamento, descricao) VALUES
('Harakiri', 2012, 'Terceiro album de estudio de Serj Tankian'),
('Black Blooms', 2019, 'EP colaborativo de Serj Tankian'),
('The Rough Dog', 2020, 'Single de Serj Tankian');

-- Albuns de Mike Shinoda
INSERT INTO album (titulo, ano_lancamento, descricao) VALUES
('The Rising Tied', 2005, 'Album de estreia do projeto Fort Minor de Mike Shinoda'),
('Post Traumatic', 2018, 'Album solo de Mike Shinoda'),
('Post Traumatic EP', 2018, 'EP de Mike Shinoda'),
('Where''d You Go', 2006, 'Single de Fort Minor');

-- Albuns de Michel Telo
INSERT INTO album (titulo, ano_lancamento, descricao) VALUES
('Bem Sertanejo', 2014, 'Album de estudio de Michel Telo'),
('Bem Sertanejo - O Show (Ao Vivo)', 2015, 'Album ao vivo de Michel Telo'),
('Bem Sertanejo - (1a Temporada) - EP', 2014, 'EP de Michel Telo');

-- Albuns de Guns N' Roses
INSERT INTO album (titulo, ano_lancamento, descricao) VALUES
('Use Your Illusion I', 1991, 'Quarto album de estudio do Guns N'' Roses'),
('Use Your Illusion II', 1991, 'Quinto album de estudio do Guns N'' Roses'),
('Greatest Hits', 2004, 'Coletanea do Guns N'' Roses');

-- Relacionamentos Artista-Album
-- Serj Tankian
INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al
WHERE a.nome = 'Serj Tankian' AND al.titulo IN ('Harakiri', 'Black Blooms', 'The Rough Dog');

-- Mike Shinoda
INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al
WHERE a.nome = 'Mike Shinoda' AND al.titulo IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go');

-- Michel Telo
INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al
WHERE a.nome = 'Michel Telo' AND al.titulo IN ('Bem Sertanejo', 'Bem Sertanejo - O Show (Ao Vivo)', 'Bem Sertanejo - (1a Temporada) - EP');

-- Guns N' Roses
INSERT INTO artista_album (artista_id, album_id)
SELECT a.id, al.id FROM artista a, album al
WHERE a.nome = 'Guns N'' Roses' AND al.titulo IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits');
