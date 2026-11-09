package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.FootballApiDto.ResumenImportacion;
import com.kevdev.fantasymvp.service.ImportadorJugadoresService;
import org.springframework.web.bind.annotation.*;

//Es el endpoint que disparara la importacion
@RestController
@RequestMapping("/api/admin")
public class ImportacionRestController {

    private final ImportadorJugadoresService importador;

    public ImportacionRestController(ImportadorJugadoresService importador) {
        this.importador = importador;
    }

    @PostMapping("/importar-jugadores")
    public ResumenImportacion importar() {
        return importador.importar();
    }
}