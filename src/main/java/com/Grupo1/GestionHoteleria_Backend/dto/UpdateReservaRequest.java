package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDate;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;

import jakarta.validation.constraints.Positive;

public record UpdateReservaRequest(
		Long usuarioId,
		Long habitacionId,
		LocalDate fechaEntrada,
		LocalDate fechaSalida,

		@Positive(message = "La cantidad de huespedes debe ser mayor a cero")
		Integer cantidadHuespedes,

		EstadoReserva estado
) {
}
