package edu.matricula.repo;

import edu.matricula.model.Periodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodoRepository extends JpaRepository<Periodo, String> {
    List<Periodo> findAllByOrderByFechaInicioDesc();
}
