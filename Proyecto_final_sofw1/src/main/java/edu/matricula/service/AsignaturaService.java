package edu.matricula.service;

import edu.matricula.model.Asignatura;
import edu.matricula.repo.AsignaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturas;

    public AsignaturaService(AsignaturaRepository asignaturas) {
        this.asignaturas = asignaturas;
    }

    @Transactional(readOnly = true)
    public List<Asignatura> buscar(String query) {
        return asignaturas.buscar(query == null ? "" : query.trim());
    }

    @Transactional(readOnly = true)
    public Asignatura obtener(String codigo) {
        return asignaturas.findById(codigo == null ? "" : codigo.trim())
                .orElseThrow(() -> NegocioException.noEncontrado("No existe la asignatura " + codigo));
    }
}
