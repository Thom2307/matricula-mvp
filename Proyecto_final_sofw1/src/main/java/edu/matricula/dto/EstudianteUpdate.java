package edu.matricula.dto;

import edu.matricula.model.EstadoEstudiante;
import edu.matricula.model.EstadoMatricula;
import jakarta.validation.constraints.*;

/** Cuerpo de PATCH /estudiantes/{id}: solo se aplican los campos presentes. El documento no se edita. */
public record EstudianteUpdate(
        @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
        String nombre,

        @Pattern(regexp = ".*\\S.*", message = "El programa no puede estar vacío")
        String programa,

        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "El email no tiene un formato válido")
        String email,

        EstadoEstudiante estado,

        @DecimalMin(value = "0.0", message = "El promedio debe estar entre 0,0 y 5,0")
        @DecimalMax(value = "5.0", message = "El promedio debe estar entre 0,0 y 5,0")
        Double promedioAcumulado,

        EstadoMatricula estadoMatricula) {
}
