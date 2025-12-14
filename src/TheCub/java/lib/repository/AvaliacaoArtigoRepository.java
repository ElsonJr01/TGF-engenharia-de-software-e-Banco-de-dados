package lib.repository;

import dominio.entidades.AvaliacaoArtigo;
import dominio.entidades.Usuario;
import dominio.entidades.Artigo;
import dominio.enums.TipoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoArtigoRepository extends JpaRepository<AvaliacaoArtigo, Long> {
    Optional<AvaliacaoArtigo> findByUsuarioAndArtigo(Usuario usuario, Artigo artigo);
    long countByArtigoAndAvaliacao(Artigo artigo, TipoAvaliacao avaliacao);
}
