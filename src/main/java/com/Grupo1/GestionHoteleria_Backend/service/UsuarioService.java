package com.Grupo1.GestionHoteleria_Backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UsuarioResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.EmailAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.exception.UsuarioNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<UsuarioResponse> findAll() {
		return usuarioRepository.findAll().stream()
				.map(UsuarioResponse::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public UsuarioResponse findById(Long id) {
		return UsuarioResponse.fromEntity(findUsuarioById(id));
	}

	@Transactional
	public UsuarioResponse create(CreateUsuarioRequest request) {
		if (usuarioRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}

		Usuario usuario = Usuario.builder()
				.nombre(request.nombre())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.rol(request.rol())
				.build();

		return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioResponse update(Long id, UpdateUsuarioRequest request) {
		Usuario usuario = findUsuarioById(id);

		if (request.nombre() != null) {
			usuario.setNombre(request.nombre());
		}
		if (request.email() != null) {
			if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
				throw new EmailAlreadyExistsException(request.email());
			}
			usuario.setEmail(request.email());
		}
		if (request.password() != null) {
			usuario.setPassword(passwordEncoder.encode(request.password()));
		}
		if (request.rol() != null) {
			usuario.setRol(request.rol());
		}

		return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
	}

	@Transactional
	public void delete(Long id) {
		if (!usuarioRepository.existsById(id)) {
			throw new UsuarioNotFoundException(id);
		}

		usuarioRepository.deleteById(id);
	}

	private Usuario findUsuarioById(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNotFoundException(id));
	}
}
