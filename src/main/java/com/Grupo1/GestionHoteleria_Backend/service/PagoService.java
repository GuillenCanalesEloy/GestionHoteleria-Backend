package com.Grupo1.GestionHoteleria_Backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreatePagoRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PagoResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.Pago;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.exception.PagoNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.ReservaNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.PagoRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Set<EstadoPago> ACTIVE_PAYMENT_STATES = Set.of(EstadoPago.PENDIENTE, EstadoPago.APROBADO);

	private final PagoRepository pagoRepository;
	private final ReservaRepository reservaRepository;

	@Transactional(readOnly = true)
	public List<PagoResponse> findAll() {
		return pagoRepository.findAll().stream()
				.map(PagoResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public PagoResponse findById(Long id) {
		return PagoResponse.fromEntity(findPagoById(id));
	}

	@Transactional(readOnly = true)
	public List<PagoResponse> findByReservaId(Long reservaId) {
		return pagoRepository.findByReservaId(reservaId).stream()
				.map(PagoResponse::fromEntity)
				.toList();
	}

	@Transactional
	public PagoResponse create(CreatePagoRequest request) {
		Reserva reserva = reservaRepository.findByIdWithUsuarioAndHabitacion(request.reservaId())
				.orElseThrow(() -> new ReservaNotFoundException(request.reservaId()));
		EstadoPago estado = request.estado() == null ? EstadoPago.APROBADO : request.estado();

		if (ACTIVE_PAYMENT_STATES.contains(estado)
				&& pagoRepository.existsByReservaIdAndEstadoIn(reserva.getId(), ACTIVE_PAYMENT_STATES)) {
			throw new IllegalArgumentException("La reserva ya tiene un pago pendiente o aprobado");
		}

		Pago pago = Pago.builder()
				.reserva(reserva)
				.monto(reserva.getPrecioTotal())
				.metodoPago(request.metodoPago())
				.estado(estado)
				.codigoOperacion(generateCodigoOperacion())
				.fechaPago(LocalDateTime.now())
				.build();

		return PagoResponse.fromEntity(pagoRepository.save(pago));
	}

	private Pago findPagoById(Long id) {
		return pagoRepository.findById(id)
				.orElseThrow(() -> new PagoNotFoundException(id));
	}

	private String generateCodigoOperacion() {
		byte[] bytes = new byte[6];
		RANDOM.nextBytes(bytes);
		return "SIM-" + HexFormat.of().formatHex(bytes).toUpperCase();
	}
}
