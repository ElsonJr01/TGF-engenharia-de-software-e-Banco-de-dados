package lib.repository;

import dominio.entidades.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNome(String nome);

    Optional<Categoria> findByNomeIgnoreCase(String nome);

    boolean existsByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    List<Categoria> findByAtivaTrue();

    List<Categoria> findByAtivaTrueOrderByNomeAsc();

    Page<Categoria> findByAtivaTrue(Pageable pageable);

    List<Categoria> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT DISTINCT c FROM Categoria c " +
            "JOIN c.artigos a " +
            "WHERE a.status = 'PUBLICADO' AND c.ativa = true " +
            "ORDER BY c.nome")
    List<Categoria> findCategoriasComArtigosPublicados();

    @Query("SELECT c FROM Categoria c " +
            "LEFT JOIN c.artigos a " +
            "WHERE c.ativa = true " +
            "GROUP BY c " +
            "ORDER BY COUNT(a) DESC")
    List<Categoria> findCategoriasOrdenadaPorQuantidadeArtigos();

    @Query("SELECT c FROM Categoria c " +
            "LEFT JOIN c.artigos a " +
            "WHERE c.ativa = true AND a.status = 'PUBLICADO' " +
            "GROUP BY c " +
            "ORDER BY COUNT(a) DESC")
    Page<Categoria> findCategoriasMaisPopulares(Pageable pageable);

    Long countByAtivaTrue();

    @Query("SELECT COUNT(a) FROM Artigo a WHERE a.categoria.id = :categoriaId AND a.status = 'PUBLICADO'")
    Long contarArtigosPublicadosPorCategoria(Long categoriaId);
}
