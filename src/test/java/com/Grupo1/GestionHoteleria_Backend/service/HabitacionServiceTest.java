package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
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
	void shouldListHabitacionesWithPaginationAndSorting() {
		when(habitacionRepository.findAll(anySpecification(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(buildHabitacion(1L, "101", EstadoHabitacion.DISPONIBLE))));

		PageResponse<HabitacionResponse> response = habitacionService.findAll(null, null, null, null, null, 0, 10, "numero", "DESC");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(habitacionRepository).findAll(anySpecification(), pageableCaptor.capture());

		Pageable pageable = pageableCaptor.getValue();
		assertThat(pageable.getPageNumber()).isEqualTo(0);
		assertThat(pageable.getPageSize()).isEqualTo(10);
		assertThat(pageable.getSort().getOrderFor("numero").isDescending()).isTrue();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().getFirst().numero()).isEqualTo("101");
	}

	@Test
	void shouldFilterHabitacionesByTipoWithPagination() {
		when(habitacionRepository.findAll(anySpecification(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(buildHabitacion(2L, "501", EstadoHabitacion.DISPONIBLE))));

		PageResponse<HabitacionResponse> response = habitacionService.findAll(TipoHabitacion.SUITE, null, null, null, null, 1, 5, "precioPorNoche", "ASC");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(habitacionRepository).findAll(anySpecification(), pageableCaptor.capture());

		Pageable pageable = pageableCaptor.getValue();
		assertThat(pageable.getPageNumber()).isEqualTo(1);
		assertThat(pageable.getPageSize()).isEqualTo(5);
		assertThat(pageable.getSort().getOrderFor("precioPorNoche").isAscending()).isTrue();
		assertThat(response.content()).hasSize(1);
	}

	@Test
	void shouldFilterHabitacionesByEstadoWithPagination() {
		when(habitacionRepository.findAll(anySpecification(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(buildHabitacion(3L, "301", EstadoHabitacion.MANTENIMIENTO))));

		PageResponse<HabitacionResponse> response = habitacionService.findAll(null, EstadoHabitacion.MANTENIMIENTO, null, null, null, 0, 20, "estado", "DESC");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(habitacionRepository).findAll(anySpecification(), pageableCaptor.capture());

		Pageable pageable = pageableCaptor.getValue();
		assertThat(pageable.getPageSize()).isEqualTo(20);
		assertThat(pageable.getSort().getOrderFor("estado").isDescending()).isTrue();
		assertThat(response.content().getFirst().estado()).isEqualTo(EstadoHabitacion.MANTENIMIENTO);
	}

	@Test
	void shouldRejectInvalidPaginationAndSorting() {
		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, null, null, -1, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El numero de pagina no puede ser negativo");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, null, null, 0, 101, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El tamano de pagina debe estar entre 1 y 100");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, null, null, 0, 10, "password", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Campo de ordenamiento no permitido: password");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, null, null, 0, 10, "id", "ARRIBA"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La direccion de ordenamiento debe ser ASC o DESC");
	}

	@Test
	void shouldFilterHabitacionesWithSimpleFiltersAndPagination() {
		when(habitacionRepository.findAll(anySpecification(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(buildHabitacion(4L, "401", EstadoHabitacion.DISPONIBLE))));

		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				TipoHabitacion.SUITE,
				EstadoHabitacion.DISPONIBLE,
				2,
				new BigDecimal("100.00"),
				new BigDecimal("300.00"),
				0,
				10,
				"precioPorNoche",
				"ASC"
		);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(habitacionRepository).findAll(anySpecification(), pageableCaptor.capture());

		assertThat(pageableCaptor.getValue().getSort().getOrderFor("precioPorNoche").isAscending()).isTrue();
		assertThat(response.content()).hasSize(1);
	}

	@Test
	void shouldRejectInvalidSimpleFilters() {
		assertThatThrownBy(() -> habitacionService.findAll(null, null, 0, null, null, 0, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La capacidad minima debe ser mayor a cero");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, new BigDecimal("-1.00"), null, 0, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El precio minimo no puede ser negativo");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, null, new BigDecimal("-1.00"), 0, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El precio maximo no puede ser negativo");

		assertThatThrownBy(() -> habitacionService.findAll(null, null, null, new BigDecimal("300.00"), new BigDecimal("100.00"), 0, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El precio minimo no puede ser mayor al precio maximo");
	}

	@Test
	void shouldRejectInvalidAvailabilityDateFilters() {
		assertThatThrownBy(() -> habitacionService.findAll(
				null,
				null,
				null,
				null,
				null,
				LocalDate.of(2026, 8, 10),
				null,
				0,
				10,
				"id",
				"ASC"
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Debe enviar fechaEntrada y fechaSalida para filtrar disponibilidad");

		assertThatThrownBy(() -> habitacionService.findAll(
				null,
				null,
				null,
				null,
				null,
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 10),
				0,
				10,
				"id",
				"ASC"
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La fecha de salida debe ser posterior a la fecha de entrada");
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

	private Specification<Habitacion> anySpecification() {
		return any();
	}
}
