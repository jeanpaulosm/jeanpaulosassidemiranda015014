-- =============================================================================
-- V5: Criacao da tabela de usuarios para autenticacao JWT
-- Processo Seletivo Simplificado 2026
-- =============================================================================

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_username ON usuario(username);
CREATE INDEX idx_usuario_ativo ON usuario(ativo);

COMMENT ON TABLE usuario IS 'Tabela de usuarios para autenticacao JWT';
COMMENT ON COLUMN usuario.role IS 'Papel do usuario: ADMIN ou USER';

-- =============================================================================
-- Usuarios padrao para autenticacao
-- Hashes BCrypt gerados com work factor 12 (padrao da aplicacao).
-- Senhas: admin=admin123, user=user123
-- =============================================================================

INSERT INTO usuario (username, password, nome, email, role, ativo) VALUES
('admin', '$2a$12$hil7IWxd03loF/1/SwBTW.4e0Xw36YgxEaS62M09w/GwbPM2x7RGy', 'Administrador', 'admin@seplag.mt.gov.br', 'ADMIN', true),
('user', '$2a$12$w1zNBFzLHtsgNUFKMbBFJeE9awaPMqqL3eIqADTR61GbP3RInA5se', 'Usuario Teste', 'user@seplag.mt.gov.br', 'USER', true);
