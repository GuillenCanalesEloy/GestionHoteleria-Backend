package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHabitacionFormRequest {

	@Pattern(regexp = ".*\\S.*", message = "El numero de habitacion no puede estar vacio")
	@Size(max = 20, message = "El numero no debe superar 20 caracteres")
	private String numero;

	@Positive(message = "El piso debe ser mayor a cero")
	private Integer piso;

	private TipoHabitacion tipo;

	private EstadoHabitacion estado;

	@Positive(message = "La capacidad debe ser mayor a cero")
	private Integer capacidad;

	@DecimalMin(value = "0.01", message = "El precio por noche debe ser mayor a cero")
	private BigDecimal precioPorNoche;

	@Size(max = 500, message = "La descripcion no debe superar 500 caracteres")
	private String descripcion;

	public UpdateHabitacionRequest toRequest(String imagenUrl) {
		return new UpdateHabitacionRequest(
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
