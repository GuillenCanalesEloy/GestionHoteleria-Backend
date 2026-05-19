package com.Grupo1.GestionHoteleria_Backend.dto;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUsuarioRequest(
		@Size(max = 120, message = "El nombre no debe superar 120 caracteres")
		String nombre,

		@Email(message = "El email no tiene un formato valido")
		@Size(max = 160, message = "El email no debe superar 160 caracteres")
		String email,

		@Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
		String password,

		Rol rol
) {
}
