package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmailAndPassword(String email, String password);
}