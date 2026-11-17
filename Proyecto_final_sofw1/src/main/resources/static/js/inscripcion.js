/* Pantalla 1: CU-01 Inscribir Asignatura (asistente de 3 pasos). */
let resumenActual = null;   // EstudianteResumen vigente
let seleccion = null;       // código de la asignatura seleccionada
let hayComprobante = false; // true cuando se muestra el comprobante del paso 3
let pasoActual = 1;

const VACIO_CONFIRMACION =
  '<div class="vacio">Selecciona una asignatura para validar las reglas y ver el resumen.</div>';

/* ---------- Navegación entre pasos ---------- */
function pasoMaximo() {
  if (!resumenActual) return 1;
  return (seleccion || hayComprobante) ? 3 : 2;
}

function irAPaso(n) {
  if (n > pasoMaximo()) return;
  const cambio = n !== pasoActual;
  pasoActual = n;
  document.querySelectorAll('.paso').forEach(p => { p.hidden = Number(p.dataset.paso) !== n; });
  document.querySelectorAll('#pasos li').forEach(li => {
    const k = Number(li.dataset.paso);
    li.classList.toggle('activo', k === n);
    li.classList.toggle('hecho', k < n);
    const b = li.querySelector('button');
    b.disabled = k > pasoMaximo();
    if (k === n) b.setAttribute('aria-current', 'step'); else b.removeAttribute('aria-current');
  });
  $('btnSiguiente1').disabled = !resumenActual;
  $('btnSiguiente2').disabled = !seleccion;
  if (cambio) {
    if (n === 2) buscarAsignaturas();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

function refrescarPasos() { irAPaso(Math.min(pasoActual, pasoMaximo())); }

/* ---------- Periodos ---------- */
async function cargarPeriodos() {
  const periodos = await api('GET', '/periodos');
  const sel = $('periodo');
  sel.innerHTML = periodos.map(p =>
    `<option value="${esc(p.codigo)}">${esc(p.codigo)} — matrícula ${p.matriculaAbierta ? 'abierta' : 'cerrada'}` +
    ` (${esc(p.fechaInicioMatricula)} a ${esc(p.fechaFinMatricula)})</option>`).join('');
  const abierto = periodos.find(p => p.matriculaAbierta);
  if (abierto) sel.value = abierto.codigo;
}

function reiniciarSeleccion() {
  seleccion = null;
  hayComprobante = false;
  $('confirmacion').innerHTML = VACIO_CONFIRMACION;
}

function reiniciarTodo() {
  resumenActual = null;
  $('resumen').innerHTML = '<div class="vacio">Ingresa tu documento para ver tu resumen de matrícula.</div>';
  $('resultados').innerHTML =
    '<div class="vacio">Consulta tu documento y busca una asignatura para ver los resultados.</div>';
  $('query').disabled = true;
  $('btnBuscar').disabled = true;
  reiniciarSeleccion();
}

/* ---------- Paso 1: identificar y mostrar resumen ---------- */
async function consultarEstudiante({ conservarSeleccion = false } = {}) {
  const documento = $('documento').value.trim();
  const campo = $('documento');
  $('errEstudiante').textContent = '';
  campo.removeAttribute('aria-invalid');

  if (!documento) {
    campo.setAttribute('aria-invalid', 'true');
    $('errEstudiante').textContent = 'El documento es obligatorio';
    return;
  }
  try {
    const r = await api('GET',
      `/estudiantes/${encodeURIComponent(documento)}/resumen?periodo=${encodeURIComponent($('periodo').value)}`);
    resumenActual = r;
    pintarResumen(r);
    $('query').disabled = false;
    $('btnBuscar').disabled = false;
    if (!conservarSeleccion) reiniciarSeleccion();
  } catch (e) {
    reiniciarTodo();                                  // A8: el documento no existe
    campo.setAttribute('aria-invalid', 'true');
    $('errEstudiante').textContent = e.message;
  }
  refrescarPasos();
}

function pintarResumen(r) {
  const est = r.estudiante;
  const pct = r.maxCreditos ? Math.min(100, Math.round(r.creditosInscritos * 100 / r.maxCreditos)) : 0;
  const activas = r.inscripcionesActivas.length
    ? `<div class="tabla-scroll"><table><thead><tr><th>Código</th><th>Asignatura</th><th class="num">Créditos</th><th>Inscrita el</th></tr></thead><tbody>` +
      r.inscripcionesActivas.map(i =>
        `<tr><td class="cod">${esc(i.codigoAsignatura)}</td><td>${esc(i.nombreAsignatura)}</td><td class="num">${i.creditos}</td><td>${esc(i.fechaInscripcion)}</td></tr>`).join('') +
      '</tbody></table></div>'
    : '<div class="vacio" style="padding:14px">Aún no tienes asignaturas inscritas en este periodo.</div>';

  $('resumen').innerHTML = `
    <div class="perfil">
      <span class="avatar grande" aria-hidden="true">${esc(iniciales(est.nombre))}</span>
      <div>
        <h4>${esc(est.nombre)} <span class="attr">Estudiante.nombre</span></h4>
        <p>${esc(est.programa)} <span class="attr">Estudiante.programa</span></p>
      </div>
      <div class="insignias">
        <span class="badge ${est.estado === 'ACTIVO' ? 'ok' : 'err'}" title="Estudiante.estado">${esc(est.estado)}</span>
        <span class="badge ${est.estadoMatricula === 'CONDICIONAL' ? 'aviso' : 'ok'}" title="Estudiante.estadoMatricula">Matrícula ${esc(est.estadoMatricula)}</span>
      </div>
    </div>
    <div class="medidor">
      <div class="etiqueta"><span>Créditos inscritos en ${esc(r.codigoPeriodo)}</span><span><b>${r.creditosInscritos}</b> de ${r.maxCreditos}</span></div>
      <div class="barra-cred ${pct >= 100 ? 'lleno' : ''}" aria-hidden="true"><span style="width:${pct}%"></span></div>
    </div>
    ${r.matriculaAbierta ? '' : `<div class="caja aviso" role="status">${ICO.alerta}<span>La matrícula de este periodo está cerrada. Puedes revisar tu resumen, pero no podrás inscribir.</span></div>`}
    <h4>Inscripciones activas</h4>
    ${activas}`;
}

/* ---------- Paso 2: buscar y elegir asignaturas ---------- */
async function buscarAsignaturas() {
  if (!resumenActual) return;
  const q = $('query').value.trim();
  const cont = $('resultados');
  try {
    const lista = await api('GET', `/asignaturas?query=${encodeURIComponent(q)}`);
    if (!lista.length) {                               // A9
      cont.innerHTML = `<div class="vacio">${ICO.buscar}No encontramos asignaturas para «${esc(q)}». Prueba con otro código o nombre.</div>`;
      return;
    }
    cont.innerHTML = `<div class="tabla-scroll alta"><table>
      <thead><tr><th>Código</th><th>Asignatura</th><th class="num">Créditos</th><th class="num">Cupos</th><th>Prerrequisitos</th><th></th></tr></thead>
      <tbody>` + lista.map(a => {
        const claseCupo = a.cupoDisponible <= 0 ? 'err' : (a.cupoDisponible <= 2 ? 'aviso' : 'ok');
        return `
        <tr class="${a.codigo === seleccion ? 'sel' : ''}">
          <td class="cod">${esc(a.codigo)}</td><td>${esc(a.nombre)}</td><td class="num">${a.creditos}</td>
          <td class="num"><span class="badge ${claseCupo}">${a.cupoDisponible} / ${a.cupoMaximo}</span></td>
          <td>${a.prerrequisitos.length ? esc(a.prerrequisitos.join(', ')) : '—'}</td>
          <td><button type="button" class="chico" data-codigo="${esc(a.codigo)}">Seleccionar</button></td>
        </tr>`;
      }).join('') + '</tbody></table></div>';
  } catch (e) {
    cont.innerHTML = `<div class="caja err">${ICO.alerta}<span>${esc(e.message)}</span></div>`;
  }
}

/* ---------- Paso 3: validar reglas (sin persistir) ---------- */
async function seleccionar(codigo) {
  if (!resumenActual) return;
  seleccion = codigo;
  hayComprobante = false;
  const cont = $('confirmacion');
  try {
    const v = await api('POST', '/inscripciones/validar', cuerpoInscripcion(codigo));
    cont.innerHTML = `
      <div class="resumen-validado">
        <dl class="datos">
          <div><dt>Estudiante</dt><dd>${esc(v.nombreEstudiante)} (${esc(v.documento)})</dd></div>
          <div><dt>Asignatura <span class="attr">Asignatura.codigo</span></dt><dd>${esc(v.codigoAsignatura)} · ${esc(v.nombreAsignatura)}</dd></div>
          <div><dt>Periodo</dt><dd>${esc(v.codigoPeriodo)}</dd></div>
          <div><dt>Créditos <span class="attr">Asignatura.creditos</span></dt><dd>${v.creditosAsignatura}</dd></div>
          <div><dt>Créditos tras inscribir</dt><dd>${v.creditosResultantes} de ${v.maxCreditos}</dd></div>
          <div><dt>Cupos disponibles <span class="attr">Asignatura.cupoDisponible</span></dt><dd>${v.cupoDisponible}</dd></div>
        </dl>
      </div>
      <div class="caja ok" role="status">${ICO.check}<span>Cumples todas las reglas. Revisa el resumen y confirma.</span></div>
      <div class="pie-form">
        <button type="button" class="sec" id="btnCancelar">Cancelar</button>
        <button type="button" class="grande" id="btnConfirmar">Confirmar inscripción</button>
      </div>`;
  } catch (e) {
    pintarErrorRegla(e);
  }
  refrescarPasos();
  irAPaso(3);
}

function pintarErrorRegla(e) {
  const regla = e.datos && e.datos.regla && e.datos.regla !== 'NO_ENCONTRADO' ? e.datos.regla : null;
  $('confirmacion').innerHTML = `
    <div class="caja err" role="alert">${ICO.alerta}<span>${regla ? `<span class="regla">${esc(regla)}</span>` : ''}${esc(e.message)}</span></div>
    <p class="attr" style="margin:10px 0 0">No se creó ninguna inscripción. Elige otra asignatura para continuar.</p>
    <div class="pie-form">
      <button type="button" id="btnCancelar">${ICO.atras} Elegir otra asignatura</button>
    </div>`;
}

function cuerpoInscripcion(codigo) {
  return {
    documento: resumenActual.estudiante.documento,
    codigoAsignatura: codigo,
    codigoPeriodo: $('periodo').value
  };
}

/* ---------- Confirmar (transacción atómica en el servidor) y comprobante ---------- */
async function confirmar() {
  const btn = $('btnConfirmar');
  btn.disabled = true;
  try {
    const c = await api('POST', '/inscripciones', cuerpoInscripcion(seleccion));
    $('confirmacion').innerHTML = `
      <div class="comprobante" role="status">
        <div class="cabeza">${ICO.check}<div><strong>Inscripción registrada</strong><span>Comprobante N.° ${c.idInscripcion}</span></div></div>
        <div class="cuerpo">
          <dl class="datos">
            <div><dt>Estudiante</dt><dd>${esc(c.nombreEstudiante)} (${esc(c.documento)})</dd></div>
            <div><dt>Asignatura</dt><dd>${esc(c.codigoAsignatura)} · ${esc(c.nombreAsignatura)}</dd></div>
            <div><dt>Periodo</dt><dd>${esc(c.codigoPeriodo)}</dd></div>
            <div><dt>Créditos</dt><dd>${c.creditos}</dd></div>
            <div><dt>Estado <span class="attr">Inscripcion.estado</span></dt><dd><span class="badge ok">${esc(c.estado)}</span></dd></div>
            <div><dt>Fecha <span class="attr">Inscripcion.fechaInscripcion</span></dt><dd>${esc(c.fechaInscripcion)}</dd></div>
            <div><dt>Sello de tiempo <span class="attr">Inscripcion.selloTiempo</span></dt><dd>${esc(formatoFechaHora(c.selloTiempo))}</dd></div>
            <div><dt>Cupos restantes</dt><dd>${c.cupoDisponibleRestante}</dd></div>
            <div><dt>Créditos inscritos</dt><dd>${c.creditosInscritos} de ${c.maxCreditos}</dd></div>
          </dl>
        </div>
      </div>
      <div class="pie-form">
        <button type="button" id="btnOtra">Inscribir otra asignatura</button>
      </div>`;
    seleccion = null;
    hayComprobante = true;
    await consultarEstudiante({ conservarSeleccion: true });   // refresca el resumen sin borrar el comprobante
    refrescarPasos();
  } catch (e) {
    pintarErrorRegla(e);                     // A11 u otra regla incumplida entre validar y confirmar
  }
}

function volverAElegir() {                   // A10 (cancelar) y «elegir otra»
  reiniciarSeleccion();
  irAPaso(2);
}

/* ---------- Eventos ---------- */
document.addEventListener('DOMContentLoaded', async () => {
  // Íconos en botones estáticos
  $('btnConsultar').insertAdjacentHTML('afterbegin', ICO.buscar);
  $('btnBuscar').insertAdjacentHTML('afterbegin', ICO.buscar);
  $('btnSiguiente1').insertAdjacentHTML('beforeend', ICO.adelante);
  $('btnSiguiente2').insertAdjacentHTML('beforeend', ICO.adelante);
  $('btnAtras2').insertAdjacentHTML('afterbegin', ICO.atras);

  try { await cargarPeriodos(); } catch (e) { $('errEstudiante').textContent = e.message; }

  $('btnConsultar').addEventListener('click', () => consultarEstudiante());
  $('documento').addEventListener('keydown', e => { if (e.key === 'Enter') consultarEstudiante(); });
  $('periodo').addEventListener('change', () => {
    if (resumenActual) consultarEstudiante();
    else reiniciarSeleccion();
  });
  $('btnBuscar').addEventListener('click', buscarAsignaturas);
  $('query').addEventListener('keydown', e => { if (e.key === 'Enter') buscarAsignaturas(); });

  // Documentos de ejemplo
  document.querySelector('.ejemplos').addEventListener('click', e => {
    const b = e.target.closest('button[data-doc]');
    if (b) { $('documento').value = b.dataset.doc; consultarEstudiante(); }
  });

  // Botones para pasar entre pasos
  $('btnSiguiente1').addEventListener('click', () => irAPaso(2));
  $('btnAtras2').addEventListener('click', () => irAPaso(1));
  $('btnSiguiente2').addEventListener('click', () => irAPaso(3));
  $('pasos').addEventListener('click', e => {
    const li = e.target.closest('li[data-paso]');
    if (li) irAPaso(Number(li.dataset.paso));
  });

  $('resultados').addEventListener('click', e => {
    const b = e.target.closest('button[data-codigo]');
    if (b) seleccionar(b.dataset.codigo);
  });
  $('confirmacion').addEventListener('click', e => {
    const b = e.target.closest('button');
    if (!b) return;
    if (b.id === 'btnConfirmar') confirmar();
    if (b.id === 'btnCancelar') volverAElegir();
    if (b.id === 'btnOtra') volverAElegir();
  });

  // Desde la pantalla de estudiantes: /?documento=1005 precarga y consulta
  const doc = new URLSearchParams(location.search).get('documento');
  if (doc) { $('documento').value = doc; consultarEstudiante(); }

  irAPaso(1);
});
