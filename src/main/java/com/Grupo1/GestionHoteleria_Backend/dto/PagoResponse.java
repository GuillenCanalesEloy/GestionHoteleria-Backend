package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.MetodoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.Pago;

public record PagoResponse(
		Long id,
		Long reservaId,
		BigDecimal monto,
		MetodoPago metodoPago,
		EstadoPago estado,
		String codigoOperacion,
		LocalDateTime fechaPago
) {

	public static PagoResponse fromEntity(Pago pago) {
		return new PagoResponse(
				pago.getId(),
				pago.getReserva().getId(),
				pago.getMonto(),
				pago.getMetodoPago(),
				pago.getEstado(),
				pago.getCodigoOperacion(),
				pago.getFechaPago()
		);
	}
}
