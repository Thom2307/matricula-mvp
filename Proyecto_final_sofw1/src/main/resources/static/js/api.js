/* Utilidades compartidas por las dos pantallas. */
async function api(metodo, url, cuerpo) {
  const opciones = { method: metodo, headers: {} };
  if (cuerpo !== undefined) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(cuerpo);
  }
  let res;
  try {
    res = await fetch(url, opciones);
  } catch (e) {
    const err = new Error('No se pudo conectar con el servidor');
    err.status = 0;
    throw err;
  }
  let datos = null;
  if (res.status !== 204) {
    try { datos = await res.json(); } catch (e) { datos = null; }
  }
  if (!res.ok) {
    const err = new Error((datos && datos.mensaje) || 'Ocurrió un error inesperado');
    err.status = res.status;
    err.datos = datos;           // {regla, mensaje} o {errores: {campo: mensaje}}
    throw err;
  }
  return datos;
}

function esc(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

const $ = id => document.getElementById(id);

function formatoFechaHora(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return isNaN(d) ? iso : d.toLocaleString('es-CO');
}

/* ---------- Íconos y ayudas visuales compartidas ---------- */
const ico = trazo => `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true">${trazo}</svg>`;
const ICO = {
  atras:    ico('<path d="M15 18l-6-6 6-6"/>'),
  adelante: ico('<path d="M9 6l6 6-6 6"/>'),
  buscar:   ico('<circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/>'),
  check:    ico('<path d="M20 6L9 17l-5-5"/>'),
  alerta:   ico('<circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16.5v.5"/>'),
  libro:    ico('<path d="M4 5a2 2 0 012-2h13v16H6a2 2 0 00-2 2z"/><path d="M4 19V5"/>'),
  usuarios: ico('<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0113 0"/><path d="M16 4.5a3.5 3.5 0 010 7M18 14a6.5 6.5 0 013.5 6"/>'),
  mas:      ico('<path d="M12 5v14M5 12h14"/>'),
  editar:   ico('<path d="M4 20h4L19 9l-4-4L4 16z"/>'),
  inscribir: ico('<path d="M12 5v14M5 12h14"/>')
};

function iniciales(nombre) {
  const p = String(nombre || '?').trim().split(/\s+/);
  return ((p[0] || '?')[0] + (p.length > 1 ? p[p.length - 1][0] : '')).toUpperCase();
}
