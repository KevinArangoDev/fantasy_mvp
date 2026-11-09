package com.kevdev.fantasymvp.repository;

import com.kevdev.fantasymvp.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findByApiId(Long apiId);
}
