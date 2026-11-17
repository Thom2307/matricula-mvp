package edu.matricula.web;

import edu.matricula.dto.ComprobanteResponse;
import edu.matricula.dto.InscripcionRequest;
import edu.matricula.dto.ValidacionResponse;
import edu.matricula.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    /** REQ006/REQ007: valida reglas sin persistir. */
    @PostMapping("/validar")
    public ValidacionResponse validar(@Valid @RequestBody InscripcionRequest req) {
        return service.validar(req);
    }

    /** REQ008-REQ010: confirma la inscripción (transacción atómica). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComprobanteResponse crear(@Valid @RequestBody InscripcionRequest req) {
        return service.crear(req);
    }
}
