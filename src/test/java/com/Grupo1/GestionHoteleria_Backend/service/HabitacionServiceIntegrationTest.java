package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;

@SpringBootTest
@ActiveProfiles("test")
class HabitacionServiceIntegrationTest {

	@Autowired
	private HabitacionRepository habitacionRepository;

	@Autowired
	private HabitacionService habitacionService;

	@BeforeEach
	void setUp() {
		habitacionRepository.deleteAll();
		habitacionRepository.save(buildHabitacion("101", TipoHabitacion.SIMPLE, EstadoHabitacion.DISPONIBLE, 1, "80.00"));
		habitacionRepository.save(buildHabitacion("201", TipoHabitacion.DOBLE, EstadoHabitacion.DISPONIBLE, 2, "150.00"));
		habitacionRepository.save(buildHabitacion("501", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE, 3, "280.00"));
		habitacionRepository.save(buildHabitacion("502", TipoHabitacion.SUITE, EstadoHabitacion.MANTENIMIENTO, 3, "260.00"));
	}

	@Test
	void shouldApplySimpleFiltersTogether() {
		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				TipoHabitacion.SUITE,
				EstadoHabitacion.DISPONIBLE,
				2,
				new BigDecimal("200.00"),
				new BigDecimal("300.00"),
				0,
				10,
				"precioPorNoche",
				"ASC"
		);

		assertThat(response.totalElements()).isEqualTo(1);
		assertThat(response.content()).extracting(HabitacionResponse::numero).containsExactly("501");
	}

	@Test
	void shouldApplyCapacityAndPriceFiltersWithoutTipoOrEstado() {
		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				null,
				null,
				2,
				new BigDecimal("100.00"),
				new BigDecimal("260.00"),
				0,
				10,
				"numero",
				"ASC"
		);

		assertThat(response.content()).extracting(HabitacionResponse::numero).containsExactly("201", "502");
	}

	private Habitacion buildHabitacion(
			String numero,
			TipoHabitacion tipo,
			EstadoHabitacion estado,
			Integer capacidad,
			String precioPorNoche
	) {
		return Habitacion.builder()
				.numero(numero)
				.piso(1)
				.tipo(tipo)
				.estado(estado)
				.capacidad(capacidad)
				.precioPorNoche(new BigDecimal(precioPorNoche))
				.descripcion("Habitacion de prueba")
				.build();
	}
}
