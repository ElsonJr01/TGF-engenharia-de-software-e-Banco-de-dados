package lib.repository;

import dominio.entidades.Noticia;
import dominio.entidades.Categoria;
import dominio.entidades.Usuario;
import dominio.enums.StatusNoticia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    List<Noticia> findByStatus(StatusNoticia status);

    Page<Noticia> findByStatus(StatusNoticia status, Pageable pageable);

    List<Noticia> findByStatusOrderByDataPublicacaoDesc(StatusNoticia status);

    Page<Noticia> findByStatusOrderByDataPublicacaoDesc(StatusNoticia status, Pageable pageable);

    List<Noticia> findByCategoria(Categoria categoria);

    Page<Noticia> findByStatusAndCategoria(StatusNoticia status, Categoria categoria, Pageable pageable);

    List<Noticia> findByAutor(Usuario autor);

    Page<Noticia> findByAutor(Usuario autor, Pageable pageable);

    @Query("SELECT a FROM Noticia a WHERE a.destaque = true AND a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    List<Noticia> findArtigosEmDestaque();

    @Query("SELECT a FROM Noticia a WHERE a.destaque = true AND a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    Page<Noticia> findArtigosEmDestaque(Pageable pageable);

    @Query("SELECT a FROM Noticia a WHERE a.status = 'PUBLICADO' ORDER BY a.visualizacoes DESC")
    Page<Noticia> findArtigosMaisVistos(Pageable pageable);

    @Query("SELECT a FROM Noticia a WHERE a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    Page<Noticia> findArtigosRecentes(Pageable pageable);

    Page<Noticia> findByTituloContainingIgnoreCaseAndStatus(String titulo, StatusNoticia status, Pageable pageable);

    @Query("""
        SELECT a FROM Noticia a
        WHERE (LOWER(a.titulo) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.conteudo) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.resumo) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND a.status = 'PUBLICADO'
        """)
    Page<Noticia> buscarPorPalavraChave(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Noticia a " +
            "WHERE a.status = 'PUBLICADO' " +
            "AND a.dataPublicacao BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataPublicacao DESC")
    Page<Noticia> findByDataPublicacaoBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable);

    Long countByStatus(StatusNoticia status);

    Long countByCategoria(Categoria categoria);

    Long countByAutor(Usuario autor);

    @Query("SELECT SUM(a.visualizacoes) FROM Noticia a WHERE a.status = 'PUBLICADO'")
    Long somarTotalVisualizacoes();

    @Modifying
    @Query("UPDATE Noticia a SET a.visualizacoes = a.visualizacoes + 1 WHERE a.id = :artigoId")
    void incrementarVisualizacoes(@Param("artigoId") Long artigoId);

    @Query("""
        SELECT a FROM Noticia a
        WHERE a.categoria = :categoria
          AND a.id <> :artigoId
          AND a.status = 'PUBLICADO'
        ORDER BY a.dataPublicacao DESC
        """)
    Page<Noticia> findArtigosRelacionados(@Param("categoria") Categoria categoria,
                                          @Param("artigoId") Long artigoId,
                                          Pageable pageable);

    @Query("""
        SELECT a FROM Noticia a
        WHERE (:status IS NULL OR a.status = :status)
          AND (:categoriaId IS NULL OR a.categoria.id = :categoriaId)
          AND (:autorId IS NULL OR a.autor.id = :autorId)
          AND (:titulo IS NULL OR LOWER(a.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
        ORDER BY a.dataCriacao DESC
        """)
    Page<Noticia> buscarArtigosPorFiltros(@Param("status") StatusNoticia status,
                                          @Param("categoriaId") Long categoriaId,
                                          @Param("autorId") Long autorId,
                                          @Param("titulo") String titulo,
                                          Pageable pageable);

    @Query("SELECT a FROM Noticia a WHERE a.status = 'REVISAO' ORDER BY a.dataCriacao ASC")
    List<Noticia> findArtigosAguardandoRevisao();
}
