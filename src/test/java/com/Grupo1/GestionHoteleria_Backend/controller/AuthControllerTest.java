package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.AuthResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.service.AuthService;

class AuthControllerTest {

	private AuthService authService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		authService = org.mockito.Mockito.mock(AuthService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
	}

	@Test
	void shouldLoginAndReturnAuthResponse() throws Exception {
		when(authService.login(any())).thenReturn(new AuthResponse(
				"jwt-generado",
				"Bearer",
				"demo@correo.com",
				"Usuario Demo",
				Rol.CLIENTE
		));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "demo@correo.com",
								  "password": "12345678"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwt-generado"))
				.andExpect(jsonPath("$.type").value("Bearer"))
				.andExpect(jsonPath("$.email").value("demo@correo.com"))
				.andExpect(jsonPath("$.nombre").value("Usuario Demo"))
				.andExpect(jsonPath("$.rol").value("CLIENTE"));
	}

	@Test
	void shouldRegisterAndReturnCreatedAuthResponse() throws Exception {
		when(authService.register(any())).thenReturn(new AuthResponse(
				"jwt-nuevo",
				"Bearer",
				"nuevo@correo.com",
				"Nuevo Usuario",
				Rol.CLIENTE
		));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Nuevo Usuario",
								  "email": "nuevo@correo.com",
								  "password": "12345678"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").value("jwt-nuevo"))
				.andExpect(jsonPath("$.type").value("Bearer"))
				.andExpect(jsonPath("$.email").value("nuevo@correo.com"))
				.andExpect(jsonPath("$.nombre").value("Nuevo Usuario"))
				.andExpect(jsonPath("$.rol").value("CLIENTE"));
	}
}
