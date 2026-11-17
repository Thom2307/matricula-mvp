package edu.matricula.dto;

import edu.matricula.model.Inscripcion;
import java.time.LocalDate;

public record InscripcionResumen(
        String codigoAsignatura,
        String nombreAsignatura,
        int creditos,
        String estado,
        LocalDate fechaInscripcion) {

    public static InscripcionResumen de(Inscripcion i) {
        return new InscripcionResumen(i.getAsignatura().getCodigo(), i.getAsignatura().getNombre(),
                i.getAsignatura().getCreditos(), i.getEstado().name(), i.getFechaInscripcion());
    }
}
