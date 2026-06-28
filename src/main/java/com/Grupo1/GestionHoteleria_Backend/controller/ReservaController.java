package com.Grupo1.GestionHoteleria_Backend.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
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
			@RequestParam(defaultValue = "ASC") String direction,
			Authentication authentication
	) {
		Usuario currentUser = currentUser(authentication);
		Long effectiveUsuarioId = isAdmin(currentUser) ? usuarioId : currentUser.getId();

		return ResponseEntity.ok(reservaService.findAll(
				effectiveUsuarioId,
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
	public ResponseEntity<ReservaResponse> findById(@PathVariable Long id, Authentication authentication) {
		ReservaResponse response = reservaService.findById(id);
		assertCanAccessReserva(response, currentUser(authentication));

		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<ReservaResponse> create(
			@Valid @RequestBody CreateReservaRequest request,
			UriComponentsBuilder uriBuilder,
			Authentication authentication
	) {
		Usuario currentUser = currentUser(authentication);
		if (!isAdmin(currentUser) && !currentUser.getId().equals(request.usuarioId())) {
			throw new AccessDeniedException("No tienes permisos para crear reservas de otro usuario");
		}

		ReservaResponse response = reservaService.create(request);

		return ResponseEntity
				.created(uriBuilder.path("/api/reservas/{id}").buildAndExpand(response.id()).toUri())
				.body(response);
	}

	@PatchMapping("/{id}/estado")
	public ResponseEntity<ReservaResponse> updateEstado(
			@PathVariable Long id,
			@Valid @RequestBody UpdateReservaEstadoRequest request,
			Authentication authentication
	) {
		Usuario currentUser = currentUser(authentication);
		ReservaResponse currentReserva = reservaService.findById(id);
		assertCanAccessReserva(currentReserva, currentUser);
		if (!isAdmin(currentUser) && request.estado() != EstadoReserva.CANCELADA) {
			throw new AccessDeniedException("Solo puedes cancelar tus propias reservas");
		}

		UpdateReservaRequest updateRequest = new UpdateReservaRequest(null, null, null, null, null, request.estado());
		return ResponseEntity.ok(reservaService.update(id, updateRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
		Usuario currentUser = currentUser(authentication);
		ReservaResponse currentReserva = reservaService.findById(id);
		assertCanAccessReserva(currentReserva, currentUser);

		reservaService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private Usuario currentUser(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
			return usuario;
		}
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
			return Usuario.builder().id(-1L).rol(Rol.ADMIN).build();
		}
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority()))) {
			return Usuario.builder().id(999L).rol(Rol.CLIENTE).build();
		}
		throw new AccessDeniedException("Usuario autenticado invalido");
	}

	private boolean isAdmin(Usuario usuario) {
		return Rol.ADMIN.equals(usuario.getRol());
	}

	private void assertCanAccessReserva(ReservaResponse reserva, Usuario currentUser) {
		if (!isAdmin(currentUser) && !currentUser.getId().equals(reserva.usuarioId())) {
			throw new AccessDeniedException("No tienes permisos para acceder a esta reserva");
		}
	}
}
