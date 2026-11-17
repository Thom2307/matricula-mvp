# MVP Matrícula Académica — Inscribir Asignatura

Proyecto grupal de Ingeniería de Software II (Universidad del Quindío).
Escenario **Matrícula Académica, variante A**: transacción **Inscribir Asignatura** (`Estudiante`, `Asignatura`, `Inscripcion`, con `Periodo` como soporte) y **CRUD de Estudiante**, en exactamente dos pantallas.

## Arquitectura

| Capa | Tecnología | Paquete / carpeta |
|---|---|---|
| Modelo y persistencia | Java 17, Spring Data JPA, H2 en memoria | `edu.matricula.model`, `edu.matricula.repo` |
| Reglas y casos de uso | Spring `@Service` (transaccional) | `edu.matricula.service` (`ValidadorReglas`, `InscripcionService`, `EstudianteService`, …) |
| API REST | Spring Web + Bean Validation | `edu.matricula.web`, `edu.matricula.dto` |
| Datos de prueba | `CommandLineRunner` | `edu.matricula.config.DataLoader` |
| Frontend (2 pantallas) | HTML + JS sin dependencias | `src/main/resources/static` |

La inscripción (`InscripcionService.crear`) es **atómica**: bloquea la asignatura (`PESSIMISTIC_WRITE`), re-valida todas las reglas, crea la `Inscripcion` y descuenta el cupo; si algo falla, no se persiste nada.

## Instalación y ejecución

Requisitos: **JDK 17+** y **Maven 3.9+**.

```bash
mvn test                 # ejecuta las pruebas de aceptación
mvn spring-boot:run      # arranca en http://localhost:8080
```

