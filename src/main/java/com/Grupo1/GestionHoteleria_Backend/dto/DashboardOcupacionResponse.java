package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardOcupacionResponse(
		LocalDate fechaEntrada,
		LocalDate fechaSalida,
		long totalHabitaciones,
		long habitacionesOcupadas,
		long habitacionesDisponibles,
		BigDecimal porcentajeOcupacion
) {
}
