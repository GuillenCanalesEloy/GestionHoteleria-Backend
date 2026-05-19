package com.Grupo1.GestionHoteleria_Backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UsuarioResponse;
import com.Grupo1.GestionHoteleria_Backend.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioService usuarioService;

	@GetMapping
	public ResponseEntity<List<UsuarioResponse>> findAll() {
		return ResponseEntity.ok(usuarioService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UsuarioResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(usuarioService.findById(id));
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody CreateUsuarioRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UsuarioResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.update(id, request));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UsuarioResponse> patch(@PathVariable Long id, @Valid @RequestBody UpdateUsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		usuarioService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
