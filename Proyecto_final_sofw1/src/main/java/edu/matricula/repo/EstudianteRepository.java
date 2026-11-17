package edu.matricula.repo;

import edu.matricula.model.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstudianteRepository extends JpaRepository<Estudiante, String> {

    @Query("""
           select e from Estudiante e
           where lower(e.documento) like lower(concat('%', :q, '%'))
              or lower(e.nombre) like lower(concat('%', :q, '%'))
              or lower(e.email) like lower(concat('%', :q, '%'))
              or lower(e.programa) like lower(concat('%', :q, '%'))
           """)
    Page<Estudiante> buscar(@Param("q") String q, Pageable pageable);
}
