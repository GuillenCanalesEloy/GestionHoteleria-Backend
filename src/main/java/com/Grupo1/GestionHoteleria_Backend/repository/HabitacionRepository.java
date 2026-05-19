package com.Grupo1.GestionHoteleria_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

	Optional<Habitacion> findByNumero(String numero);

	boolean existsByNumero(String numero);

	boolean existsByNumeroAndIdNot(String numero, Long id);

	List<Habitacion> findByTipo(TipoHabitacion tipo);

	List<Habitacion> findByEstado(EstadoHabitacion estado);
}
