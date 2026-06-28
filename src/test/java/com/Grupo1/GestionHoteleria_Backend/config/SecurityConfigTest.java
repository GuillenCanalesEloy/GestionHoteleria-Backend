package com.Grupo1.GestionHoteleria_Backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.Grupo1.GestionHoteleria_Backend.security.JwtAuthenticationFilter;

class SecurityConfigTest {

	@Test
	void shouldConfigureCorsForFrontendAndAuthorizationHeaders() {
		SecurityConfig securityConfig = new SecurityConfig(nullJwtAuthenticationFilter(), username -> null);
		setFrontendUrl(securityConfig, "http://localhost:5173");

		CorsConfigurationSource source = securityConfig.corsConfigurationSource();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
		CorsConfiguration configuration = source.getCorsConfiguration(request);

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins()).containsExactly("http://localhost:5173");
		assertThat(configuration.getAllowedMethods()).containsAll(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
		assertThat(configuration.getExposedHeaders()).contains("Authorization");
		assertThat(configuration.getAllowCredentials()).isTrue();
	}

	private JwtAuthenticationFilter nullJwtAuthenticationFilter() {
		return null;
	}

	private void setFrontendUrl(SecurityConfig securityConfig, String frontendUrl) {
		try {
			java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField("frontendUrl");
			field.setAccessible(true);
			field.set(securityConfig, frontendUrl);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("No se pudo configurar frontendUrl para la prueba", exception);
		}
	}
}
