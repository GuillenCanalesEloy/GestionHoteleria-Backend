package com.Grupo1.GestionHoteleria_Backend.exception;

public class ReservaNotFoundException extends ResourceNotFoundException {

	public ReservaNotFoundException(Long id) {
		super("Reserva no encontrada con id: " + id);
	}
}
