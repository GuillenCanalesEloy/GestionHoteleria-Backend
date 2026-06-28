package com.Grupo1.GestionHoteleria_Backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Grupo1.GestionHoteleria_Backend.dto.AreaComunRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.AreaComunResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;
import com.Grupo1.GestionHoteleria_Backend.service.AreaComunService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/areas-comunes")
@RequiredArgsConstructor
public class AreaComunController {

	private final AreaComunService areaComunService;

	@GetMapping
	public ResponseEntity<List<AreaComunResponse>> findAll() {
		List<AreaComun> areas = areaComunService.findAll();
		return ResponseEntity.ok(areas.stream().map(AreaComunResponse::fromEntity).toList());
	}

	@GetMapping("/disponibles")
	public ResponseEntity<List<AreaComunResponse>> findDisponibles() {
		List<AreaComun> areas = areaComunService.findAllDisponibles();
		return ResponseEntity.ok(areas.stream().map(AreaComunResponse::fromEntity).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AreaComunResponse> findById(@PathVariable Long id) {
		AreaComun area = areaComunService.findById(id);
		return ResponseEntity.ok(AreaComunResponse.fromEntity(area));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AreaComunResponse> create(@Valid @RequestBody AreaComunRequest request) {
		AreaComun areaComun = AreaComun.builder()
				.nombre(request.getNombre())
				.descripcion(request.getDescripcion())
				.capacidadMaxima(request.getCapacidadMaxima())
				.precioPorHora(request.getPrecioPorHora())
				.estado(EstadoAreaComun.DISPONIBLE)
				.build();
		AreaComun created = areaComunService.create(areaComun);
		return ResponseEntity.status(HttpStatus.CREATED).body(AreaComunResponse.fromEntity(created));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AreaComunResponse> update(@PathVariable Long id,
			@Valid @RequestBody AreaComunRequest request) {
		AreaComun areaComun = AreaComun.builder()
				.nombre(request.getNombre())
				.descripcion(request.getDescripcion())
				.capacidadMaxima(request.getCapacidadMaxima())
				.precioPorHora(request.getPrecioPorHora())
				.build();
		AreaComun updated = areaComunService.update(id, areaComun);
		return ResponseEntity.ok(AreaComunResponse.fromEntity(updated));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		areaComunService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/estado")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AreaComunResponse> updateEstado(@PathVariable Long id,
			@RequestBody EstadoAreaComun nuevoEstado) {
		AreaComun updated = areaComunService.updateEstado(id, nuevoEstado);
		return ResponseEntity.ok(AreaComunResponse.fromEntity(updated));
	}
}
