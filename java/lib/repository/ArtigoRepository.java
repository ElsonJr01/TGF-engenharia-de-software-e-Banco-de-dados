package lib.repository;

import dominio.entidades.Artigo;
import dominio.entidades.Categoria;
import dominio.entidades.Usuario;
import dominio.enums.StatusArtigo;
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
public interface ArtigoRepository extends JpaRepository<Artigo, Long> {

    List<Artigo> findByStatus(StatusArtigo status);

    Page<Artigo> findByStatus(StatusArtigo status, Pageable pageable);

    List<Artigo> findByStatusOrderByDataPublicacaoDesc(StatusArtigo status);

    Page<Artigo> findByStatusOrderByDataPublicacaoDesc(StatusArtigo status, Pageable pageable);

    List<Artigo> findByCategoria(Categoria categoria);

    Page<Artigo> findByStatusAndCategoria(StatusArtigo status, Categoria categoria, Pageable pageable);

    List<Artigo> findByAutor(Usuario autor);

    Page<Artigo> findByAutor(Usuario autor, Pageable pageable);

    @Query("SELECT a FROM Artigo a WHERE a.destaque = true AND a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    List<Artigo> findArtigosEmDestaque();

    @Query("SELECT a FROM Artigo a WHERE a.destaque = true AND a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    Page<Artigo> findArtigosEmDestaque(Pageable pageable);

    @Query("SELECT a FROM Artigo a WHERE a.status = 'PUBLICADO' ORDER BY a.visualizacoes DESC")
    Page<Artigo> findArtigosMaisVistos(Pageable pageable);

    @Query("SELECT a FROM Artigo a WHERE a.status = 'PUBLICADO' ORDER BY a.dataPublicacao DESC")
    Page<Artigo> findArtigosRecentes(Pageable pageable);

    Page<Artigo> findByTituloContainingIgnoreCaseAndStatus(String titulo, StatusArtigo status, Pageable pageable);

    @Query("""
        SELECT a FROM Artigo a
        WHERE (LOWER(a.titulo) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.conteudo) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.resumo) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND a.status = 'PUBLICADO'
        """)
    Page<Artigo> buscarPorPalavraChave(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Artigo a " +
            "WHERE a.status = 'PUBLICADO' " +
            "AND a.dataPublicacao BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataPublicacao DESC")
    Page<Artigo> findByDataPublicacaoBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable);

    Long countByStatus(StatusArtigo status);

    Long countByCategoria(Categoria categoria);

    Long countByAutor(Usuario autor);

    @Query("SELECT SUM(a.visualizacoes) FROM Artigo a WHERE a.status = 'PUBLICADO'")
    Long somarTotalVisualizacoes();

    @Modifying
    @Query("UPDATE Artigo a SET a.visualizacoes = a.visualizacoes + 1 WHERE a.id = :artigoId")
    void incrementarVisualizacoes(@Param("artigoId") Long artigoId);

    @Query("""
        SELECT a FROM Artigo a
        WHERE a.categoria = :categoria
          AND a.id <> :artigoId
          AND a.status = 'PUBLICADO'
        ORDER BY a.dataPublicacao DESC
        """)
    Page<Artigo> findArtigosRelacionados(@Param("categoria") Categoria categoria,
                                         @Param("artigoId") Long artigoId,
                                         Pageable pageable);

    @Query("""
        SELECT a FROM Artigo a
        WHERE (:status IS NULL OR a.status = :status)
          AND (:categoriaId IS NULL OR a.categoria.id = :categoriaId)
          AND (:autorId IS NULL OR a.autor.id = :autorId)
          AND (:titulo IS NULL OR LOWER(a.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
        ORDER BY a.dataCriacao DESC
        """)
    Page<Artigo> buscarArtigosPorFiltros(@Param("status") StatusArtigo status,
                                         @Param("categoriaId") Long categoriaId,
                                         @Param("autorId") Long autorId,
                                         @Param("titulo") String titulo,
                                         Pageable pageable);

    @Query("SELECT a FROM Artigo a WHERE a.status = 'REVISAO' ORDER BY a.dataCriacao ASC")
    List<Artigo> findArtigosAguardandoRevisao();
}
