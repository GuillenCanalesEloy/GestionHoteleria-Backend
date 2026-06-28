package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@NotNull(message = "El ID del usuario es requerido")
	private Long usuarioId;

	@NotNull(message = "El ID del área común es requerido")
	private Long areaComunId;

	@NotNull(message = "La fecha es requerida")
	@Schema(example = "2026-06-01")
	private LocalDate fecha;

	@NotNull(message = "La hora de inicio es requerida")
	@Schema(type = "string", example = "14:00:00", description = "Hora de inicio en formato HH:mm:ss")
	private LocalTime horaInicio;

	@NotNull(message = "La hora de fin es requerida")
	@Schema(type = "string", example = "16:00:00", description = "Hora de fin en formato HH:mm:ss")
	private LocalTime horaFin;
}
