package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.Grupo1.GestionHoteleria_Backend.dto.DashboardIngresosResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardOcupacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardResumenResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

class DashboardServiceTest {

	private HabitacionRepository habitacionRepository;
	private ReservaRepository reservaRepository;
	private UsuarioRepository usuarioRepository;
	private DashboardService dashboardService;

	@BeforeEach
	void setUp() {
		habitacionRepository = org.mockito.Mockito.mock(HabitacionRepository.class);
		reservaRepository = org.mockito.Mockito.mock(ReservaRepository.class);
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		dashboardService = new DashboardService(habitacionRepository, reservaRepository, usuarioRepository);
	}

	@Test
	void shouldBuildResumen() {
		when(habitacionRepository.count()).thenReturn(10L);
		when(habitacionRepository.countByEstado(EstadoHabitacion.DISPONIBLE)).thenReturn(6L);
		when(habitacionRepository.countByEstado(EstadoHabitacion.OCUPADA)).thenReturn(3L);
		when(habitacionRepository.countByEstado(EstadoHabitacion.MANTENIMIENTO)).thenReturn(1L);
		when(reservaRepository.count()).thenReturn(20L);
		when(reservaRepository.countByEstado(EstadoReserva.PENDIENTE)).thenReturn(4L);
		when(reservaRepository.countByEstado(EstadoReserva.CONFIRMADA)).thenReturn(8L);
		when(reservaRepository.countByEstado(EstadoReserva.CANCELADA)).thenReturn(2L);
		when(reservaRepository.countByEstado(EstadoReserva.FINALIZADA)).thenReturn(6L);
		when(usuarioRepository.count()).thenReturn(12L);
		when(usuarioRepository.countByRol(Rol.CLIENTE)).thenReturn(10L);
		when(reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.CONFIRMADA, EstadoReserva.FINALIZADA),
				null,
				null
		)).thenReturn(new BigDecimal("2400.00"));

		DashboardResumenResponse response = dashboardService.getResumen();

		assertThat(response.totalHabitaciones()).isEqualTo(10);
		assertThat(response.habitacionesDisponibles()).isEqualTo(6);
		assertThat(response.reservasConfirmadas()).isEqualTo(8);
		assertThat(response.totalClientes()).isEqualTo(10);
		assertThat(response.ingresosHistoricos()).isEqualByComparingTo("2400.00");
	}

	@Test
	void shouldBuildOcupacionForDateRange() {
		LocalDate fechaEntrada = LocalDate.of(2026, 9, 10);
		LocalDate fechaSalida = LocalDate.of(2026, 9, 12);
		when(habitacionRepository.count()).thenReturn(8L);
		when(reservaRepository.countHabitacionesOcupadasBetween(fechaEntrada, fechaSalida)).thenReturn(3L);

		DashboardOcupacionResponse response = dashboardService.getOcupacion(fechaEntrada, fechaSalida);

		assertThat(response.totalHabitaciones()).isEqualTo(8);
		assertThat(response.habitacionesOcupadas()).isEqualTo(3);
		assertThat(response.habitacionesDisponibles()).isEqualTo(5);
		assertThat(response.porcentajeOcupacion()).isEqualByComparingTo("37.50");
	}

	@Test
	void shouldRejectInvalidOcupacionRange() {
		assertThatThrownBy(() -> dashboardService.getOcupacion(
				LocalDate.of(2026, 9, 10),
				LocalDate.of(2026, 9, 10)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fechaSalida debe ser posterior a fechaEntrada");
	}

	@Test
	void shouldBuildIngresosForDateRange() {
		LocalDate fechaDesde = LocalDate.of(2026, 9, 1);
		LocalDate fechaHasta = LocalDate.of(2026, 9, 30);
		when(reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.CONFIRMADA),
				fechaDesde,
				fechaHasta
		)).thenReturn(new BigDecimal("500.00"));
		when(reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.FINALIZADA),
				fechaDesde,
				fechaHasta
		)).thenReturn(new BigDecimal("700.00"));
		when(reservaRepository.countByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.CONFIRMADA, EstadoReserva.FINALIZADA),
				fechaDesde,
				fechaHasta
		)).thenReturn(4L);

		DashboardIngresosResponse response = dashboardService.getIngresos(fechaDesde, fechaHasta);

		assertThat(response.ingresosConfirmados()).isEqualByComparingTo("500.00");
		assertThat(response.ingresosFinalizados()).isEqualByComparingTo("700.00");
		assertThat(response.ingresosTotales()).isEqualByComparingTo("1200.00");
		assertThat(response.reservasContabilizadas()).isEqualTo(4);
	}

	@Test
	void shouldRejectInvalidIngresosRange() {
		assertThatThrownBy(() -> dashboardService.getIngresos(
				LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 9, 30)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fechaHasta no puede ser anterior a fechaDesde");
	}
}
