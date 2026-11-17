package edu.matricula;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.matricula.model.EstadoEstudiante;
import edu.matricula.model.EstadoInscripcion;
import edu.matricula.repo.AsignaturaRepository;
import edu.matricula.repo.EstudianteRepository;
import edu.matricula.repo.InscripcionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Pruebas de aceptación (Given-When-Then). Usan los datos demo de DataLoader.
 * Cada prueba se revierte al terminar (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InscripcionAceptacionTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AsignaturaRepository asignaturas;
    @Autowired InscripcionRepository inscripciones;
    @Autowired EstudianteRepository estudiantes;

    // ---------- ayudas ----------

    private static String cuerpo(String doc, String asig, String per) {
        return "{\"documento\":\"%s\",\"codigoAsignatura\":\"%s\",\"codigoPeriodo\":\"%s\"}".formatted(doc, asig, per);
    }

    private JsonNode ejecutar(MockHttpServletRequestBuilder req, int estadoEsperado) throws Exception {
        MvcResult r = mvc.perform(req).andReturn();
        assertEquals(estadoEsperado, r.getResponse().getStatus(), "Código HTTP");
        byte[] bytes = r.getResponse().getContentAsByteArray();
        return bytes.length == 0 ? null : mapper.readTree(bytes);
    }

    private MockHttpServletRequestBuilder inscribir(String doc, String asig, String per) {
        return post("/inscripciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo(doc, asig, per));
    }

    private void assertBloqueo(String doc, String asig, String per, String regla, String mensaje) throws Exception {
        int cupoAntes = asignaturas.findById(asig).orElseThrow().getCupoDisponible();
        long inscripcionesAntes = inscripciones.count();

        JsonNode error = ejecutar(inscribir(doc, asig, per), 422);

        assertEquals(regla, error.get("regla").asText());
        assertEquals(mensaje, error.get("mensaje").asText());
        assertEquals(inscripcionesAntes, inscripciones.count(), "No debe crearse la inscripción");
        assertEquals(cupoAntes, asignaturas.findById(asig).orElseThrow().getCupoDisponible(), "El cupo no cambia");
    }

    // ---------- CU-01: flujo exitoso ----------

    @Test
    @DisplayName("Escenario 1: inscripción exitosa crea la inscripción y descuenta el cupo")
    void inscripcionExitosa() throws Exception {
        // Given: 1005 aprobó los prerrequisitos de SDI-401, que tiene 2 cupos y la matrícula 2027-1 está abierta
        int cupoAntes = asignaturas.findById("SDI-401").orElseThrow().getCupoDisponible();
        assertEquals(2, cupoAntes);

        // When
        JsonNode comprobante = ejecutar(inscribir("1005", "SDI-401", "2027-1"), 201);

        // Then
        assertEquals("ACTIVA", comprobante.get("estado").asText());
        assertEquals(1, comprobante.get("cupoDisponibleRestante").asInt());
        assertEquals(3, comprobante.get("creditosInscritos").asInt());
        assertNotNull(comprobante.get("selloTiempo"));
        assertEquals(1, asignaturas.findById("SDI-401").orElseThrow().getCupoDisponible());
        assertTrue(inscripciones.existsByEstudianteDocumentoAndAsignaturaCodigoAndPeriodoCodigoAndEstado(
                "1005", "SDI-401", "2027-1", EstadoInscripcion.ACTIVA));
    }

    // ---------- CU-01: un caso por regla ----------

    @Test
    @DisplayName("RN-01 / A1: estudiante inactivo")
    void estudianteInactivo() throws Exception {
        assertBloqueo("1003", "MAT-101", "2027-1", "RN-01", "Tu estado de estudiante está inactivo");
    }

    @Test
    @DisplayName("RN-02 / A2: asignatura sin cupos")
    void sinCupo() throws Exception {
        assertBloqueo("1007", "PRG-310", "2027-1", "RN-02", "La asignatura no tiene cupos disponibles");
    }

    @Test
    @DisplayName("RN-03 / A3: prerrequisitos no aprobados")
    void sinPrerrequisitos() throws Exception {
        assertBloqueo("1004", "EDD-210", "2027-1", "RN-03", "No has aprobado los prerrequisitos de esta asignatura");
    }

    @Test
    @DisplayName("RN-04 / A4: asignatura ya inscrita en el periodo")
    void duplicada() throws Exception {
        assertBloqueo("1001", "BDA-201", "2027-1", "RN-04", "Ya tienes inscrita esta asignatura en el periodo");
    }

    @Test
    @DisplayName("RN-05 / A5: supera el máximo de créditos")
    void superaCreditos() throws Exception {
        assertBloqueo("1006", "FIS-101", "2027-1", "RN-05", "Superarías el máximo de créditos permitido");
    }

    @Test
    @DisplayName("RN-06 / A6: matrícula condicional con 2 asignaturas")
    void matriculaCondicional() throws Exception {
        assertBloqueo("1002", "MAT-102", "2027-1", "RN-06",
                "Estás en matrícula condicional: solo puedes inscribir 2 asignaturas por periodo");
    }

    @Test
    @DisplayName("RN-07 / A7: matrícula cerrada")
    void matriculaCerrada() throws Exception {
        assertBloqueo("1005", "SDI-401", "2026-2", "RN-07", "La matrícula de este periodo está cerrada");
    }

    @Test
    @DisplayName("A8: documento inexistente")
    void documentoInexistente() throws Exception {
        JsonNode error = ejecutar(get("/estudiantes/9999/resumen"), 404);
        assertEquals("No existe un estudiante con documento 9999", error.get("mensaje").asText());
    }

    // ---------- validar no tiene efectos ----------

    @Test
    @DisplayName("POST /inscripciones/validar no persiste nada")
    void validarSinEfectos() throws Exception {
        long antes = inscripciones.count();
        int cupoAntes = asignaturas.findById("SDI-401").orElseThrow().getCupoDisponible();

        JsonNode r = ejecutar(post("/inscripciones/validar").contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("1005", "SDI-401", "2027-1")), 200);

        assertTrue(r.get("valido").asBoolean());
        assertEquals(3, r.get("creditosResultantes").asInt());
        assertEquals(antes, inscripciones.count());
        assertEquals(cupoAntes, asignaturas.findById("SDI-401").orElseThrow().getCupoDisponible());
    }

    // ---------- CU-02: CRUD de Estudiante ----------

    @Test
    @DisplayName("CRUD: crear, actualizar y dar de baja un estudiante sin inscripciones")
    void crudCompleto() throws Exception {
        String nuevo = "{\"documento\":\"2001\",\"nombre\":\"Nuevo Estudiante\","
                + "\"programa\":\"Ingeniería de Sistemas y Computación\","
                + "\"email\":\"nuevo@uqvirtual.edu.co\",\"promedioAcumulado\":3.5}";

        JsonNode creado = ejecutar(post("/estudiantes").contentType(MediaType.APPLICATION_JSON).content(nuevo), 201);
        assertEquals("ACTIVO", creado.get("estado").asText());
        assertEquals("NORMAL", creado.get("estadoMatricula").asText());

        JsonNode editado = ejecutar(patch("/estudiantes/2001").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nombre Editado\",\"promedioAcumulado\":4.0}"), 200);
        assertEquals("Nombre Editado", editado.get("nombre").asText());
        assertEquals(4.0, editado.get("promedioAcumulado").asDouble());

        ejecutar(delete("/estudiantes/2001"), 204);
        assertEquals(EstadoEstudiante.INACTIVO, estudiantes.findById("2001").orElseThrow().getEstado());
    }

    @Test
    @DisplayName("CRUD: documento duplicado")
    void documentoDuplicado() throws Exception {
        String dup = "{\"documento\":\"1001\",\"nombre\":\"Otra Persona\",\"programa\":\"Sistemas\","
                + "\"email\":\"otra@uqvirtual.edu.co\",\"promedioAcumulado\":3.0}";
        JsonNode error = ejecutar(post("/estudiantes").contentType(MediaType.APPLICATION_JSON).content(dup), 409);
        assertEquals("DOCUMENTO_DUPLICADO", error.get("regla").asText());
    }

    @Test
    @DisplayName("CRUD: email inválido y promedio fuera de rango")
    void validacionCampos() throws Exception {
        String malo = "{\"documento\":\"2002\",\"nombre\":\"Alguien\",\"programa\":\"Sistemas\","
                + "\"email\":\"no-es-un-email\",\"promedioAcumulado\":7.5}";
        JsonNode r = ejecutar(post("/estudiantes").contentType(MediaType.APPLICATION_JSON).content(malo), 400);
        assertTrue(r.get("errores").has("email"));
        assertTrue(r.get("errores").has("promedioAcumulado"));
        assertFalse(estudiantes.existsById("2002"));
    }

    @Test
    @DisplayName("RC-01: no se puede dar de baja a un estudiante con inscripciones activas")
    void bajaConInscripcionesActivas() throws Exception {
        JsonNode error = ejecutar(delete("/estudiantes/1001"), 409);
        assertEquals("RC-01", error.get("regla").asText());
        assertEquals("No se puede dar de baja: tiene 2 inscripciones activas", error.get("mensaje").asText());
        assertEquals(EstadoEstudiante.ACTIVO, estudiantes.findById("1001").orElseThrow().getEstado());
    }

    @Test
    @DisplayName("RC-01 también aplica al actualizar el estado a INACTIVO")
    void patchInactivoConInscripcionesActivas() throws Exception {
        JsonNode error = ejecutar(patch("/estudiantes/1001").contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"INACTIVO\"}"), 409);
        assertEquals("RC-01", error.get("regla").asText());
    }
}
