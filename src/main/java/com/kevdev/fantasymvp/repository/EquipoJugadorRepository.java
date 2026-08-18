package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.EquipoJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoJugadorRepository extends JpaRepository<EquipoJugador, Long> {
    List<EquipoJugador> findByEquipoUsuarioId(Long equipoUsuarioId);
}