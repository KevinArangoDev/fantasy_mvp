package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.dto.RegistroForm;
import com.kevdev.fantasymvp.exception.EmailYaExistenteException;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final EquipoUsuarioRepository equipoUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
                           EquipoUsuarioRepository equipoUsuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.equipoUsuarioRepository = equipoUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registrar(RegistroForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaExistenteException();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setRol("USER");
        usuarioRepository.save(usuario);

        EquipoUsuario equipo = new EquipoUsuario();
        equipo.setUsuario(usuario);
        equipo.setNombreDelEquipo(usuario.getNombre() + " FC");
        equipoUsuarioRepository.save(equipo);
    }
}