package edu.matricula.web;

import edu.matricula.dto.*;
import edu.matricula.model.Estudiante;
import edu.matricula.service.EstudianteService;
import edu.matricula.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estudiantes")
public class EstudianteController {

    private final EstudianteService service;
    private final InscripcionService inscripcionService;

    public EstudianteController(EstudianteService service, InscripcionService inscripcionService) {
        this.service = service;
        this.inscripcionService = inscripcionService;
    }

    /** REQ011 (listar con búsqueda y paginación). */
    @GetMapping
    public PaginaResponse<Estudiante> listar(@RequestParam(defaultValue = "") String query,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return service.listar(query, page, size);
    }

    @GetMapping("/{id}")
    public Estudiante obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    /** REQ001/REQ002: resumen del estudiante en un periodo (A8 si no existe). */
    @GetMapping("/{id}/resumen")
    public EstudianteResumen resumen(@PathVariable String id,
                                     @RequestParam(required = false) String periodo) {
        return inscripcionService.resumen(id, periodo);
    }

    /** REQ012. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante crear(@Valid @RequestBody EstudianteCreate dto) {
        return service.crear(dto);
    }

    /** REQ013. */
    @PatchMapping("/{id}")
    public Estudiante actualizar(@PathVariable String id, @Valid @RequestBody EstudianteUpdate dto) {
        return service.actualizar(id, dto);
    }

    /** REQ014: baja lógica (RC-01). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable String id) {
        service.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
