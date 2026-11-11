package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.EquipoJugador;
import com.kevdev.fantasymvp.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class EquipoController {

    @Autowired
    private EquipoUsuarioRepository equipoUsuarioRepository;

    @Autowired
    private EquipoJugadorRepository equipoJugadorRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/fichar")
    public String ficharJugador(@RequestParam Long jugadorId, Principal principal) {
        Long usuarioId = idDeUsuarioActual(principal);

        var equipo = equipoUsuarioRepository.findByUsuarioId(usuarioId);
        var jugador = jugadorRepository.findById(jugadorId).orElseThrow();

        EquipoJugador equipoJugador = new EquipoJugador();
        equipoJugador.setEquipoUsuario(equipo);
        equipoJugador.setJugador(jugador);
        equipoJugadorRepository.save(equipoJugador);

        return "redirect:/mi-equipo";
    }

    @GetMapping("/mi-equipo")
    public String verMiEquipo(Principal principal, Model model) {
        Long usuarioId = idDeUsuarioActual(principal);

        var equipo = equipoUsuarioRepository.findByUsuarioId(usuarioId);
        var jugadoresDelEquipo = equipoJugadorRepository.findByEquipoUsuarioId(equipo.getId());

        model.addAttribute("equipo", equipo);
        model.addAttribute("jugadoresDelEquipo", jugadoresDelEquipo);
        return "mi-equipo";
    }

    private Long idDeUsuarioActual(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName()).orElseThrow().getId();
    }
}