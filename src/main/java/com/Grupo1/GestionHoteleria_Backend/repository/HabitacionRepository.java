package com.Grupo1.GestionHoteleria_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

import jakarta.persistence.LockModeType;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long>, JpaSpecificationExecutor<Habitacion> {

	Optional<Habitacion> findByNumero(String numero);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select h from Habitacion h where h.id = :id")
	Optional<Habitacion> findByIdForUpdate(@Param("id") Long id);

	boolean existsByNumero(String numero);

	boolean existsByNumeroAndIdNot(String numero, Long id);

	long countByEstado(EstadoHabitacion estado);

	@Query("""
			select h.tipo as tipo, count(h.id) as total
			from Habitacion h
			group by h.tipo
			""")
	List<TipoHabitacionCount> countHabitacionesByTipo();

	List<Habitacion> findByTipo(TipoHabitacion tipo);

	List<Habitacion> findByEstado(EstadoHabitacion estado);

	Page<Habitacion> findByTipo(TipoHabitacion tipo, Pageable pageable);

	Page<Habitacion> findByEstado(EstadoHabitacion estado, Pageable pageable);
}
