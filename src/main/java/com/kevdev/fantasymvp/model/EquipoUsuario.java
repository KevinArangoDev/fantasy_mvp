package com.kevdev.fantasymvp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos_usuario")
public class EquipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreDelEquipo;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public EquipoUsuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreDelEquipo() { return nombreDelEquipo; }
    public void setNombreDelEquipo(String nombreDelEquipo) { this.nombreDelEquipo = nombreDelEquipo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}