package edu.matricula.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Entidad transaccional que une Estudiante, Asignatura y Periodo. */
@Entity
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Estudiante estudiante;

    @ManyToOne(optional = false)
    private Asignatura asignatura;

    @ManyToOne(optional = false)
    private Periodo periodo;

    @Column(nullable = false)
    private LocalDate fechaInscripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado;

    /** RN: auditoría, sello de tiempo de la confirmación. */
    @Column(nullable = false)
    private LocalDateTime selloTiempo;

    protected Inscripcion() { }

    /** Inscripción nueva, aún sin confirmar. */
    public Inscripcion(Estudiante estudiante, Asignatura asignatura, Periodo periodo) {
        this.estudiante = estudiante;
        this.asignatura = asignatura;
        this.periodo = periodo;
    }

    /** Inscripción histórica (datos de prueba). */
    public Inscripcion(Estudiante estudiante, Asignatura asignatura, Periodo periodo,
                       EstadoInscripcion estado, LocalDate fecha, LocalDateTime sello) {
        this(estudiante, asignatura, periodo);
        this.estado = estado;
        this.fechaInscripcion = fecha;
        this.selloTiempo = sello;
    }

    public void confirmar() {
        this.estado = EstadoInscripcion.ACTIVA;
        this.selloTiempo = LocalDateTime.now();
        this.fechaInscripcion = selloTiempo.toLocalDate();
    }

    public void cancelar() { this.estado = EstadoInscripcion.CANCELADA; }

    public Long getId() { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public Asignatura getAsignatura() { return asignatura; }
    public Periodo getPeriodo() { return periodo; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public EstadoInscripcion getEstado() { return estado; }
    public LocalDateTime getSelloTiempo() { return selloTiempo; }
}
