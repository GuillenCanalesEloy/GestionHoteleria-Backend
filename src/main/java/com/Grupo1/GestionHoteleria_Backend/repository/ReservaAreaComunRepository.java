package com.Grupo1.GestionHoteleria_Backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.ReservaAreaComun;

public interface ReservaAreaComunRepository extends JpaRepository<ReservaAreaComun, Long> {

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	List<ReservaAreaComun> findAll();

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	Optional<ReservaAreaComun> findById(Long id);

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	List<ReservaAreaComun> findByUsuarioId(Long usuarioId);

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	List<ReservaAreaComun> findByAreaComunId(Long areaComunId);

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	List<ReservaAreaComun> findByEstado(EstadoReserva estado);

	@Query("""
			select count(r) > 0
			from ReservaAreaComun r
			where r.areaComun.id = :areaComunId
			  and r.fecha = :fecha
			  and r.estado <> com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva.CANCELADA
			  and (
			    (r.horaInicio < :horaFin and r.horaFin > :horaInicio)
			  )
			""")
	boolean existsOverlapingReservation(
			@Param("areaComunId") Long areaComunId,
			@Param("fecha") LocalDate fecha,
			@Param("horaInicio") LocalTime horaInicio,
			@Param("horaFin") LocalTime horaFin
	);

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	@Query("""
			select r from ReservaAreaComun r
			where r.areaComun.id = :areaComunId
			  and r.fecha = :fecha
			  and r.estado <> com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva.CANCELADA
			order by r.horaInicio
			""")
	List<ReservaAreaComun> findReservasByAreaAndDate(
			@Param("areaComunId") Long areaComunId,
			@Param("fecha") LocalDate fecha
	);

	@EntityGraph(attributePaths = {"usuario", "areaComun"})
	@Query("""
			select r from ReservaAreaComun r
			where r.usuario.id = :usuarioId
			  and r.fecha >= :desde
			  and r.fecha <= :hasta
			order by r.fecha, r.horaInicio
			""")
	List<ReservaAreaComun> findByUsuarioAndDateRange(
			@Param("usuarioId") Long usuarioId,
			@Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta
	);
}
