package com.kevdev.fantasymvp.controller;

import com.kevdev.fantasymvp.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/ranking")
    public String verRanking(Model model) {
        model.addAttribute("ranking", rankingService.calcular());
        return "ranking";
    }
}