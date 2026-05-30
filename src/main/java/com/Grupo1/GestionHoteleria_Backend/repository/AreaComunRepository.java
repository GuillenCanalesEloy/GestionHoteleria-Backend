package com.Grupo1.GestionHoteleria_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;

public interface AreaComunRepository extends JpaRepository<AreaComun, Long> {

	Optional<AreaComun> findByNombre(String nombre);

	List<AreaComun> findByEstado(EstadoAreaComun estado);

	@Query("""
			select ac from AreaComun ac
			where ac.estado = com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun.DISPONIBLE
			order by ac.nombre
			""")
	List<AreaComun> findAllDisponibles();
}
