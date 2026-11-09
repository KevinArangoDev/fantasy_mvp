package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.FootballApiDto.ResumenImportacion;
import com.kevdev.fantasymvp.dto.FootballApiDto.ResumenSincronizacion;
import com.kevdev.fantasymvp.service.ImportadorJugadoresService;
import com.kevdev.fantasymvp.service.SincronizadorPuntosService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class ImportacionRestController {

    private final ImportadorJugadoresService importador;
    private final SincronizadorPuntosService sincronizador;

    public ImportacionRestController(ImportadorJugadoresService importador,
                                     SincronizadorPuntosService sincronizador) {
        this.importador = importador;
        this.sincronizador = sincronizador;
    }

    @PostMapping("/importar-jugadores")
    public ResumenImportacion importar() {
        return importador.importar();
    }

    @PostMapping("/sincronizar-puntos")
    public ResumenSincronizacion sincronizar(@RequestParam(defaultValue = "false") boolean lineaBase) {
        return sincronizador.sincronizar(lineaBase);
    }
}