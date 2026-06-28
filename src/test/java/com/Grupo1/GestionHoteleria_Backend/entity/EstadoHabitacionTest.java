package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EstadoHabitacionTest {

	@Test
	void shouldDefineSupportedRoomStatuses() {
		assertThat(EstadoHabitacion.values())
				.containsExactly(
						EstadoHabitacion.DISPONIBLE,
						EstadoHabitacion.OCUPADA,
						EstadoHabitacion.MANTENIMIENTO
				);
	}

	@Test
	void shouldExposeDisplayName() {
		assertThat(EstadoHabitacion.DISPONIBLE.getNombre()).isEqualTo("Disponible");
		assertThat(EstadoHabitacion.OCUPADA.getNombre()).isEqualTo("Ocupada");
		assertThat(EstadoHabitacion.MANTENIMIENTO.getNombre()).isEqualTo("Mantenimiento");
	}
}
