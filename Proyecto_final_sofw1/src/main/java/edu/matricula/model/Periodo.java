package edu.matricula.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Periodo {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fechaInicio;
    @Column(nullable = false)
    private LocalDate fechaFin;
    @Column(nullable = false)
    private LocalDate fechaInicioMatricula;
    @Column(nullable = false)
    private LocalDate fechaFinMatricula;
    @Column(nullable = false)
    private int maxCreditos;

    protected Periodo() { }

    public Periodo(String codigo, LocalDate fechaInicio, LocalDate fechaFin,
                   LocalDate fechaInicioMatricula, LocalDate fechaFinMatricula, int maxCreditos) {
        this.codigo = codigo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaInicioMatricula = fechaInicioMatricula;
        this.fechaFinMatricula = fechaFinMatricula;
        this.maxCreditos = maxCreditos;
    }

    /** RN-07: hoy dentro de la ventana de matrícula (extremos incluidos). */
    public boolean matriculaAbierta() { return matriculaAbierta(LocalDate.now()); }

    public boolean matriculaAbierta(LocalDate hoy) {
        return !hoy.isBefore(fechaInicioMatricula) && !hoy.isAfter(fechaFinMatricula);
    }

    public String getCodigo() { return codigo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public LocalDate getFechaInicioMatricula() { return fechaInicioMatricula; }
    public LocalDate getFechaFinMatricula() { return fechaFinMatricula; }
    public int getMaxCreditos() { return maxCreditos; }
}
