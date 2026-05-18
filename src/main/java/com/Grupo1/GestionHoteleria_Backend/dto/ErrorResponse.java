package com.Grupo1.GestionHoteleria_Backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		Map<String, String> validations
) {
}
