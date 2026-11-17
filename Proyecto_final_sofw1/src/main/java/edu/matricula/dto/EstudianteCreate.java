package edu.matricula.dto;

import edu.matricula.model.EstadoEstudiante;
import edu.matricula.model.EstadoMatricula;
import jakarta.validation.constraints.*;

/** Cuerpo de POST /estudiantes. Los nombres coinciden con Estudiante del modelo de dominio. */
public record EstudianteCreate(
        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El programa es obligatorio")
        String programa,

        @NotBlank(message = "El email es obligatorio")
        @Pattern(regexp = "^(\\s*|[^@\\s]+@[^@\\s]+\\.[^@\\s]+)$", message = "El email no tiene un formato válido")
        String email,

        EstadoEstudiante estado,

        @NotNull(message = "El promedio acumulado es obligatorio")
        @DecimalMin(value = "0.0", message = "El promedio debe estar entre 0,0 y 5,0")
        @DecimalMax(value = "5.0", message = "El promedio debe estar entre 0,0 y 5,0")
        Double promedioAcumulado,

        EstadoMatricula estadoMatricula) {
}
