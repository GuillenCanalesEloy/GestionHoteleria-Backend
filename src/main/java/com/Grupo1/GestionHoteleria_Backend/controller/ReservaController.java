package com.Grupo1.GestionHoteleria_Backend.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaEstadoRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

	private final ReservaService reservaService;

	@GetMapping
	public ResponseEntity<PageResponse<ReservaResponse>> findAll(
			@RequestParam(required = false) Long usuarioId,
			@RequestParam(required = false) Long habitacionId,
			@RequestParam(required = false) EstadoReserva estado,
			@RequestParam(required = false) LocalDate fechaEntradaDesde,
			@RequestParam(required = false) LocalDate fechaEntradaHasta,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "ASC") String direction
	) {
		return ResponseEntity.ok(reservaService.findAll(
				usuarioId,
				habitacionId,
				estado,
				fechaEntradaDesde,
				fechaEntradaHasta,
				page,
				size,
				sortBy,
				direction
		));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReservaResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(reservaService.findById(id));
	}

	@PostMapping
	public ResponseEntity<ReservaResponse> create(
			@Valid @RequestBody CreateReservaRequest request,
			UriComponentsBuilder uriBuilder
	) {
		ReservaResponse response = reservaService.create(request);

		return ResponseEntity
				.created(uriBuilder.path("/api/reservas/{id}").buildAndExpand(response.id()).toUri())
				.body(response);
	}

	@PatchMapping("/{id}/estado")
	public ResponseEntity<ReservaResponse> updateEstado(
			@PathVariable Long id,
			@Valid @RequestBody UpdateReservaEstadoRequest request
	) {
		UpdateReservaRequest updateRequest = new UpdateReservaRequest(null, null, null, null, null, request.estado());
		return ResponseEntity.ok(reservaService.update(id, updateRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		reservaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
