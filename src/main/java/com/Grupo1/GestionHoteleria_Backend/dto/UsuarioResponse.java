package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;

public record UsuarioResponse(
		Long id,
		String nombre,
		String email,
		Rol rol,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static UsuarioResponse fromEntity(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getEmail(),
				usuario.getRol(),
				usuario.getCreatedAt(),
				usuario.getUpdatedAt()
		);
	}
}
