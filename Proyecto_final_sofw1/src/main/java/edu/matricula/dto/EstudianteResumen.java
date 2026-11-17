package edu.matricula.dto;

import edu.matricula.model.Estudiante;
import java.util.List;

/** Paso 2 del CU-01: resumen del estudiante en el periodo. */
public record EstudianteResumen(
        Estudiante estudiante,
        String codigoPeriodo,
        boolean matriculaAbierta,
        int creditosInscritos,
        int maxCreditos,
        List<InscripcionResumen> inscripcionesActivas) {
}
