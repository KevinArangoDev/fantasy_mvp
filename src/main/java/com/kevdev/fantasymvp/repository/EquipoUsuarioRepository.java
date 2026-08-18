package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.EquipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoUsuarioRepository extends JpaRepository<EquipoUsuario, Long> {
    EquipoUsuario findByUsuarioId(Long usuarioId);
}