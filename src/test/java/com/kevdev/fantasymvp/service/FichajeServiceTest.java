package com.kevdev.fantasymvp.service;

import com.kevdev.fantasymvp.exception.RecursoNoEncontradoException;
import com.kevdev.fantasymvp.exception.ReglaFichajeException;
import com.kevdev.fantasymvp.model.EquipoJugador;
import com.kevdev.fantasymvp.model.EquipoUsuario;
import com.kevdev.fantasymvp.model.Jugador;
import com.kevdev.fantasymvp.model.Usuario;
import com.kevdev.fantasymvp.repository.EquipoJugadorRepository;
import com.kevdev.fantasymvp.repository.EquipoUsuarioRepository;
import com.kevdev.fantasymvp.repository.JugadorRepository;
import com.kevdev.fantasymvp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FichajeServiceTest {

    private static final String EMAIL = "kevin@test.com";

    @Mock UsuarioRepository usuarioRepository;
    @Mock EquipoUsuarioRepository equipoUsuarioRepository;
    @Mock EquipoJugadorRepository equipoJugadorRepository;
    @Mock JugadorRepository jugadorRepository;

    @InjectMocks FichajeService fichajeService;

    private EquipoUsuario equipo;
    private List<EquipoJugador> plantilla;

    @BeforeEach
    void preparar() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        equipo = new EquipoUsuario();
        equipo.setId(10L);

        plantilla = new ArrayList<>();

        lenient().when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        lenient().when(equipoUsuarioRepository.findByUsuarioId(1L)).thenReturn(equipo);
        lenient().when(equipoJugadorRepository.findByEquipoUsuarioIdAndActivoTrue(10L)).thenReturn(plantilla);
    }

    // ---------- helpers ----------

    private Jugador jugador(long id, String equipoReal, String posicion, Integer precio) {
        Jugador j = new Jugador();
        j.setId(id);
        j.setNombre("Jugador " + id);
        j.setEquipo(equipoReal);
        j.setPosicion(posicion);
        j.setPrecio(precio);
        return j;
    }

    private EquipoJugador enPlantilla(Jugador j) {
        EquipoJugador fila = new EquipoJugador();
        fila.setEquipoUsuario(equipo);
        fila.setJugador(j);
        fila.setActivo(true);
        plantilla.add(fila);
        return fila;
    }

    private void quiereFichar(Jugador j) {
        when(jugadorRepository.findById(j.getId())).thenReturn(Optional.of(j));
    }

    // ---------- fichar ----------

    @Test
    void ficha_cuandoCumpleTodasLasReglas() {
        Jugador nuevo = jugador(1, "Arsenal FC", "Delantero", 8);
        quiereFichar(nuevo);

        fichajeService.fichar(EMAIL, 1L);

        ArgumentCaptor<EquipoJugador> captor = ArgumentCaptor.forClass(EquipoJugador.class);
        verify(equipoJugadorRepository).save(captor.capture());
        assertSame(nuevo, captor.getValue().getJugador());
        assertTrue(captor.getValue().isActivo());
    }

    @Test
    void rechazaJugadorYaFichado() {
        Jugador j = jugador(1, "Arsenal FC", "Delantero", 8);
        enPlantilla(j);
        quiereFichar(j);

        ReglaFichajeException e = assertThrows(ReglaFichajeException.class,
                () -> fichajeService.fichar(EMAIL, 1L));

        assertTrue(e.getMessage().contains("Ya tienes a ese jugador"));
        verify(equipoJugadorRepository, never()).save(any());
    }

    @Test
    void rechazaCuandoLaPlantillaEstaLlena() {
        for (long i = 100; i < 115; i++) {
            enPlantilla(jugador(i, "Equipo " + i, "Defensa", 1));
        }
        Jugador nuevo = jugador(1, "Arsenal FC", "Delantero", 8);
        quiereFichar(nuevo);

        ReglaFichajeException e = assertThrows(ReglaFichajeException.class,
                () -> fichajeService.fichar(EMAIL, 1L));

        assertTrue(e.getMessage().contains("15"));
        verify(equipoJugadorRepository, never()).save(any());
    }

    @Test
    void rechazaCuandoSeExcedeElTopeDeLaPosicion() {
        enPlantilla(jugador(100, "Equipo A", "Portero", 5));
        enPlantilla(jugador(101, "Equipo B", "Portero", 5));
        Jugador tercero = jugador(1, "Equipo C", "Portero", 5);
        quiereFichar(tercero);

        ReglaFichajeException e = assertThrows(ReglaFichajeException.class,
                () -> fichajeService.fichar(EMAIL, 1L));

        assertTrue(e.getMessage().contains("Portero"));
        verify(equipoJugadorRepository, never()).save(any());
    }

    @Test
    void rechazaElCuartoJugadorDelMismoEquipoReal() {
        enPlantilla(jugador(100, "Arsenal FC", "Defensa", 5));
        enPlantilla(jugador(101, "Arsenal FC", "Mediocampista", 7));
        enPlantilla(jugador(102, "Arsenal FC", "Delantero", 8));
        Jugador cuarto = jugador(1, "Arsenal FC", "Portero", 5);
        quiereFichar(cuarto);

        ReglaFichajeException e = assertThrows(ReglaFichajeException.class,
                () -> fichajeService.fichar(EMAIL, 1L));

        assertTrue(e.getMessage().contains("Arsenal FC"));
        verify(equipoJugadorRepository, never()).save(any());
    }

    @Test
    void rechazaCuandoSuperaElPresupuesto() {
        enPlantilla(jugador(100, "Equipo A", "Delantero", 98));
        Jugador nuevo = jugador(1, "Equipo B", "Defensa", 5);
        quiereFichar(nuevo);

        ReglaFichajeException e = assertThrows(ReglaFichajeException.class,
                () -> fichajeService.fichar(EMAIL, 1L));

        assertTrue(e.getMessage().contains("Presupuesto insuficiente"));
        verify(equipoJugadorRepository, never()).save(any());
    }

    @Test
    void aceptaCuandoElGastoQuedaExactamenteEnElPresupuesto() {
        enPlantilla(jugador(100, "Equipo A", "Delantero", 95));
        Jugador nuevo = jugador(1, "Equipo B", "Defensa", 5);
        quiereFichar(nuevo);

        assertDoesNotThrow(() -> fichajeService.fichar(EMAIL, 1L));
        verify(equipoJugadorRepository).save(any());
    }

    @Test
    void fallaSiElJugadorNoExiste() {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> fichajeService.fichar(EMAIL, 99L));
        verify(equipoJugadorRepository, never()).save(any());
    }

    // ---------- vender ----------

    @Test
    void vender_marcaInactivaLaFilaSinBorrarlaYConservaLosPuntos() {
        Jugador j = jugador(1, "Arsenal FC", "Delantero", 8);
        EquipoJugador fila = enPlantilla(j);
        fila.setPuntosObtenidos(12);
        when(equipoJugadorRepository.findByEquipoUsuarioIdAndJugadorIdAndActivoTrue(10L, 1L))
                .thenReturn(Optional.of(fila));

        fichajeService.vender(EMAIL, 1L);

        assertFalse(fila.isActivo());
        assertEquals(12, fila.getPuntosObtenidos());
        verify(equipoJugadorRepository).save(fila);
        verify(equipoJugadorRepository, never()).delete(any());
    }

    @Test
    void vender_fallaSiElJugadorNoEstaEnElEquipo() {
        when(equipoJugadorRepository.findByEquipoUsuarioIdAndJugadorIdAndActivoTrue(10L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> fichajeService.vender(EMAIL, 5L));
    }

    // ---------- presupuesto ----------

    @Test
    void presupuestoRestante_descuentaElPrecioDeLosJugadoresActivos() {
        enPlantilla(jugador(100, "Equipo A", "Defensa", 5));
        enPlantilla(jugador(101, "Equipo B", "Delantero", 8));

        assertEquals(87, fichajeService.presupuestoRestante(equipo));
    }
}