package edu.matricula.dto;

import edu.matricula.model.Periodo;
import java.time.LocalDate;

public record PeriodoDto(
        String codigo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaInicioMatricula,
        LocalDate fechaFinMatricula,
        int maxCreditos,
        boolean matriculaAbierta) {

    public static PeriodoDto de(Periodo p) {
        return new PeriodoDto(p.getCodigo(), p.getFechaInicio(), p.getFechaFin(),
                p.getFechaInicioMatricula(), p.getFechaFinMatricula(), p.getMaxCreditos(), p.matriculaAbierta());
    }
}
