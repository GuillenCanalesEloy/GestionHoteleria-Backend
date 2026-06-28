package com.Grupo1.GestionHoteleria_Backend.entity;

public enum EstadoAreaComun {
	DISPONIBLE("Disponible"),
	OCUPADA("Ocupada"),
	MANTENIMIENTO("Mantenimiento");

	private final String nombre;

	EstadoAreaComun(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
}
