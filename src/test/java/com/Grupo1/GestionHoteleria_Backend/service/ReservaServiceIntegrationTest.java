package com.Grupo1.GestionHoteleria_Backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Grupo1.GestionHoteleria_Backend.dto.CreateReservaRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNoDisponibleException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
class ReservaServiceIntegrationTest {

	@Autowired
	private ReservaService reservaService;

	@Autowired
	private ReservaRepository reservaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private HabitacionRepository habitacionRepository;

	private Usuario usuario;
	private Habitacion habitacion;

	@BeforeEach
	void setUp() {
		reservaRepository.deleteAll();
		habitacionRepository.deleteAll();
		usuarioRepository.deleteAll();

		usuario = usuarioRepository.save(Usuario.builder()
				.nombre("Cliente Reserva")
				.email("reserva@correo.com")
				.password("password")
				.rol(Rol.CLIENTE)
				.build());

		habitacion = habitacionRepository.save(Habitacion.builder()
				.numero("701")
				.piso(7)
				.tipo(TipoHabitacion.DOBLE)
				.estado(EstadoHabitacion.DISPONIBLE)
				.capacidad(2)
				.precioPorNoche(new BigDecimal("150.00"))
				.descripcion("Habitacion para reservas")
				.build());
	}

	@Test
	void shouldCreateReservaAndPersistCalculatedPrice() {
		ReservaResponse response = reservaService.create(new CreateReservaRequest(
				usuario.getId(),
				habitacion.getId(),
				LocalDate.of(2026, 8, 10),
				LocalDate.of(2026, 8, 13),
				2
		));

		assertThat(response.id()).isNotNull();
		assertThat(response.estado()).isEqualTo(EstadoReserva.PENDIENTE);
		assertThat(response.precioTotal()).isEqualByComparingTo("450.00");
		assertThat(reservaRepository.findById(response.id())).isPresent()
				.get()
				.extracting(reserva -> reserva.getHabitacion().getId())
				.isEqualTo(habitacion.getId());
	}

	@Test
	void shouldFilterReservasByRelationsStatusAndDateRange() {
		ReservaResponse created = reservaService.create(new CreateReservaRequest(
				usuario.getId(),
				habitacion.getId(),
				LocalDate.of(2026, 9, 5),
				LocalDate.of(2026, 9, 7),
				1
		));

		PageResponse<ReservaResponse> response = reservaService.findAll(
				usuario.getId(),
				habitacion.getId(),
				EstadoReserva.PENDIENTE,
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 9, 30),
				0,
				10,
				"fechaEntrada",
				"ASC"
		);

		assertThat(response.content()).extracting(ReservaResponse::id).containsExactly(created.id());
	}

	@Test
	void shouldRejectReservaWhenHabitacionHasActiveOverlap() {
		reservaService.create(new CreateReservaRequest(
				usuario.getId(),
				habitacion.getId(),
				LocalDate.of(2026, 10, 10),
				LocalDate.of(2026, 10, 15),
				1
		));

		assertThatThrownBy(() -> reservaService.create(new CreateReservaRequest(
				usuario.getId(),
				habitacion.getId(),
				LocalDate.of(2026, 10, 12),
				LocalDate.of(2026, 10, 16),
				1
		)))
				.isInstanceOf(HabitacionNoDisponibleException.class)
				.hasMessage("La habitacion " + habitacion.getId()
						+ " no esta disponible entre 2026-10-12 y 2026-10-16");
	}

	@Test
	void shouldAllowReservaWhenPreviousOverlapIsCancelled() {
		reservaRepository.save(Reserva.builder()
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(LocalDate.of(2026, 11, 10))
				.fechaSalida(LocalDate.of(2026, 11, 15))
				.cantidadHuespedes(1)
				.precioTotal(new BigDecimal("750.00"))
				.estado(EstadoReserva.CANCELADA)
				.build());

		ReservaResponse response = reservaService.create(new CreateReservaRequest(
				usuario.getId(),
				habitacion.getId(),
				LocalDate.of(2026, 11, 12),
				LocalDate.of(2026, 11, 14),
				1
		));

		assertThat(response.id()).isNotNull();
		assertThat(response.precioTotal()).isEqualByComparingTo("300.00");
	}
}
