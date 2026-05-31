package com.Grupo1.GestionHoteleria_Backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaAreaComunRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaAreaComunResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateReservaEstadoRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.ReservaAreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaAreaComunService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservas-areas-comunes")
@RequiredArgsConstructor
public class ReservaAreaComunController {

	private final ReservaAreaComunService reservaService;

	@GetMapping("/{id}")
	public ResponseEntity<ReservaAreaComunResponse> findById(@PathVariable Long id) {
		ReservaAreaComun reserva = reservaService.findById(id);
		return ResponseEntity.ok(ReservaAreaComunResponse.fromEntity(reserva));
	}

	@GetMapping("/usuario/{usuarioId}")
	public ResponseEntity<List<ReservaAreaComunResponse>> findByUsuarioId(@PathVariable Long usuarioId) {
		List<ReservaAreaComun> reservas = reservaService.findByUsuarioId(usuarioId);
		return ResponseEntity.ok(reservas.stream().map(ReservaAreaComunResponse::fromEntity).toList());
	}

	@GetMapping("/usuario/{usuarioId}/rango")
	public ResponseEntity<List<ReservaAreaComunResponse>> findByUsuarioAndDateRange(
			@PathVariable Long usuarioId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		List<ReservaAreaComun> reservas = reservaService.findByUsuarioAndDateRange(usuarioId, desde, hasta);
		return ResponseEntity.ok(reservas.stream().map(ReservaAreaComunResponse::fromEntity).toList());
	}

	@GetMapping("/area/{areaComunId}")
	public ResponseEntity<List<ReservaAreaComunResponse>> findByAreaComunId(@PathVariable Long areaComunId) {
		List<ReservaAreaComun> reservas = reservaService.findByAreaComunId(areaComunId);
		return ResponseEntity.ok(reservas.stream().map(ReservaAreaComunResponse::fromEntity).toList());
	}

	@GetMapping("/area/{areaComunId}/fecha")
	public ResponseEntity<List<ReservaAreaComunResponse>> findByAreaAndDate(
			@PathVariable Long areaComunId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		List<ReservaAreaComun> reservas = reservaService.findReservasByAreaAndDate(areaComunId, fecha);
		return ResponseEntity.ok(reservas.stream().map(ReservaAreaComunResponse::fromEntity).toList());
	}

	@PostMapping
	public ResponseEntity<ReservaAreaComunResponse> create(@Valid @RequestBody CreateReservaAreaComunRequest request,
			Authentication authentication) {
		Usuario usuario = (Usuario) authentication.getPrincipal();

		ReservaAreaComun reserva = reservaService.create(
				usuario.getId(),
				request.getAreaComunId(),
				request.getFecha(),
				request.getHoraInicio(),
				request.getHoraFin()
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(ReservaAreaComunResponse.fromEntity(reserva));
	}

	@PutMapping("/{id}/estado")
	public ResponseEntity<ReservaAreaComunResponse> updateEstado(@PathVariable Long id,
			@RequestBody UpdateReservaEstadoRequest request,
			Authentication authentication) {
		Usuario usuario = (Usuario) authentication.getPrincipal();
		ReservaAreaComun reserva = reservaService.findById(id);

		// Validar que es el propietario o admin
		if (!reserva.getUsuario().getId().equals(usuario.getId()) && usuario.getRol().name().equals("CLIENTE")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		ReservaAreaComun updated = reservaService.updateEstado(id, request.estado());
		return ResponseEntity.ok(ReservaAreaComunResponse.fromEntity(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
		Usuario usuario = (Usuario) authentication.getPrincipal();
		ReservaAreaComun reserva = reservaService.findById(id);

		// Validar que es el propietario o admin
		if (!reserva.getUsuario().getId().equals(usuario.getId()) && usuario.getRol().name().equals("CLIENTE")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		reservaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
