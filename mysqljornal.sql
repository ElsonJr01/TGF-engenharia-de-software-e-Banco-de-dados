-- ================================

-- Desativa restrições e checagem de unicidade temporariamente
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ================================
-- Criação do schema/banco principal
-- ================================
CREATE SCHEMA IF NOT EXISTS `JornalTheClub` DEFAULT CHARACTER SET utf8mb4;
USE `JornalTheClub`;

-- ================================
-- Tabela USUARIO (equivalente a Pessoa)
-- ================================
CREATE TABLE IF NOT EXISTS `Usuario` (
  `idUsuario` BIGINT AUTO_INCREMENT PRIMARY KEY, -- Identificador único do usuário
  `nome` VARCHAR(100) NOT NULL,                  -- Nome completo do usuário
  `email` VARCHAR(120) NOT NULL UNIQUE,          -- E-mail único (login)
  `senha` VARCHAR(255) NOT NULL,                 -- Senha criptografada
  `tipo` ENUM('ADMIN', 'EDITOR', 'REDATOR', 'LEITOR') NOT NULL DEFAULT 'LEITOR', -- Perfis
  `bio` TEXT,                                    -- Biografia/descrição
  `foto` VARCHAR(255),                           -- URL/foto do usuário
  `ativo` BOOLEAN DEFAULT TRUE,                  -- Está ativo?
  `data_criacao` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `data_atualizacao` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- ================================
-- Tabela ENDERECO (Relacionada ao usuário)
-- ================================
CREATE TABLE IF NOT EXISTS `Endereco` (
  `idEndereco` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `bairro` VARCHAR(100),
  `rua` VARCHAR(100),
  `cep` VARCHAR(20),
  `usuario_idUsuario` BIGINT NOT NULL,
  FOREIGN KEY (`usuario_idUsuario`) REFERENCES `Usuario`(`idUsuario`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ================================
-- Tabela CATEGORIA
-- ================================
CREATE TABLE IF NOT EXISTS `Categoria` (
  `idCategoria` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `nome` VARCHAR(80) NOT NULL,
  `descricao` TEXT,
  `cor` VARCHAR(7) DEFAULT '#007bff',
  `ativo` BOOLEAN DEFAULT TRUE
) ENGINE = InnoDB;

-- ================================
-- Tabela ARTIGO pode ser ligado a pessoa/usuario)
-- ================================
CREATE TABLE IF NOT EXISTS `Artigo` (
  `idArtigo` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `titulo` VARCHAR(255) NOT NULL,
  `resumo` TEXT NOT NULL,
  `conteudo` LONGTEXT NOT NULL,
  `imagem_capa` VARCHAR(255),
  `status` ENUM('RASCUNHO', 'REVISAO', 'PUBLICADO', 'ARQUIVADO') NOT NULL DEFAULT 'RASCUNHO',
  `data_publicacao` DATETIME NULL,
  `data_criacao` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `data_atualizacao` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `visualizacoes` INT DEFAULT 0,
  `autor_idUsuario` BIGINT NOT NULL,
  `categoria_idCategoria` BIGINT NOT NULL,
  FOREIGN KEY (`autor_idUsuario`) REFERENCES `Usuario`(`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`categoria_idCategoria`) REFERENCES `Categoria`(`idCategoria`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ================================
-- Tabela COMENTARIO (ligada a Artigo e Usuario)
-- ================================
CREATE TABLE IF NOT EXISTS `Comentario` (
  `idComentario` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `artigo_idArtigo` BIGINT NOT NULL,
  `usuario_idUsuario` BIGINT NOT NULL,
  `comentario` TEXT NOT NULL,
  `data_comentario` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `aprovado` BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (`artigo_idArtigo`) REFERENCES `Artigo`(`idArtigo`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`usuario_idUsuario`) REFERENCES `Usuario`(`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;


-- ================================
-- Tabela AVALIACAO_ARTIGO (avaliação de artigo pelo usuário)
-- ================================
CREATE TABLE IF NOT EXISTS `AvaliacaoArtigo` (
  `idAvaliacao` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `usuario_idUsuario` BIGINT NOT NULL,
  `artigo_idArtigo` BIGINT NOT NULL,
  `avaliacao` ENUM('GOSTEI', 'NAO_GOSTEI', 'NEUTRO') NOT NULL,
  `data_avaliacao` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_usuario_artigo` (`usuario_idUsuario`, `artigo_idArtigo`),
  FOREIGN KEY (`usuario_idUsuario`) REFERENCES `Usuario`(`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`artigo_idArtigo`) REFERENCES `Artigo`(`idArtigo`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;


-- ================================
-- Tabela EVENTO (ligado ao usuario organizador)
-- ================================
CREATE TABLE IF NOT EXISTS `Evento` (
  `idEvento` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `titulo` VARCHAR(200) NOT NULL,
  `descricao` TEXT NOT NULL,
  `data_evento` DATETIME NOT NULL,
  `local_evento` VARCHAR(150) NOT NULL,
  `link_inscricao` VARCHAR(255),
  `imagem` VARCHAR(255),
  `ativo` BOOLEAN DEFAULT TRUE,
  `data_criacao` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `organizador_idUsuario` BIGINT NOT NULL,
  FOREIGN KEY (`organizador_idUsuario`) REFERENCES `Usuario`(`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ================================
-- Tabela EDITAL (ligado ao usuario autor)
-- ================================
CREATE TABLE IF NOT EXISTS `Edital` (
  `idEdital` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `titulo` VARCHAR(200) NOT NULL,
  `descricao` TEXT,
  `arquivo_url` VARCHAR(500) NOT NULL,
  `arquivo_nome` VARCHAR(255) NOT NULL,
  `data_publicacao` DATETIME NOT NULL,
  `data_validade` DATETIME,
  `ativo` BOOLEAN DEFAULT TRUE,
  `visualizacoes` INT DEFAULT 0,
  `autor_idUsuario` BIGINT NOT NULL,
  `data_criacao` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `data_atualizacao` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`autor_idUsuario`) REFERENCES `Usuario`(`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ================================
-- Reativa restrições e regras de unicidade/origem
-- ================================
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;



