package lib.repository;

import dominio.entidades.Evento;
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
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByAtivoTrue();

    Page<Evento> findByAtivoTrue(Pageable pageable);

    List<Evento> findByOrganizador(Usuario organizador);

    Page<Evento> findByOrganizador(Usuario organizador, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.dataEvento >= :agora AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Evento> findProximosEventos(@Param("agora") LocalDateTime agora);

    @Query("SELECT e FROM Evento e WHERE e.dataEvento >= :agora AND e.ativo = true ORDER BY e.dataEvento ASC")
    Page<Evento> findProximosEventos(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.dataEvento < :agora ORDER BY e.dataEvento DESC")
    Page<Evento> findEventosPassados(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.dataEvento BETWEEN :dataInicio AND :dataFim AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Evento> findEventosPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    @Query("SELECT e FROM Evento e WHERE e.dataEvento BETWEEN :agora AND :dataLimite AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Evento> findEventosProximosNDias(
            @Param("agora") LocalDateTime agora,
            @Param("dataLimite") LocalDateTime dataLimite
    );

    Page<Evento> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<Evento> findByTituloContainingIgnoreCaseAndAtivoTrue(String titulo, Pageable pageable);

    Page<Evento> findByLocalEventoContainingIgnoreCaseAndAtivoTrue(String local, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.dataEvento BETWEEN :agora AND :semanaDepois AND e.ativo = true ORDER BY e.dataEvento ASC")
    List<Evento> findEventosEmDestaque(
            @Param("agora") LocalDateTime agora,
            @Param("semanaDepois") LocalDateTime semanaDepois
    );

    long countByAtivoTrue();

    long countByAtivoFalse();

    long countByDataEventoAfter(LocalDateTime dataEvento);

    long countByDataEventoBefore(LocalDateTime dataEvento);

    long countByOrganizador(Usuario organizador);

    @Query("""
        SELECT e FROM Evento e
        WHERE (:titulo IS NULL OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
          AND (:local IS NULL OR LOWER(e.localEvento) LIKE LOWER(CONCAT('%', :local, '%')))
          AND (:organizadorId IS NULL OR e.organizador.id = :organizadorId)
          AND (:ativo IS NULL OR e.ativo = :ativo)
          AND (:dataInicio IS NULL OR e.dataEvento >= :dataInicio)
          AND (:dataFim IS NULL OR e.dataEvento <= :dataFim)
        ORDER BY e.dataEvento ASC
        """)
    Page<Evento> buscarComFiltros(
            @Param("titulo") String titulo,
            @Param("local") String local,
            @Param("organizadorId") Long organizadorId,
            @Param("ativo") Boolean ativo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}
