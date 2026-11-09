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
}