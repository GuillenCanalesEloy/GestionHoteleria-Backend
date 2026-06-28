package com.Grupo1.GestionHoteleria_Backend.exception;

import java.time.LocalDate;

public class HabitacionNoDisponibleException extends RuntimeException {

	public HabitacionNoDisponibleException(Long habitacionId, LocalDate fechaEntrada, LocalDate fechaSalida) {
		super("La habitacion " + habitacionId + " no esta disponible entre "
				+ fechaEntrada + " y " + fechaSalida);
	}
}
