package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpSession;


@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipoUsuarioRepository equipoUsuarioRepository;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(Usuario usuario) {
        usuarioRepository.save(usuario);

        EquipoUsuario equipo = new EquipoUsuario();
        equipo.setUsuario(usuario);
        equipo.setNombreDelEquipo(usuario.getNombre() + " FC");
        equipoUsuarioRepository.save(equipo);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin() {
        return "login";
    }

    /*
    Nos permite saber quien esta logeado una vez ingrese sus datos por medio de una funcion de
    Springweb la cual es HttpSession.
     */
    @PostMapping("/login")
    public String procesarLogin(String email, String password, HttpSession session) {
        Usuario usuario = usuarioRepository.findByEmailAndPassword(email, password);
        if (usuario != null) {
            session.setAttribute("usuarioId", usuario.getId());
            return "redirect:/jugadores";
        } else {
            return "redirect:/login?error";
        }
    }
}
