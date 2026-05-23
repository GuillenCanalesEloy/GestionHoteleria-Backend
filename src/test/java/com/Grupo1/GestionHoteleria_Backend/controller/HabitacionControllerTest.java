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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.service.HabitacionService;

class HabitacionControllerTest {

	private HabitacionService habitacionService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		habitacionService = org.mockito.Mockito.mock(HabitacionService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new HabitacionController(habitacionService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldListHabitaciones() throws Exception {
		when(habitacionService.findAll(null, null, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(1L, "101", TipoHabitacion.SIMPLE, EstadoHabitacion.DISPONIBLE)), 0, 10, 1));

		mockMvc.perform(get("/api/habitaciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].numero").value("101"))
				.andExpect(jsonPath("$.content[0].tipo").value("SIMPLE"))
				.andExpect(jsonPath("$.content[0].estado").value("DISPONIBLE"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	@Test
	void shouldFilterHabitacionesByTipo() throws Exception {
		when(habitacionService.findAll(TipoHabitacion.SUITE, null, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(2L, "501", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE)), 0, 10, 1));

		mockMvc.perform(get("/api/habitaciones").param("tipo", "SUITE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].tipo").value("SUITE"));
	}

	@Test
	void shouldFilterHabitacionesByEstado() throws Exception {
		when(habitacionService.findAll(null, EstadoHabitacion.MANTENIMIENTO, null, null, null, 0, 10, "id", "ASC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(3L, "301", TipoHabitacion.DOBLE, EstadoHabitacion.MANTENIMIENTO)), 0, 10, 1));

		mockMvc.perform(get("/api/habitaciones").param("estado", "MANTENIMIENTO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].estado").value("MANTENIMIENTO"));
	}

	@Test
	void shouldListHabitacionesWithPaginationAndSorting() throws Exception {
		when(habitacionService.findAll(null, null, null, null, null, 1, 5, "precioPorNoche", "DESC"))
				.thenReturn(buildPageResponse(List.of(buildResponse(4L, "401", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE)), 1, 5, 12));

		mockMvc.perform(get("/api/habitaciones")
						.param("page", "1")
						.param("size", "5")
						.param("sortBy", "precioPorNoche")
						.param("direction", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].numero").value("401"))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.size").value(5))
				.andExpect(jsonPath("$.totalElements").value(12))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.first").value(false))
				.andExpect(jsonPath("$.last").value(false));
	}

	@Test
	void shouldFilterHabitacionesWithSimpleFilters() throws Exception {
		when(habitacionService.findAll(
				TipoHabitacion.SUITE,
				EstadoHabitacion.DISPONIBLE,
				2,
				new BigDecimal("100.00"),
				new BigDecimal("300.00"),
				0,
				10,
				"precioPorNoche",
				"ASC"
		)).thenReturn(buildPageResponse(List.of(buildResponse(5L, "502", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE)), 0, 10, 1));

		mockMvc.perform(get("/api/habitaciones")
						.param("tipo", "SUITE")
						.param("estado", "DISPONIBLE")
						.param("capacidadMin", "2")
						.param("precioMin", "100.00")
						.param("precioMax", "300.00")
						.param("sortBy", "precioPorNoche")
						.param("direction", "ASC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(5))
				.andExpect(jsonPath("$.content[0].tipo").value("SUITE"))
				.andExpect(jsonPath("$.content[0].estado").value("DISPONIBLE"));
	}

	@Test
	void shouldRejectInvalidPaginationParameters() throws Exception {
		when(habitacionService.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(-1), eq(10), eq("id"), eq("ASC")))
				.thenThrow(new IllegalArgumentException("El numero de pagina no puede ser negativo"));

		mockMvc.perform(get("/api/habitaciones").param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El numero de pagina no puede ser negativo"))
				.andExpect(jsonPath("$.path").value("/api/habitaciones"));
	}

	@Test
	void shouldFindHabitacionById() throws Exception {
		when(habitacionService.findById(1L)).thenReturn(buildResponse(1L, "101", TipoHabitacion.SIMPLE, EstadoHabitacion.DISPONIBLE));

		mockMvc.perform(get("/api/habitaciones/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.numero").value("101"))
				.andExpect(jsonPath("$.precioPorNoche").value(120.00));
	}

	@Test
	void shouldReturnNotFoundWhenHabitacionDoesNotExist() throws Exception {
		when(habitacionService.findById(99L)).thenThrow(new HabitacionNotFoundException(99L));

		mockMvc.perform(get("/api/habitaciones/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Habitacion no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/habitaciones/99"));
	}

	@Test
	void shouldCreateHabitacion() throws Exception {
		when(habitacionService.create(any(CreateHabitacionRequest.class)))
				.thenReturn(buildResponse(10L, "201", TipoHabitacion.DOBLE, EstadoHabitacion.DISPONIBLE));

		mockMvc.perform(post("/api/habitaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "201",
								  "piso": 2,
								  "tipo": "DOBLE",
								  "capacidad": 2,
								  "precioPorNoche": 180.00,
								  "descripcion": "Habitacion doble"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/habitaciones/10"))
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.numero").value("201"))
				.andExpect(jsonPath("$.tipo").value("DOBLE"));
	}

	@Test
	void shouldRejectCreateHabitacionWithInvalidFields() throws Exception {
		mockMvc.perform(post("/api/habitaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "   ",
								  "piso": 0,
								  "tipo": null,
								  "capacidad": 0,
								  "precioPorNoche": 0,
								  "descripcion": "%s"
								}
								""".formatted("x".repeat(501))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
				.andExpect(jsonPath("$.validations.numero").value("El numero de habitacion es obligatorio"))
				.andExpect(jsonPath("$.validations.piso").value("El piso debe ser mayor a cero"))
				.andExpect(jsonPath("$.validations.tipo").value("El tipo de habitacion es obligatorio"))
				.andExpect(jsonPath("$.validations.capacidad").value("La capacidad debe ser mayor a cero"))
				.andExpect(jsonPath("$.validations.precioPorNoche").value("El precio por noche debe ser mayor a cero"))
				.andExpect(jsonPath("$.validations.descripcion").value("La descripcion no debe superar 500 caracteres"));
	}

	@Test
	void shouldRejectCreateHabitacionWithInvalidEnum() throws Exception {
		mockMvc.perform(post("/api/habitaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "201",
								  "piso": 2,
								  "tipo": "PRESIDENCIAL",
								  "capacidad": 2,
								  "precioPorNoche": 180.00
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El cuerpo de la peticion tiene datos invalidos"));
	}

	@Test
	void shouldUpdateHabitacionWithPut() throws Exception {
		when(habitacionService.update(eq(1L), any(UpdateHabitacionRequest.class)))
				.thenReturn(buildResponse(1L, "202", TipoHabitacion.DOBLE, EstadoHabitacion.MANTENIMIENTO));

		mockMvc.perform(put("/api/habitaciones/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "202",
								  "tipo": "DOBLE",
								  "estado": "MANTENIMIENTO"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numero").value("202"))
				.andExpect(jsonPath("$.estado").value("MANTENIMIENTO"));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingMissingHabitacionWithPut() throws Exception {
		when(habitacionService.update(eq(99L), any(UpdateHabitacionRequest.class)))
				.thenThrow(new HabitacionNotFoundException(99L));

		mockMvc.perform(put("/api/habitaciones/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "202"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Habitacion no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/habitaciones/99"));
	}

	@Test
	void shouldRejectUpdateHabitacionWithInvalidOptionalFields() throws Exception {
		mockMvc.perform(put("/api/habitaciones/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "numero": "   ",
								  "piso": -1,
								  "capacidad": 0,
								  "precioPorNoche": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Datos de entrada invalidos"))
				.andExpect(jsonPath("$.validations.numero").value("El numero de habitacion no puede estar vacio"))
				.andExpect(jsonPath("$.validations.piso").value("El piso debe ser mayor a cero"))
				.andExpect(jsonPath("$.validations.capacidad").value("La capacidad debe ser mayor a cero"))
				.andExpect(jsonPath("$.validations.precioPorNoche").value("El precio por noche debe ser mayor a cero"));
	}

	@Test
	void shouldUpdateHabitacionWithPatch() throws Exception {
		when(habitacionService.update(eq(1L), any(UpdateHabitacionRequest.class)))
				.thenReturn(buildResponse(1L, "101", TipoHabitacion.SIMPLE, EstadoHabitacion.OCUPADA));

		mockMvc.perform(patch("/api/habitaciones/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "OCUPADA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("OCUPADA"));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingMissingHabitacionWithPatch() throws Exception {
		when(habitacionService.update(eq(99L), any(UpdateHabitacionRequest.class)))
				.thenThrow(new HabitacionNotFoundException(99L));

		mockMvc.perform(patch("/api/habitaciones/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "MANTENIMIENTO"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Habitacion no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/habitaciones/99"));
	}

	@Test
	void shouldDeleteHabitacion() throws Exception {
		mockMvc.perform(delete("/api/habitaciones/1"))
				.andExpect(status().isNoContent());

		verify(habitacionService).delete(1L);
	}

	@Test
	void shouldReturnNotFoundWhenDeletingMissingHabitacion() throws Exception {
		org.mockito.Mockito.doThrow(new HabitacionNotFoundException(99L)).when(habitacionService).delete(99L);

		mockMvc.perform(delete("/api/habitaciones/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Habitacion no encontrada con id: 99"))
				.andExpect(jsonPath("$.path").value("/api/habitaciones/99"));
	}

	@Test
	void shouldRejectInvalidFilterEnum() throws Exception {
		mockMvc.perform(get("/api/habitaciones").param("tipo", "PRESIDENCIAL"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Parametros de entrada invalidos"))
				.andExpect(jsonPath("$.validations.tipo").value("Valor invalido para el parametro tipo"));
	}

	private HabitacionResponse buildResponse(Long id, String numero, TipoHabitacion tipo, EstadoHabitacion estado) {
		return new HabitacionResponse(
				id,
				numero,
				1,
				tipo,
				estado,
				1,
				new BigDecimal("120.00"),
				"Habitacion de prueba",
				LocalDateTime.of(2026, 5, 19, 10, 0),
				LocalDateTime.of(2026, 5, 19, 11, 0)
		);
	}

	private PageResponse<HabitacionResponse> buildPageResponse(
			List<HabitacionResponse> content,
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
