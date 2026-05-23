package com.Grupo1.GestionHoteleria_Backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.ReservaNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.UsuarioNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
			"id",
			"fechaEntrada",
			"fechaSalida",
			"cantidadHuespedes",
			"precioTotal",
			"estado",
			"createdAt"
	);

	private final ReservaRepository reservaRepository;
	private final UsuarioRepository usuarioRepository;
	private final HabitacionRepository habitacionRepository;

	@Transactional(readOnly = true)
	public PageResponse<ReservaResponse> findAll(
			Long usuarioId,
			Long habitacionId,
			EstadoReserva estado,
			LocalDate fechaEntradaDesde,
			LocalDate fechaEntradaHasta,
			int page,
			int size,
			String sortBy,
			String direction
	) {
		validateFilterDates(fechaEntradaDesde, fechaEntradaHasta);
		PageRequest pageable = buildPageRequest(page, size, sortBy, direction);
		Specification<Reserva> specification = buildSpecification(
				usuarioId,
				habitacionId,
				estado,
				fechaEntradaDesde,
				fechaEntradaHasta
		);

		return PageResponse.fromPage(reservaRepository.findAll(specification, pageable)
				.map(ReservaResponse::fromEntity));
	}

	@Transactional(readOnly = true)
	public ReservaResponse findById(Long id) {
		return ReservaResponse.fromEntity(findReservaById(id));
	}

	@Transactional
	public ReservaResponse create(CreateReservaRequest request) {
		Usuario usuario = findUsuarioById(request.usuarioId());
		Habitacion habitacion = findHabitacionById(request.habitacionId());

		validateReservationData(request.fechaEntrada(), request.fechaSalida(), request.cantidadHuespedes(), habitacion);

		Reserva reserva = Reserva.builder()
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(request.fechaEntrada())
				.fechaSalida(request.fechaSalida())
				.cantidadHuespedes(request.cantidadHuespedes())
				.precioTotal(calculatePrecioTotal(request.fechaEntrada(), request.fechaSalida(), habitacion))
				.estado(EstadoReserva.PENDIENTE)
				.build();

		return ReservaResponse.fromEntity(reservaRepository.save(reserva));
	}

	@Transactional
	public ReservaResponse update(Long id, UpdateReservaRequest request) {
		Reserva reserva = findReservaById(id);

		if (request.usuarioId() != null) {
			reserva.setUsuario(findUsuarioById(request.usuarioId()));
		}
		if (request.habitacionId() != null) {
			reserva.setHabitacion(findHabitacionById(request.habitacionId()));
		}
		if (request.fechaEntrada() != null) {
			reserva.setFechaEntrada(request.fechaEntrada());
		}
		if (request.fechaSalida() != null) {
			reserva.setFechaSalida(request.fechaSalida());
		}
		if (request.cantidadHuespedes() != null) {
			reserva.setCantidadHuespedes(request.cantidadHuespedes());
		}
		if (request.estado() != null) {
			reserva.setEstado(request.estado());
		}

		validateReservationData(
				reserva.getFechaEntrada(),
				reserva.getFechaSalida(),
				reserva.getCantidadHuespedes(),
				reserva.getHabitacion()
		);
		reserva.setPrecioTotal(calculatePrecioTotal(
				reserva.getFechaEntrada(),
				reserva.getFechaSalida(),
				reserva.getHabitacion()
		));

		return ReservaResponse.fromEntity(reservaRepository.save(reserva));
	}

	@Transactional
	public void delete(Long id) {
		if (!reservaRepository.existsById(id)) {
			throw new ReservaNotFoundException(id);
		}

		reservaRepository.deleteById(id);
	}

	private Reserva findReservaById(Long id) {
		return reservaRepository.findById(id)
				.orElseThrow(() -> new ReservaNotFoundException(id));
	}

	private Usuario findUsuarioById(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNotFoundException(id));
	}

	private Habitacion findHabitacionById(Long id) {
		return habitacionRepository.findById(id)
				.orElseThrow(() -> new HabitacionNotFoundException(id));
	}

	private void validateReservationData(
			LocalDate fechaEntrada,
			LocalDate fechaSalida,
			Integer cantidadHuespedes,
			Habitacion habitacion
	) {
		if (!fechaSalida.isAfter(fechaEntrada)) {
			throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada");
		}
		if (cantidadHuespedes < 1) {
			throw new IllegalArgumentException("La cantidad de huespedes debe ser mayor a cero");
		}
		if (cantidadHuespedes > habitacion.getCapacidad()) {
			throw new IllegalArgumentException("La cantidad de huespedes no puede superar la capacidad de la habitacion");
		}
	}

	private BigDecimal calculatePrecioTotal(LocalDate fechaEntrada, LocalDate fechaSalida, Habitacion habitacion) {
		long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
		return habitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));
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

	private void validateFilterDates(LocalDate fechaEntradaDesde, LocalDate fechaEntradaHasta) {
		if (fechaEntradaDesde != null && fechaEntradaHasta != null && fechaEntradaDesde.isAfter(fechaEntradaHasta)) {
			throw new IllegalArgumentException("La fecha de entrada desde no puede ser posterior a la fecha de entrada hasta");
		}
	}

	private Specification<Reserva> buildSpecification(
			Long usuarioId,
			Long habitacionId,
			EstadoReserva estado,
			LocalDate fechaEntradaDesde,
			LocalDate fechaEntradaHasta
	) {
		return Specification
				.where(hasUsuarioId(usuarioId))
				.and(hasHabitacionId(habitacionId))
				.and(hasEstado(estado))
				.and(hasFechaEntradaDesde(fechaEntradaDesde))
				.and(hasFechaEntradaHasta(fechaEntradaHasta));
	}

	private Specification<Reserva> hasUsuarioId(Long usuarioId) {
		return (root, query, criteriaBuilder) ->
				usuarioId == null ? null : criteriaBuilder.equal(root.get("usuario").get("id"), usuarioId);
	}

	private Specification<Reserva> hasHabitacionId(Long habitacionId) {
		return (root, query, criteriaBuilder) ->
				habitacionId == null ? null : criteriaBuilder.equal(root.get("habitacion").get("id"), habitacionId);
	}

	private Specification<Reserva> hasEstado(EstadoReserva estado) {
		return (root, query, criteriaBuilder) ->
				estado == null ? null : criteriaBuilder.equal(root.get("estado"), estado);
	}

	private Specification<Reserva> hasFechaEntradaDesde(LocalDate fechaEntradaDesde) {
		return (root, query, criteriaBuilder) ->
				fechaEntradaDesde == null
						? null
						: criteriaBuilder.greaterThanOrEqualTo(root.get("fechaEntrada"), fechaEntradaDesde);
	}

	private Specification<Reserva> hasFechaEntradaHasta(LocalDate fechaEntradaHasta) {
		return (root, query, criteriaBuilder) ->
				fechaEntradaHasta == null
						? null
						: criteriaBuilder.lessThanOrEqualTo(root.get("fechaEntrada"), fechaEntradaHasta);
	}
}
