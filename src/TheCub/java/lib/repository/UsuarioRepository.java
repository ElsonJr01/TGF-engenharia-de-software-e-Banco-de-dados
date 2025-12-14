package lib.repository;

import dominio.entidades.Usuario;
import dominio.enums.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndAtivoTrue(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByTipo(TipoUsuario tipo);

    List<Usuario> findByTipoAndAtivoTrue(TipoUsuario tipo);

    Page<Usuario> findByTipo(TipoUsuario tipo, Pageable pageable);

    List<Usuario> findByAtivoTrue();

    Page<Usuario> findByAtivoTrue(Pageable pageable);

    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.tipo IN ('ADMIN', 'EDITOR', 'REDATOR') AND u.ativo = true")
    List<Usuario> findUsuariosQuePodePublicar();

    Long countByTipo(TipoUsuario tipo);

    Long countByAtivoTrue();

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN u.artigos a " +
            "WHERE a.status = 'PUBLICADO' " +
            "GROUP BY u " +
            "ORDER BY COUNT(a) DESC")
    Page<Usuario> findTopAutores(Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE " +
            "(:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:tipo IS NULL OR u.tipo = :tipo) AND " +
            "(:ativo IS NULL OR u.ativo = :ativo)")
    Page<Usuario> findByFiltros(
            @Param("nome") String nome,
            @Param("email") String email,
            @Param("tipo") TipoUsuario tipo,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );
}
