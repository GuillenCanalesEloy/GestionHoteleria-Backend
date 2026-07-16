package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.ReservaAreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.repository.AreaComunRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaAreaComunRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

class ReservaAreaComunServiceTest {

	private ReservaAreaComunRepository reservaRepository;
	private AreaComunRepository areaComunRepository;
	private UsuarioRepository usuarioRepository;
	private ReservaAreaComunService reservaService;

	@BeforeEach
	void setUp() {
		reservaRepository = org.mockito.Mockito.mock(ReservaAreaComunRepository.class);
		areaComunRepository = org.mockito.Mockito.mock(AreaComunRepository.class);
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		reservaService = new ReservaAreaComunService(reservaRepository, areaComunRepository, usuarioRepository);
	}

	@Test
	void shouldAllowReservationWhenAreaIsMarkedOccupiedButScheduleDoesNotOverlap() {
		Usuario usuario = buildUsuario();
		AreaComun areaComun = buildAreaComun(EstadoAreaComun.OCUPADA);
		LocalDate fecha = LocalDate.of(2026, 8, 10);
		LocalTime horaInicio = LocalTime.of(14, 0);
		LocalTime horaFin = LocalTime.of(16, 0);

		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(areaComunRepository.findById(2L)).thenReturn(Optional.of(areaComun));
		when(reservaRepository.existsOverlapingReservation(2L, fecha, horaInicio, horaFin)).thenReturn(false);
		when(reservaRepository.save(any(ReservaAreaComun.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReservaAreaComun reserva = reservaService.create(1L, 2L, fecha, horaInicio, horaFin);

		assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
		assertThat(reserva.getPrecioTotal()).isEqualByComparingTo("100.00");
		assertThat(reserva.getDuracionMinutos()).isEqualTo(120);
		verify(reservaRepository).save(any(ReservaAreaComun.class));
	}

	@Test
	void shouldRejectReservationWhenAreaIsInMaintenance() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario()));
		when(areaComunRepository.findById(2L)).thenReturn(Optional.of(buildAreaComun(EstadoAreaComun.MANTENIMIENTO)));

		assertThatThrownBy(() -> reservaService.create(
				1L,
				2L,
				LocalDate.of(2026, 8, 10),
				LocalTime.of(14, 0),
				LocalTime.of(16, 0)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El area comun se encuentra en mantenimiento");

		verify(reservaRepository, never()).save(any(ReservaAreaComun.class));
	}

	private Usuario buildUsuario() {
		return Usuario.builder()
				.id(1L)
				.nombre("Cliente")
				.email("cliente@correo.com")
				.password("secret")
				.rol(Rol.CLIENTE)
				.build();
	}

	private AreaComun buildAreaComun(EstadoAreaComun estado) {
		return AreaComun.builder()
				.id(2L)
				.nombre("Sala")
				.capacidadMaxima(10)
				.precioPorHora(new BigDecimal("50.00"))
				.estado(estado)
				.build();
	}
}
