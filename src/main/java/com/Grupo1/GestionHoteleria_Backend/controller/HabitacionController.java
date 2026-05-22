package com.Grupo1.GestionHoteleria_Backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.service.HabitacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

	private final HabitacionService habitacionService;

	@GetMapping
	public ResponseEntity<List<HabitacionResponse>> findAll(
			@RequestParam(required = false) TipoHabitacion tipo,
			@RequestParam(required = false) EstadoHabitacion estado
	) {
		if (tipo != null) {
			return ResponseEntity.ok(habitacionService.findByTipo(tipo));
		}
		if (estado != null) {
			return ResponseEntity.ok(habitacionService.findByEstado(estado));
		}

		return ResponseEntity.ok(habitacionService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<HabitacionResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(habitacionService.findById(id));
	}

	@PostMapping
	public ResponseEntity<HabitacionResponse> create(
			@Valid @RequestBody CreateHabitacionRequest request,
			UriComponentsBuilder uriBuilder
	) {
		HabitacionResponse response = habitacionService.create(request);

		return ResponseEntity
				.created(uriBuilder.path("/api/habitaciones/{id}").buildAndExpand(response.id()).toUri())
				.body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<HabitacionResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateHabitacionRequest request) {
		return ResponseEntity.ok(habitacionService.update(id, request));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<HabitacionResponse> patch(@PathVariable Long id, @Valid @RequestBody UpdateHabitacionRequest request) {
		return ResponseEntity.ok(habitacionService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		habitacionService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
