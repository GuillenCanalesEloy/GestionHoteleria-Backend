package com.Grupo1.GestionHoteleria_Backend.dto;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUsuarioRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 120, message = "El nombre no debe superar 120 caracteres")
		String nombre,

		@NotBlank(message = "El email es obligatorio")
		@Email(message = "El email no tiene un formato valido")
		@Size(max = 160, message = "El email no debe superar 160 caracteres")
		String email,

		@NotBlank(message = "La contrasena es obligatoria")
		@Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
		String password,

		@NotNull(message = "El rol es obligatorio")
		Rol rol
) {
}
