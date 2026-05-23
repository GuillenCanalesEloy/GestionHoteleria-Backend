package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNoDisponibleException;
import com.Grupo1.GestionHoteleria_Backend.exception.ReservaNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

class ReservaControllerTest {

	private ReservaService reservaService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		reservaService = org.mockito.Mockito.mock(ReservaService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new ReservaController(reservaService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldRejectRequestWithoutUsuarioPrincipal() throws Exception {
		mockMvc.perform(get("/api/reservas"))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldListReservasAsAdmin() throws Exception {
		when(reservaService.findAll(null, null, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(1L, 10L, EstadoReserva.PENDIENTE)), 0, 10, 1));

		mockMvc.perform(get("/api/reservas").principal(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].usuarioId").value(10))
				.andExpect(jsonPath("$.content[0].habitacionId").value(20))
				.andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void shouldListReservasAsClienteForcingOwnUsuarioId() throws Exception {
		when(reservaService.findAll(10L, null, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(1L, 10L, EstadoReserva.PENDIENTE)), 0, 10, 1));

		mockMvc.perform(get("/api/reservas")
						.param("usuarioId", "99")
						.principal(clienteAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].usuarioId").value(10));
	}

	@Test
	void shouldListReservasWithFiltersPaginationAndSortingAsAdmin() throws Exception {
		LocalDate desde = LocalDate.of(2026, 8, 1);
		LocalDate hasta = LocalDate.of(2026, 8, 31);
		when(reservaService.findAll(10L, 20L, EstadoReserva.CONFIRMADA, desde, hasta, 1, 5, "fechaEntrada", "DESC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(2L, 10L, EstadoReserva.CONFIRMADA)), 1, 5, 8));

		mockMvc.perform(get("/api/reservas")
						.param("usuarioId", "10")
						.param("habitacionId", "20")
						.param("estado", "CONFIRMADA")
						.param("fechaEntradaDesde", "2026-08-01")
						.param("fechaEntradaHasta", "2026-08-31")
						.param("page", "1")
						.param("size", "5")
						.param("sortBy", "fechaEntrada")
						.param("direction", "DESC")
						.principal(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(2))
				.andExpect(jsonPath("$.content[0].estado").value("CONFIRMADA"))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(5))
				.andExpect(jsonPath("$.totalElements").value(8));
	}

	@Test
	void shouldRejectInvalidFilterEnum() throws Exception {
		mockMvc.perform(get("/api/reservas").param("estado", "APROBADA").principal(adminAuth()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Parametros de entrada invalidos"))
				.andExpect(jsonPath("$.validations.estado").value("Valor invalido para el parametro estado"));
	}

	@Test
	void shouldRejectInvalidPaginationParameters() throws Exception {
		when(reservaService.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(-1), eq(10), eq("id"), eq("ASC")))
				.thenThrow(new IllegalArgumentException("El numero de pagina no puede ser negativo"));

		mockMvc.perform(get("/api/reservas").param("page", "-1").principal(adminAuth()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El numero de pagina no puede ser negativo"))
				.andExpect(jsonPath("$.path").value("/api/reservas"));
	}

	@Test
	void shouldFindReservaById() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 10L, EstadoReserva.PENDIENTE));

		mockMvc.perform(get("/api/reservas/1").principal(clienteAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.usuarioEmail").value("cliente@correo.com"))
				.andExpect(jsonPath("$.habitacionNumero").value("101"))
				.andExpect(jsonPath("$.precioTotal").value(240.00));
	}

	@Test
	void shouldRejectClienteWhenFindingAnotherUserReserva() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 99L, EstadoReserva.PENDIENTE));

		mockMvc.perform(get("/api/reservas/1").principal(clienteAuth()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No tienes permisos para acceder a esta reserva"));
	}

	@Test
	void shouldReturnNotFoundWhenReservaDoesNotExist() throws Exception {
		when(reservaService.findById(99L)).thenThrow(new ReservaNotFoundException(99L));

		mockMvc.perform(get("/api/reservas/99").principal(adminAuth()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Reserva no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/reservas/99"));
	}

	@Test
	void shouldCreateReserva() throws Exception {
		when(reservaService.create(any(CreateReservaRequest.class)))
				.thenReturn(buildResponse(10L, 10L, EstadoReserva.PENDIENTE));

		mockMvc.perform(post("/api/reservas")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": 10,
								  "habitacionId": 20,
								  "fechaEntrada": "2026-08-10",
								  "fechaSalida": "2026-08-12",
								  "cantidadHuespedes": 2
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/reservas/10"))
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.estado").value("PENDIENTE"));
	}

	@Test
	void shouldRejectClienteWhenCreatingReservaForAnotherUser() throws Exception {
		mockMvc.perform(post("/api/reservas")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": 99,
								  "habitacionId": 20,
								  "fechaEntrada": "2026-08-10",
								  "fechaSalida": "2026-08-12",
								  "cantidadHuespedes": 2
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No tienes permisos para crear reservas de otro usuario"));
	}

	@Test
	void shouldReturnConflictWhenHabitacionIsNotAvailable() throws Exception {
		when(reservaService.create(any(CreateReservaRequest.class)))
				.thenThrow(new HabitacionNoDisponibleException(
						20L,
						LocalDate.of(2026, 8, 10),
						LocalDate.of(2026, 8, 12)
				));

		mockMvc.perform(post("/api/reservas")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": 10,
								  "habitacionId": 20,
								  "fechaEntrada": "2026-08-10",
								  "fechaSalida": "2026-08-12",
								  "cantidadHuespedes": 2
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("Conflict"))
				.andExpect(jsonPath("$.message").value("La habitacion 20 no esta disponible entre 2026-08-10 y 2026-08-12"))
				.andExpect(jsonPath("$.path").value("/api/reservas"));
	}

	@Test
	void shouldRejectCreateReservaWithInvalidFields() throws Exception {
		mockMvc.perform(post("/api/reservas")
						.principal(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": null,
								  "habitacionId": null,
								  "fechaEntrada": null,
								  "fechaSalida": null,
								  "cantidadHuespedes": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
				.andExpect(jsonPath("$.validations.usuarioId").value("El usuario es obligatorio"))
				.andExpect(jsonPath("$.validations.habitacionId").value("La habitacion es obligatoria"))
				.andExpect(jsonPath("$.validations.fechaEntrada").value("La fecha de entrada es obligatoria"))
				.andExpect(jsonPath("$.validations.fechaSalida").value("La fecha de salida es obligatoria"))
				.andExpect(jsonPath("$.validations.cantidadHuespedes").value("La cantidad de huespedes debe ser mayor a cero"));
	}

	@Test
	void shouldUpdateReservaEstadoAsAdmin() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 10L, EstadoReserva.PENDIENTE));
		when(reservaService.update(eq(1L), any(UpdateReservaRequest.class)))
				.thenReturn(buildResponse(1L, 10L, EstadoReserva.CONFIRMADA));

		mockMvc.perform(patch("/api/reservas/1/estado")
						.principal(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CONFIRMADA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CONFIRMADA"));
	}

	@Test
	void shouldAllowClienteToCancelOwnReserva() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 10L, EstadoReserva.PENDIENTE));
		when(reservaService.update(eq(1L), any(UpdateReservaRequest.class)))
				.thenReturn(buildResponse(1L, 10L, EstadoReserva.CANCELADA));

		mockMvc.perform(patch("/api/reservas/1/estado")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CANCELADA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CANCELADA"));
	}

	@Test
	void shouldRejectClienteWhenConfirmingReserva() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 10L, EstadoReserva.PENDIENTE));

		mockMvc.perform(patch("/api/reservas/1/estado")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CONFIRMADA"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Solo puedes cancelar tus propias reservas"));
	}

	@Test
	void shouldRejectUpdateReservaEstadoWithInvalidBody() throws Exception {
		mockMvc.perform(patch("/api/reservas/1/estado")
						.principal(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": null
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
				.andExpect(jsonPath("$.validations.estado").value("El estado de la reserva es obligatorio"));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingMissingReservaEstado() throws Exception {
		when(reservaService.update(eq(99L), any(UpdateReservaRequest.class)))
				.thenThrow(new ReservaNotFoundException(99L));
		when(reservaService.findById(99L)).thenThrow(new ReservaNotFoundException(99L));

		mockMvc.perform(patch("/api/reservas/99/estado")
						.principal(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CANCELADA"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reserva no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/reservas/99/estado"));
	}

	@Test
	void shouldDeleteReserva() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, 10L, EstadoReserva.PENDIENTE));

		mockMvc.perform(delete("/api/reservas/1").principal(clienteAuth()))
				.andExpect(status().isNoContent());

		verify(reservaService).delete(1L);
	}

	@Test
	void shouldReturnNotFoundWhenDeletingMissingReserva() throws Exception {
		org.mockito.Mockito.doThrow(new ReservaNotFoundException(99L)).when(reservaService).delete(99L);
		when(reservaService.findById(99L)).thenThrow(new ReservaNotFoundException(99L));

		mockMvc.perform(delete("/api/reservas/99").principal(adminAuth()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reserva no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/reservas/99"));
	}

	private ReservaResponse buildResponse(Long id, Long usuarioId, EstadoReserva estado) {
		return new ReservaResponse(
				id,
				usuarioId,
				"Cliente Uno",
				"cliente@correo.com",
				20L,
				"101",
				TipoHabitacion.DOBLE,
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				2,
				new BigDecimal("240.00"),
				estado,
				LocalDateTime.of(2026, 5, 22, 10, 0),
				LocalDateTime.of(2026, 5, 22, 11, 0)
		);
	}

	private Authentication adminAuth() {
		return new UsernamePasswordAuthenticationToken(buildUsuario(1L, Rol.ADMIN), null, buildUsuario(1L, Rol.ADMIN).getAuthorities());
	}

	private Authentication clienteAuth() {
		return new UsernamePasswordAuthenticationToken(buildUsuario(10L, Rol.CLIENTE), null, buildUsuario(10L, Rol.CLIENTE).getAuthorities());
	}

	private Usuario buildUsuario(Long id, Rol rol) {
		return Usuario.builder()
				.id(id)
				.nombre(rol.name())
				.email(rol.name().toLowerCase() + "@correo.com")
				.password("password")
				.rol(rol)
				.build();
	}

	private PageResponse<ReservaResponse> buildPageResponse(
			List<ReservaResponse> content,
			int page,
			int size,
			long totalElements
	) {
		int totalPages = (int) Math.ceil((double) totalElements / size);
		return new PageResponse<>(
				content,
				page,
				size,
				totalElements,
				totalPages,
				page == 0,
				page + 1 >= totalPages
		);
	}
}
