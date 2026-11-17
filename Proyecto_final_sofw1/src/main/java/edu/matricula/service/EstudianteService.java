package edu.matricula.service;

import edu.matricula.dto.EstudianteCreate;
import edu.matricula.dto.EstudianteUpdate;
import edu.matricula.dto.PaginaResponse;
import edu.matricula.model.*;
import edu.matricula.repo.EstudianteRepository;
import edu.matricula.repo.InscripcionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CU-02: Gestionar Estudiante (listar, crear, actualizar, dar de baja). */
@Service
public class EstudianteService {

    private final EstudianteRepository estudiantes;
    private final InscripcionRepository inscripciones;

    public EstudianteService(EstudianteRepository estudiantes, InscripcionRepository inscripciones) {
        this.estudiantes = estudiantes;
        this.inscripciones = inscripciones;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<Estudiante> listar(String query, int pagina, int tamano) {
        int p = Math.max(pagina, 0);
        int t = Math.min(Math.max(tamano, 1), 100);
        Page<Estudiante> r = estudiantes.buscar(query == null ? "" : query.trim(),
                PageRequest.of(p, t, Sort.by("documento")));
        return new PaginaResponse<>(r.getContent(), r.getNumber(), r.getSize(),
                r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public Estudiante obtener(String documento) {
        String doc = documento == null ? "" : documento.trim();
        return estudiantes.findById(doc)
                .orElseThrow(() -> NegocioException.noEncontrado(
                        "No existe un estudiante con documento " + doc));
    }

    @Transactional
    public Estudiante crear(EstudianteCreate dto) {
        String doc = dto.documento().trim();
        if (estudiantes.existsById(doc)) {
            throw NegocioException.conflicto("DOCUMENTO_DUPLICADO",
                    "Ya existe un estudiante con el documento " + doc);
        }
        Estudiante e = new Estudiante(doc, dto.nombre().trim(), dto.programa().trim(), dto.email().trim(),
                dto.estado() == null ? EstadoEstudiante.ACTIVO : dto.estado(),
                dto.promedioAcumulado(),
                dto.estadoMatricula() == null ? EstadoMatricula.NORMAL : dto.estadoMatricula());
        return estudiantes.save(e);
    }

    @Transactional
    public Estudiante actualizar(String documento, EstudianteUpdate dto) {
        Estudiante e = obtener(documento);
        if (dto.nombre() != null) e.setNombre(dto.nombre().trim());
        if (dto.programa() != null) e.setPrograma(dto.programa().trim());
        if (dto.email() != null) e.setEmail(dto.email().trim());
        if (dto.promedioAcumulado() != null) e.setPromedioAcumulado(dto.promedioAcumulado());
        if (dto.estadoMatricula() != null) e.setEstadoMatricula(dto.estadoMatricula());
        if (dto.estado() != null) {
            if (dto.estado() == EstadoEstudiante.INACTIVO && e.estaActivo()) {
                verificarSinInscripcionesActivas(e);   // RC-01
            }
            e.setEstado(dto.estado());
        }
        return estudiantes.save(e);
    }

    /** Baja lógica: pasa a INACTIVO si no tiene inscripciones ACTIVAS (RC-01). */
    @Transactional
    public Estudiante darDeBaja(String documento) {
        Estudiante e = obtener(documento);
        if (e.estaActivo()) {
            verificarSinInscripcionesActivas(e);
            e.setEstado(EstadoEstudiante.INACTIVO);
            estudiantes.save(e);
        }
        return e;
    }

    private void verificarSinInscripcionesActivas(Estudiante e) {
        long n = inscripciones.countByEstudianteDocumentoAndEstado(e.getDocumento(), EstadoInscripcion.ACTIVA);
        if (n > 0) {
            String detalle = n == 1 ? "1 inscripción activa" : n + " inscripciones activas";
            throw NegocioException.conflicto("RC-01", "No se puede dar de baja: tiene " + detalle);
        }
    }
}
