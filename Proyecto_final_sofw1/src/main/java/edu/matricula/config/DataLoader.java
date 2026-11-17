package edu.matricula.config;

import edu.matricula.model.*;
import edu.matricula.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos de prueba (RNF-07): 12 estudiantes, 22 asignaturas y 2 periodos, con fechas relativas a «hoy».
 * Cada caso demo dispara una regla distinta (ver README).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final String PROGRAMA = "Ingeniería de Sistemas y Computación";

    private final EstudianteRepository estudiantes;
    private final AsignaturaRepository asignaturas;
    private final PeriodoRepository periodos;
    private final InscripcionRepository inscripciones;

    public DataLoader(EstudianteRepository estudiantes, AsignaturaRepository asignaturas,
                      PeriodoRepository periodos, InscripcionRepository inscripciones) {
        this.estudiantes = estudiantes;
        this.asignaturas = asignaturas;
        this.periodos = periodos;
        this.inscripciones = inscripciones;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (estudiantes.count() > 0) return;
        LocalDate hoy = LocalDate.now();

        // ---------- Periodos ----------
        Periodo actual = periodos.save(new Periodo("2027-1",
                hoy.plusDays(30), hoy.plusDays(150), hoy.minusDays(10), hoy.plusDays(20), 18));   // matrícula ABIERTA
        Periodo previo = periodos.save(new Periodo("2026-2",
                hoy.minusDays(150), hoy.minusDays(30), hoy.minusDays(180), hoy.minusDays(140), 18)); // matrícula CERRADA

        // ---------- Asignaturas (22) ----------
        Map<String, Asignatura> a = new LinkedHashMap<>();
        asig(a, "MAT-101", "Cálculo Diferencial", 4, 40);
        asig(a, "FIS-101", "Física Mecánica", 4, 35);
        asig(a, "PRG-101", "Fundamentos de Programación", 3, 40);
        asig(a, "MAT-102", "Álgebra Lineal", 3, 35);
        asig(a, "PRG-201", "Programación Orientada a Objetos", 4, 30);
        asig(a, "MAT-201", "Cálculo Integral", 4, 35);
        asig(a, "FIS-201", "Física de Campos", 4, 30);
        asig(a, "EST-201", "Probabilidad y Estadística", 3, 35);
        asig(a, "BDA-201", "Bases de Datos", 3, 30);
        asig(a, "EDD-210", "Estructuras de Datos", 4, 30);
        asig(a, "PRG-310", "Programación Avanzada", 4, 2);          // cupo mínimo: se llena en el seed (RN-02)
        asig(a, "ISW-301", "Ingeniería de Software I", 3, 30);
        asig(a, "ISW-302", "Ingeniería de Software II", 3, 30);
        asig(a, "RED-301", "Redes de Computadores", 3, 30);
        asig(a, "SOP-301", "Sistemas Operativos", 3, 30);
        asig(a, "ARQ-301", "Arquitectura de Computadores", 3, 30);
        asig(a, "SDI-401", "Sistemas Distribuidos", 3, 4);           // 2 cupos libres tras el seed
        asig(a, "ADM-201", "Administración de Proyectos", 2, 40);
        asig(a, "ETI-101", "Ética Profesional", 2, 50);
        asig(a, "ING-101", "Inglés I", 2, 50);
        asig(a, "ING-102", "Inglés II", 2, 50);
        asig(a, "PRG-401", "Programación Concurrente", 3, 30);

        pre(a, "PRG-201", "PRG-101");
        pre(a, "MAT-201", "MAT-101");
        pre(a, "FIS-201", "FIS-101");
        pre(a, "EST-201", "MAT-101");
        pre(a, "BDA-201", "PRG-101");
        pre(a, "EDD-210", "PRG-201");
        pre(a, "PRG-310", "PRG-201");
        pre(a, "ISW-301", "PRG-201");
        pre(a, "ISW-302", "ISW-301");
        pre(a, "RED-301", "PRG-101");
        pre(a, "SOP-301", "EDD-210");
        pre(a, "SDI-401", "EDD-210");
        pre(a, "ING-102", "ING-101");
        pre(a, "PRG-401", "EDD-210");

        // ---------- Estudiantes (12) ----------
        Estudiante e1001 = est("1001", "Laura Marcela Gómez Ríos", "laura.gomez@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 4.1, EstadoMatricula.NORMAL);
        Estudiante e1002 = est("1002", "Carlos Andrés Pérez Londoño", "carlos.perez@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 1.8, EstadoMatricula.CONDICIONAL);
        Estudiante e1003 = est("1003", "Valentina Ospina Cardona", "valentina.ospina@uqvirtual.edu.co", EstadoEstudiante.INACTIVO, 3.5, EstadoMatricula.NORMAL);
        Estudiante e1004 = est("1004", "Juan David Salazar Mora", "juan.salazar@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 3.6, EstadoMatricula.NORMAL);
        Estudiante e1005 = est("1005", "Mariana Castaño Duque", "mariana.castano@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 4.3, EstadoMatricula.NORMAL);
        Estudiante e1006 = est("1006", "Santiago Cardona Arias", "santiago.cardona@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 3.9, EstadoMatricula.NORMAL);
        Estudiante e1007 = est("1007", "Daniela Restrepo Giraldo", "daniela.restrepo@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 4.0, EstadoMatricula.NORMAL);
        Estudiante e1008 = est("1008", "Andrés Felipe Marín Vélez", "andres.marin@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 3.4, EstadoMatricula.NORMAL);
        Estudiante e1009 = est("1009", "Camila Torres Henao", "camila.torres@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 3.7, EstadoMatricula.NORMAL);
        Estudiante e1010 = est("1010", "Sebastián Herrera Quintero", "sebastian.herrera@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 3.2, EstadoMatricula.NORMAL);
        Estudiante e1011 = est("1011", "Paola Andrea Mejía Zapata", "paola.mejia@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 4.5, EstadoMatricula.NORMAL);
        Estudiante e1012 = est("1012", "Julián Vargas Orozco", "julian.vargas@uqvirtual.edu.co", EstadoEstudiante.ACTIVO, 1.9, EstadoMatricula.CONDICIONAL);

        // ---------- Historial académico (2026-2) ----------
        aprobadas(previo, hoy, e1001, a, "PRG-101", "PRG-201", "EDD-210", "MAT-101");
        aprobadas(previo, hoy, e1004, a, "PRG-101");
        aprobadas(previo, hoy, e1005, a, "PRG-101", "PRG-201", "EDD-210", "MAT-101", "FIS-101");
        aprobadas(previo, hoy, e1006, a, "MAT-101", "PRG-101");
        aprobadas(previo, hoy, e1007, a, "PRG-101", "PRG-201");
        aprobadas(previo, hoy, e1008, a, "PRG-101", "PRG-201");
        aprobadas(previo, hoy, e1009, a, "PRG-101", "PRG-201");
        aprobadas(previo, hoy, e1010, a, "PRG-101", "PRG-201", "EDD-210");
        aprobadas(previo, hoy, e1011, a, "PRG-101", "PRG-201", "EDD-210");
        aprobadas(previo, hoy, e1012, a, "ING-101");
        inscripciones.save(new Inscripcion(e1002, a.get("PRG-101"), previo, EstadoInscripcion.REPROBADA,
                hoy.minusDays(170), hoy.minusDays(170).atTime(9, 0)));

        // ---------- Inscripciones ACTIVAS en 2027-1 ----------
        activas(actual, hoy, e1001, a, "BDA-201", "ISW-301");                                     // 6 créditos; RN-04 con BDA-201
        activas(actual, hoy, e1002, a, "ING-101", "ETI-101");                                     // condicional con 2 -> RN-06
        activas(actual, hoy, e1006, a, "MAT-201", "EST-201", "BDA-201", "RED-301", "ETI-101");    // 15 créditos -> RN-05
        activas(actual, hoy, e1008, a, "PRG-310");
        activas(actual, hoy, e1009, a, "PRG-310");                                                // PRG-310 sin cupo -> RN-02
        activas(actual, hoy, e1010, a, "SDI-401");
        activas(actual, hoy, e1011, a, "SDI-401");                                                // SDI-401: 2 cupos libres

        // ---------- cupoDisponible = cupoMaximo - inscripciones ACTIVAS ----------
        for (Asignatura asig : a.values()) {
            long ocupados = inscripciones.countByAsignaturaCodigoAndEstado(asig.getCodigo(), EstadoInscripcion.ACTIVA);
            asig.setCupoDisponible(asig.getCupoMaximo() - (int) ocupados);
        }
    }

    // ---------- ayudas ----------

    private void asig(Map<String, Asignatura> mapa, String codigo, String nombre, int creditos, int cupo) {
        mapa.put(codigo, asignaturas.save(new Asignatura(codigo, nombre, creditos, cupo)));
    }

    private void pre(Map<String, Asignatura> mapa, String dependiente, String prerrequisito) {
        mapa.get(dependiente).getPrerrequisitos().add(mapa.get(prerrequisito));
    }

    private Estudiante est(String doc, String nombre, String email, EstadoEstudiante estado,
                           double promedio, EstadoMatricula matricula) {
        return estudiantes.save(new Estudiante(doc, nombre, PROGRAMA, email, estado, promedio, matricula));
    }

    private void aprobadas(Periodo p, LocalDate hoy, Estudiante e, Map<String, Asignatura> a, String... codigos) {
        for (String c : codigos) {
            inscripciones.save(new Inscripcion(e, a.get(c), p, EstadoInscripcion.APROBADA,
                    hoy.minusDays(170), hoy.minusDays(170).atTime(9, 0)));
        }
    }

    private void activas(Periodo p, LocalDate hoy, Estudiante e, Map<String, Asignatura> a, String... codigos) {
        for (String c : codigos) {
            inscripciones.save(new Inscripcion(e, a.get(c), p, EstadoInscripcion.ACTIVA,
                    hoy.minusDays(3), hoy.minusDays(3).atTime(10, 15)));
        }
    }
}
