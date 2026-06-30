package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDate;

public class ReservaRequest {

    private Long clienteId;
    private Long usuarioId;
    private Long habitacionId;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer cantidadHuespedes;

    private String nombreHuesped;
    private String emailHuesped;
    private String telefonoHuesped;
    private String solicitudesEspeciales;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(Long habitacionId) {
        this.habitacionId = habitacionId;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Integer getCantidadHuespedes() {
        return cantidadHuespedes;
    }

    public void setCantidadHuespedes(Integer cantidadHuespedes) {
        this.cantidadHuespedes = cantidadHuespedes;
    }

    public String getNombreHuesped() {
        return nombreHuesped;
    }

    public void setNombreHuesped(String nombreHuesped) {
        this.nombreHuesped = nombreHuesped;
    }

    public String getEmailHuesped() {
        return emailHuesped;
    }

    public void setEmailHuesped(String emailHuesped) {
        this.emailHuesped = emailHuesped;
    }

    public String getTelefonoHuesped() {
        return telefonoHuesped;
    }

    public void setTelefonoHuesped(String telefonoHuesped) {
        this.telefonoHuesped = telefonoHuesped;
    }

    public String getSolicitudesEspeciales() {
        return solicitudesEspeciales;
    }

    public void setSolicitudesEspeciales(String solicitudesEspeciales) {
        this.solicitudesEspeciales = solicitudesEspeciales;
    }
}