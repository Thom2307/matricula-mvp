package edu.matricula.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Asignatura {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int creditos;

    @Column(nullable = false)
    private int cupoMaximo;

    /** Derivado: cupoMaximo - inscripciones ACTIVAS. Se mantiene al confirmar la transacción. */
    @Column(nullable = false)
    private int cupoDisponible;

    /** Asociación reflexiva: roles «dependiente» (esta) y «prerrequisito» (las del conjunto). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "asignatura_prerrequisito",
            joinColumns = @JoinColumn(name = "dependiente"),
            inverseJoinColumns = @JoinColumn(name = "prerrequisito"))
    private Set<Asignatura> prerrequisitos = new HashSet<>();

    protected Asignatura() { }

    public Asignatura(String codigo, String nombre, int creditos, int cupoMaximo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditos = creditos;
        this.cupoMaximo = cupoMaximo;
        this.cupoDisponible = cupoMaximo;
    }

    /** RN-02. */
    public boolean tieneCupo() { return cupoDisponible > 0; }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getCreditos() { return creditos; }
    public int getCupoMaximo() { return cupoMaximo; }
    public int getCupoDisponible() { return cupoDisponible; }
    public void setCupoDisponible(int cupoDisponible) { this.cupoDisponible = cupoDisponible; }
    public Set<Asignatura> getPrerrequisitos() { return prerrequisitos; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Asignatura a && Objects.equals(codigo, a.codigo);
    }

    @Override
    public int hashCode() { return Objects.hashCode(codigo); }
}
