package com.Grupo1.GestionHoteleria_Backend.exception;

public class PagoNotFoundException extends ResourceNotFoundException {

	public PagoNotFoundException(Long id) {
		super("Pago no encontrado con id: " + id);
	}
}
