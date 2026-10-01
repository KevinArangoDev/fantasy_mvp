package com.kevdev.fantasymvp.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraPuntos {

    public int calcularPuntos(int goles, int asistencias, int tarjetasAmarillas, boolean porteriaEnCero, String posicion) {
        int puntos = 0;

        puntos += asistencias * 3;
        puntos += tarjetasAmarillas * -1;

        if (posicion.equals("Defensa") || posicion.equals("Portero")) {
            puntos += goles * 6;
            if (porteriaEnCero) {
                puntos += 4;
            }
        } else {
            puntos += goles * 4;
        }

        return puntos;
    }
}