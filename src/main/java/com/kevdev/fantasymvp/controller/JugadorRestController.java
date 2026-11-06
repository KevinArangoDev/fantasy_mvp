package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorRestController {

    @Autowired
    private JugadorRepository jugadorRepository;

    @GetMapping
    public List<Jugador> listar() {
        return jugadorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jugador> obtenerPorId(@PathVariable Long id) {
        return jugadorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}