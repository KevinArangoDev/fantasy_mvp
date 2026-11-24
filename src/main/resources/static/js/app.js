const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

const CUPOS = { Delantero: 3, Mediocampista: 5, Defensa: 5, Portero: 2 };
const ORDEN_CAMPO = ['Delantero', 'Mediocampista', 'Defensa', 'Portero'];
const CLASE_POS = { Portero: 'por', Defensa: 'def', Mediocampista: 'med', Delantero: 'del' };

let todosLosJugadores = [];

// ---------- utilidades ----------

function el(etiqueta, clase, texto) {
    const nodo = document.createElement(etiqueta);
    if (clase) nodo.className = clase;
    if (texto !== undefined) nodo.textContent = texto;
    return nodo;
}

function celda(contenido, clase) {
    const td = el('td', clase);
    if (contenido instanceof Node) td.append(contenido);
    else td.textContent = contenido;
    return td;
}

function insignia(posicion) {
    return el('span', 'badge badge-' + (CLASE_POS[posicion] || 'del'), posicion);
}

function iniciales(nombre) {
    if (!nombre) return '?';
    const partes = nombre.trim().split(/\s+/);
    const a = partes[0][0];
    const b = partes.length > 1 ? partes[partes.length - 1][0] : (partes[0][1] || '');
    return (a + b).toUpperCase();
}

function nombreCorto(nombre) {
    if (!nombre) return '';
    const partes = nombre.trim().split(/\s+/);
    return partes.length > 1 ? partes[0][0] + '. ' + partes.slice(1).join(' ') : nombre;
}

function tono(texto) {
    let h = 0;
    for (const c of texto) h = (h * 31 + c.charCodeAt(0)) % 360;
    return h;
}

function mostrarMensaje(texto, esError) {
    const m = document.getElementById('mensaje');
    m.textContent = texto;
    m.className = 'msg ' + (esError ? 'msg-error' : 'msg-ok');
    m.hidden = false;
}

async function pedirJson(url) {
    const resp = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!resp.ok) throw new Error('Error ' + resp.status + ' en ' + url);
    return resp.json();
}

// ---------- cancha ----------

function ficha(j) {
    const f = el('div', 'token pos-' + (CLASE_POS[j.posicion] || 'del'));
    f.title = j.nombre + ' · ' + j.equipoReal;

    const avatar = el('div', 'token-avatar', iniciales(j.nombre));
    avatar.style.setProperty('--hue', tono(j.equipoReal || j.nombre));

    f.append(avatar, el('div', 'token-name', nombreCorto(j.nombre)), el('div', 'token-pts', j.puntos + ' pts'));
    return f;
}

function fichaVacia() {
    const f = el('div', 'token token-empty');
    f.append(el('div', 'token-avatar', '+'), el('div', 'token-name', 'libre'));
    return f;
}

function dibujarCampo(jugadores) {
    const campo = document.getElementById('pitch');
    campo.replaceChildren();

    for (const posicion of ORDEN_CAMPO) {
        const fila = el('div', 'pitch-row');
        const deLaPosicion = jugadores.filter(j => j.posicion === posicion);

        for (const j of deLaPosicion) fila.append(ficha(j));
        for (let i = deLaPosicion.length; i < CUPOS[posicion]; i++) fila.append(fichaVacia());

        campo.append(fila);
    }
}

// ---------- carga de datos ----------

async function cargarUsuario() {
    const u = await pedirJson('/api/me');
    document.getElementById('usuario').textContent = u.nombre + ' (' + u.rol + ')';
}

