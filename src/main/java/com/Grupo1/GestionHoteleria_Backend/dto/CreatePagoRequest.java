package com.Grupo1.GestionHoteleria_Backend.dto;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.MetodoPago;

import jakarta.validation.constraints.NotNull;

public record CreatePagoRequest(
		@NotNull(message = "La reserva es obligatoria")
		Long reservaId,

		@NotNull(message = "El metodo de pago es obligatorio")
		MetodoPago metodoPago,

		EstadoPago estado
) {
}
