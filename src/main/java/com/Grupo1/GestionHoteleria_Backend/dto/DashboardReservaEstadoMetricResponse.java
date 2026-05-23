package com.Grupo1.GestionHoteleria_Backend.dto;

public record DashboardReservaEstadoMetricResponse(
		long pendientes,
		long confirmadas,
		long totalActivas
) {
}
