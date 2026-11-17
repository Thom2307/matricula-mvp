package edu.matricula.web;

import edu.matricula.dto.AsignaturaDto;
import edu.matricula.service.AsignaturaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asignaturas")
public class AsignaturaController {

    private final AsignaturaService service;

    public AsignaturaController(AsignaturaService service) {
        this.service = service;
    }

    /** REQ003/REQ004: búsqueda por código o nombre. */
    @GetMapping
    public List<AsignaturaDto> buscar(@RequestParam(defaultValue = "") String query) {
        return service.buscar(query).stream().map(AsignaturaDto::de).toList();
    }
}
