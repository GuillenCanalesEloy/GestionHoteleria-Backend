package com.Grupo1.GestionHoteleria_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

	List<Reserva> findByUsuarioId(Long usuarioId);

	List<Reserva> findByHabitacionId(Long habitacionId);

	List<Reserva> findByEstado(EstadoReserva estado);
}
