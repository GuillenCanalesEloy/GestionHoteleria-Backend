package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TipoHabitacionTest {

	@Test
	void shouldDefineSupportedRoomTypes() {
		assertThat(TipoHabitacion.values())
				.containsExactly(
						TipoHabitacion.SIMPLE,
						TipoHabitacion.DOBLE,
						TipoHabitacion.MATRIMONIAL,
						TipoHabitacion.FAMILIAR,
						TipoHabitacion.SUITE
				);
	}

	@Test
	void shouldExposeDisplayName() {
		assertThat(TipoHabitacion.SIMPLE.getNombre()).isEqualTo("Simple");
		assertThat(TipoHabitacion.DOBLE.getNombre()).isEqualTo("Doble");
		assertThat(TipoHabitacion.MATRIMONIAL.getNombre()).isEqualTo("Matrimonial");
		assertThat(TipoHabitacion.FAMILIAR.getNombre()).isEqualTo("Familiar");
		assertThat(TipoHabitacion.SUITE.getNombre()).isEqualTo("Suite");
	}
}
