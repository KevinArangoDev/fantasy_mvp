package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.EquipoJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EquipoJugadorRepository extends JpaRepository<EquipoJugador, Long> {

    List<EquipoJugador> findByEquipoUsuarioIdAndActivoTrue(Long equipoUsuarioId);

    List<EquipoJugador> findByJugadorIdAndActivoTrue(Long jugadorId);

    Optional<EquipoJugador> findByEquipoUsuarioIdAndJugadorIdAndActivoTrue(Long equipoUsuarioId, Long jugadorId);

    @Query("SELECT SUM(ej.puntosObtenidos) FROM EquipoJugador ej WHERE ej.equipoUsuario.id = :equipoId")
    Integer sumarPuntosPorEquipo(Long equipoId);
}