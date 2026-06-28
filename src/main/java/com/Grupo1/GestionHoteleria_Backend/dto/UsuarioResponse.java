package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;

public record UsuarioResponse(
		Long id,
		String nombre,
		String email,
		Rol rol,
		String telefono,
		String ciudad,
		String notas,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static UsuarioResponse fromEntity(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getEmail(),
				usuario.getRol(),
				usuario.getTelefono(),
				usuario.getCiudad(),
				usuario.getNotas(),
				usuario.getCreatedAt(),
				usuario.getUpdatedAt()
		);
	}
}
