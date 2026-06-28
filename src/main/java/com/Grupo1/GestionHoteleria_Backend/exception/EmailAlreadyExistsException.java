package com.Grupo1.GestionHoteleria_Backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException(String email) {
		super("Ya existe un usuario registrado con el email: " + email);
	}
}
