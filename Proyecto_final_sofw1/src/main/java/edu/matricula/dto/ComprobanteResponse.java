package edu.matricula.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Comprobante del paso 10 del CU-01. */
public record ComprobanteResponse(
        Long idInscripcion,
        String documento,
        String nombreEstudiante,
        String codigoAsignatura,
        String nombreAsignatura,
        int creditos,
        String codigoPeriodo,
        LocalDate fechaInscripcion,
        LocalDateTime selloTiempo,
        String estado,
        int cupoDisponibleRestante,
        int creditosInscritos,
        int maxCreditos) {
}
