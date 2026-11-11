package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoUsuarioRepository equipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(Usuario usuario) {
        usuario.setId(null);
        usuario.setRol("USER");
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
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
}
