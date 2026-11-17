package edu.matricula.web;

import edu.matricula.dto.PeriodoDto;
import edu.matricula.service.PeriodoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/periodos")
public class PeriodoController {

    private final PeriodoService service;

    public PeriodoController(PeriodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PeriodoDto> listar() {
        return service.listar().stream().map(PeriodoDto::de).toList();
    }
}
