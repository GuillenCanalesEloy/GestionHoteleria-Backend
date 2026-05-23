package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.Grupo1.GestionHoteleria_Backend.dto.DashboardIngresosResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardOcupacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardResumenResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
import com.Grupo1.GestionHoteleria_Backend.service.DashboardService;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

class DashboardControllerTest {

	private DashboardService dashboardService;
	private ReservaService reservaService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		dashboardService = org.mockito.Mockito.mock(DashboardService.class);
		reservaService = org.mockito.Mockito.mock(ReservaService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService, reservaService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldGetResumen() throws Exception {
		when(dashboardService.getResumen()).thenReturn(new DashboardResumenResponse(
				10,
				6,
				3,
				1,
				20,
				4,
				8,
				2,
				6,
				12,
				10,
				new BigDecimal("2400.00")
		));

		mockMvc.perform(get("/api/dashboard/resumen"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalHabitaciones").value(10))
				.andExpect(jsonPath("$.reservasConfirmadas").value(8))
				.andExpect(jsonPath("$.ingresosHistoricos").value(2400.00));
	}

	@Test
	void shouldGetReservas() throws Exception {
		LocalDate desde = LocalDate.of(2026, 9, 1);
		LocalDate hasta = LocalDate.of(2026, 9, 30);
		when(reservaService.findAll(1L, 2L, EstadoReserva.CONFIRMADA, desde, hasta, 0, 10, "fechaEntrada", "DESC"))
				.thenReturn(buildPageResponse(List.of(buildReservaResponse()), 0, 10, 1));

		mockMvc.perform(get("/api/dashboard/reservas")
						.param("usuarioId", "1")
						.param("habitacionId", "2")
						.param("estado", "CONFIRMADA")
						.param("fechaEntradaDesde", "2026-09-01")
						.param("fechaEntradaHasta", "2026-09-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(50))
				.andExpect(jsonPath("$.content[0].estado").value("CONFIRMADA"));
	}

	@Test
	void shouldRejectInvalidReservasParams() throws Exception {
		when(reservaService.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(-1), eq(10), eq("fechaEntrada"), eq("DESC")))
				.thenThrow(new IllegalArgumentException("El numero de pagina no puede ser negativo"));

		mockMvc.perform(get("/api/dashboard/reservas").param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El numero de pagina no puede ser negativo"));
	}

	@Test
	void shouldGetOcupacion() throws Exception {
		LocalDate fechaEntrada = LocalDate.of(2026, 9, 10);
		LocalDate fechaSalida = LocalDate.of(2026, 9, 12);
		when(dashboardService.getOcupacion(fechaEntrada, fechaSalida))
				.thenReturn(new DashboardOcupacionResponse(
						fechaEntrada,
						fechaSalida,
						8,
						3,
						5,
						new BigDecimal("37.50")
				));

		mockMvc.perform(get("/api/dashboard/ocupacion")
						.param("fechaEntrada", "2026-09-10")
						.param("fechaSalida", "2026-09-12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.habitacionesOcupadas").value(3))
				.andExpect(jsonPath("$.porcentajeOcupacion").value(37.50));
	}

	@Test
	void shouldGetIngresos() throws Exception {
		LocalDate fechaDesde = LocalDate.of(2026, 9, 1);
		LocalDate fechaHasta = LocalDate.of(2026, 9, 30);
		when(dashboardService.getIngresos(fechaDesde, fechaHasta))
				.thenReturn(new DashboardIngresosResponse(
						fechaDesde,
						fechaHasta,
						new BigDecimal("500.00"),
						new BigDecimal("700.00"),
						new BigDecimal("1200.00"),
						4
				));

		mockMvc.perform(get("/api/dashboard/ingresos")
						.param("fechaDesde", "2026-09-01")
						.param("fechaHasta", "2026-09-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ingresosTotales").value(1200.00))
				.andExpect(jsonPath("$.reservasContabilizadas").value(4));
	}

	private ReservaResponse buildReservaResponse() {
		return new ReservaResponse(
				50L,
				1L,
				"Cliente",
				"cliente@correo.com",
				2L,
				"201",
				TipoHabitacion.DOBLE,
				LocalDate.of(2026, 9, 10),
				LocalDate.of(2026, 9, 12),
				2,
				new BigDecimal("300.00"),
				EstadoReserva.CONFIRMADA,
				LocalDateTime.of(2026, 8, 1, 10, 0),
				LocalDateTime.of(2026, 8, 1, 11, 0)
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
