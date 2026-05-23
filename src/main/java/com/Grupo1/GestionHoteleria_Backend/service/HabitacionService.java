package com.Grupo1.GestionHoteleria_Backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
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
			Integer capacidadMin,
			BigDecimal precioMin,
			BigDecimal precioMax,
			LocalDate fechaEntrada,
			LocalDate fechaSalida,
			int page,
			int size,
			String sortBy,
			String direction
	) {
		validateFilters(capacidadMin, precioMin, precioMax, fechaEntrada, fechaSalida);
		PageRequest pageable = buildPageRequest(page, size, sortBy, direction);
		Specification<Habitacion> specification = buildSpecification(
				tipo,
				estado,
				capacidadMin,
				precioMin,
				precioMax,
				fechaEntrada,
				fechaSalida
		);

		return PageResponse.fromPage(habitacionRepository.findAll(specification, pageable)
				.map(HabitacionResponse::fromEntity));
	}

	@Transactional(readOnly = true)
	public PageResponse<HabitacionResponse> findAll(
			TipoHabitacion tipo,
			EstadoHabitacion estado,
			Integer capacidadMin,
			BigDecimal precioMin,
			BigDecimal precioMax,
			int page,
			int size,
			String sortBy,
			String direction
	) {
		return findAll(tipo, estado, capacidadMin, precioMin, precioMax, null, null, page, size, sortBy, direction);
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

	private void validateFilters(
			Integer capacidadMin,
			BigDecimal precioMin,
			BigDecimal precioMax,
			LocalDate fechaEntrada,
			LocalDate fechaSalida
	) {
		if (capacidadMin != null && capacidadMin < 1) {
			throw new IllegalArgumentException("La capacidad minima debe ser mayor a cero");
		}
		if (precioMin != null && precioMin.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El precio minimo no puede ser negativo");
		}
		if (precioMax != null && precioMax.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El precio maximo no puede ser negativo");
		}
		if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
			throw new IllegalArgumentException("El precio minimo no puede ser mayor al precio maximo");
		}
		if ((fechaEntrada == null) != (fechaSalida == null)) {
			throw new IllegalArgumentException("Debe enviar fechaEntrada y fechaSalida para filtrar disponibilidad");
		}
		if (fechaEntrada != null && !fechaSalida.isAfter(fechaEntrada)) {
			throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada");
		}
	}

	private Specification<Habitacion> buildSpecification(
			TipoHabitacion tipo,
			EstadoHabitacion estado,
			Integer capacidadMin,
			BigDecimal precioMin,
			BigDecimal precioMax,
			LocalDate fechaEntrada,
			LocalDate fechaSalida
	) {
		return Specification
				.where(hasTipo(tipo))
				.and(hasEstado(estado))
				.and(hasCapacidadMin(capacidadMin))
				.and(hasPrecioMin(precioMin))
				.and(hasPrecioMax(precioMax))
				.and(isAvailableBetween(fechaEntrada, fechaSalida));
	}

	private Specification<Habitacion> hasTipo(TipoHabitacion tipo) {
		return (root, query, criteriaBuilder) ->
				tipo == null ? null : criteriaBuilder.equal(root.get("tipo"), tipo);
	}

	private Specification<Habitacion> hasEstado(EstadoHabitacion estado) {
		return (root, query, criteriaBuilder) ->
				estado == null ? null : criteriaBuilder.equal(root.get("estado"), estado);
	}

	private Specification<Habitacion> hasCapacidadMin(Integer capacidadMin) {
		return (root, query, criteriaBuilder) ->
				capacidadMin == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("capacidad"), capacidadMin);
	}

	private Specification<Habitacion> hasPrecioMin(BigDecimal precioMin) {
		return (root, query, criteriaBuilder) ->
				precioMin == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("precioPorNoche"), precioMin);
	}

	private Specification<Habitacion> hasPrecioMax(BigDecimal precioMax) {
		return (root, query, criteriaBuilder) ->
				precioMax == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("precioPorNoche"), precioMax);
	}

	private Specification<Habitacion> isAvailableBetween(LocalDate fechaEntrada, LocalDate fechaSalida) {
		return (root, query, criteriaBuilder) -> {
			if (fechaEntrada == null || fechaSalida == null) {
				return null;
			}

			var overlappingReserva = query.subquery(Long.class);
			var reserva = overlappingReserva.from(Reserva.class);
			overlappingReserva.select(reserva.get("id"))
					.where(
							criteriaBuilder.equal(reserva.get("habitacion").get("id"), root.get("id")),
							criteriaBuilder.notEqual(reserva.get("estado"), EstadoReserva.CANCELADA),
							criteriaBuilder.lessThan(reserva.get("fechaEntrada"), fechaSalida),
							criteriaBuilder.greaterThan(reserva.get("fechaSalida"), fechaEntrada)
					);

			return criteriaBuilder.not(criteriaBuilder.exists(overlappingReserva));
		};
	}
}
