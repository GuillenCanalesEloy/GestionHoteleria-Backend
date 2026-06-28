package com.Grupo1.GestionHoteleria_Backend.entity;

public enum TipoHabitacion {
	SIMPLE("Simple"),
	DOBLE("Doble"),
	MATRIMONIAL("Matrimonial"),
	FAMILIAR("Familiar"),
	SUITE("Suite");

	private final String nombre;

	TipoHabitacion(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
}
