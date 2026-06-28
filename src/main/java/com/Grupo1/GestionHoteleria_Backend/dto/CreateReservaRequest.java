package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReservaRequest(
		@NotNull(message = "El usuario es obligatorio")
		Long usuarioId,

		@NotNull(message = "La habitacion es obligatoria")
		Long habitacionId,

		@NotNull(message = "La fecha de entrada es obligatoria")
		LocalDate fechaEntrada,

		@NotNull(message = "La fecha de salida es obligatoria")
		LocalDate fechaSalida,

		@NotNull(message = "La cantidad de huespedes es obligatoria")
		@Positive(message = "La cantidad de huespedes debe ser mayor a cero")
		Integer cantidadHuespedes
) {
}
