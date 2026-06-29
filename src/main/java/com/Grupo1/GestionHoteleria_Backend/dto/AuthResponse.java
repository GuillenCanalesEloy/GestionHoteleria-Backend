package com.Grupo1.GestionHoteleria_Backend.dto;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;

public record AuthResponse(
		Long id,
		String token,
		String type,
		String email,
		String nombre,
		Rol rol
) {
}
