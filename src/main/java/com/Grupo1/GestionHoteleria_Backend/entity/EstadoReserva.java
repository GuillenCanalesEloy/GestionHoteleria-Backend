package com.Grupo1.GestionHoteleria_Backend.entity;

public enum EstadoReserva {
	PENDIENTE("Pendiente"),
	CONFIRMADA("Confirmada"),
	CANCELADA("Cancelada"),
	FINALIZADA("Finalizada");

	private final String nombre;

	EstadoReserva(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
}
