package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(Usuario usuario) {
        usuarioRepository.save(usuario);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmailAndPassword(email, password);
        if (usuario != null) {
            return "redirect:/jugadores";
        } else {
            return "redirect:/login?error";
        }
    }
}
