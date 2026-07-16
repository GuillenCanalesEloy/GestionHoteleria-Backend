package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHabitacionFormRequest {

	@NotBlank(message = "El numero de habitacion es obligatorio")
	@Size(max = 20, message = "El numero no debe superar 20 caracteres")
	private String numero;

	@NotNull(message = "El piso es obligatorio")
	@Positive(message = "El piso debe ser mayor a cero")
	private Integer piso;

	@NotNull(message = "El tipo de habitacion es obligatorio")
	private TipoHabitacion tipo;

	private EstadoHabitacion estado;

	@NotNull(message = "La capacidad es obligatoria")
	@Positive(message = "La capacidad debe ser mayor a cero")
	private Integer capacidad;

	@NotNull(message = "El precio por noche es obligatorio")
	@DecimalMin(value = "0.01", message = "El precio por noche debe ser mayor a cero")
	private BigDecimal precioPorNoche;

	@Size(max = 500, message = "La descripcion no debe superar 500 caracteres")
	private String descripcion;

	public CreateHabitacionRequest toRequest(String imagenUrl) {
		return new CreateHabitacionRequest(
				numero,
				piso,
				tipo,
				estado,
				capacidad,
				precioPorNoche,
				descripcion,
				imagenUrl
		);
	}
}
