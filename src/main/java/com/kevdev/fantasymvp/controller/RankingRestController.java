package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.dto.FilaRanking;
import com.kevdev.fantasymvp.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingRestController {

    private final RankingService rankingService;

    public RankingRestController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<FilaRanking> ranking() {
        return rankingService.calcular();
    }
}