package com.Grupo1.GestionHoteleria_Backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

class HabitacionResponseTest {

	@Test
	void shouldMapHabitacionEntity() {
		LocalDateTime createdAt = LocalDateTime.of(2026, 5, 19, 10, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 19, 11, 0);
		Habitacion habitacion = Habitacion.builder()
				.id(1L)
				.numero("101")
				.piso(1)
				.tipo(TipoHabitacion.SIMPLE)
				.estado(EstadoHabitacion.DISPONIBLE)
				.capacidad(1)
				.precioPorNoche(new BigDecimal("120.00"))
				.descripcion("Habitacion simple")
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();

		HabitacionResponse response = HabitacionResponse.fromEntity(habitacion);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.numero()).isEqualTo("101");
		assertThat(response.piso()).isEqualTo(1);
		assertThat(response.tipo()).isEqualTo(TipoHabitacion.SIMPLE);
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
		assertThat(response.capacidad()).isEqualTo(1);
		assertThat(response.precioPorNoche()).isEqualByComparingTo("120.00");
		assertThat(response.descripcion()).isEqualTo("Habitacion simple");
		assertThat(response.createdAt()).isEqualTo(createdAt);
		assertThat(response.updatedAt()).isEqualTo(updatedAt);
	}
}
