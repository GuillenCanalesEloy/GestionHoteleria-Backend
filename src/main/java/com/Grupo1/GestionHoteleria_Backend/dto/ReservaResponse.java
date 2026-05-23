package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

public record ReservaResponse(
		Long id,
		Long usuarioId,
		String usuarioNombre,
		String usuarioEmail,
		Long habitacionId,
		String habitacionNumero,
		TipoHabitacion habitacionTipo,
		LocalDate fechaEntrada,
		LocalDate fechaSalida,
		Integer cantidadHuespedes,
		BigDecimal precioTotal,
		EstadoReserva estado,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static ReservaResponse fromEntity(Reserva reserva) {
		return new ReservaResponse(
				reserva.getId(),
				reserva.getUsuario().getId(),
				reserva.getUsuario().getNombre(),
				reserva.getUsuario().getEmail(),
				reserva.getHabitacion().getId(),
				reserva.getHabitacion().getNumero(),
				reserva.getHabitacion().getTipo(),
				reserva.getFechaEntrada(),
				reserva.getFechaSalida(),
				reserva.getCantidadHuespedes(),
				reserva.getPrecioTotal(),
				reserva.getEstado(),
				reserva.getCreatedAt(),
				reserva.getUpdatedAt()
		);
	}
}
