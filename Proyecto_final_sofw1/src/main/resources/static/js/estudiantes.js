/* Pantalla 2: CU-02 Gestionar Estudiante (listar, crear, editar, dar de baja). */
const TAMANO = 5;
let pagina = 0;
let totalPaginas = 0;
let consulta = '';
let editando = null;                       // documento en edición, o null si es nuevo
let cacheEstudiantes = [];

const CAMPOS = ['documento', 'nombre', 'programa', 'email', 'estado', 'promedioAcumulado', 'estadoMatricula'];

let temporizadorMensaje = null;
function mensaje(tipo, texto) {
  clearTimeout(temporizadorMensaje);
  $('mensaje').innerHTML = texto
    ? `<div class="caja ${tipo}" role="${tipo === 'err' ? 'alert' : 'status'}">${tipo === 'ok' ? ICO.check : ICO.alerta}<span>${esc(texto)}</span></div>`
    : '';
  if (texto && tipo === 'ok') temporizadorMensaje = setTimeout(() => { $('mensaje').innerHTML = ''; }, 6000);
}

/* ---------- listado (REQ011) ---------- */
async function cargar() {
  try {
    const r = await api('GET', `/estudiantes?query=${encodeURIComponent(consulta)}&page=${pagina}&size=${TAMANO}`);
    cacheEstudiantes = r.contenido;
    totalPaginas = r.totalPaginas;
    if (pagina > 0 && r.contenido.length === 0 && totalPaginas > 0) { pagina = totalPaginas - 1; return cargar(); }
    pintarLista(r);
  } catch (e) {
    $('lista').innerHTML = '';
    mensaje('err', e.message);
  }
}

function pintarLista(r) {
  const cont = $('lista');
  if (r.totalElementos === 0) {
    cont.innerHTML = consulta
      ? `<div class="vacio">${ICO.buscar}No hay resultados para «${esc(consulta)}». Prueba con otro documento, nombre o programa.</div>`          // C-A2
      : `<div class="vacio">${ICO.usuarios}Aún no hay estudiantes registrados. Usa «Nuevo estudiante» para crear el primero.</div>`;                // C-A1
  } else {
    cont.innerHTML = `<div class="tabla-scroll"><table>
      <thead><tr><th>Estudiante</th><th>Documento</th><th>Programa</th><th>Estado</th><th class="num">Promedio</th><th>Matrícula</th><th>Acciones</th></tr></thead>
      <tbody>` + r.contenido.map(e => `
        <tr>
          <td><div class="persona"><span class="avatar" aria-hidden="true">${esc(iniciales(e.nombre))}</span>
            <div>${esc(e.nombre)}<small>${esc(e.email)}</small></div></div></td>
          <td class="cod">${esc(e.documento)}</td>
          <td class="programa">${esc(e.programa)}</td>
          <td><span class="badge ${e.estado === 'ACTIVO' ? 'ok' : 'err'}">${esc(e.estado)}</span></td>
          <td class="num">${Number(e.promedioAcumulado).toFixed(1)}</td>
          <td><span class="badge ${e.estadoMatricula === 'CONDICIONAL' ? 'aviso' : 'ok'}">${esc(e.estadoMatricula)}</span></td>
          <td><div class="acciones">
            ${e.estado === 'ACTIVO'
              ? `<a class="btn chico" style="padding:6px 12px;font-size:.85rem" href="/?documento=${encodeURIComponent(e.documento)}">Inscribir</a>` : ''}
            <button type="button" class="chico sec" data-accion="editar" data-id="${esc(e.documento)}">Editar</button>
            <button type="button" class="chico peligro" data-accion="baja" data-id="${esc(e.documento)}"
              ${e.estado === 'INACTIVO' ? 'disabled' : ''}>Dar de baja</button>
          </div></td>
        </tr>`).join('') + '</tbody></table></div>';
  }
  $('infoPagina').textContent = r.totalElementos === 0 ? '' :
    `Página ${r.pagina + 1} de ${r.totalPaginas} — ${r.totalElementos} estudiantes`;
  $('btnAnterior').disabled = r.pagina <= 0;
  $('btnSiguiente').disabled = r.pagina + 1 >= r.totalPaginas;
}

/* ---------- formulario crear / editar (REQ012, REQ013) ---------- */
function limpiarErrores() {
  CAMPOS.forEach(c => {
    const e = $('e-' + c); if (e) e.textContent = '';
    const f = $('f-' + c); if (f) f.removeAttribute('aria-invalid');
  });
  $('errGeneral').hidden = true;
  $('errGeneral').textContent = '';
}

function errorCampo(campo, texto) {
  const e = $('e-' + campo), f = $('f-' + campo);
  if (e) e.textContent = texto;
  if (f) f.setAttribute('aria-invalid', 'true');
}

function abrirFormulario(estudiante) {
  limpiarErrores();
  editando = estudiante ? estudiante.documento : null;
  $('dlgTitulo').textContent = estudiante ? 'Editar estudiante' : 'Nuevo estudiante';
  $('f-documento').value = estudiante ? estudiante.documento : '';
  $('f-documento').disabled = !!estudiante;                 // el documento no se edita
  $('f-nombre').value = estudiante ? estudiante.nombre : '';
  $('f-programa').value = estudiante ? estudiante.programa : 'Ingeniería de Sistemas y Computación';
  $('f-email').value = estudiante ? estudiante.email : '';
  $('f-estado').value = estudiante ? estudiante.estado : 'ACTIVO';
  $('f-promedioAcumulado').value = estudiante ? estudiante.promedioAcumulado : '';
  $('f-estadoMatricula').value = estudiante ? estudiante.estadoMatricula : 'NORMAL';
  $('dlg').showModal();
  (estudiante ? $('f-nombre') : $('f-documento')).focus();
}

