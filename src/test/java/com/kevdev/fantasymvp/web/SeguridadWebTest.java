package com.kevdev.fantasymvp.web;

import com.kevdev.fantasymvp.config.SecurityConfig;
import com.kevdev.fantasymvp.controller.EquipoRestController;
import com.kevdev.fantasymvp.controller.ImportacionRestController;
import com.kevdev.fantasymvp.controller.JugadorRestController;
import com.kevdev.fantasymvp.controller.PuntosController;
import com.kevdev.fantasymvp.controller.RankingRestController;
import com.kevdev.fantasymvp.dto.FootballApiDto.ResumenSincronizacion;
import com.kevdev.fantasymvp.exception.ReglaFichajeException;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import com.kevdev.fantasymvp.service.CalculadoraPuntos;
import com.kevdev.fantasymvp.service.FichajeService;
import com.kevdev.fantasymvp.service.ImportadorJugadoresService;
import com.kevdev.fantasymvp.service.RankingService;
import com.kevdev.fantasymvp.service.SincronizadorPuntosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest({
        PuntosController.class,
        ImportacionRestController.class,
        EquipoRestController.class,
        RankingRestController.class,
        JugadorRestController.class
})
@Import(SecurityConfig.class)
class SeguridadWebTest {

    @Autowired WebApplicationContext contexto;

    MockMvc mockMvc;

    @BeforeEach
    void prepararMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .build();
    };

    @MockitoBean JugadorRepository jugadorRepository;
    @MockitoBean EquipoJugadorRepository equipoJugadorRepository;
    @MockitoBean UsuarioRepository usuarioRepository;
    @MockitoBean CalculadoraPuntos calculadoraPuntos;
    @MockitoBean ImportadorJugadoresService importadorJugadoresService;
    @MockitoBean SincronizadorPuntosService sincronizadorPuntosService;
    @MockitoBean FichajeService fichajeService;
    @MockitoBean RankingService rankingService;

    // ---------- rutas públicas ----------

    @Test
    void apiJugadores_esPublicaParaGET() throws Exception {
        mockMvc.perform(get("/api/jugadores"))
                .andExpect(status().isOk());
    }

    @Test
    void apiRanking_esPublica() throws Exception {
        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk());
    }

    // ---------- sin sesión ----------

    @Test
    void apiMe_sinSesion_responde401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiMiEquipo_sinSesion_responde401() throws Exception {
        mockMvc.perform(get("/api/mi-equipo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paginaMiEquipo_sinSesion_redirigeAlLogin() throws Exception {
        mockMvc.perform(get("/mi-equipo").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminPuntos_sinSesion_redirigeAlLogin() throws Exception {
        mockMvc.perform(get("/admin/puntos").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ---------- control de roles ----------

    @Test
    @WithMockUser(roles = "USER")
    void adminPuntos_conRolUser_responde403() throws Exception {
        mockMvc.perform(get("/admin/puntos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void crearJugadorPorApi_conRolUser_responde403() throws Exception {
        mockMvc.perform(post("/api/jugadores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Intruso\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void sincronizarPuntos_conRolUser_responde403() throws Exception {
        mockMvc.perform(post("/api/admin/sincronizar-puntos").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sincronizarPuntos_conRolAdmin_funciona() throws Exception {
        when(sincronizadorPuntosService.sincronizar(false))
                .thenReturn(new ResumenSincronizacion("puntos", 145, 2, 0));

        mockMvc.perform(post("/api/admin/sincronizar-puntos").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modo").value("puntos"));
    }

    // ---------- CSRF ----------

    @Test
    @WithMockUser(roles = "USER")
    void ficharSinTokenCsrf_responde403() throws Exception {
        mockMvc.perform(post("/api/equipo/fichar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jugadorId\":1}"))
                .andExpect(status().isForbidden());
    }

    // ---------- comportamiento de la API de equipo ----------

    @Test
    @WithMockUser(roles = "USER")
    void ficharConTokenCsrf_responde201() throws Exception {
        when(fichajeService.equipoDe("user")).thenReturn(new EquipoUsuario());

        mockMvc.perform(post("/api/equipo/fichar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jugadorId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void ficharViolandoUnaRegla_responde409ConElMensaje() throws Exception {
        doThrow(new ReglaFichajeException("Ya tienes a ese jugador"))
                .when(fichajeService).fichar("user", 1L);

        mockMvc.perform(post("/api/equipo/fichar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jugadorId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya tienes a ese jugador"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void venderJugador_conSesionYToken_funciona() throws Exception {
        when(fichajeService.equipoDe("user")).thenReturn(new EquipoUsuario());

        mockMvc.perform(delete("/api/equipo/jugadores/1").with(csrf()))
                .andExpect(status().isOk());
    }

    // ---------- la API no filtr datos sensibles ----------

    @Test
    @WithMockUser(username = "kevin@test.com", roles = "USER")
    void apiMe_noExponeLaContrasena() throws Exception {
        Usuario u = new Usuario();
        u.setNombre("Kevin");
        u.setEmail("kevin@test.com");
        u.setPassword("$2a$10$hashquenodebeverse");
        u.setRol("USER");
        when(usuarioRepository.findByEmail("kevin@test.com")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kevin@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}