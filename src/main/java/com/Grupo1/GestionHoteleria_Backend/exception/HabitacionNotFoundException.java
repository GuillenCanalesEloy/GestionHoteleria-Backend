package com.Grupo1.GestionHoteleria_Backend.exception;

public class HabitacionNotFoundException extends ResourceNotFoundException {

	public HabitacionNotFoundException(Long id) {
		super("Habitacion no encontrada con id: " + id);
	}
}
