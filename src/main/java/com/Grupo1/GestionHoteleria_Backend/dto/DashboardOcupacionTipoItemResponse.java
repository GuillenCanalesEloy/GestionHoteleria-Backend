package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

public record DashboardOcupacionTipoItemResponse(
		TipoHabitacion tipo,
		long totalHabitaciones,
		long habitacionesOcupadas,
		long habitacionesDisponibles,
		BigDecimal porcentajeOcupacion
) {
}
