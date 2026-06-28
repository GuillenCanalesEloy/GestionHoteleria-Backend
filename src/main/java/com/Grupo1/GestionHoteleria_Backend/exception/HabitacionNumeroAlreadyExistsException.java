package com.Grupo1.GestionHoteleria_Backend.exception;

public class HabitacionNumeroAlreadyExistsException extends RuntimeException {

	public HabitacionNumeroAlreadyExistsException(String numero) {
		super("Ya existe una habitacion con el numero: " + numero);
	}
}
