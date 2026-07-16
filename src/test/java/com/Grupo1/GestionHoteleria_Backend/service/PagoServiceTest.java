package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.dto.CreatePagoRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PagoResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.MetodoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.Pago;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.ReservaNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.PagoRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;

class PagoServiceTest {

	private PagoRepository pagoRepository;
	private ReservaRepository reservaRepository;
	private PagoService pagoService;

	@BeforeEach
	void setUp() {
		pagoRepository = org.mockito.Mockito.mock(PagoRepository.class);
		reservaRepository = org.mockito.Mockito.mock(ReservaRepository.class);
		pagoService = new PagoService(pagoRepository, reservaRepository);
	}

	@Test
	void shouldCreateApprovedPaymentUsingReservationTotal() {
		Reserva reserva = buildReserva();
		when(reservaRepository.findByIdWithUsuarioAndHabitacion(1L)).thenReturn(Optional.of(reserva));
		when(pagoRepository.existsByReservaIdAndEstadoIn(any(), any())).thenReturn(false);
		when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
			Pago pago = invocation.getArgument(0);
			pago.setId(10L);
			return pago;
		});

		PagoResponse response = pagoService.create(new CreatePagoRequest(1L, MetodoPago.TARJETA, null));

		assertThat(response.id()).isEqualTo(10L);
		assertThat(response.reservaId()).isEqualTo(1L);
		assertThat(response.monto()).isEqualByComparingTo("240.00");
		assertThat(response.metodoPago()).isEqualTo(MetodoPago.TARJETA);
		assertThat(response.estado()).isEqualTo(EstadoPago.APROBADO);
		assertThat(response.codigoOperacion()).startsWith("SIM-");
		assertThat(response.fechaPago()).isNotNull();
		assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
		verify(pagoRepository).save(any(Pago.class));
	}

	@Test
	void shouldKeepRequestedPaymentState() {
		Reserva reserva = buildReserva();
		when(reservaRepository.findByIdWithUsuarioAndHabitacion(1L)).thenReturn(Optional.of(reserva));
		when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PagoResponse response = pagoService.create(new CreatePagoRequest(1L, MetodoPago.PAYPAL, EstadoPago.RECHAZADO));

		assertThat(response.estado()).isEqualTo(EstadoPago.RECHAZADO);
		assertThat(response.metodoPago()).isEqualTo(MetodoPago.PAYPAL);
		assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
	}

	@Test
	void shouldRejectDuplicateActivePaymentForReservation() {
		when(reservaRepository.findByIdWithUsuarioAndHabitacion(1L)).thenReturn(Optional.of(buildReserva()));
		when(pagoRepository.existsByReservaIdAndEstadoIn(any(), any())).thenReturn(true);

		assertThatThrownBy(() -> pagoService.create(new CreatePagoRequest(1L, MetodoPago.TARJETA, EstadoPago.APROBADO)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La reserva ya tiene un pago pendiente o aprobado");
	}

	@Test
	void shouldReturnNotFoundWhenReservationDoesNotExist() {
		when(reservaRepository.findByIdWithUsuarioAndHabitacion(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> pagoService.create(new CreatePagoRequest(99L, MetodoPago.TARJETA, null)))
				.isInstanceOf(ReservaNotFoundException.class)
				.hasMessage("Reserva no encontrada con id: 99");
	}

	@Test
	void shouldListPaymentsByReservation() {
		when(pagoRepository.findByReservaId(1L)).thenReturn(List.of(buildPago()));

		List<PagoResponse> response = pagoService.findByReservaId(1L);

		assertThat(response).hasSize(1);
		assertThat(response.get(0).codigoOperacion()).isEqualTo("SIM-ABC123");
	}

	private Pago buildPago() {
		return Pago.builder()
				.id(5L)
				.reserva(buildReserva())
				.monto(new BigDecimal("240.00"))
				.metodoPago(MetodoPago.TARJETA)
				.estado(EstadoPago.APROBADO)
				.codigoOperacion("SIM-ABC123")
				.build();
	}

	private Reserva buildReserva() {
		Usuario usuario = Usuario.builder()
				.id(10L)
				.nombre("Cliente")
				.email("cliente@correo.com")
				.password("secret")
				.build();
		Habitacion habitacion = Habitacion.builder()
				.id(20L)
				.numero("101")
				.precioPorNoche(new BigDecimal("120.00"))
				.build();

		return Reserva.builder()
				.id(1L)
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(LocalDate.of(2026, 8, 10))
				.fechaSalida(LocalDate.of(2026, 8, 12))
				.cantidadHuespedes(2)
				.precioTotal(new BigDecimal("240.00"))
				.build();
	}
}
