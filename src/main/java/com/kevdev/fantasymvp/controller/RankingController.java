package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class RankingController {

    @Autowired
    private EquipoUsuarioRepository equipoUsuarioRepository;

    @Autowired
    private EquipoJugadorRepository equipoJugadorRepository;

    @GetMapping("/ranking")
    public String verRanking(Model model) {
        List<EquipoUsuario> equipos = equipoUsuarioRepository.findAll();

        Map<EquipoUsuario, Integer> puntajes = new HashMap<>();
        for (EquipoUsuario equipo : equipos) {
            Integer total = equipoJugadorRepository.sumarPuntosPorEquipo(equipo.getId());
            puntajes.put(equipo, total != null ? total : 0);
        }

        List<Map.Entry<EquipoUsuario, Integer>> ranking = new ArrayList<>(puntajes.entrySet());
        ranking.sort((a, b) -> b.getValue() - a.getValue());

        model.addAttribute("ranking", ranking);
        return "ranking";
    }
}