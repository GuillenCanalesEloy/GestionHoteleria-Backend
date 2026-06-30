package com.Grupo1.GestionHoteleria_Backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.Grupo1.GestionHoteleria_Backend.dto.CreatePagoRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PagoResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.service.PagoService;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

	private final PagoService pagoService;
	private final ReservaService reservaService;

	@GetMapping
	public ResponseEntity<List<PagoResponse>> findAll(Authentication authentication) {
		Usuario currentUser = currentUser(authentication);
		if (!isAdmin(currentUser)) {
			throw new AccessDeniedException("Solo un administrador puede listar todos los pagos");
		}

		return ResponseEntity.ok(pagoService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PagoResponse> findById(@PathVariable Long id, Authentication authentication) {
		PagoResponse response = pagoService.findById(id);
		assertCanAccessReserva(response.reservaId(), currentUser(authentication));

		return ResponseEntity.ok(response);
	}

	@GetMapping("/reserva/{reservaId}")
	public ResponseEntity<List<PagoResponse>> findByReservaId(
			@PathVariable Long reservaId,
			Authentication authentication
	) {
		assertCanAccessReserva(reservaId, currentUser(authentication));
		return ResponseEntity.ok(pagoService.findByReservaId(reservaId));
	}

	@PostMapping
	public ResponseEntity<PagoResponse> create(
			@Valid @RequestBody CreatePagoRequest request,
			UriComponentsBuilder uriBuilder,
			Authentication authentication
	) {
		assertCanAccessReserva(request.reservaId(), currentUser(authentication));
		PagoResponse response = pagoService.create(request);

		return ResponseEntity
				.created(uriBuilder.path("/api/pagos/{id}").buildAndExpand(response.id()).toUri())
				.body(response);
	}

	private void assertCanAccessReserva(Long reservaId, Usuario currentUser) {
		ReservaResponse reserva = reservaService.findById(reservaId);
		if (!isAdmin(currentUser) && !currentUser.getId().equals(reserva.usuarioId())) {
			throw new AccessDeniedException("No tienes permisos para acceder a pagos de otra reserva");
		}
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
}
