package com.Grupo1.GestionHoteleria_Backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

	private static final String SECRET = "clave_super_segura_para_pruebas_jwt_123456789";
	private static final long EXPIRATION_MS = 60000;

	private final JwtService jwtService = new JwtService(SECRET, EXPIRATION_MS);

	@Test
	void shouldGenerateTokenAndExtractUsername() {
		UserDetails userDetails = buildUser("demo@correo.com");

		String token = jwtService.generateToken(userDetails);

		assertThat(token).isNotBlank();
		assertThat(jwtService.extractUsername(token)).isEqualTo("demo@correo.com");
	}

	@Test
	void shouldValidateTokenForMatchingUser() {
		UserDetails userDetails = buildUser("demo@correo.com");
		String token = jwtService.generateToken(userDetails);

		assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
	}

	@Test
	void shouldRejectTokenForDifferentUser() {
		String token = jwtService.generateToken(buildUser("demo@correo.com"));

		assertThat(jwtService.isTokenValid(token, buildUser("otro@correo.com"))).isFalse();
	}

	@Test
	void shouldRejectMalformedToken() {
		UserDetails userDetails = buildUser("demo@correo.com");

		assertThat(jwtService.isTokenValid("token-invalido", userDetails)).isFalse();
	}

	private UserDetails buildUser(String email) {
		return new User(
				email,
				"password",
				List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
		);
	}
}
