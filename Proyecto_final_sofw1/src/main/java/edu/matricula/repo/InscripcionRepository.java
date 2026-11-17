package edu.matricula.repo;

import edu.matricula.model.EstadoInscripcion;
import edu.matricula.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /** RN-04. */
    boolean existsByEstudianteDocumentoAndAsignaturaCodigoAndPeriodoCodigoAndEstado(
            String documento, String codigoAsignatura, String codigoPeriodo, EstadoInscripcion estado);

    /** RN-03 (historial). */
    boolean existsByEstudianteDocumentoAndAsignaturaCodigoAndEstado(
            String documento, String codigoAsignatura, EstadoInscripcion estado);

    /** RN-06. */
    long countByEstudianteDocumentoAndPeriodoCodigoAndEstado(
            String documento, String codigoPeriodo, EstadoInscripcion estado);

    /** RC-01. */
    long countByEstudianteDocumentoAndEstado(String documento, EstadoInscripcion estado);

    /** Derivación de cupoDisponible. */
    long countByAsignaturaCodigoAndEstado(String codigoAsignatura, EstadoInscripcion estado);

    List<Inscripcion> findByEstudianteDocumentoAndPeriodoCodigoAndEstadoOrderByFechaInscripcionDesc(
            String documento, String codigoPeriodo, EstadoInscripcion estado);

    /** RN-05. */
    @Query("""
           select coalesce(sum(i.asignatura.creditos), 0) from Inscripcion i
           where i.estudiante.documento = :documento
             and i.periodo.codigo = :periodo
             and i.estado = :estado
           """)
    Long sumarCreditos(@Param("documento") String documento,
                       @Param("periodo") String periodo,
                       @Param("estado") EstadoInscripcion estado);
}
