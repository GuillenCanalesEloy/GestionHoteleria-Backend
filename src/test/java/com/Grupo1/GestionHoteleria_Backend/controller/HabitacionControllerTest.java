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
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
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
		when(habitacionService.findAll()).thenReturn(List.of(buildResponse(1L, "101", TipoHabitacion.SIMPLE, EstadoHabitacion.DISPONIBLE)));

		mockMvc.perform(get("/api/habitaciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].numero").value("101"))
				.andExpect(jsonPath("$[0].tipo").value("SIMPLE"))
				.andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
	}

	@Test
	void shouldFilterHabitacionesByTipo() throws Exception {
		when(habitacionService.findByTipo(TipoHabitacion.SUITE))
				.thenReturn(List.of(buildResponse(2L, "501", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE)));

		mockMvc.perform(get("/api/habitaciones").param("tipo", "SUITE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tipo").value("SUITE"));
	}

	@Test
	void shouldFilterHabitacionesByEstado() throws Exception {
		when(habitacionService.findByEstado(EstadoHabitacion.MANTENIMIENTO))
				.thenReturn(List.of(buildResponse(3L, "301", TipoHabitacion.DOBLE, EstadoHabitacion.MANTENIMIENTO)));

		mockMvc.perform(get("/api/habitaciones").param("estado", "MANTENIMIENTO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].estado").value("MANTENIMIENTO"));
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
	void shouldDeleteHabitacion() throws Exception {
		mockMvc.perform(delete("/api/habitaciones/1"))
				.andExpect(status().isNoContent());

		verify(habitacionService).delete(1L);
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
}
