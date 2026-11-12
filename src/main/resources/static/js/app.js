const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

let todosLosJugadores = [];

async function pedirJson(url) {
    const resp = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!resp.ok) throw new Error('Error ' + resp.status + ' en ' + url);
    return resp.json();
}

function celda(texto) {
    const td = document.createElement('td');
    td.textContent = texto;
    return td;
}

async function cargarUsuario() {
    const u = await pedirJson('/api/me');
    document.getElementById('usuario').textContent = u.nombre + ' (' + u.rol + ')';
}

async function cargarEquipo() {
    const equipo = await pedirJson('/api/mi-equipo');
    document.getElementById('nombre-equipo').textContent =
        equipo.nombreDelEquipo + ' - ' + equipo.puntosTotales + ' puntos';

    const cuerpo = document.getElementById('cuerpo-equipo');
    cuerpo.replaceChildren();
    for (const j of equipo.jugadores) {
        const fila = document.createElement('tr');
        fila.append(celda(j.nombre), celda(j.equipoReal), celda(j.posicion), celda(j.puntos));
        cuerpo.append(fila);
    }
}

async function cargarRanking() {
    const ranking = await pedirJson('/api/ranking');
    const cuerpo = document.getElementById('cuerpo-ranking');
    cuerpo.replaceChildren();
    for (const f of ranking) {
        const fila = document.createElement('tr');
        fila.append(celda(f.posicion), celda(f.equipo), celda(f.puntos));
        cuerpo.append(fila);
    }
}

function mostrarResultados(texto) {
    const lista = document.getElementById('resultados');
    lista.replaceChildren();
    if (texto.length < 2) return;

    const coincidencias = todosLosJugadores
        .filter(j => j.nombre.toLowerCase().includes(texto.toLowerCase()))
        .slice(0, 15);

    for (const j of coincidencias) {
        const li = document.createElement('li');
        li.textContent = j.nombre + ' - ' + j.equipo + ' (' + j.posicion + ') ';

        const boton = document.createElement('button');
        boton.textContent = 'Fichar';
        boton.addEventListener('click', () => fichar(j.id));

        li.append(boton);
        lista.append(li);
    }
}

async function fichar(jugadorId) {
    const resp = await fetch('/api/equipo/fichar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
        body: JSON.stringify({ jugadorId })
    });

    const mensaje = document.getElementById('mensaje');
    if (resp.status === 201) {
        mensaje.textContent = 'Jugador fichado';
        await cargarEquipo();
        await cargarRanking();
    } else if (resp.status === 409) {
        mensaje.textContent = 'Ya tienes a ese jugador';
    } else if (resp.status === 404) {
        mensaje.textContent = 'El jugador no existe';
    } else {
        mensaje.textContent = 'Error inesperado (' + resp.status + ')';
    }
}

async function iniciar() {
    try {
        todosLosJugadores = await pedirJson('/api/jugadores');
        await Promise.all([cargarUsuario(), cargarEquipo(), cargarRanking()]);
    } catch (e) {
        document.getElementById('mensaje').textContent = e.message;
    }
    document.getElementById('buscador')
        .addEventListener('input', e => mostrarResultados(e.target.value));
}

iniciar();