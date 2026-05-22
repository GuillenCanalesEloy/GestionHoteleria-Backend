package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateHabitacionRequest(
		@NotBlank(message = "El numero de habitacion es obligatorio")
		@Size(max = 20, message = "El numero no debe superar 20 caracteres")
		String numero,

		@NotNull(message = "El piso es obligatorio")
		@Positive(message = "El piso debe ser mayor a cero")
		Integer piso,

		@NotNull(message = "El tipo de habitacion es obligatorio")
		TipoHabitacion tipo,

		EstadoHabitacion estado,

		@NotNull(message = "La capacidad es obligatoria")
		@Positive(message = "La capacidad debe ser mayor a cero")
		Integer capacidad,

		@NotNull(message = "El precio por noche es obligatorio")
		@DecimalMin(value = "0.01", message = "El precio por noche debe ser mayor a cero")
		BigDecimal precioPorNoche,

		@Size(max = 500, message = "La descripcion no debe superar 500 caracteres")
		String descripcion
) {
}
