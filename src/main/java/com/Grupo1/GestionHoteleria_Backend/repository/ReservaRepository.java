package com.Grupo1.GestionHoteleria_Backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

	List<Reserva> findByUsuarioId(Long usuarioId);

	List<Reserva> findByHabitacionId(Long habitacionId);

	List<Reserva> findByEstado(EstadoReserva estado);

	long countByEstado(EstadoReserva estado);

	@Query("""
			select count(r) > 0
			from Reserva r
			where r.habitacion.id = :habitacionId
			  and r.estado <> com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva.CANCELADA
			  and r.fechaEntrada < :fechaSalida
			  and r.fechaSalida > :fechaEntrada
			""")
	boolean existsActiveOverlap(
			@Param("habitacionId") Long habitacionId,
			@Param("fechaEntrada") java.time.LocalDate fechaEntrada,
			@Param("fechaSalida") java.time.LocalDate fechaSalida
	);

	@Query("""
			select count(r) > 0
			from Reserva r
			where r.habitacion.id = :habitacionId
			  and r.id <> :reservaId
			  and r.estado <> com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva.CANCELADA
			  and r.fechaEntrada < :fechaSalida
			  and r.fechaSalida > :fechaEntrada
			""")
	boolean existsActiveOverlapExcludingReserva(
			@Param("habitacionId") Long habitacionId,
			@Param("reservaId") Long reservaId,
			@Param("fechaEntrada") java.time.LocalDate fechaEntrada,
			@Param("fechaSalida") java.time.LocalDate fechaSalida
	);

	@Query("""
			select count(distinct r.habitacion.id)
			from Reserva r
			where r.estado <> com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva.CANCELADA
			  and r.fechaEntrada < :fechaSalida
			  and r.fechaSalida > :fechaEntrada
			""")
	long countHabitacionesOcupadasBetween(
			@Param("fechaEntrada") LocalDate fechaEntrada,
			@Param("fechaSalida") LocalDate fechaSalida
	);

	@Query("""
			select coalesce(sum(r.precioTotal), 0)
			from Reserva r
			where r.estado in :estados
			  and (:fechaDesde is null or r.fechaEntrada >= :fechaDesde)
			  and (:fechaHasta is null or r.fechaEntrada <= :fechaHasta)
			""")
	BigDecimal sumIngresosByEstadosAndFechaEntradaBetween(
			@Param("estados") List<EstadoReserva> estados,
			@Param("fechaDesde") LocalDate fechaDesde,
			@Param("fechaHasta") LocalDate fechaHasta
	);

	@Query("""
			select count(r)
			from Reserva r
			where r.estado in :estados
			  and (:fechaDesde is null or r.fechaEntrada >= :fechaDesde)
			  and (:fechaHasta is null or r.fechaEntrada <= :fechaHasta)
			""")
	long countByEstadosAndFechaEntradaBetween(
			@Param("estados") List<EstadoReserva> estados,
			@Param("fechaDesde") LocalDate fechaDesde,
			@Param("fechaHasta") LocalDate fechaHasta
	);
}
