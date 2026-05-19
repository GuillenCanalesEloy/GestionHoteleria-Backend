package com.Grupo1.GestionHoteleria_Backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Import(SecurityAuthorizationTest.TestEndpoints.class)
class SecurityAuthorizationTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	void shouldAllowPublicHabitacionesCatalog() throws Exception {
		mockMvc.perform(get("/api/habitaciones"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenCreatingHabitaciones() throws Exception {
		mockMvc.perform(post("/api/habitaciones"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenCreatingHabitaciones() throws Exception {
		mockMvc.perform(post("/api/habitaciones"))
				.andExpect(status().isOk());
	}

	@Test
	void shouldRejectAnonymousWhenAccessingUsuariosManagement() throws Exception {
		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenAccessingUsuariosManagement() throws Exception {
		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenCreatingUsuario() throws Exception {
		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Admin Seguridad",
								  "email": "admin.seguridad@correo.com",
								  "password": "12345678",
								  "rol": "ADMIN"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenUpdatingUsuario() throws Exception {
		mockMvc.perform(put("/api/usuarios/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario Editado"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenPatchingUsuario() throws Exception {
		mockMvc.perform(patch("/api/usuarios/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario Editado"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldRejectClienteWhenDeletingUsuario() throws Exception {
		mockMvc.perform(delete("/api/usuarios/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenAccessingUsuariosManagement() throws Exception {
		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenCreatingUsuario() throws Exception {
		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Admin Seguridad",
								  "email": "admin.seguridad@correo.com",
								  "password": "12345678",
								  "rol": "ADMIN"
								}
								"""))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenUpdatingUsuario() throws Exception {
		mockMvc.perform(put("/api/usuarios/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario Editado"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenPatchingUsuario() throws Exception {
		mockMvc.perform(patch("/api/usuarios/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario Editado"
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldAllowAdminWhenDeletingUsuario() throws Exception {
		mockMvc.perform(delete("/api/usuarios/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "CLIENTE")
	void shouldAllowClienteWhenAccessingReservas() throws Exception {
		mockMvc.perform(get("/api/reservas"))
				.andExpect(status().isOk());
	}

	@RestController
	static class TestEndpoints {

		@GetMapping("/api/habitaciones")
		ResponseEntity<Void> getHabitaciones() {
			return ResponseEntity.ok().build();
		}

		@PostMapping("/api/habitaciones")
		ResponseEntity<Void> createHabitacion() {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/api/reservas")
		ResponseEntity<Void> getReservas() {
			return ResponseEntity.ok().build();
		}
	}
}