async function cargarEquipo() {
    const equipo = await pedirJson('/api/mi-equipo');

    document.getElementById('nombre-equipo').textContent = equipo.nombreDelEquipo;
    document.getElementById('stat-puntos').textContent = equipo.puntosTotales;
    document.getElementById('stat-presupuesto').textContent = equipo.presupuestoRestante;
    document.getElementById('stat-jugadores').textContent = equipo.jugadores.length + ' / 15';

    dibujarCampo(equipo.jugadores);

    const cuerpo = document.getElementById('cuerpo-equipo');
    cuerpo.replaceChildren();

    if (equipo.jugadores.length === 0) {
        const fila = el('tr');
        const td = el('td', 'empty', 'Todavía no tienes jugadores. Búscalos más abajo.');
        td.colSpan = 6;
        fila.append(td);
        cuerpo.append(fila);
        return;
    }

    for (const j of equipo.jugadores) {
        const boton = el('button', 'btn btn-danger btn-sm', 'Vender');
        boton.addEventListener('click', () => vender(j.jugadorId));

        const fila = el('tr');
        fila.append(
            celda(j.nombre, 'strong'),
            celda(j.equipoReal, 'muted'),
            celda(insignia(j.posicion)),
            celda(j.precio, 'num'),
            celda(j.puntos, 'num strong'),
            celda(boton, 'num')
        );
        cuerpo.append(fila);
    }
}

async function cargarRanking() {
    const ranking = await pedirJson('/api/ranking');
    const cuerpo = document.getElementById('cuerpo-ranking');
    cuerpo.replaceChildren();

    if (ranking.length === 0) {
        const fila = el('tr');
        const td = el('td', 'empty', 'Aún no hay equipos en el ranking.');
        td.colSpan = 3;
        fila.append(td);
        cuerpo.append(fila);
        return;
    }

    for (const f of ranking) {
        const fila = el('tr', f.posicion === 1 ? 'top' : '');
        fila.append(celda(f.posicion, 'num'), celda(f.equipo, 'strong'), celda(f.puntos, 'num strong'));
        cuerpo.append(fila);
    }
}

// ---------- buscador ----------

function mostrarResultados(texto) {
    const lista = document.getElementById('resultados');
    lista.replaceChildren();
    if (texto.trim().length < 2) return;

    const coincidencias = todosLosJugadores
        .filter(j => j.nombre.toLowerCase().includes(texto.trim().toLowerCase()))
        .slice(0, 15);

    if (coincidencias.length === 0) {
        const li = el('li');
        li.append(el('span', 'muted', 'Sin resultados.'));
        lista.append(li);
        return;
    }

    for (const j of coincidencias) {
        const info = el('div', 'info');
        info.append(el('span', 'strong', j.nombre), el('span', 'muted', j.equipo), insignia(j.posicion));

        const boton = el('button', 'btn btn-primary btn-sm', 'Fichar');
        boton.addEventListener('click', () => fichar(j.id));

        const li = el('li');
        li.append(info, boton);
        lista.append(li);
    }
}

// ---------- acciones ----------

async function enviar(url, opciones, textoExito) {
    const resp = await fetch(url, {
        ...opciones,
        headers: { ...(opciones.headers || {}), [csrfHeader]: csrfToken }
    });

    if (resp.ok) {
        mostrarMensaje(textoExito, false);
        await Promise.all([cargarEquipo(), cargarRanking()]);
    } else {
        const cuerpo = await resp.json().catch(() => null);
        mostrarMensaje(cuerpo?.error ?? 'Error inesperado (' + resp.status + ')', true);
    }
}

function fichar(jugadorId) {
    return enviar('/api/equipo/fichar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ jugadorId })
    }, 'Jugador fichado');
}

function vender(jugadorId) {
    return enviar('/api/equipo/jugadores/' + jugadorId, { method: 'DELETE' }, 'Jugador vendido');
}

// ---------- inicio ----------

async function iniciar() {
    try {
        todosLosJugadores = await pedirJson('/api/jugadores');
        await Promise.all([cargarUsuario(), cargarEquipo(), cargarRanking()]);
    } catch (e) {
        mostrarMensaje(e.message, true);
    }
    document.getElementById('buscador')
        .addEventListener('input', e => mostrarResultados(e.target.value));
}

iniciar();