package edu.matricula.model;

import jakarta.persistence.*;

/** Entidad maestra del CRUD (CU-02) y actor de la transacción (CU-01). */
@Entity
public class Estudiante {

    @Id
    @Column(length = 20)
    private String documento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String programa;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEstudiante estado = EstadoEstudiante.ACTIVO;

    @Column(nullable = false)
    private double promedioAcumulado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estadoMatricula = EstadoMatricula.NORMAL;

    protected Estudiante() { }

    public Estudiante(String documento, String nombre, String programa, String email,
                      EstadoEstudiante estado, double promedioAcumulado, EstadoMatricula estadoMatricula) {
        this.documento = documento;
        this.nombre = nombre;
        this.programa = programa;
        this.email = email;
        this.estado = estado;
        this.promedioAcumulado = promedioAcumulado;
        this.estadoMatricula = estadoMatricula;
    }

    /** RN-01. */
    public boolean estaActivo() { return estado == EstadoEstudiante.ACTIVO; }

    public boolean esCondicional() { return estadoMatricula == EstadoMatricula.CONDICIONAL; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPrograma() { return programa; }
    public void setPrograma(String programa) { this.programa = programa; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public EstadoEstudiante getEstado() { return estado; }
    public void setEstado(EstadoEstudiante estado) { this.estado = estado; }
    public double getPromedioAcumulado() { return promedioAcumulado; }
    public void setPromedioAcumulado(double promedioAcumulado) { this.promedioAcumulado = promedioAcumulado; }
    public EstadoMatricula getEstadoMatricula() { return estadoMatricula; }
    public void setEstadoMatricula(EstadoMatricula estadoMatricula) { this.estadoMatricula = estadoMatricula; }
}
