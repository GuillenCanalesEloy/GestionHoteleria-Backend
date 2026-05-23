package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ReservaTest {

	@Test
	void shouldUsePendienteAsDefaultStatusBeforePersisting() {
		Reserva reserva = buildReserva();
		reserva.setEstado(null);

		reserva.prePersist();

		assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
	}

	@Test
	void shouldSetTimestampsBeforePersisting() {
		Reserva reserva = buildReserva();

		reserva.prePersist();

		assertThat(reserva.getCreatedAt()).isNotNull();
		assertThat(reserva.getUpdatedAt()).isNotNull();
	}

	@Test
	void shouldUpdateTimestampBeforeUpdating() throws InterruptedException {
		Reserva reserva = buildReserva();
		reserva.prePersist();
		var previousUpdatedAt = reserva.getUpdatedAt();

		Thread.sleep(1);
		reserva.preUpdate();

		assertThat(reserva.getUpdatedAt()).isAfter(previousUpdatedAt);
	}

	@Test
	void shouldKeepReservationRelationsAndFields() {
		Usuario usuario = buildUsuario();
		Habitacion habitacion = buildHabitacion();
		Reserva reserva = buildReserva(usuario, habitacion);

		assertThat(reserva.getUsuario()).isSameAs(usuario);
		assertThat(reserva.getHabitacion()).isSameAs(habitacion);
		assertThat(reserva.getFechaEntrada()).isEqualTo(LocalDate.of(2026, 6, 10));
		assertThat(reserva.getFechaSalida()).isEqualTo(LocalDate.of(2026, 6, 12));
		assertThat(reserva.getCantidadHuespedes()).isEqualTo(2);
		assertThat(reserva.getPrecioTotal()).isEqualByComparingTo("240.00");
	}

	private Reserva buildReserva() {
		return buildReserva(buildUsuario(), buildHabitacion());
	}

	private Reserva buildReserva(Usuario usuario, Habitacion habitacion) {
		return Reserva.builder()
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(LocalDate.of(2026, 6, 10))
				.fechaSalida(LocalDate.of(2026, 6, 12))
				.cantidadHuespedes(2)
				.precioTotal(new BigDecimal("240.00"))
				.build();
	}

	private Usuario buildUsuario() {
		return Usuario.builder()
				.nombre("Cliente Uno")
				.email("cliente@correo.com")
				.password("password")
				.rol(Rol.CLIENTE)
				.build();
	}

	private Habitacion buildHabitacion() {
		return Habitacion.builder()
				.numero("101")
				.piso(1)
				.tipo(TipoHabitacion.DOBLE)
				.capacidad(2)
				.precioPorNoche(new BigDecimal("120.00"))
				.descripcion("Habitacion doble")
				.build();
	}
}
