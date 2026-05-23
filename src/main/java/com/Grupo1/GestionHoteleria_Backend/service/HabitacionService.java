package com.Grupo1.GestionHoteleria_Backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
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

	private static final int MAX_PAGE_SIZE = 100;
	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"id",
			"numero",
			"piso",
			"tipo",
			"estado",
			"capacidad",
			"precioPorNoche"
	);

	private final HabitacionRepository habitacionRepository;

	@Transactional(readOnly = true)
	public PageResponse<HabitacionResponse> findAll(
			TipoHabitacion tipo,
			EstadoHabitacion estado,
			int page,
			int size,
			String sortBy,
			String direction
	) {
		PageRequest pageable = buildPageRequest(page, size, sortBy, direction);

		if (tipo != null) {
			return PageResponse.fromPage(habitacionRepository.findByTipo(tipo, pageable)
					.map(HabitacionResponse::fromEntity));
		}
		if (estado != null) {
			return PageResponse.fromPage(habitacionRepository.findByEstado(estado, pageable)
					.map(HabitacionResponse::fromEntity));
		}

		return PageResponse.fromPage(habitacionRepository.findAll(pageable)
				.map(HabitacionResponse::fromEntity));
	}

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

	private PageRequest buildPageRequest(int page, int size, String sortBy, String direction) {
		if (page < 0) {
			throw new IllegalArgumentException("El numero de pagina no puede ser negativo");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("El tamano de pagina debe estar entre 1 y " + MAX_PAGE_SIZE);
		}
		if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
			throw new IllegalArgumentException("Campo de ordenamiento no permitido: " + sortBy);
		}

		Sort.Direction sortDirection = parseDirection(direction);
		return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
	}

	private Sort.Direction parseDirection(String direction) {
		try {
			return Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("La direccion de ordenamiento debe ser ASC o DESC");
		}
	}
}
