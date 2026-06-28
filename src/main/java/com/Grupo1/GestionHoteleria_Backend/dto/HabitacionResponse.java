package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

public record HabitacionResponse(
		Long id,
		String numero,
		Integer piso,
		TipoHabitacion tipo,
		EstadoHabitacion estado,
		Integer capacidad,
		BigDecimal precioPorNoche,
		String descripcion,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static HabitacionResponse fromEntity(Habitacion habitacion) {
		return new HabitacionResponse(
				habitacion.getId(),
				habitacion.getNumero(),
				habitacion.getPiso(),
				habitacion.getTipo(),
				habitacion.getEstado(),
				habitacion.getCapacidad(),
				habitacion.getPrecioPorNoche(),
				habitacion.getDescripcion(),
				habitacion.getCreatedAt(),
				habitacion.getUpdatedAt()
		);
	}
}
