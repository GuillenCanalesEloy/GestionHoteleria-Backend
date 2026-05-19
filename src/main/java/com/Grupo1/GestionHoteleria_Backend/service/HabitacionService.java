package com.Grupo1.GestionHoteleria_Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNumeroAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitacionService {

	private final HabitacionRepository habitacionRepository;

	@Transactional(readOnly = true)
	public List<HabitacionResponse> findAll() {
		return habitacionRepository.findAll().stream()
				.map(HabitacionResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public HabitacionResponse findById(Long id) {
		return HabitacionResponse.fromEntity(findHabitacionById(id));
	}

	@Transactional(readOnly = true)
	public List<HabitacionResponse> findByTipo(TipoHabitacion tipo) {
		return habitacionRepository.findByTipo(tipo).stream()
				.map(HabitacionResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<HabitacionResponse> findByEstado(EstadoHabitacion estado) {
		return habitacionRepository.findByEstado(estado).stream()
				.map(HabitacionResponse::fromEntity)
				.toList();
	}

	@Transactional
	public HabitacionResponse create(CreateHabitacionRequest request) {
		if (habitacionRepository.existsByNumero(request.numero())) {
			throw new HabitacionNumeroAlreadyExistsException(request.numero());
		}

		Habitacion habitacion = Habitacion.builder()
				.numero(request.numero())
				.piso(request.piso())
				.tipo(request.tipo())
				.estado(request.estado() != null ? request.estado() : EstadoHabitacion.DISPONIBLE)
				.capacidad(request.capacidad())
				.precioPorNoche(request.precioPorNoche())
				.descripcion(request.descripcion())
				.build();

		return HabitacionResponse.fromEntity(habitacionRepository.save(habitacion));
	}

	@Transactional
	public HabitacionResponse update(Long id, UpdateHabitacionRequest request) {
		Habitacion habitacion = findHabitacionById(id);

		if (request.numero() != null) {
			if (habitacionRepository.existsByNumeroAndIdNot(request.numero(), id)) {
				throw new HabitacionNumeroAlreadyExistsException(request.numero());
			}
			habitacion.setNumero(request.numero());
		}
		if (request.piso() != null) {
			habitacion.setPiso(request.piso());
		}
		if (request.tipo() != null) {
			habitacion.setTipo(request.tipo());
		}
		if (request.estado() != null) {
			habitacion.setEstado(request.estado());
		}
		if (request.capacidad() != null) {
			habitacion.setCapacidad(request.capacidad());
		}
		if (request.precioPorNoche() != null) {
			habitacion.setPrecioPorNoche(request.precioPorNoche());
		}
		if (request.descripcion() != null) {
			habitacion.setDescripcion(request.descripcion());
		}

		return HabitacionResponse.fromEntity(habitacionRepository.save(habitacion));
	}

	@Transactional
	public void delete(Long id) {
		if (!habitacionRepository.existsById(id)) {
			throw new HabitacionNotFoundException(id);
		}

		habitacionRepository.deleteById(id);
	}

	private Habitacion findHabitacionById(Long id) {
		return habitacionRepository.findById(id)
				.orElseThrow(() -> new HabitacionNotFoundException(id));
	}
}
