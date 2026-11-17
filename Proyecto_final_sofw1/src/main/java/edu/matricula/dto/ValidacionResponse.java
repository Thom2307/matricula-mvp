package edu.matricula.dto;

/** Resultado de validar sin persistir (paso 6-7 del CU-01). */
public record ValidacionResponse(
        boolean valido,
        String documento,
        String nombreEstudiante,
        String codigoAsignatura,
        String nombreAsignatura,
        String codigoPeriodo,
        int creditosAsignatura,
        int creditosActuales,
        int creditosResultantes,
        int maxCreditos,
        int cupoDisponible) {
}
