package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

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

    @PostMapping
    public ResponseEntity<Jugador> crear(@RequestBody Jugador jugador) {
        jugador.setId(null);
        Jugador guardado = jugadorRepository.save(jugador);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jugador> actualizar(@PathVariable Long id, @RequestBody Jugador datos) {
        return jugadorRepository.findById(id)
                .map(existente -> {
                    existente.setNombre(datos.getNombre());
                    existente.setEquipo(datos.getEquipo());
                    existente.setPosicion(datos.getPosicion());
                    existente.setPrecio(datos.getPrecio());
                    return ResponseEntity.ok(jugadorRepository.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!jugadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            jugadorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}