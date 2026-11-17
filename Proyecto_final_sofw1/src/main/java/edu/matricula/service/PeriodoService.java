package edu.matricula.service;

import edu.matricula.model.Periodo;
import edu.matricula.repo.PeriodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PeriodoService {

    private final PeriodoRepository periodos;

    public PeriodoService(PeriodoRepository periodos) {
        this.periodos = periodos;
    }

    @Transactional(readOnly = true)
    public List<Periodo> listar() {
        return periodos.findAllByOrderByFechaInicioDesc();
    }

    @Transactional(readOnly = true)
    public Periodo obtener(String codigo) {
        return periodos.findById(codigo == null ? "" : codigo.trim())
                .orElseThrow(() -> NegocioException.noEncontrado("No existe el periodo " + codigo));
    }

    /** Periodo con matrícula abierta; si no hay ninguno, el más reciente. */
    @Transactional(readOnly = true)
    public Periodo vigente() {
        List<Periodo> todos = listar();
        return todos.stream().filter(Periodo::matriculaAbierta).findFirst()
                .orElseGet(() -> todos.stream().findFirst()
                        .orElseThrow(() -> NegocioException.noEncontrado("No hay periodos configurados")));
    }
}
