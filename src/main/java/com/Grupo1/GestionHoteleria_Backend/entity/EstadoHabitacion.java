package com.Grupo1.GestionHoteleria_Backend.entity;

public enum EstadoHabitacion {
	DISPONIBLE("Disponible"),
	OCUPADA("Ocupada"),
	MANTENIMIENTO("Mantenimiento");

	private final String nombre;

	EstadoHabitacion(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
}
