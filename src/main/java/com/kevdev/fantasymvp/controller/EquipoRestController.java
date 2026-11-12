package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.EquipoApiDto.*;
import com.kevdev.fantasymvp.model.EquipoJugador;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EquipoRestController {

    private final UsuarioRepository usuarioRepository;
    private final EquipoUsuarioRepository equipoUsuarioRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final JugadorRepository jugadorRepository;

    public EquipoRestController(UsuarioRepository usuarioRepository,
                                EquipoUsuarioRepository equipoUsuarioRepository,
                                EquipoJugadorRepository equipoJugadorRepository,
                                JugadorRepository jugadorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.equipoUsuarioRepository = equipoUsuarioRepository;
        this.equipoJugadorRepository = equipoJugadorRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @GetMapping("/me")
    public UsuarioActual me(Principal principal) {
        Usuario u = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        return new UsuarioActual(u.getNombre(), u.getEmail(), u.getRol());
    }

    @GetMapping("/mi-equipo")
    public MiEquipo miEquipo(Principal principal) {
        return construir(equipoDe(principal));
    }

    @PostMapping("/equipo/fichar")
    public ResponseEntity<MiEquipo> fichar(@RequestBody FicharRequest peticion, Principal principal) {
        EquipoUsuario equipo = equipoDe(principal);

        Jugador jugador = jugadorRepository.findById(peticion.jugadorId()).orElse(null);
        if (jugador == null) {
            return ResponseEntity.notFound().build();
        }
        if (equipoJugadorRepository.existsByEquipoUsuarioIdAndJugadorId(equipo.getId(), jugador.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        EquipoJugador fila = new EquipoJugador();
        fila.setEquipoUsuario(equipo);
        fila.setJugador(jugador);
        equipoJugadorRepository.save(fila);

        return ResponseEntity.status(HttpStatus.CREATED).body(construir(equipo));
    }

    private EquipoUsuario equipoDe(Principal principal) {
        Usuario u = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        return equipoUsuarioRepository.findByUsuarioId(u.getId());
    }

    private MiEquipo construir(EquipoUsuario equipo) {
        List<JugadorEnEquipo> jugadores = equipoJugadorRepository
                .findByEquipoUsuarioId(equipo.getId()).stream()
                .map(ej -> new JugadorEnEquipo(
                        ej.getJugador().getId(),
                        ej.getJugador().getNombre(),
                        ej.getJugador().getEquipo(),
                        ej.getJugador().getPosicion(),
                        ej.getPuntosObtenidos()))
                .toList();

        int total = jugadores.stream().mapToInt(JugadorEnEquipo::puntos).sum();
        return new MiEquipo(equipo.getNombreDelEquipo(), total, jugadores);
    }
}