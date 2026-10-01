package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.EquipoJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipoJugadorRepository extends JpaRepository<EquipoJugador, Long> {
    List<EquipoJugador> findByEquipoUsuarioId(Long equipoUsuarioId);
    List<EquipoJugador> findByJugadorId(Long jugadorId);
    @Query("SELECT SUM(ej.puntosObtenidos) FROM EquipoJugador ej WHERE ej.equipoUsuario.id = :equipoId")
    Integer sumarPuntosPorEquipo(Long equipoId);
}