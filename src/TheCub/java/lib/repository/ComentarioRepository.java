package lib.repository;

import dominio.entidades.Artigo;
import dominio.entidades.Comentario;
import dominio.entidades.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByArtigo(Artigo artigo);

    List<Comentario> findByArtigoAndAprovadoTrueOrderByDataComentarioDesc(Artigo artigo);

    Page<Comentario> findByArtigoAndAprovadoTrue(Artigo artigo, Pageable pageable);

    List<Comentario> findByArtigoId(Long artigoId);

    List<Comentario> findByArtigoIdAndAprovadoTrueOrderByDataComentarioDesc(Long artigoId);

    List<Comentario> findByUsuario(Usuario usuario);

    Page<Comentario> findByUsuario(Usuario usuario, Pageable pageable);

    List<Comentario> findByAprovadoFalseOrderByDataComentarioAsc();

    Page<Comentario> findByAprovadoFalse(Pageable pageable);

    List<Comentario> findByAprovadoTrueOrderByDataComentarioDesc();

    Page<Comentario> findByAprovadoTrue(Pageable pageable);

    Long countByArtigo(Artigo artigo);

    Long countByArtigoAndAprovadoTrue(Artigo artigo);

    Long countByArtigoId(Long artigoId);

    Long countByArtigoIdAndAprovadoTrue(Long artigoId);

    Long countByAprovadoFalse();

    Long countByUsuario(Usuario usuario);

    long countByAprovadoTrue();

    @Query("SELECT c FROM Comentario c WHERE c.aprovado = true ORDER BY c.dataComentario DESC")
    Page<Comentario> findComentariosRecentes(Pageable pageable);

    @Query("SELECT c.usuario FROM Comentario c WHERE c.aprovado = true GROUP BY c.usuario ORDER BY COUNT(c) DESC")
    Page<Usuario> findUsuariosMaisAtivos(Pageable pageable);

    @Query("""
        SELECT c FROM Comentario c
        WHERE (COALESCE(:artigoId, 0) = 0 OR c.artigo.id = :artigoId)
          AND (COALESCE(:usuarioId, 0) = 0 OR c.usuario.id = :usuarioId)
          AND (:aprovado IS NULL OR c.aprovado = :aprovado)
        ORDER BY c.dataComentario DESC
        """)
    Page<Comentario> findByFiltros(
            @Param("artigoId") Long artigoId,
            @Param("usuarioId") Long usuarioId,
            @Param("aprovado") Boolean aprovado,
            Pageable pageable
    );

    @Query("SELECT c FROM Comentario c WHERE c.aprovado = false ORDER BY c.dataComentario ASC")
    Page<Comentario> listarPendentesDeAprovacaoComPaginacao(Pageable pageable);

    @Query("""
        SELECT new lib.repository.ComentarioEstatisticas(
            COUNT(c),
            SUM(CASE WHEN c.aprovado = true THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.aprovado = false THEN 1 ELSE 0 END)
        )
        FROM Comentario c
        """)
    ComentarioEstatisticas obterEstatisticas();

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.aprovado = false")
    Long contarPendentesDeAprovacao();
}
