package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

public record DashboardResumenResponse(
		long totalHabitaciones,
		long habitacionesDisponibles,
		long habitacionesOcupadas,
		long habitacionesMantenimiento,
		long totalReservas,
		long reservasPendientes,
		long reservasConfirmadas,
		long reservasCanceladas,
		long reservasFinalizadas,
		long totalUsuarios,
		long totalClientes,
		BigDecimal ingresosHistoricos
) {
}
