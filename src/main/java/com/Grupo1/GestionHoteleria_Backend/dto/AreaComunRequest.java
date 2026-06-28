package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaComunRequest {

	@NotBlank(message = "El nombre es requerido")
	private String nombre;

	private String descripcion;

	@NotNull(message = "La capacidad máxima es requerida")
	@Min(value = 1, message = "La capacidad máxima debe ser mayor a 0")
	private Integer capacidadMaxima;

	@NotNull(message = "El precio por hora es requerido")
	@DecimalMin(value = "0.01", message = "El precio por hora debe ser mayor a 0")
	private BigDecimal precioPorHora;
}
