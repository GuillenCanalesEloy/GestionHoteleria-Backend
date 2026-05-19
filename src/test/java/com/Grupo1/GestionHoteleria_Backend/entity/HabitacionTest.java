package com.Grupo1.GestionHoteleria_Backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class HabitacionTest {

	@Test
	void shouldUseDisponibleAsDefaultStatusBeforePersisting() {
		Habitacion habitacion = buildHabitacion();
		habitacion.setEstado(null);

		habitacion.prePersist();

		assertThat(habitacion.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
		assertThat(habitacion.isDisponible()).isTrue();
	}

	@Test
	void shouldSetTimestampsBeforePersisting() {
		Habitacion habitacion = buildHabitacion();

		habitacion.prePersist();

		assertThat(habitacion.getCreatedAt()).isNotNull();
		assertThat(habitacion.getUpdatedAt()).isNotNull();
	}

	@Test
	void shouldUpdateTimestampBeforeUpdating() throws InterruptedException {
		Habitacion habitacion = buildHabitacion();
		habitacion.prePersist();
		var previousUpdatedAt = habitacion.getUpdatedAt();

		Thread.sleep(1);
		habitacion.preUpdate();

		assertThat(habitacion.getUpdatedAt()).isAfter(previousUpdatedAt);
	}

	@Test
	void shouldDetectUnavailableRoom() {
		Habitacion habitacion = buildHabitacion();
		habitacion.setEstado(EstadoHabitacion.OCUPADA);

		assertThat(habitacion.isDisponible()).isFalse();
	}

	private Habitacion buildHabitacion() {
		return Habitacion.builder()
				.numero("101")
				.piso(1)
				.tipo(TipoHabitacion.SIMPLE)
				.capacidad(1)
				.precioPorNoche(new BigDecimal("120.00"))
				.descripcion("Habitacion simple de prueba")
				.build();
	}
}
