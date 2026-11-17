package edu.matricula.repo;

import edu.matricula.model.Asignatura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsignaturaRepository extends JpaRepository<Asignatura, String> {

    @Query("""
           select a from Asignatura a
           where lower(a.codigo) like lower(concat('%', :q, '%'))
              or lower(a.nombre) like lower(concat('%', :q, '%'))
           order by a.codigo
           """)
    List<Asignatura> buscar(@Param("q") String q);

    /** Bloqueo pesimista: serializa inscripciones concurrentes sobre la misma asignatura (RNF-02). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Asignatura a where a.codigo = :codigo")
    Optional<Asignatura> buscarParaActualizar(@Param("codigo") String codigo);
}
