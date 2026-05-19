package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UsuarioTest {

	@Test
	void shouldReturnAdminAuthority() {
		Usuario usuario = Usuario.builder()
				.email("admin@correo.com")
				.password("password")
				.rol(Rol.ADMIN)
				.build();

		assertThat(usuario.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
	}

	@Test
	void shouldReturnClienteAuthority() {
		Usuario usuario = Usuario.builder()
				.email("cliente@correo.com")
				.password("password")
				.rol(Rol.CLIENTE)
				.build();

		assertThat(usuario.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_CLIENTE");
	}

	@Test
	void shouldUseClienteAsDefaultRoleBeforePersisting() {
		Usuario usuario = Usuario.builder()
				.email("nuevo@correo.com")
				.password("password")
				.rol(null)
				.build();

		usuario.prePersist();

		assertThat(usuario.getRol()).isEqualTo(Rol.CLIENTE);
		assertThat(usuario.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_CLIENTE");
	}
}
