package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

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
public class CreateReservaAreaComunRequest {

	@NotNull(message = "El ID del área común es requerido")
	private Long areaComunId;

	@NotNull(message = "La fecha es requerida")
	private LocalDate fecha;

	@NotNull(message = "La hora de inicio es requerida")
	private LocalTime horaInicio;

	@NotNull(message = "La hora de fin es requerida")
	private LocalTime horaFin;
}
