package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.dto.FilaRanking;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final EquipoUsuarioRepository equipoUsuarioRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;

    public RankingService(EquipoUsuarioRepository equipoUsuarioRepository,
                          EquipoJugadorRepository equipoJugadorRepository) {
        this.equipoUsuarioRepository = equipoUsuarioRepository;
        this.equipoJugadorRepository = equipoJugadorRepository;
    }

    public List<FilaRanking> calcular() {
        List<FilaRanking> filas = new ArrayList<>();
        for (EquipoUsuario equipo : equipoUsuarioRepository.findAll()) {
            Integer total = equipoJugadorRepository.sumarPuntosPorEquipo(equipo.getId());
            filas.add(new FilaRanking(0, equipo.getNombreDelEquipo(), total != null ? total : 0));
        }

        filas.sort((a, b) -> Integer.compare(b.puntos(), a.puntos()));

        List<FilaRanking> ranking = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            FilaRanking f = filas.get(i);
            ranking.add(new FilaRanking(i + 1, f.equipo(), f.puntos()));
        }
        return ranking;
    }
}