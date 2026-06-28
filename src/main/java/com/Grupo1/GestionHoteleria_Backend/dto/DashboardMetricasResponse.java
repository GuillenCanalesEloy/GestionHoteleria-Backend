package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardMetricasResponse(
		long totalHabitaciones,
		long habitacionesDisponibles,
		long habitacionesOcupadas,
		long habitacionesMantenimiento,
		DashboardReservaEstadoMetricResponse reservasActivasPorEstado,
		LocalDate ingresosFechaDesde,
		LocalDate ingresosFechaHasta,
		BigDecimal ingresosPorRango,
		LocalDate ocupacionFechaEntrada,
		LocalDate ocupacionFechaSalida,
		List<DashboardOcupacionTipoItemResponse> ocupacionPorTipo
) {
}
