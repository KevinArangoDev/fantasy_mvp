package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.dto.FootballApiDto.*;
import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class ImportadorJugadoresService {

    private final RestClient restClient;
    private final JugadorRepository jugadorRepository;

    public ImportadorJugadoresService(JugadorRepository jugadorRepository,
                                      @Value("${football.api.token}") String token) {
        this.jugadorRepository = jugadorRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.football-data.org/v4")
                .defaultHeader("X-Auth-Token", token)
                .build();
    }

    @Transactional
    public ResumenImportacion importar() {
        RespuestaEquipos respuesta = restClient.get()
                .uri("/competitions/PL/teams")
                .retrieve()
                .body(RespuestaEquipos.class);

        int nuevos = 0, actualizados = 0, omitidos = 0;

        for (EquipoApi equipo : respuesta.teams()) {
            if (equipo.squad() == null) continue;

            for (JugadorApi j : equipo.squad()) {
                String posicion = traducirPosicion(j.position());
                if (posicion == null) {
                    omitidos++;
                    continue;
                }

                Jugador jugador = jugadorRepository.findByApiId(j.id()).orElse(null);
                if (jugador == null) {
                    jugador = new Jugador();
                    jugador.setApiId(j.id());
                    jugador.setPrecio(precioBase(posicion));
                    nuevos++;
                } else {
                    actualizados++;
                }

                jugador.setNombre(j.name());
                jugador.setEquipo(equipo.name());
                jugador.setPosicion(posicion);
                jugadorRepository.save(jugador);
            }
        }
        return new ResumenImportacion(nuevos, actualizados, omitidos);
    }

    private String traducirPosicion(String posicionApi) {
        if (posicionApi == null) return null;
        return switch (posicionApi) {
            case "Goalkeeper" -> "Portero";
            case "Defence" -> "Defensa";
            case "Midfield" -> "Mediocampista";
            case "Offence" -> "Delantero";
            default -> null;
        };
    }

    private int precioBase(String posicion) {
        return switch (posicion) {
            case "Portero", "Defensa" -> 5;
            case "Mediocampista" -> 7;
            default -> 8;
        };
    }
}
