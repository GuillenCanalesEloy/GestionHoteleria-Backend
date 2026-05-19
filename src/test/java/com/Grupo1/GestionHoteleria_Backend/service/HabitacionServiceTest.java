package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNumeroAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;

class HabitacionServiceTest {

	private HabitacionRepository habitacionRepository;
	private HabitacionService habitacionService;

	@BeforeEach
	void setUp() {
		habitacionRepository = org.mockito.Mockito.mock(HabitacionRepository.class);
		habitacionService = new HabitacionService(habitacionRepository);
	}

	@Test
	void shouldListHabitaciones() {
		when(habitacionRepository.findAll()).thenReturn(List.of(buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE)));

		List<HabitacionResponse> habitaciones = habitacionService.findAll();

		assertThat(habitaciones).hasSize(1);
		assertThat(habitaciones.getFirst().numero()).isEqualTo("101");
		assertThat(habitaciones.getFirst().estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
	}

	@Test
	void shouldFindHabitacionById() {
		when(habitacionRepository.findById(1L)).thenReturn(Optional.of(buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE)));

		HabitacionResponse response = habitacionService.findById(1L);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.numero()).isEqualTo("101");
	}

	@Test
	void shouldFilterHabitacionesByTipo() {
		when(habitacionRepository.findByTipo(TipoHabitacion.SIMPLE))
				.thenReturn(List.of(buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE)));

		List<HabitacionResponse> habitaciones = habitacionService.findByTipo(TipoHabitacion.SIMPLE);

		assertThat(habitaciones).hasSize(1);
		assertThat(habitaciones.getFirst().tipo()).isEqualTo(TipoHabitacion.SIMPLE);
	}

	@Test
	void shouldFilterHabitacionesByEstado() {
		when(habitacionRepository.findByEstado(EstadoHabitacion.DISPONIBLE))
				.thenReturn(List.of(buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE)));

		List<HabitacionResponse> habitaciones = habitacionService.findByEstado(EstadoHabitacion.DISPONIBLE);

		assertThat(habitaciones).hasSize(1);
		assertThat(habitaciones.getFirst().estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
	}

	@Test
	void shouldCreateHabitacionWithDefaultDisponibleStatus() {
		CreateHabitacionRequest request = new CreateHabitacionRequest(
				"101",
				1,
				TipoHabitacion.SIMPLE,
				null,
				1,
				new BigDecimal("120.00"),
				"Habitacion simple"
		);

		when(habitacionRepository.existsByNumero("101")).thenReturn(false);
		when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(invocation -> {
			Habitacion habitacion = invocation.getArgument(0);
			habitacion.setId(10L);
			return habitacion;
		});

		HabitacionResponse response = habitacionService.create(request);

		verify(habitacionRepository).save(any(Habitacion.class));
		assertThat(response.id()).isEqualTo(10L);
		assertThat(response.numero()).isEqualTo("101");
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
	}

	@Test
	void shouldRejectCreateWhenNumeroAlreadyExists() {
		CreateHabitacionRequest request = new CreateHabitacionRequest(
				"101",
				1,
				TipoHabitacion.SIMPLE,
				EstadoHabitacion.DISPONIBLE,
				1,
				new BigDecimal("120.00"),
				null
		);

		when(habitacionRepository.existsByNumero("101")).thenReturn(true);

		assertThatThrownBy(() -> habitacionService.create(request))
				.isInstanceOf(HabitacionNumeroAlreadyExistsException.class)
				.hasMessageContaining("101");

		verify(habitacionRepository, never()).save(any(Habitacion.class));
	}

	@Test
	void shouldUpdateHabitacionPartially() {
		Habitacion habitacion = buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE);
		UpdateHabitacionRequest request = new UpdateHabitacionRequest(
				"102",
				2,
				TipoHabitacion.DOBLE,
				EstadoHabitacion.MANTENIMIENTO,
				2,
				new BigDecimal("180.00"),
				"Habitacion actualizada"
		);

		when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
		when(habitacionRepository.existsByNumeroAndIdNot("102", 1L)).thenReturn(false);
		when(habitacionRepository.save(habitacion)).thenReturn(habitacion);

		HabitacionResponse response = habitacionService.update(1L, request);

		assertThat(response.numero()).isEqualTo("102");
		assertThat(response.piso()).isEqualTo(2);
		assertThat(response.tipo()).isEqualTo(TipoHabitacion.DOBLE);
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.MANTENIMIENTO);
		assertThat(response.capacidad()).isEqualTo(2);
		assertThat(response.precioPorNoche()).isEqualByComparingTo("180.00");
		assertThat(response.descripcion()).isEqualTo("Habitacion actualizada");
	}

	@Test
	void shouldRejectUpdateWhenNumeroBelongsToAnotherHabitacion() {
		Habitacion habitacion = buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE);
		UpdateHabitacionRequest request = new UpdateHabitacionRequest("102", null, null, null, null, null, null);

		when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
		when(habitacionRepository.existsByNumeroAndIdNot("102", 1L)).thenReturn(true);

		assertThatThrownBy(() -> habitacionService.update(1L, request))
				.isInstanceOf(HabitacionNumeroAlreadyExistsException.class)
				.hasMessageContaining("102");

		verify(habitacionRepository, never()).save(any(Habitacion.class));
	}

	@Test
	void shouldDeleteExistingHabitacion() {
		when(habitacionRepository.existsById(1L)).thenReturn(true);

		habitacionService.delete(1L);

		verify(habitacionRepository).deleteById(1L);
	}

	@Test
	void shouldRejectDeleteWhenHabitacionDoesNotExist() {
		when(habitacionRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> habitacionService.delete(99L))
				.isInstanceOf(HabitacionNotFoundException.class)
				.hasMessageContaining("99");

		verify(habitacionRepository, never()).deleteById(99L);
	}

	private Habitacion buildHabitacion(Long id, String numero, EstadoHabitacion estado) {
		return Habitacion.builder()
				.id(id)
				.numero(numero)
				.piso(1)
				.tipo(TipoHabitacion.SIMPLE)
				.estado(estado)
				.capacidad(1)
				.precioPorNoche(new BigDecimal("120.00"))
				.descripcion("Habitacion simple")
				.build();
	}
}
