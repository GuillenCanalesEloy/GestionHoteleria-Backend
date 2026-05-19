package com.Grupo1.GestionHoteleria_Backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;

class UsuarioResponseTest {

	@Test
	void shouldMapUsuarioWithoutPassword() {
		LocalDateTime createdAt = LocalDateTime.of(2026, 5, 19, 10, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 19, 11, 0);
		Usuario usuario = Usuario.builder()
				.id(1L)
				.nombre("Admin Demo")
				.email("admin@correo.com")
				.password("password-encriptado")
				.rol(Rol.ADMIN)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();

		UsuarioResponse response = UsuarioResponse.fromEntity(usuario);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.nombre()).isEqualTo("Admin Demo");
		assertThat(response.email()).isEqualTo("admin@correo.com");
		assertThat(response.rol()).isEqualTo(Rol.ADMIN);
		assertThat(response.createdAt()).isEqualTo(createdAt);
		assertThat(response.updatedAt()).isEqualTo(updatedAt);
		assertThat(UsuarioResponse.class.getRecordComponents())
				.extracting(java.lang.reflect.RecordComponent::getName)
				.doesNotContain("password");
	}
}
