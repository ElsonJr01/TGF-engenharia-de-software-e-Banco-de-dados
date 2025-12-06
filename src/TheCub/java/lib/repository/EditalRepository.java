package lib.repository;

import dominio.entidades.Edital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EditalRepository extends JpaRepository<Edital, Long> {

    Page<Edital> findByAtivoTrueOrderByDataPublicacaoDesc(Pageable pageable);

    List<Edital> findByAtivoTrueAndDataValidadeAfterOrderByDataPublicacaoDesc(LocalDateTime agora);
}
