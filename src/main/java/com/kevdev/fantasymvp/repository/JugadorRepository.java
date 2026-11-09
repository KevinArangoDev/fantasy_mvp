package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findByApiId(Long apiId);
    @Query("SELECT j FROM Jugador j WHERE (:equipo IS NULL OR j.equipo = :equipo) " +
            "AND (:posicion IS NULL OR j.posicion = :posicion) ORDER BY j.nombre")
    List<Jugador> filtrar(@Param("equipo") String equipo, @Param("posicion") String posicion);

    @Query("SELECT DISTINCT j.equipo FROM Jugador j ORDER BY j.equipo")
    List<String> listarEquipos();

    boolean existsByGolesSincronizadosIsNotNull();
}
