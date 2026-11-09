package com.kevdev.fantasymvp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class FootballApiDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespuestaEquipos(List<EquipoApi> teams) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EquipoApi(String name, List<JugadorApi> squad) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JugadorApi(Long id, String name, String position) {}

    public record ResumenImportacion(int nuevos, int actualizados, int omitidos) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespuestaGoleadores(List<GoleadorApi> scorers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoleadorApi(JugadorApi player, Integer goals, Integer assists) {}

    public record ResumenSincronizacion(String modo, int jugadoresActualizados,
                                        int goleadoresSinCoincidencia, int puntosRepartidos) {}
}