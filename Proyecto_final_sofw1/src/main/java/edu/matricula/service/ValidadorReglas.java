package edu.matricula.service;

import edu.matricula.model.*;
import edu.matricula.repo.InscripcionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reglas de negocio del CU-01. Orden de validación (Act-01 / Sec-01):
 * RN-07, RN-01, RN-04, RN-03, RN-06, RN-05, RN-02.
 * Las consultas que el diagrama de clases asigna a Estudiante (tieneInscrita, aproboPrerrequisitos,
 * asignaturasInscritas, creditosInscritos) se resuelven aquí contra el repositorio.
 */
@Component
public class ValidadorReglas {

    public static final int MAX_ASIGNATURAS_CONDICIONAL = 2;

    private final InscripcionRepository inscripciones;

    public ValidadorReglas(InscripcionRepository inscripciones) {
        this.inscripciones = inscripciones;
    }

    /** Lanza NegocioException en la primera regla incumplida. */
    public void validarReglas(Estudiante e, Asignatura a, Periodo p) {
        validarMatriculaAbierta(p);          // RN-07
        validarEstudianteActivo(e);          // RN-01
        validarSinDuplicado(e, a, p);        // RN-04
        validarPrerrequisitos(e, a);         // RN-03
        validarMatriculaCondicional(e, p);   // RN-06
        validarCreditos(e, a, p);            // RN-05
        validarCupo(a);                      // RN-02
    }

    // ---- consultas del dominio ----

    public boolean tieneInscrita(Estudiante e, Asignatura a, Periodo p) {
        return inscripciones.existsByEstudianteDocumentoAndAsignaturaCodigoAndPeriodoCodigoAndEstado(
                e.getDocumento(), a.getCodigo(), p.getCodigo(), EstadoInscripcion.ACTIVA);
    }

    public boolean aproboPrerrequisitos(Estudiante e, Asignatura a) {
        return a.getPrerrequisitos().stream().allMatch(pre ->
                inscripciones.existsByEstudianteDocumentoAndAsignaturaCodigoAndEstado(
                        e.getDocumento(), pre.getCodigo(), EstadoInscripcion.APROBADA));
    }

    public List<Inscripcion> asignaturasInscritas(Estudiante e, Periodo p) {
        return inscripciones.findByEstudianteDocumentoAndPeriodoCodigoAndEstadoOrderByFechaInscripcionDesc(
                e.getDocumento(), p.getCodigo(), EstadoInscripcion.ACTIVA);
    }

    public int creditosInscritos(Estudiante e, Periodo p) {
        Long total = inscripciones.sumarCreditos(e.getDocumento(), p.getCodigo(), EstadoInscripcion.ACTIVA);
        return total == null ? 0 : total.intValue();
    }

    // ---- reglas ----

    void validarMatriculaAbierta(Periodo p) {
        if (!p.matriculaAbierta()) {
            throw new NegocioException("RN-07", "La matrícula de este periodo está cerrada");
        }
    }

    void validarEstudianteActivo(Estudiante e) {
        if (!e.estaActivo()) {
            throw new NegocioException("RN-01", "Tu estado de estudiante está inactivo");
        }
    }

    void validarSinDuplicado(Estudiante e, Asignatura a, Periodo p) {
        if (tieneInscrita(e, a, p)) {
            throw new NegocioException("RN-04", "Ya tienes inscrita esta asignatura en el periodo");
        }
    }

    void validarPrerrequisitos(Estudiante e, Asignatura a) {
        if (!aproboPrerrequisitos(e, a)) {
            throw new NegocioException("RN-03", "No has aprobado los prerrequisitos de esta asignatura");
        }
    }

    void validarMatriculaCondicional(Estudiante e, Periodo p) {
        if (e.esCondicional() && asignaturasInscritas(e, p).size() >= MAX_ASIGNATURAS_CONDICIONAL) {
            throw new NegocioException("RN-06",
                    "Estás en matrícula condicional: solo puedes inscribir 2 asignaturas por periodo");
        }
    }

    void validarCreditos(Estudiante e, Asignatura a, Periodo p) {
        if (creditosInscritos(e, p) + a.getCreditos() > p.getMaxCreditos()) {
            throw new NegocioException("RN-05", "Superarías el máximo de créditos permitido");
        }
    }

    void validarCupo(Asignatura a) {
        if (!a.tieneCupo()) {
            throw new NegocioException("RN-02", "La asignatura no tiene cupos disponibles");
        }
    }
}
