package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.exception.RecursoNoEncontradoException;
import com.kevdev.fantasymvp.exception.ReglaFichajeException;
import com.kevdev.fantasymvp.model.EquipoJugador;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class FichajeService {

    public static final int PRESUPUESTO_TOTAL = 100;
    public static final int MAX_JUGADORES = 15;
    public static final int MAX_POR_EQUIPO_REAL = 3;
    private static final Map<String, Integer> MAX_POR_POSICION = Map.of(
            "Portero", 2, "Defensa", 5, "Mediocampista", 5, "Delantero", 3);

    private final UsuarioRepository usuarioRepository;
    private final EquipoUsuarioRepository equipoUsuarioRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final JugadorRepository jugadorRepository;

    public FichajeService(UsuarioRepository usuarioRepository,
                          EquipoUsuarioRepository equipoUsuarioRepository,
                          EquipoJugadorRepository equipoJugadorRepository,
                          JugadorRepository jugadorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.equipoUsuarioRepository = equipoUsuarioRepository;
        this.equipoJugadorRepository = equipoJugadorRepository;
        this.jugadorRepository = jugadorRepository;
    }

    public static int precioDe(Jugador jugador) {
        return jugador.getPrecio() != null ? jugador.getPrecio() : 0;
    }

    public EquipoUsuario equipoDe(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        EquipoUsuario equipo = equipoUsuarioRepository.findByUsuarioId(usuario.getId());
        if (equipo == null) {
            throw new RecursoNoEncontradoException("El usuario no tiene equipo");
        }
        return equipo;
    }

    public int presupuestoRestante(EquipoUsuario equipo) {
        int gastado = equipoJugadorRepository.findByEquipoUsuarioIdAndActivoTrue(equipo.getId())
                .stream().mapToInt(ej -> precioDe(ej.getJugador())).sum();
        return PRESUPUESTO_TOTAL - gastado;
    }

    @Transactional
    public void fichar(String email, Long jugadorId) {
        EquipoUsuario equipo = equipoDe(email);
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El jugador no existe"));

        List<EquipoJugador> plantilla =
                equipoJugadorRepository.findByEquipoUsuarioIdAndActivoTrue(equipo.getId());

        if (plantilla.stream().anyMatch(ej -> ej.getJugador().getId().equals(jugador.getId()))) {
            throw new ReglaFichajeException("Ya tienes a ese jugador");
        }

        if (plantilla.size() >= MAX_JUGADORES) {
            throw new ReglaFichajeException("Tu plantilla ya tiene el máximo de " + MAX_JUGADORES + " jugadores");
        }

        Integer tope = MAX_POR_POSICION.get(jugador.getPosicion());
        if (tope == null) {
            throw new ReglaFichajeException("El jugador no tiene una posición válida");
        }
        long enPosicion = plantilla.stream()
                .filter(ej -> jugador.getPosicion().equals(ej.getJugador().getPosicion()))
                .count();
        if (enPosicion >= tope) {
            throw new ReglaFichajeException(
                    "Ya tienes el máximo de " + jugador.getPosicion() + " (" + tope + ")");
        }

        long delMismoEquipo = plantilla.stream()
                .filter(ej -> Objects.equals(jugador.getEquipo(), ej.getJugador().getEquipo()))
                .count();
        if (delMismoEquipo >= MAX_POR_EQUIPO_REAL) {
            throw new ReglaFichajeException(
                    "Máximo " + MAX_POR_EQUIPO_REAL + " jugadores de " + jugador.getEquipo());
        }

        int gastado = plantilla.stream().mapToInt(ej -> precioDe(ej.getJugador())).sum();
        int precio = precioDe(jugador);
        if (gastado + precio > PRESUPUESTO_TOTAL) {
            throw new ReglaFichajeException("Presupuesto insuficiente: cuesta " + precio
                    + " y te quedan " + (PRESUPUESTO_TOTAL - gastado));
        }

        EquipoJugador fila = new EquipoJugador();
        fila.setEquipoUsuario(equipo);
        fila.setJugador(jugador);
        fila.setActivo(true);
        equipoJugadorRepository.save(fila);
    }

    @Transactional
    public void vender(String email, Long jugadorId) {
        EquipoUsuario equipo = equipoDe(email);
        EquipoJugador fila = equipoJugadorRepository
                .findByEquipoUsuarioIdAndJugadorIdAndActivoTrue(equipo.getId(), jugadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ese jugador no está en tu equipo"));
        fila.setActivo(false);
        equipoJugadorRepository.save(fila);
    }
}