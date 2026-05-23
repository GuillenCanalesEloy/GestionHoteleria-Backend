package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardIngresosResponse(
		LocalDate fechaDesde,
		LocalDate fechaHasta,
		BigDecimal ingresosConfirmados,
		BigDecimal ingresosFinalizados,
		BigDecimal ingresosTotales,
		long reservasContabilizadas
) {
}
