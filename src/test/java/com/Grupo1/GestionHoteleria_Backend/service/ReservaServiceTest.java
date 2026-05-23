package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNoDisponibleException;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.ReservaNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.UsuarioNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

class ReservaServiceTest {

	private ReservaRepository reservaRepository;
	private UsuarioRepository usuarioRepository;
	private HabitacionRepository habitacionRepository;
	private ReservaService reservaService;

	@BeforeEach
	void setUp() {
		reservaRepository = org.mockito.Mockito.mock(ReservaRepository.class);
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		habitacionRepository = org.mockito.Mockito.mock(HabitacionRepository.class);
		reservaService = new ReservaService(reservaRepository, usuarioRepository, habitacionRepository);
	}

	@Test
	void shouldListReservasWithPaginationSortingAndFilters() {
		Reserva reserva = buildReserva();
		when(reservaRepository.findAll(anySpecification(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(reserva)));

		PageResponse<ReservaResponse> response = reservaService.findAll(
				1L,
				10L,
				EstadoReserva.CONFIRMADA,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 30),
				0,
				10,
				"fechaEntrada",
				"DESC"
		);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(reservaRepository).findAll(anySpecification(), pageableCaptor.capture());

		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("fechaEntrada").isDescending()).isTrue();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().getFirst().id()).isEqualTo(reserva.getId());
	}

	@Test
	void shouldRejectInvalidListParameters() {
		assertThatThrownBy(() -> reservaService.findAll(null, null, null, null, null, -1, 10, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El numero de pagina no puede ser negativo");

		assertThatThrownBy(() -> reservaService.findAll(null, null, null, null, null, 0, 101, "id", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El tamano de pagina debe estar entre 1 y 100");

		assertThatThrownBy(() -> reservaService.findAll(null, null, null, null, null, 0, 10, "usuario.email", "ASC"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Campo de ordenamiento no permitido: usuario.email");

		assertThatThrownBy(() -> reservaService.findAll(null, null, null, null, null, 0, 10, "id", "ARRIBA"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La direccion de ordenamiento debe ser ASC o DESC");

		assertThatThrownBy(() -> reservaService.findAll(
				null,
				null,
				null,
				LocalDate.of(2026, 7, 1),
				LocalDate.of(2026, 6, 1),
				0,
				10,
				"id",
				"ASC"
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La fecha de entrada desde no puede ser posterior a la fecha de entrada hasta");
	}

	@Test
	void shouldFindReservaById() {
		when(reservaRepository.findById(1L)).thenReturn(Optional.of(buildReserva()));

		ReservaResponse response = reservaService.findById(1L);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.usuarioId()).isEqualTo(1L);
		assertThat(response.habitacionId()).isEqualTo(10L);
	}

	@Test
	void shouldRejectFindWhenReservaDoesNotExist() {
		when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reservaService.findById(99L))
				.isInstanceOf(ReservaNotFoundException.class)
				.hasMessage("Reserva no encontrada con id: 99");
	}

	@Test
	void shouldCreateReservaWithPendingStatusAndCalculatedPrice() {
		CreateReservaRequest request = new CreateReservaRequest(
				1L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 13),
				2
		);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario()));
		when(habitacionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(buildHabitacion()));
		when(reservaRepository.existsActiveOverlap(10L, request.fechaEntrada(), request.fechaSalida())).thenReturn(false);
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
			Reserva reserva = invocation.getArgument(0);
			reserva.setId(20L);
			return reserva;
		});

		ReservaResponse response = reservaService.create(request);

		ArgumentCaptor<Reserva> reservaCaptor = ArgumentCaptor.forClass(Reserva.class);
		verify(reservaRepository).save(reservaCaptor.capture());

		assertThat(reservaCaptor.getValue().getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
		assertThat(reservaCaptor.getValue().getPrecioTotal()).isEqualByComparingTo("360.00");
		assertThat(response.id()).isEqualTo(20L);
		assertThat(response.precioTotal()).isEqualByComparingTo("360.00");
	}

	@Test
	void shouldRejectCreateWhenHabitacionIsNotAvailableForDateRange() {
		CreateReservaRequest request = new CreateReservaRequest(
				1L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 13),
				2
		);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario()));
		when(habitacionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(buildHabitacion()));
		when(reservaRepository.existsActiveOverlap(10L, request.fechaEntrada(), request.fechaSalida())).thenReturn(true);

		assertThatThrownBy(() -> reservaService.create(request))
				.isInstanceOf(HabitacionNoDisponibleException.class)
				.hasMessage("La habitacion 10 no esta disponible entre 2026-06-10 y 2026-06-13");

		verify(reservaRepository, never()).save(any(Reserva.class));
	}

	@Test
	void shouldRejectCreateWhenUsuarioDoesNotExist() {
		CreateReservaRequest request = new CreateReservaRequest(
				99L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 13),
				2
		);
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reservaService.create(request))
				.isInstanceOf(UsuarioNotFoundException.class)
				.hasMessage("Usuario no encontrado con id: 99");

		verify(reservaRepository, never()).save(any(Reserva.class));
	}

	@Test
	void shouldRejectCreateWhenHabitacionDoesNotExist() {
		CreateReservaRequest request = new CreateReservaRequest(
				1L,
				99L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 13),
				2
		);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario()));
		when(habitacionRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reservaService.create(request))
				.isInstanceOf(HabitacionNotFoundException.class)
				.hasMessage("Habitacion no encontrada con id: 99");

		verify(reservaRepository, never()).save(any(Reserva.class));
	}

	@Test
	void shouldRejectInvalidReservationDatesAndCapacity() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario()));
		when(habitacionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(buildHabitacion()));

		assertThatThrownBy(() -> reservaService.create(new CreateReservaRequest(
				1L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 10),
				2
		)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La fecha de salida debe ser posterior a la fecha de entrada");

		assertThatThrownBy(() -> reservaService.create(new CreateReservaRequest(
				1L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 12),
				0
		)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad de huespedes debe ser mayor a cero");

		assertThatThrownBy(() -> reservaService.create(new CreateReservaRequest(
				1L,
				10L,
				LocalDate.of(2026, 6, 10),
				LocalDate.of(2026, 6, 12),
				3
		)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad de huespedes no puede superar la capacidad de la habitacion");
	}

	@Test
	void shouldUpdateReservaAndRecalculatePrice() {
		Reserva reserva = buildReserva();
		Habitacion nuevaHabitacion = buildHabitacion();
		nuevaHabitacion.setId(11L);
		nuevaHabitacion.setNumero("202");
		nuevaHabitacion.setCapacidad(4);
		nuevaHabitacion.setPrecioPorNoche(new BigDecimal("200.00"));
		UpdateReservaRequest request = new UpdateReservaRequest(
				null,
				11L,
				LocalDate.of(2026, 7, 1),
				LocalDate.of(2026, 7, 4),
				4,
				EstadoReserva.CONFIRMADA
		);

		when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
		when(habitacionRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(nuevaHabitacion));
		when(reservaRepository.existsActiveOverlapExcludingReserva(
				11L,
				1L,
				request.fechaEntrada(),
				request.fechaSalida()
		)).thenReturn(false);
		when(reservaRepository.save(reserva)).thenReturn(reserva);

		ReservaResponse response = reservaService.update(1L, request);

		assertThat(response.habitacionId()).isEqualTo(11L);
		assertThat(response.fechaEntrada()).isEqualTo(LocalDate.of(2026, 7, 1));
		assertThat(response.fechaSalida()).isEqualTo(LocalDate.of(2026, 7, 4));
		assertThat(response.cantidadHuespedes()).isEqualTo(4);
		assertThat(response.estado()).isEqualTo(EstadoReserva.CONFIRMADA);
		assertThat(response.precioTotal()).isEqualByComparingTo("600.00");
	}

	@Test
	void shouldRejectUpdateWhenHabitacionIsNotAvailableForDateRange() {
		Reserva reserva = buildReserva();
		UpdateReservaRequest request = new UpdateReservaRequest(
				null,
				null,
				LocalDate.of(2026, 7, 1),
				LocalDate.of(2026, 7, 4),
				null,
				null
		);

		when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
		when(habitacionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(buildHabitacion()));
		when(reservaRepository.existsActiveOverlapExcludingReserva(
				10L,
				1L,
				request.fechaEntrada(),
				request.fechaSalida()
		)).thenReturn(true);

		assertThatThrownBy(() -> reservaService.update(1L, request))
				.isInstanceOf(HabitacionNoDisponibleException.class)
				.hasMessage("La habitacion 10 no esta disponible entre 2026-07-01 y 2026-07-04");

		verify(reservaRepository, never()).save(any(Reserva.class));
	}

	@Test
	void shouldDeleteExistingReserva() {
		when(reservaRepository.existsById(1L)).thenReturn(true);

		reservaService.delete(1L);

		verify(reservaRepository).deleteById(1L);
	}

	@Test
	void shouldRejectDeleteWhenReservaDoesNotExist() {
		when(reservaRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> reservaService.delete(99L))
				.isInstanceOf(ReservaNotFoundException.class)
				.hasMessage("Reserva no encontrada con id: 99");

		verify(reservaRepository, never()).deleteById(99L);
	}

	private Reserva buildReserva() {
		return Reserva.builder()
				.id(1L)
				.usuario(buildUsuario())
				.habitacion(buildHabitacion())
				.fechaEntrada(LocalDate.of(2026, 6, 10))
				.fechaSalida(LocalDate.of(2026, 6, 12))
				.cantidadHuespedes(2)
				.precioTotal(new BigDecimal("240.00"))
				.estado(EstadoReserva.PENDIENTE)
				.build();
	}

	private Usuario buildUsuario() {
		return Usuario.builder()
				.id(1L)
				.nombre("Cliente Uno")
				.email("cliente@correo.com")
				.password("password")
				.rol(Rol.CLIENTE)
				.build();
	}

	private Habitacion buildHabitacion() {
		return Habitacion.builder()
				.id(10L)
				.numero("101")
				.piso(1)
				.tipo(TipoHabitacion.DOBLE)
				.estado(EstadoHabitacion.DISPONIBLE)
				.capacidad(2)
				.precioPorNoche(new BigDecimal("120.00"))
				.descripcion("Habitacion doble")
				.build();
	}

	private Specification<Reserva> anySpecification() {
		return any();
	}
}
