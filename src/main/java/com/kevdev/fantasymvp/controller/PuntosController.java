package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.repository.*;
import com.kevdev.fantasymvp.service.CalculadoraPuntos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PuntosController {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private EquipoJugadorRepository equipoJugadorRepository;

    @Autowired
    private CalculadoraPuntos calculadoraPuntos;

    @GetMapping("/admin/puntos")
    public String mostrarFormulario(Model model) {
        model.addAttribute("jugadores", jugadorRepository.findAll());
        return "admin-puntos";
    }

    @PostMapping("/admin/puntos")
    public String registrarPuntos(@RequestParam Long jugadorId,
                                  @RequestParam int goles,
                                  @RequestParam int asistencias,
                                  @RequestParam int tarjetasAmarillas,
                                  @RequestParam(defaultValue = "false") boolean porteriaEnCero) {

        Jugador jugador = jugadorRepository.findById(jugadorId).orElseThrow();

        int puntos = calculadoraPuntos.calcularPuntos(goles, asistencias, tarjetasAmarillas, porteriaEnCero, jugador.getPosicion());

        var equiposConEseJugador = equipoJugadorRepository.findByJugadorId(jugadorId);
        for (var equipoJugador : equiposConEseJugador) {
            equipoJugador.setPuntosObtenidos(equipoJugador.getPuntosObtenidos() + puntos);
            equipoJugadorRepository.save(equipoJugador);
        }

        return "redirect:/admin/puntos";
    }
}