function leerFormulario() {
  const prom = $('f-promedioAcumulado').value.trim();
  return {
    documento: $('f-documento').value.trim(),
    nombre: $('f-nombre').value.trim(),
    programa: $('f-programa').value.trim(),
    email: $('f-email').value.trim(),
    estado: $('f-estado').value,
    promedioAcumulado: prom === '' ? null : Number(prom.replace(',', '.')),
    estadoMatricula: $('f-estadoMatricula').value
  };
}

/* Validación en el cliente (anticipa; el servidor valida de nuevo, RNF-01). */
function validarCliente(d) {
  let ok = true;
  const req = (campo, etiqueta) => {
    if (!d[campo]) { errorCampo(campo, `${etiqueta} es obligatorio`); ok = false; }
  };
  if (!editando) req('documento', 'El documento');
  req('nombre', 'El nombre');
  req('programa', 'El programa');
  req('email', 'El email');
  if (d.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(d.email)) {
    errorCampo('email', 'El email no tiene un formato válido'); ok = false;
  }
  if (d.promedioAcumulado === null) {
    errorCampo('promedioAcumulado', 'El promedio acumulado es obligatorio'); ok = false;
  } else if (isNaN(d.promedioAcumulado) || d.promedioAcumulado < 0 || d.promedioAcumulado > 5) {
    errorCampo('promedioAcumulado', 'El promedio debe estar entre 0,0 y 5,0'); ok = false;
  }
  return ok;
}

async function guardar(ev) {
  ev.preventDefault();
  limpiarErrores();
  const d = leerFormulario();
  if (!validarCliente(d)) return;

  $('btnGuardar').disabled = true;
  try {
    if (editando) {
      const { documento, ...cambios } = d;                  // el documento no viaja en el PATCH
      await api('PATCH', `/estudiantes/${encodeURIComponent(editando)}`, cambios);
      mensaje('ok', `Estudiante ${editando} actualizado.`);
    } else {
      await api('POST', '/estudiantes', d);
      mensaje('ok', `Estudiante ${d.documento} creado.`);
    }
    $('dlg').close();
    await cargar();
  } catch (e) {
    if (e.datos && e.datos.errores) {
      Object.entries(e.datos.errores).forEach(([campo, texto]) => {
        if ($('e-' + campo)) errorCampo(campo, texto);
        else { $('errGeneral').hidden = false; $('errGeneral').textContent = texto; }
      });
    } else if (e.datos && e.datos.regla === 'DOCUMENTO_DUPLICADO') {
      errorCampo('documento', e.message);
    } else {
      $('errGeneral').hidden = false;
      $('errGeneral').textContent = e.message;
    }
  } finally {
    $('btnGuardar').disabled = false;
  }
}

/* ---------- baja lógica (REQ014, RC-01) ---------- */
function confirmarBaja(texto) {
  return new Promise(resolve => {
    const dlg = $('dlgBaja');
    $('dlgBajaTexto').textContent = texto;
    const cerrar = valor => { dlg.close(); $('btnBajaConfirmar').onclick = null; $('btnBajaCancelar').onclick = null; resolve(valor); };
    $('btnBajaConfirmar').onclick = () => cerrar(true);
    $('btnBajaCancelar').onclick = () => cerrar(false);
    dlg.addEventListener('cancel', () => resolve(false), { once: true });   // tecla Esc
    dlg.showModal();
    $('btnBajaCancelar').focus();
  });
}

async function darDeBaja(id) {
  const e = cacheEstudiantes.find(x => x.documento === id);
  const nombre = e ? e.nombre : id;
  const ok = await confirmarBaja(`¿Dar de baja a ${nombre} (${id})? Su estado pasará a INACTIVO.`);   // S3-A2
  if (!ok) return;
  try {
    await api('DELETE', `/estudiantes/${encodeURIComponent(id)}`);
    mensaje('ok', `${nombre} fue dado de baja (INACTIVO).`);
    await cargar();
  } catch (err) {
    mensaje('err', err.message);                                                                // RC-01
  }
}

/* ---------- eventos ---------- */
document.addEventListener('DOMContentLoaded', () => {
  $('btnBuscar').insertAdjacentHTML('afterbegin', ICO.buscar);
  $('btnNuevo').insertAdjacentHTML('afterbegin', ICO.mas);
  $('btnAnterior').insertAdjacentHTML('afterbegin', ICO.atras);
  $('btnSiguiente').insertAdjacentHTML('beforeend', ICO.adelante);
  $('irInscribir').insertAdjacentHTML('beforeend', ICO.adelante);
  cargar();

  const buscar = () => { consulta = $('busqueda').value.trim(); pagina = 0; mensaje('', ''); cargar(); };
  $('btnBuscar').addEventListener('click', buscar);
  $('busqueda').addEventListener('keydown', e => { if (e.key === 'Enter') buscar(); });
  $('btnNuevo').addEventListener('click', () => abrirFormulario(null));
  $('btnAnterior').addEventListener('click', () => { if (pagina > 0) { pagina--; cargar(); } });
  $('btnSiguiente').addEventListener('click', () => { if (pagina + 1 < totalPaginas) { pagina++; cargar(); } });

  $('lista').addEventListener('click', ev => {
    const b = ev.target.closest('button[data-accion]');
    if (!b) return;
    mensaje('', '');
    if (b.dataset.accion === 'editar') abrirFormulario(cacheEstudiantes.find(x => x.documento === b.dataset.id));
    if (b.dataset.accion === 'baja') darDeBaja(b.dataset.id);
  });

  $('form').addEventListener('submit', guardar);
  $('btnCancelarForm').addEventListener('click', () => $('dlg').close());
});
