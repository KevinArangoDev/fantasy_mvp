package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.dto.FootballApiDto.*;
import com.kevdev.fantasymvp.model.EquipoJugador;
import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SincronizadorPuntosService {

    private final RestClient restClient;
    private final JugadorRepository jugadorRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final CalculadoraPuntos calculadoraPuntos;

    public SincronizadorPuntosService(JugadorRepository jugadorRepository,
                                      EquipoJugadorRepository equipoJugadorRepository,
                                      CalculadoraPuntos calculadoraPuntos,
                                      @Value("${football.api.token}") String token) {
        this.jugadorRepository = jugadorRepository;
        this.equipoJugadorRepository = equipoJugadorRepository;
        this.calculadoraPuntos = calculadoraPuntos;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.football-data.org/v4")
                .defaultHeader("X-Auth-Token", token)
                .build();
    }

    @Transactional
    public ResumenSincronizacion sincronizar(boolean soloLineaBase) {
        if (!soloLineaBase && !jugadorRepository.existsByGolesSincronizadosIsNotNull()) {
            throw new IllegalStateException(
                    "No hay línea base. Ejecuta primero la sincronización con lineaBase=true.");
        }
        RespuestaGoleadores respuesta = restClient.get()
                .uri("/competitions/PL/scorers?limit=500")
                .retrieve()
                .body(RespuestaGoleadores.class);

        Map<Long, GoleadorApi> porApiId = new HashMap<>();
        for (GoleadorApi g : respuesta.scorers()) {
            porApiId.put(g.player().id(), g);
        }

        return soloLineaBase ? establecerLineaBase(porApiId) : repartirPuntos(porApiId);
    }

    private ResumenSincronizacion establecerLineaBase(Map<Long, GoleadorApi> porApiId) {
        int actualizados = 0, coincidencias = 0;

        for (Jugador jugador : jugadorRepository.findAll()) {
            GoleadorApi g = porApiId.get(jugador.getApiId());
            if (g != null) coincidencias++;

            jugador.setGolesSincronizados(g != null && g.goals() != null ? g.goals() : 0);
            jugador.setAsistenciasSincronizadas(g != null && g.assists() != null ? g.assists() : 0);
            jugadorRepository.save(jugador);
            actualizados++;
        }
        return new ResumenSincronizacion("linea-base", actualizados, porApiId.size() - coincidencias, 0);
    }

    private ResumenSincronizacion repartirPuntos(Map<Long, GoleadorApi> porApiId) {
        int actualizados = 0, sinCoincidencia = 0, repartidos = 0;

        for (GoleadorApi g : porApiId.values()) {
            Jugador jugador = jugadorRepository.findByApiId(g.player().id()).orElse(null);
            if (jugador == null) {
                sinCoincidencia++;
                continue;
            }

            int golesApi = g.goals() != null ? g.goals() : 0;
            int asistApi = g.assists() != null ? g.assists() : 0;
            int golesGuardados = jugador.getGolesSincronizados() != null ? jugador.getGolesSincronizados() : 0;
            int asistGuardadas = jugador.getAsistenciasSincronizadas() != null ? jugador.getAsistenciasSincronizadas() : 0;

            int nuevosGoles = Math.max(0, golesApi - golesGuardados);
            int nuevasAsist = Math.max(0, asistApi - asistGuardadas);

            if (nuevosGoles > 0 || nuevasAsist > 0) {
                int puntos = calculadoraPuntos.calcularPuntos(nuevosGoles, nuevasAsist, 0, false, jugador.getPosicion());
                for (EquipoJugador ej : equipoJugadorRepository.findByJugadorId(jugador.getId())) {
                    ej.setPuntosObtenidos(ej.getPuntosObtenidos() + puntos);
                    equipoJugadorRepository.save(ej);
                    repartidos += puntos;
                }
            }

            jugador.setGolesSincronizados(golesApi);
            jugador.setAsistenciasSincronizadas(asistApi);
            jugadorRepository.save(jugador);
            actualizados++;
        }
        return new ResumenSincronizacion("puntos", actualizados, sinCoincidencia, repartidos);
    }
}