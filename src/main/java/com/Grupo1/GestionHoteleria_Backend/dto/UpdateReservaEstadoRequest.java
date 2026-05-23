package com.Grupo1.GestionHoteleria_Backend.dto;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;

import jakarta.validation.constraints.NotNull;

public record UpdateReservaEstadoRequest(
		@NotNull(message = "El estado de la reserva es obligatorio")
		EstadoReserva estado
) {
}
