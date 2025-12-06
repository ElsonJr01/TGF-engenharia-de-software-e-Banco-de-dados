package lib.repository;

import dominio.entidades.Cultura;
import dominio.entidades.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CulturaRepository extends JpaRepository<Cultura, Long> {

    List<Cultura> findByAtivoTrue();

    Page<Cultura> findByAtivoTrue(Pageable pageable);

    List<Cultura> findByOrganizador(Usuario organizador);

    Page<Cultura> findByOrganizador(Usuario organizador, Pageable pageable);

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento >= :agora AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Cultura> findProximosEventos(@Param("agora") LocalDateTime agora);

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento >= :agora AND e.ativo = true ORDER BY e.dataEvento ASC")
    Page<Cultura> findProximosEventos(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento < :agora ORDER BY e.dataEvento DESC")
    Page<Cultura> findEventosPassados(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento BETWEEN :dataInicio AND :dataFim AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Cultura> findEventosPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento BETWEEN :agora AND :dataLimite AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Cultura> findEventosProximosNDias(
            @Param("agora") LocalDateTime agora,
            @Param("dataLimite") LocalDateTime dataLimite
    );

    Page<Cultura> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<Cultura> findByTituloContainingIgnoreCaseAndAtivoTrue(String titulo, Pageable pageable);

    Page<Cultura> findByLocalEventoContainingIgnoreCaseAndAtivoTrue(String local, Pageable pageable);

    @Query("SELECT e FROM Cultura e WHERE e.dataEvento BETWEEN :agora AND :semanaDepois AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Cultura> findEventosEmDestaque(
            @Param("agora") LocalDateTime agora,
            @Param("semanaDepois") LocalDateTime semanaDepois
    );

    long countByAtivoTrue();

    long countByAtivoFalse();

    long countByDataEventoAfter(LocalDateTime dataEvento);

    long countByDataEventoBefore(LocalDateTime dataEvento);

    long countByOrganizador(Usuario organizador);

    @Query("""
        SELECT e FROM Cultura e
        WHERE (:titulo IS NULL OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
          AND (:local IS NULL OR LOWER(e.localEvento) LIKE LOWER(CONCAT('%', :local, '%')))
          AND (:organizadorId IS NULL OR e.organizador.id = :organizadorId)
          AND (:ativo IS NULL OR e.ativo = :ativo)
          AND (:dataInicio IS NULL OR e.dataEvento >= :dataInicio)
          AND (:dataFim IS NULL OR e.dataEvento <= :dataFim)
        ORDER BY e.dataEvento ASC
        """)
    Page<Cultura> buscarComFiltros(
            @Param("titulo") String titulo,
            @Param("local") String local,
            @Param("organizadorId") Long organizadorId,
            @Param("ativo") Boolean ativo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}
