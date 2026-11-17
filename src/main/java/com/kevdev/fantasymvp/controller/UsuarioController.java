package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.RegistroForm;
import com.kevdev.fantasymvp.exception.EmailYaExistenteException;
import com.kevdev.fantasymvp.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

    private final RegistroService registroService;

    public UsuarioController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute("registroForm") RegistroForm form,
                                   BindingResult resultado) {
        if (resultado.hasErrors()) {
            return "registro";
        }
        try {
            registroService.registrar(form);
        } catch (EmailYaExistenteException | DataIntegrityViolationException e) {
            resultado.rejectValue("email", "email.existente", "Ese email ya está registrado");
            return "registro";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login";
    }
}
