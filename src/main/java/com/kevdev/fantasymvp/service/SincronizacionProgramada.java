package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.dto.FootballApiDto.ResumenSincronizacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
Sincronizacion programada la implemente para que cadas cierto tiempo llame sola al servicio
sincronizacionPuntosService y le pide que sincronice sin tener que esta ejecutando comandos
la logica general esta en sincronizacionPuntosService ya que en sincronizacion Programada
solo sera como una especie de alarma
 */


@Component
@ConditionalOnProperty(name = "fantasy.sync.habilitada", havingValue = "true")
public class SincronizacionProgramada {

    private static final Logger log = LoggerFactory.getLogger(SincronizacionProgramada.class);

    private final SincronizadorPuntosService sincronizador;

    public SincronizacionProgramada(SincronizadorPuntosService sincronizador) {
        this.sincronizador = sincronizador;
    }

    @Scheduled(fixedDelayString = "${fantasy.sync.intervalo-ms:3600000}",
            initialDelayString = "${fantasy.sync.intervalo-ms:3600000}")
    public void ejecutar() {
        try {
            ResumenSincronizacion r = sincronizador.sincronizar(false);
            log.info("Sincronizacion programada: {} jugadores, {} puntos repartidos",
                    r.jugadoresActualizados(), r.puntosRepartidos());
        } catch (Exception e) {
            log.warn("Sincronizacion programada omitida: {}", e.getMessage());
        }
    }
}