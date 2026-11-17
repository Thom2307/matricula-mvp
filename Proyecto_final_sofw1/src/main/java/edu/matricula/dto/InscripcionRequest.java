package edu.matricula.dto;

import jakarta.validation.constraints.NotBlank;

public record InscripcionRequest(
        @NotBlank(message = "El documento es obligatorio") String documento,
        @NotBlank(message = "La asignatura es obligatoria") String codigoAsignatura,
        @NotBlank(message = "El periodo es obligatorio") String codigoPeriodo) {
}
