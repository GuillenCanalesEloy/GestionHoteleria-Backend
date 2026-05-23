package com.Grupo1.GestionHoteleria_Backend.repository;

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
}
