package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.EquipoApiDto.*;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import com.kevdev.fantasymvp.service.FichajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EquipoRestController {

    private final FichajeService fichajeService;
    private final UsuarioRepository usuarioRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;

    public EquipoRestController(FichajeService fichajeService,
                                UsuarioRepository usuarioRepository,
                                EquipoJugadorRepository equipoJugadorRepository) {
        this.fichajeService = fichajeService;
        this.usuarioRepository = usuarioRepository;
        this.equipoJugadorRepository = equipoJugadorRepository;
    }

    @GetMapping("/me")
    public UsuarioActual me(Principal principal) {
        Usuario u = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        return new UsuarioActual(u.getNombre(), u.getEmail(), u.getRol());
    }

    @GetMapping("/mi-equipo")
    public MiEquipo miEquipo(Principal principal) {
        return construir(fichajeService.equipoDe(principal.getName()));
    }

    @PostMapping("/equipo/fichar")
    public ResponseEntity<MiEquipo> fichar(@RequestBody FicharRequest peticion, Principal principal) {
        fichajeService.fichar(principal.getName(), peticion.jugadorId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(construir(fichajeService.equipoDe(principal.getName())));
    }

    @DeleteMapping("/equipo/jugadores/{jugadorId}")
    public MiEquipo vender(@PathVariable Long jugadorId, Principal principal) {
        fichajeService.vender(principal.getName(), jugadorId);
        return construir(fichajeService.equipoDe(principal.getName()));
    }

    private MiEquipo construir(EquipoUsuario equipo) {
        List<JugadorEnEquipo> jugadores = equipoJugadorRepository
                .findByEquipoUsuarioIdAndActivoTrue(equipo.getId()).stream()
                .map(ej -> new JugadorEnEquipo(
                        ej.getJugador().getId(),
                        ej.getJugador().getNombre(),
                        ej.getJugador().getEquipo(),
                        ej.getJugador().getPosicion(),
                        FichajeService.precioDe(ej.getJugador()),
                        ej.getPuntosObtenidos()))
                .toList();

        Integer total = equipoJugadorRepository.sumarPuntosPorEquipo(equipo.getId());
        return new MiEquipo(equipo.getNombreDelEquipo(),
                total != null ? total : 0,
                fichajeService.presupuestoRestante(equipo),
                jugadores);
    }
}