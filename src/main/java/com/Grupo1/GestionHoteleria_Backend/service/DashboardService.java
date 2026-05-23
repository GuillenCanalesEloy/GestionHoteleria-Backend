package com.Grupo1.GestionHoteleria_Backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.DashboardIngresosResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardOcupacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardResumenResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private static final List<EstadoReserva> ESTADOS_INGRESOS = List.of(
			EstadoReserva.CONFIRMADA,
			EstadoReserva.FINALIZADA
	);

	private final HabitacionRepository habitacionRepository;
	private final ReservaRepository reservaRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public DashboardResumenResponse getResumen() {
		BigDecimal ingresosHistoricos = reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				ESTADOS_INGRESOS,
				null,
				null
		);

		return new DashboardResumenResponse(
				habitacionRepository.count(),
				habitacionRepository.countByEstado(EstadoHabitacion.DISPONIBLE),
				habitacionRepository.countByEstado(EstadoHabitacion.OCUPADA),
				habitacionRepository.countByEstado(EstadoHabitacion.MANTENIMIENTO),
				reservaRepository.count(),
				reservaRepository.countByEstado(EstadoReserva.PENDIENTE),
				reservaRepository.countByEstado(EstadoReserva.CONFIRMADA),
				reservaRepository.countByEstado(EstadoReserva.CANCELADA),
				reservaRepository.countByEstado(EstadoReserva.FINALIZADA),
				usuarioRepository.count(),
				usuarioRepository.countByRol(Rol.CLIENTE),
				ingresosHistoricos
		);
	}

	@Transactional(readOnly = true)
	public DashboardOcupacionResponse getOcupacion(LocalDate fechaEntrada, LocalDate fechaSalida) {
		LocalDate effectiveFechaEntrada = fechaEntrada != null ? fechaEntrada : LocalDate.now();
		LocalDate effectiveFechaSalida = fechaSalida != null ? fechaSalida : effectiveFechaEntrada.plusDays(1);
		validateDateRange(effectiveFechaEntrada, effectiveFechaSalida, "fechaSalida", "fechaEntrada");

		long totalHabitaciones = habitacionRepository.count();
		long habitacionesOcupadas = reservaRepository.countHabitacionesOcupadasBetween(
				effectiveFechaEntrada,
				effectiveFechaSalida
		);
		long habitacionesDisponibles = Math.max(totalHabitaciones - habitacionesOcupadas, 0);
		BigDecimal porcentajeOcupacion = totalHabitaciones == 0
				? BigDecimal.ZERO.setScale(2)
				: BigDecimal.valueOf(habitacionesOcupadas)
						.multiply(BigDecimal.valueOf(100))
						.divide(BigDecimal.valueOf(totalHabitaciones), 2, RoundingMode.HALF_UP);

		return new DashboardOcupacionResponse(
				effectiveFechaEntrada,
				effectiveFechaSalida,
				totalHabitaciones,
				habitacionesOcupadas,
				habitacionesDisponibles,
				porcentajeOcupacion
		);
	}

	@Transactional(readOnly = true)
	public DashboardIngresosResponse getIngresos(LocalDate fechaDesde, LocalDate fechaHasta) {
		validateOptionalDateRange(fechaDesde, fechaHasta, "fechaHasta", "fechaDesde");

		BigDecimal ingresosConfirmados = reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.CONFIRMADA),
				fechaDesde,
				fechaHasta
		);
		BigDecimal ingresosFinalizados = reservaRepository.sumIngresosByEstadosAndFechaEntradaBetween(
				List.of(EstadoReserva.FINALIZADA),
				fechaDesde,
				fechaHasta
		);
		long reservasContabilizadas = reservaRepository.countByEstadosAndFechaEntradaBetween(
				ESTADOS_INGRESOS,
				fechaDesde,
				fechaHasta
		);

		return new DashboardIngresosResponse(
				fechaDesde,
				fechaHasta,
				ingresosConfirmados,
				ingresosFinalizados,
				ingresosConfirmados.add(ingresosFinalizados),
				reservasContabilizadas
		);
	}

	private void validateOptionalDateRange(
			LocalDate fechaDesde,
			LocalDate fechaHasta,
			String fechaHastaName,
			String fechaDesdeName
	) {
		if (fechaDesde != null && fechaHasta != null) {
			if (fechaHasta.isBefore(fechaDesde)) {
				throw new IllegalArgumentException(fechaHastaName + " no puede ser anterior a " + fechaDesdeName);
			}
		}
	}

	private void validateDateRange(
			LocalDate fechaDesde,
			LocalDate fechaHasta,
			String fechaHastaName,
			String fechaDesdeName
	) {
		if (fechaHasta.isBefore(fechaDesde)) {
			throw new IllegalArgumentException(fechaHastaName + " no puede ser anterior a " + fechaDesdeName);
		}
		if (fechaHasta.isEqual(fechaDesde)) {
			throw new IllegalArgumentException(fechaHastaName + " debe ser posterior a " + fechaDesdeName);
		}
	}
}
