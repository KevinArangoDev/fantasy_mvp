package com.kevdev.fantasymvp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo_jugador")
public class EquipoJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_usuario_id")
    private EquipoUsuario equipoUsuario;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    private Integer puntosObtenidos = 0;

    public EquipoJugador() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EquipoUsuario getEquipoUsuario() { return equipoUsuario; }
    public void setEquipoUsuario(EquipoUsuario equipoUsuario) { this.equipoUsuario = equipoUsuario; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Integer getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(Integer puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }
}
