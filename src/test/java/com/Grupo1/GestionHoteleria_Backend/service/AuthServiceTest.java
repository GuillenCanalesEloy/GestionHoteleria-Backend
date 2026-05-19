package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.Grupo1.GestionHoteleria_Backend.dto.RegisterRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.EmailAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;
import com.Grupo1.GestionHoteleria_Backend.security.JwtService;

class AuthServiceTest {

	private UsuarioRepository usuarioRepository;
	private AuthenticationManager authenticationManager;
	private JwtService jwtService;
	private PasswordEncoder passwordEncoder;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
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

	@Test
	void shouldRegisterUserWithEncryptedPasswordAndReturnJwt() {
		RegisterRequest request = new RegisterRequest("Nuevo Usuario", "nuevo@correo.com", "12345678");

		when(usuarioRepository.existsByEmail("nuevo@correo.com")).thenReturn(false);
		when(passwordEncoder.encode("12345678")).thenReturn("password-encriptado");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtService.generateToken(any(Usuario.class))).thenReturn("jwt-nuevo");

		AuthResponse response = authService.register(request);

		verify(passwordEncoder).encode("12345678");
		verify(usuarioRepository).save(any(Usuario.class));
		assertThat(response.token()).isEqualTo("jwt-nuevo");
		assertThat(response.type()).isEqualTo("Bearer");
		assertThat(response.email()).isEqualTo("nuevo@correo.com");
		assertThat(response.nombre()).isEqualTo("Nuevo Usuario");
		assertThat(response.rol()).isEqualTo(Rol.CLIENTE);
	}

	@Test
	void shouldRejectRegisterWhenEmailAlreadyExists() {
		RegisterRequest request = new RegisterRequest("Usuario Existente", "demo@correo.com", "12345678");

		when(usuarioRepository.existsByEmail("demo@correo.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessageContaining("demo@correo.com");
	}
}
