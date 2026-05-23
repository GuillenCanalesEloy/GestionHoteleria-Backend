package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EstadoReservaTest {

	@Test
	void shouldDefineSupportedReservationStatuses() {
		assertThat(EstadoReserva.values())
				.containsExactly(
						EstadoReserva.PENDIENTE,
						EstadoReserva.CONFIRMADA,
						EstadoReserva.CANCELADA,
						EstadoReserva.FINALIZADA
				);
	}

	@Test
	void shouldExposeDisplayName() {
		assertThat(EstadoReserva.PENDIENTE.getNombre()).isEqualTo("Pendiente");
		assertThat(EstadoReserva.CONFIRMADA.getNombre()).isEqualTo("Confirmada");
		assertThat(EstadoReserva.CANCELADA.getNombre()).isEqualTo("Cancelada");
		assertThat(EstadoReserva.FINALIZADA.getNombre()).isEqualTo("Finalizada");
	}
}
