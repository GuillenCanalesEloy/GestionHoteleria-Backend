package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UsuarioResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
import com.Grupo1.GestionHoteleria_Backend.exception.UsuarioNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.service.UsuarioService;

class UsuarioControllerTest {

	private UsuarioService usuarioService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		usuarioService = org.mockito.Mockito.mock(UsuarioService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldListUsers() throws Exception {
		when(usuarioService.findAll()).thenReturn(List.of(buildResponse(1L, "Admin", "admin@correo.com", Rol.ADMIN)));

		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].nombre").value("Admin"))
				.andExpect(jsonPath("$[0].email").value("admin@correo.com"))
				.andExpect(jsonPath("$[0].rol").value("ADMIN"))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	void shouldFindUserById() throws Exception {
		when(usuarioService.findById(1L)).thenReturn(buildResponse(1L, "Cliente", "cliente@correo.com", Rol.CLIENTE));

		mockMvc.perform(get("/api/usuarios/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("cliente@correo.com"))
				.andExpect(jsonPath("$.rol").value("CLIENTE"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
		when(usuarioService.findById(99L)).thenThrow(new UsuarioNotFoundException(99L));

		mockMvc.perform(get("/api/usuarios/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Usuario no encontrado con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/usuarios/99"));
	}

	@Test
	void shouldCreateUser() throws Exception {
		when(usuarioService.create(any(CreateUsuarioRequest.class)))
				.thenReturn(buildResponse(10L, "Admin Nuevo", "admin@correo.com", Rol.ADMIN));

		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Admin Nuevo",
								  "email": "admin@correo.com",
								  "password": "12345678",
								  "rol": "ADMIN"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.rol").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldUpdateUserWithPut() throws Exception {
		when(usuarioService.update(eq(1L), any(UpdateUsuarioRequest.class)))
				.thenReturn(buildResponse(1L, "Admin Editado", "admin@correo.com", Rol.ADMIN));

		mockMvc.perform(put("/api/usuarios/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Admin Editado",
								  "email": "admin@correo.com",
								  "rol": "ADMIN"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Admin Editado"))
				.andExpect(jsonPath("$.rol").value("ADMIN"));
	}

	@Test
	void shouldUpdateUserWithPatch() throws Exception {
		when(usuarioService.update(eq(1L), any(UpdateUsuarioRequest.class)))
				.thenReturn(buildResponse(1L, "Cliente Editado", "cliente@correo.com", Rol.CLIENTE));

		mockMvc.perform(patch("/api/usuarios/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Cliente Editado"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Cliente Editado"))
				.andExpect(jsonPath("$.rol").value("CLIENTE"));
	}

	@Test
	void shouldDeleteUser() throws Exception {
		mockMvc.perform(delete("/api/usuarios/1"))
				.andExpect(status().isNoContent());

		verify(usuarioService).delete(1L);
	}

	@Test
	void shouldReturnNotFoundWhenDeletingMissingUser() throws Exception {
		org.mockito.Mockito.doThrow(new UsuarioNotFoundException(99L)).when(usuarioService).delete(99L);

		mockMvc.perform(delete("/api/usuarios/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Usuario no encontrado con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/usuarios/99"));
	}

	private UsuarioResponse buildResponse(Long id, String nombre, String email, Rol rol) {
		return new UsuarioResponse(
				id,
				nombre,
				email,
				rol,
				LocalDateTime.of(2026, 5, 19, 10, 0),
				LocalDateTime.of(2026, 5, 19, 11, 0)
		);
	}
}
