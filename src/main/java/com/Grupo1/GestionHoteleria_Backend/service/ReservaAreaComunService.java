package com.Grupo1.GestionHoteleria_Backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.ReservaAreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.ResourceNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.AreaComunRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaAreaComunRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaAreaComunService {

	private final ReservaAreaComunRepository reservaRepository;
	private final AreaComunRepository areaComunRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public ReservaAreaComun findById(Long id) {
		return reservaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Reserva de área común no encontrada con id: " + id));
	}

	@Transactional(readOnly = true)
	public List<ReservaAreaComun> findByUsuarioId(Long usuarioId) {
		return reservaRepository.findByUsuarioId(usuarioId);
	}

	@Transactional(readOnly = true)
	public List<ReservaAreaComun> findByAreaComunId(Long areaComunId) {
		return reservaRepository.findByAreaComunId(areaComunId);
	}

	@Transactional(readOnly = true)
	public List<ReservaAreaComun> findByUsuarioAndDateRange(Long usuarioId, LocalDate desde, LocalDate hasta) {
		return reservaRepository.findByUsuarioAndDateRange(usuarioId, desde, hasta);
	}

	@Transactional(readOnly = true)
	public List<ReservaAreaComun> findReservasByAreaAndDate(Long areaComunId, LocalDate fecha) {
		return reservaRepository.findReservasByAreaAndDate(areaComunId, fecha);
	}

	@Transactional
	public ReservaAreaComun create(Long usuarioId, Long areaComunId, LocalDate fecha, LocalTime horaInicio,
			LocalTime horaFin) {

		// Validar usuario existe
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

		// Validar área común existe
		AreaComun areaComun = areaComunRepository.findById(areaComunId)
				.orElseThrow(() -> new ResourceNotFoundException("Área común no encontrada con id: " + areaComunId));

		// Validar que horaInicio < horaFin
		if (!horaInicio.isBefore(horaFin)) {
			throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
		}

		// Validar que no se solape con otras reservas
		if (reservaRepository.existsOverlapingReservation(areaComunId, fecha, horaInicio, horaFin)) {
			throw new IllegalArgumentException("El horario solicitado no está disponible en esta área común");
		}

		// Calcular duración en minutos
		int duracionMinutos = (int) java.time.temporal.ChronoUnit.MINUTES.between(horaInicio, horaFin);

		// Calcular precio: (duracionMinutos / 60) * precioPorHora
		BigDecimal horas = BigDecimal.valueOf(duracionMinutos).divide(BigDecimal.valueOf(60), 2,
				java.math.RoundingMode.HALF_UP);
		BigDecimal precioTotal = areaComun.getPrecioPorHora().multiply(horas);

		ReservaAreaComun reserva = ReservaAreaComun.builder()
				.usuario(usuario)
				.areaComun(areaComun)
				.fecha(fecha)
				.horaInicio(horaInicio)
				.horaFin(horaFin)
				.duracionMinutos(duracionMinutos)
				.precioTotal(precioTotal)
				.estado(EstadoReserva.PENDIENTE)
				.build();

		return reservaRepository.save(reserva);
	}

	@Transactional
	public ReservaAreaComun updateEstado(Long id, EstadoReserva nuevoEstado) {
		ReservaAreaComun reserva = findById(id);
		reserva.setEstado(nuevoEstado);
		return reservaRepository.save(reserva);
	}

	@Transactional
	public void delete(Long id) {
		ReservaAreaComun reserva = findById(id);
		reservaRepository.delete(reserva);
	}
}
