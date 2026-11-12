package com.kevdev.fantasymvp.dto;

import java.util.List;

public class EquipoApiDto {

    public record UsuarioActual(String nombre, String email, String rol) {}

    public record JugadorEnEquipo(Long jugadorId, String nombre, String equipoReal,
                                  String posicion, int puntos) {}

    public record MiEquipo(String nombreDelEquipo, int puntosTotales,
                           List<JugadorEnEquipo> jugadores) {}

    public record FicharRequest(Long jugadorId) {}
}