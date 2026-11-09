package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepository;

    @GetMapping("/jugadores")
    public String listarJugadores(@RequestParam(required = false) String equipo,
                                  @RequestParam(required = false) String posicion,
                                  Model model) {
        String eq = (equipo == null || equipo.isBlank()) ? null : equipo;
        String pos = (posicion == null || posicion.isBlank()) ? null : posicion;

        model.addAttribute("jugadores", jugadorRepository.filtrar(eq, pos));
        model.addAttribute("equipos", jugadorRepository.listarEquipos());
        model.addAttribute("equipoSeleccionado", eq);
        model.addAttribute("posicionSeleccionada", pos);
        return "jugadores";
    }
}