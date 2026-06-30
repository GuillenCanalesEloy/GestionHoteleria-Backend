package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.Grupo1.GestionHoteleria_Backend.dto.PagoResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.MetodoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.GlobalExceptionHandler;
import com.Grupo1.GestionHoteleria_Backend.service.PagoService;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

class PagoControllerTest {

	private PagoService pagoService;
	private ReservaService reservaService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		pagoService = org.mockito.Mockito.mock(PagoService.class);
		reservaService = org.mockito.Mockito.mock(ReservaService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new PagoController(pagoService, reservaService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldCreatePaymentForOwnReservation() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildReserva(10L));
		when(pagoService.create(org.mockito.ArgumentMatchers.any()))
				.thenReturn(buildPago(1L, EstadoPago.APROBADO, MetodoPago.TARJETA));

		mockMvc.perform(post("/api/pagos")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservaId": 1,
								  "metodoPago": "TARJETA"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/pagos/7"))
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.reservaId").value(1))
				.andExpect(jsonPath("$.monto").value(240.00))
				.andExpect(jsonPath("$.metodoPago").value("TARJETA"))
				.andExpect(jsonPath("$.estado").value("APROBADO"))
				.andExpect(jsonPath("$.codigoOperacion").value("SIM-ABC123"));
	}

	@Test
	void shouldRejectPaymentForAnotherUserReservation() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildReserva(99L));

		mockMvc.perform(post("/api/pagos")
						.principal(clienteAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservaId": 1,
								  "metodoPago": "PAYPAL"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No tienes permisos para acceder a pagos de otra reserva"));
	}

	@Test
	void shouldAllowAdminToListPayments() throws Exception {
		when(pagoService.findAll()).thenReturn(List.of(buildPago(1L, EstadoPago.APROBADO, MetodoPago.PAYPAL)));

		mockMvc.perform(get("/api/pagos").principal(adminAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].metodoPago").value("PAYPAL"));
	}

	@Test
	void shouldRejectClienteWhenListingAllPayments() throws Exception {
		mockMvc.perform(get("/api/pagos").principal(clienteAuth()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Solo un administrador puede listar todos los pagos"));
	}

	@Test
	void shouldGetPaymentsByReservationForOwner() throws Exception {
		when(reservaService.findById(1L)).thenReturn(buildReserva(10L));
		when(pagoService.findByReservaId(1L)).thenReturn(List.of(buildPago(1L, EstadoPago.PENDIENTE, MetodoPago.TARJETA)));

		mockMvc.perform(get("/api/pagos/reserva/1").principal(clienteAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
	}

	@Test
	void shouldValidateRequiredPaymentFields() throws Exception {
		mockMvc.perform(post("/api/pagos")
						.principal(adminAuth())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validations.reservaId").value("La reserva es obligatoria"))
				.andExpect(jsonPath("$.validations.metodoPago").value("El metodo de pago es obligatorio"));
	}

	private PagoResponse buildPago(Long reservaId, EstadoPago estado, MetodoPago metodoPago) {
		return new PagoResponse(
				7L,
				reservaId,
				new BigDecimal("240.00"),
				metodoPago,
				estado,
				"SIM-ABC123",
				LocalDateTime.of(2026, 6, 30, 16, 0)
		);
	}

	private ReservaResponse buildReserva(Long usuarioId) {
		return new ReservaResponse(
				1L,
				usuarioId,
				"Cliente",
				"cliente@correo.com",
				20L,
				"101",
				TipoHabitacion.SIMPLE,
				null,
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 12),
				2,
				new BigDecimal("240.00"),
				EstadoReserva.PENDIENTE,
				LocalDateTime.of(2026, 6, 30, 10, 0),
				LocalDateTime.of(2026, 6, 30, 10, 0)
		);
	}

	private Authentication adminAuth() {
		return new UsernamePasswordAuthenticationToken(
				Usuario.builder().id(1L).rol(Rol.ADMIN).email("admin@correo.com").password("secret").build(),
				null
		);
	}

	private Authentication clienteAuth() {
		return new UsernamePasswordAuthenticationToken(
				Usuario.builder().id(10L).rol(Rol.CLIENTE).email("cliente@correo.com").password("secret").build(),
				null
		);
	}
}
