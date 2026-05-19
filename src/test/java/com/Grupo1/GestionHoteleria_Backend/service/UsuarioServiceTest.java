package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateUsuarioRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.UsuarioResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.EmailAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.exception.UsuarioNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

class UsuarioServiceTest {

	private UsuarioRepository usuarioRepository;
	private PasswordEncoder passwordEncoder;
	private UsuarioService usuarioService;

	@BeforeEach
	void setUp() {
		usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
		passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
		usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
	}

	@Test
	void shouldListUsersWithoutPassword() {
		when(usuarioRepository.findAll()).thenReturn(List.of(buildUsuario(1L, "Admin", "admin@correo.com", Rol.ADMIN)));

		List<UsuarioResponse> usuarios = usuarioService.findAll();

		assertThat(usuarios).hasSize(1);
		assertThat(usuarios.getFirst().id()).isEqualTo(1L);
		assertThat(usuarios.getFirst().email()).isEqualTo("admin@correo.com");
	}

	@Test
	void shouldFindUserById() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(buildUsuario(1L, "Cliente", "cliente@correo.com", Rol.CLIENTE)));

		UsuarioResponse response = usuarioService.findById(1L);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.rol()).isEqualTo(Rol.CLIENTE);
	}

	@Test
	void shouldCreateUserWithEncryptedPassword() {
		CreateUsuarioRequest request = new CreateUsuarioRequest("Admin", "admin@correo.com", "12345678", Rol.ADMIN);

		when(usuarioRepository.existsByEmail("admin@correo.com")).thenReturn(false);
		when(passwordEncoder.encode("12345678")).thenReturn("password-encriptado");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario usuario = invocation.getArgument(0);
			usuario.setId(10L);
			return usuario;
		});

		UsuarioResponse response = usuarioService.create(request);

		verify(passwordEncoder).encode("12345678");
		verify(usuarioRepository).save(any(Usuario.class));
		assertThat(response.id()).isEqualTo(10L);
		assertThat(response.rol()).isEqualTo(Rol.ADMIN);
	}

	@Test
	void shouldRejectCreateWhenEmailAlreadyExists() {
		CreateUsuarioRequest request = new CreateUsuarioRequest("Admin", "admin@correo.com", "12345678", Rol.ADMIN);

		when(usuarioRepository.existsByEmail("admin@correo.com")).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.create(request))
				.isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessageContaining("admin@correo.com");

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void shouldUpdateUserPartially() {
		Usuario usuario = buildUsuario(1L, "Cliente", "cliente@correo.com", Rol.CLIENTE);
		UpdateUsuarioRequest request = new UpdateUsuarioRequest("Admin Editado", "admin@correo.com", "87654321", Rol.ADMIN);

		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.existsByEmailAndIdNot("admin@correo.com", 1L)).thenReturn(false);
		when(passwordEncoder.encode("87654321")).thenReturn("password-nuevo");
		when(usuarioRepository.save(usuario)).thenReturn(usuario);

		UsuarioResponse response = usuarioService.update(1L, request);

		assertThat(response.nombre()).isEqualTo("Admin Editado");
		assertThat(response.email()).isEqualTo("admin@correo.com");
		assertThat(response.rol()).isEqualTo(Rol.ADMIN);
		assertThat(usuario.getPassword()).isEqualTo("password-nuevo");
	}

	@Test
	void shouldRejectUpdateWhenEmailBelongsToAnotherUser() {
		Usuario usuario = buildUsuario(1L, "Cliente", "cliente@correo.com", Rol.CLIENTE);
		UpdateUsuarioRequest request = new UpdateUsuarioRequest(null, "admin@correo.com", null, null);

		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.existsByEmailAndIdNot("admin@correo.com", 1L)).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.update(1L, request))
				.isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessageContaining("admin@correo.com");

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void shouldDeleteExistingUser() {
		when(usuarioRepository.existsById(1L)).thenReturn(true);

		usuarioService.delete(1L);

		verify(usuarioRepository).deleteById(1L);
	}

	@Test
	void shouldRejectDeleteWhenUserDoesNotExist() {
		when(usuarioRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> usuarioService.delete(99L))
				.isInstanceOf(UsuarioNotFoundException.class)
				.hasMessageContaining("99");

		verify(usuarioRepository, never()).deleteById(99L);
	}

	private Usuario buildUsuario(Long id, String nombre, String email, Rol rol) {
		return Usuario.builder()
				.id(id)
				.nombre(nombre)
				.email(email)
				.password("password-encriptado")
				.rol(rol)
				.build();
	}
}
