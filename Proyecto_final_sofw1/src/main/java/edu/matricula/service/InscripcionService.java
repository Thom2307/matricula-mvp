package edu.matricula.service;

import edu.matricula.dto.*;
import edu.matricula.model.Asignatura;
import edu.matricula.model.Estudiante;
import edu.matricula.model.Inscripcion;
import edu.matricula.model.Periodo;
import edu.matricula.repo.AsignaturaRepository;
import edu.matricula.repo.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CU-01: Inscribir Asignatura. */
@Service
public class InscripcionService {

    private final EstudianteService estudiantes;
    private final AsignaturaService asignaturaService;
    private final AsignaturaRepository asignaturas;
    private final PeriodoService periodos;
    private final InscripcionRepository inscripciones;
    private final ValidadorReglas validador;

    public InscripcionService(EstudianteService estudiantes, AsignaturaService asignaturaService,
                              AsignaturaRepository asignaturas, PeriodoService periodos,
                              InscripcionRepository inscripciones, ValidadorReglas validador) {
        this.estudiantes = estudiantes;
        this.asignaturaService = asignaturaService;
        this.asignaturas = asignaturas;
        this.periodos = periodos;
        this.inscripciones = inscripciones;
        this.validador = validador;
    }

    /** Pasos 1-2: resumen del estudiante (A8 si el documento no existe). */
    @Transactional(readOnly = true)
    public EstudianteResumen resumen(String documento, String codigoPeriodo) {
        Estudiante e = estudiantes.obtener(documento);
        Periodo p = (codigoPeriodo == null || codigoPeriodo.isBlank())
                ? periodos.vigente() : periodos.obtener(codigoPeriodo);
        List<InscripcionResumen> activas = validador.asignaturasInscritas(e, p).stream()
                .map(InscripcionResumen::de).toList();
        return new EstudianteResumen(e, p.getCodigo(), p.matriculaAbierta(),
                validador.creditosInscritos(e, p), p.getMaxCreditos(), activas);
    }

    /** Pasos 6-7: valida todas las reglas sin persistir nada. */
    @Transactional(readOnly = true)
    public ValidacionResponse validar(InscripcionRequest req) {
        Estudiante e = estudiantes.obtener(req.documento());
        Asignatura a = asignaturaService.obtener(req.codigoAsignatura());
        Periodo p = periodos.obtener(req.codigoPeriodo());

        validador.validarReglas(e, a, p);

        int actuales = validador.creditosInscritos(e, p);
        return new ValidacionResponse(true, e.getDocumento(), e.getNombre(), a.getCodigo(), a.getNombre(),
                p.getCodigo(), a.getCreditos(), actuales, actuales + a.getCreditos(),
                p.getMaxCreditos(), a.getCupoDisponible());
    }

    /**
     * Paso 9: transacción única y atómica (RNF-02). Bloquea la asignatura, re-verifica todas las reglas
     * (incluido el cupo, A11), crea la inscripción y descuenta el cupo. Si algo falla, nada se persiste.
     */
    @Transactional
    public ComprobanteResponse crear(InscripcionRequest req) {
        Estudiante e = estudiantes.obtener(req.documento());
        Periodo p = periodos.obtener(req.codigoPeriodo());
        Asignatura a = asignaturas.buscarParaActualizar(req.codigoAsignatura() == null ? "" : req.codigoAsignatura().trim())
                .orElseThrow(() -> NegocioException.noEncontrado("No existe la asignatura " + req.codigoAsignatura()));

        validador.validarReglas(e, a, p);

        Inscripcion i = new Inscripcion(e, a, p);
        i.confirmar();                                          // ACTIVA + fecha + selloTiempo
        a.setCupoDisponible(a.getCupoDisponible() - 1);         // actualizarCupo()
        inscripciones.save(i);
        asignaturas.save(a);

        return new ComprobanteResponse(i.getId(), e.getDocumento(), e.getNombre(), a.getCodigo(), a.getNombre(),
                a.getCreditos(), p.getCodigo(), i.getFechaInscripcion(), i.getSelloTiempo(), i.getEstado().name(),
                a.getCupoDisponible(), validador.creditosInscritos(e, p), p.getMaxCreditos());
    }
}
