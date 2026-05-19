package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Grupo1.GestionHoteleria_Backend.dto.AuthResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.LoginRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;
import com.Grupo1.GestionHoteleria_Backend.security.JwtService;

class AuthServiceTest {

	private UsuarioRepository usuarioRepository;
	private AuthenticationManager authenticationManager;
	private JwtService jwtService;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
		authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
		jwtService = org.mockito.Mockito.mock(JwtService.class);
		authService = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService);
	}

	@Test
	void shouldAuthenticateUserAndReturnJwt() {
		LoginRequest request = new LoginRequest("demo@correo.com", "12345678");
		Usuario usuario = Usuario.builder()
				.nombre("Usuario Demo")
				.email("demo@correo.com")
				.password("password-encriptado")
				.rol(Rol.CLIENTE)
				.build();

		when(usuarioRepository.findByEmail("demo@correo.com")).thenReturn(Optional.of(usuario));
		when(jwtService.generateToken(usuario)).thenReturn("jwt-generado");

		AuthResponse response = authService.login(request);

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		assertThat(response.token()).isEqualTo("jwt-generado");
		assertThat(response.type()).isEqualTo("Bearer");
		assertThat(response.email()).isEqualTo("demo@correo.com");
		assertThat(response.nombre()).isEqualTo("Usuario Demo");
		assertThat(response.rol()).isEqualTo(Rol.CLIENTE);
	}
}
