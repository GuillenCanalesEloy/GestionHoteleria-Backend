package com.Grupo1.GestionHoteleria_Backend.exception;

public class UsuarioNotFoundException extends RuntimeException {

	public UsuarioNotFoundException(Long id) {
		super("Usuario no encontrado con id: " + id);
	}
}
