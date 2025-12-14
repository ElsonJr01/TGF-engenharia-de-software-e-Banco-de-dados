package lib.repository;

import dominio.entidades.AvaliacaoNoticia;
import dominio.entidades.Usuario;
import dominio.entidades.Noticia;
import dominio.enums.TipoAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoNoticiaRepository extends JpaRepository<AvaliacaoNoticia, Long> {
    Optional<AvaliacaoNoticia> findByUsuarioAndArtigo(Usuario usuario, Noticia artigo);
    long countByArtigoAndAvaliacao(Noticia artigo, TipoAvaliacao avaliacao);
}
