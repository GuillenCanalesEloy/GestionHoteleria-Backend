package com.Grupo1.GestionHoteleria_Backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Grupo1.GestionHoteleria_Backend.dto.AuthResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.LoginRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.RegisterRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.EmailAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;
import com.Grupo1.GestionHoteleria_Backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthResponse register(RegisterRequest request) {
		if (usuarioRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}

		Usuario usuario = Usuario.builder()
				.nombre(request.nombre())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.rol(Rol.CLIENTE)
				.build();

		Usuario savedUsuario = usuarioRepository.save(usuario);
		return buildAuthResponse(savedUsuario);
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		Usuario usuario = usuarioRepository.findByEmail(request.email())
				.orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

		return buildAuthResponse(usuario);
	}

	private AuthResponse buildAuthResponse(Usuario usuario) {
		String token = jwtService.generateToken(usuario);
		return new AuthResponse(
				token,
				TOKEN_TYPE,
				usuario.getEmail(),
				usuario.getNombre(),
				usuario.getRol()
		);
	}
}
