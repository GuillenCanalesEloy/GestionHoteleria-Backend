package com.Grupo1.GestionHoteleria_Backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoPago;
import com.Grupo1.GestionHoteleria_Backend.entity.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

	@Override
	@EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
	List<Pago> findAll();

	@Override
	@EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
	Optional<Pago> findById(Long id);

	@EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
	List<Pago> findByReservaId(Long reservaId);

	boolean existsByReservaIdAndEstadoIn(Long reservaId, Collection<EstadoPago> estados);
}