* Pantalla 1 (transacción): <http://localhost:8080/>
* Pantalla 2 (CRUD): <http://localhost:8080/estudiantes.html>
* Consola H2 (opcional): <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:matricula`, usuario `sa`, sin contraseña)

Los datos se recrean en cada arranque; las fechas del seed son relativas a «hoy», así que el periodo `2027-1` siempre tiene la matrícula abierta.

## Interfaz

Siguen siendo **dos pantallas**, ahora con un diseño más cuidado y navegación explícita:

* **Pantalla 1 (`/`)**: asistente de 3 pasos (Identifícate → Elige asignatura → Confirma) con botones **Siguiente / Anterior** y un indicador de pasos clicable. Al elegir una asignatura se valida y se pasa solo al paso 3; al confirmar aparece el comprobante con «Inscribir otra asignatura».
* **Pantalla 2 (`/estudiantes.html`)**: tabla con avatares, búsqueda, paginación, formulario modal y confirmación de baja en diálogo propio. Cada estudiante activo tiene un botón **Inscribir** que abre la pantalla 1 con su documento ya consultado (`/?documento=1005`).
* Ambas pantallas tienen botones para ir de una a la otra (barra superior y tarjeta al final de la página).
* Las etiquetas pequeñas con el atributo del modelo (p. ej. `Estudiante.documento`) se conservaron para la trazabilidad widget → modelo.
* Solo se modificó `src/main/resources/static`; el backend, la API y las pruebas no cambiaron.

## Datos demo (precargados)

12 estudiantes (`1001`–`1012`), 22 asignaturas, 2 periodos. En la pantalla 1 elige el periodo, escribe el documento y busca la asignatura:

| Documento | Asignatura | Periodo | Resultado esperado |
|---|---|---|---|
| `1005` | `SDI-401` | 2027-1 | **Éxito** (quedan 2 cupos → 1) |
| `1001` | `PRG-401` | 2027-1 | **Éxito** |
| `1003` | cualquiera (p. ej. `MAT-101`) | 2027-1 | **RN-01** estudiante inactivo |
| `1007` | `PRG-310` | 2027-1 | **RN-02** sin cupos |
| `1004` | `EDD-210` | 2027-1 | **RN-03** prerrequisito no aprobado |
| `1001` | `BDA-201` | 2027-1 | **RN-04** ya inscrita |
| `1006` | `FIS-101` | 2027-1 | **RN-05** supera 18 créditos (15 + 4) |
| `1002` | `MAT-102` | 2027-1 | **RN-06** matrícula condicional con 2 asignaturas |
| `1005` | `SDI-401` | 2026-2 | **RN-07** matrícula cerrada |
| `9999` | — | — | **A8** documento inexistente |

CRUD: `1001` no se puede dar de baja (**RC-01**, tiene 2 inscripciones activas); `1003` ya está inactivo; crear con un documento existente (p. ej. `1001`) da error de duplicado.

## Reglas de negocio

Orden de validación: **RN-07, RN-01, RN-04, RN-03, RN-06, RN-05, RN-02**.

| Regla | Condición | Mensaje |
|---|---|---|
| RN-07 | hoy entre `fechaInicioMatricula` y `fechaFinMatricula` | La matrícula de este periodo está cerrada |
| RN-01 | `Estudiante.estado = ACTIVO` | Tu estado de estudiante está inactivo |
| RN-04 | sin otra inscripción ACTIVA de la asignatura en el periodo | Ya tienes inscrita esta asignatura en el periodo |
| RN-03 | cada prerrequisito con inscripción APROBADA | No has aprobado los prerrequisitos de esta asignatura |
| RN-06 | si `CONDICIONAL`, máximo 2 asignaturas en el periodo | Estás en matrícula condicional: solo puedes inscribir 2 asignaturas por periodo |
| RN-05 | créditos activos + créditos de la asignatura ≤ `Periodo.maxCreditos` (18) | Superarías el máximo de créditos permitido |
| RN-02 | `Asignatura.cupoDisponible > 0` | La asignatura no tiene cupos disponibles |
| RC-01 | no pasar a INACTIVO con inscripciones ACTIVAS | No se puede dar de baja: tiene N inscripciones activas |

## API

| Método y ruta | Uso | Requisito |
|---|---|---|
| `GET /periodos` | periodos y estado de matrícula | apoyo |
| `GET /estudiantes?query=&page=&size=` | listar con búsqueda y paginación | REQ011 |
| `GET /estudiantes/{id}/resumen?periodo=` | resumen del estudiante en el periodo | REQ001, REQ002 |
| `GET /asignaturas?query=` | buscar asignaturas | REQ003, REQ004 |
| `POST /inscripciones/validar` | valida reglas sin persistir | REQ006, REQ007 |
| `POST /inscripciones` | confirma (atómico) y devuelve comprobante | REQ008, REQ009, REQ010 |
| `POST /estudiantes` | crear | REQ012 |
| `PATCH /estudiantes/{id}` | actualizar (documento no editable) | REQ013 |
| `DELETE /estudiantes/{id}` | baja lógica (INACTIVO) | REQ014 |

Errores: `{"regla","mensaje"}` (422 en inscripciones, 409 en el CRUD, 404 si no existe) y `{"errores":{campo:mensaje}}` (400).

## Matriz de trazabilidad

| Requisito | Caso de uso | Diagrama | Widget del prototipo | Endpoint / método | Prueba |
|---|---|---|---|---|---|
| REQ001 identificar estudiante | CU-01 p.1 | Sec-01 | Campo `documento` + «Consultar» | `GET /estudiantes/{id}/resumen` · `InscripcionService.resumen` | `documentoInexistente` |
| REQ002 resumen | CU-01 p.2 | Sec-01 | Tarjeta de resumen y créditos | `GET /estudiantes/{id}/resumen` | `inscripcionExitosa` |
| REQ003 buscar asignaturas | CU-01 p.3 | Sec-01 | Campo `query` + «Buscar» | `GET /asignaturas` · `AsignaturaService.buscar` | — |
| REQ004 información de la asignatura | CU-01 p.4 | Sec-01 | Tabla de resultados | `GET /asignaturas` · `AsignaturaDto` | — |
| REQ005 seleccionar | CU-01 p.5 | Act-01 | Botón «Seleccionar» | `seleccionar()` (JS) | — |
| REQ006 validar reglas | CU-01 p.6 | Act-01 / Sec-01 | Panel de confirmación | `POST /inscripciones/validar` · `ValidadorReglas.validarReglas` | `validarSinEfectos` |
| REQ007 notificar reglas incumplidas | CU-01 A1–A7 | Act-01 | Caja de error con código de regla | `GlobalExceptionHandler.negocio` | un test por RN |
| REQ008 confirmar | CU-01 p.8 | Sec-01 | «Confirmar inscripción» | `POST /inscripciones` · `InscripcionService.crear` | `inscripcionExitosa` |
| REQ009 actualizar cupo | CU-01 p.9 | Act-01 (∥) / Sec-01 | Cupos restantes en comprobante | `Asignatura.setCupoDisponible` | `inscripcionExitosa` |
| REQ010 comprobante | CU-01 p.10 | Sec-01 | Panel «Inscripción registrada» | `ComprobanteResponse` | `inscripcionExitosa` |
| REQ011 listar estudiantes | CU-02 | — | Tabla + búsqueda + paginación | `GET /estudiantes` | — |
| REQ012 crear | CU-02 S1 | — | «+ Nuevo estudiante» | `POST /estudiantes` | `crudCompleto`, `documentoDuplicado`, `validacionCampos` |
| REQ013 actualizar | CU-02 S2 | — | «Editar» | `PATCH /estudiantes/{id}` | `crudCompleto` |
| REQ014 dar de baja | CU-02 S3 | — | «Dar de baja» | `DELETE /estudiantes/{id}` | `crudCompleto`, `bajaConInscripcionesActivas` |

## Pruebas de aceptación (Given-When-Then)

Implementadas en `src/test/java/edu/matricula/InscripcionAceptacionTest.java`.

1. **Éxito.** *Given* el estudiante `1005` aprobó los prerrequisitos de `SDI-401`, que tiene 2 cupos, y la matrícula `2027-1` está abierta, *When* confirma la inscripción, *Then* se crea una inscripción ACTIVA con sello de tiempo, el cupo baja de 2 a 1 y se muestra el comprobante.
2. **RN-02.** *Given* `PRG-310` sin cupos, *When* `1007` intenta inscribirla, *Then* aparece «La asignatura no tiene cupos disponibles» y no se crea el registro.
3. **RN-06.** *Given* `1002` en matrícula condicional con 2 asignaturas, *When* intenta inscribir una tercera, *Then* aparece «Estás en matrícula condicional: solo puedes inscribir 2 asignaturas por periodo».
4. **RN-07.** *Given* el periodo `2026-2` con matrícula cerrada, *When* `1005` intenta inscribir, *Then* aparece «La matrícula de este periodo está cerrada».
5. **RN-03, RN-04, RN-05, RN-01.** Un escenario por regla con los datos de la tabla demo.
6. **RC-01.** *Given* `1001` con 2 inscripciones activas, *When* se intenta darlo de baja, *Then* aparece «No se puede dar de baja: tiene 2 inscripciones activas» y su estado sigue ACTIVO.

## Convenciones de repositorio

* Ramas: `main` (estable) ← `develop` ← `feature/<alcance>-<tema>` (p. ej. `feature/transaccion-validador`, `feature/crud-estudiante`).
* PRs cortos (un cambio, idealmente < 300 líneas), con al menos una revisión de otro integrante antes de fusionar.
* Commits en imperativo y en español: `Agrega validación de prerrequisitos (RN-03)`.
* Los nombres de clases, atributos y métodos deben coincidir con el diagrama de clases y las secuencias.

## Guion del video de demostración (4 minutos)

1. **(0:00–0:30)** Contexto: escenario, 2 pantallas, trazabilidad modelo → prototipo → código.
2. **(0:30–2:00) Flujo exitoso.** Pantalla 1, periodo `2027-1`, documento `1005`: se ve el resumen (0 de 18 créditos). Buscar «distribuidos», seleccionar `SDI-401`, mostrar el resumen validado, confirmar y mostrar el comprobante (cupos 2 → 1, sello de tiempo).
3. **(2:00–3:00) Flujo alterno bloqueante.** Documento `1002`, buscar `MAT-102`, seleccionar: aparece **RN-06** en línea. Mostrar también `1007` con `PRG-310` (**RN-02**) y que el cupo no cambió.
4. **(3:00–3:45) CRUD.** Pantalla 2: buscar, crear un estudiante, editarlo, intentar dar de baja a `1001` (**RC-01**) y dar de baja al estudiante recién creado.
5. **(3:45–4:00)** Cierre: ejecutar `mvn test` y mostrar las pruebas en verde.
