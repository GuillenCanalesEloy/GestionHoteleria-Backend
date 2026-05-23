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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
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
	void shouldListReservas() throws Exception {
		when(reservaService.findAll(null, null, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(1L, EstadoReserva.PENDIENTE)), 0, 10, 1));

		mockMvc.perform(get("/api/reservas"))
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
	void shouldListReservasWithFiltersPaginationAndSorting() throws Exception {
		LocalDate desde = LocalDate.of(2026, 8, 1);
		LocalDate hasta = LocalDate.of(2026, 8, 31);
		when(reservaService.findAll(10L, 20L, EstadoReserva.CONFIRMADA, desde, hasta, 1, 5, "fechaEntrada", "DESC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(2L, EstadoReserva.CONFIRMADA)), 1, 5, 8));

		mockMvc.perform(get("/api/reservas")
						.param("usuarioId", "10")
						.param("habitacionId", "20")
						.param("estado", "CONFIRMADA")
						.param("fechaEntradaDesde", "2026-08-01")
						.param("fechaEntradaHasta", "2026-08-31")
						.param("page", "1")
						.param("size", "5")
						.param("sortBy", "fechaEntrada")
						.param("direction", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(2))
				.andExpect(jsonPath("$.content[0].estado").value("CONFIRMADA"))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(5))
				.andExpect(jsonPath("$.totalElements").value(8));
	}

	@Test
	void shouldRejectInvalidFilterEnum() throws Exception {
		mockMvc.perform(get("/api/reservas").param("estado", "APROBADA"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Parametros de entrada invalidos"))
				.andExpect(jsonPath("$.validations.estado").value("Valor invalido para el parametro estado"));
	}

	@Test
	void shouldRejectInvalidPaginationParameters() throws Exception {
		when(reservaService.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(-1), eq(10), eq("id"), eq("ASC")))
				.thenThrow(new IllegalArgumentException("El numero de pagina no puede ser negativo"));

		mockMvc.perform(get("/api/reservas").param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El numero de pagina no puede ser negativo"))
				.andExpect(jsonPath("$.path").value("/api/reservas"));
	}

	@Test
	void shouldFindReservaById() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildResponse(1L, EstadoReserva.PENDIENTE));

		mockMvc.perform(get("/api/reservas/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.usuarioEmail").value("cliente@correo.com"))
				.andExpect(jsonPath("$.habitacionNumero").value("101"))
				.andExpect(jsonPath("$.precioTotal").value(240.00));
	}

	@Test
	void shouldReturnNotFoundWhenReservaDoesNotExist() throws Exception {
		when(reservaService.findById(99L)).thenThrow(new ReservaNotFoundException(99L));

		mockMvc.perform(get("/api/reservas/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Reserva no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/reservas/99"));
	}

	@Test
	void shouldCreateReserva() throws Exception {
		when(reservaService.create(any(CreateReservaRequest.class)))
				.thenReturn(buildResponse(10L, EstadoReserva.PENDIENTE));

		mockMvc.perform(post("/api/reservas")
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
	void shouldRejectCreateReservaWithInvalidFields() throws Exception {
		mockMvc.perform(post("/api/reservas")
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
	void shouldUpdateReservaEstado() throws Exception {
		when(reservaService.update(eq(1L), any(UpdateReservaRequest.class)))
				.thenReturn(buildResponse(1L, EstadoReserva.CONFIRMADA));

		mockMvc.perform(patch("/api/reservas/1/estado")
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
	void shouldRejectUpdateReservaEstadoWithInvalidBody() throws Exception {
		mockMvc.perform(patch("/api/reservas/1/estado")
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

		mockMvc.perform(patch("/api/reservas/99/estado")
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
		mockMvc.perform(delete("/api/reservas/1"))
				.andExpect(status().isNoContent());

		verify(reservaService).delete(1L);
	}

	@Test
	void shouldReturnNotFoundWhenDeletingMissingReserva() throws Exception {
		org.mockito.Mockito.doThrow(new ReservaNotFoundException(99L)).when(reservaService).delete(99L);

		mockMvc.perform(delete("/api/reservas/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Reserva no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/reservas/99"));
	}

	private ReservaResponse buildResponse(Long id, EstadoReserva estado) {
		return new ReservaResponse(
				id,
				10L,
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
