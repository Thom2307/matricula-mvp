package edu.matricula.dto;

import edu.matricula.model.Asignatura;
import java.util.List;

public record AsignaturaDto(
        String codigo,
        String nombre,
        int creditos,
        int cupoMaximo,
        int cupoDisponible,
        List<String> prerrequisitos) {

    public static AsignaturaDto de(Asignatura a) {
        List<String> pre = a.getPrerrequisitos().stream().map(Asignatura::getCodigo).sorted().toList();
        return new AsignaturaDto(a.getCodigo(), a.getNombre(), a.getCreditos(),
                a.getCupoMaximo(), a.getCupoDisponible(), pre);
    }
}
