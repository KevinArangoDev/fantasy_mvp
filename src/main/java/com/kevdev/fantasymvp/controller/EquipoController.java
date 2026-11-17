package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.exception.RecursoNoEncontradoException;
import com.kevdev.fantasymvp.exception.ReglaFichajeException;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.service.FichajeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class EquipoController {

    private final FichajeService fichajeService;
    private final EquipoJugadorRepository equipoJugadorRepository;

    public EquipoController(FichajeService fichajeService,
                            EquipoJugadorRepository equipoJugadorRepository) {
        this.fichajeService = fichajeService;
        this.equipoJugadorRepository = equipoJugadorRepository;
    }

    @PostMapping("/fichar")
    public String ficharJugador(@RequestParam Long jugadorId, Principal principal,
                                RedirectAttributes redirect) {
        try {
            fichajeService.fichar(principal.getName(), jugadorId);
        } catch (ReglaFichajeException | RecursoNoEncontradoException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/jugadores";
        }
        redirect.addFlashAttribute("exito", "Jugador fichado");
        return "redirect:/mi-equipo";
    }

    @PostMapping("/vender")
    public String venderJugador(@RequestParam Long jugadorId, Principal principal,
                                RedirectAttributes redirect) {
        try {
            fichajeService.vender(principal.getName(), jugadorId);
            redirect.addFlashAttribute("exito", "Jugador vendido");
        } catch (RecursoNoEncontradoException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-equipo";
    }

    @GetMapping("/mi-equipo")
    public String verMiEquipo(Principal principal, Model model) {
        var equipo = fichajeService.equipoDe(principal.getName());
        Integer total = equipoJugadorRepository.sumarPuntosPorEquipo(equipo.getId());

        model.addAttribute("equipo", equipo);
        model.addAttribute("jugadoresDelEquipo",
                equipoJugadorRepository.findByEquipoUsuarioIdAndActivoTrue(equipo.getId()));
        model.addAttribute("puntosTotales", total != null ? total : 0);
        model.addAttribute("presupuestoRestante", fichajeService.presupuestoRestante(equipo));
        model.addAttribute("presupuestoTotal", FichajeService.PRESUPUESTO_TOTAL);
        return "mi-equipo";
    }
}