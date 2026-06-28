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

import com.Grupo1.GestionHoteleria_Backend.dto.CreateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.dto.HabitacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.UpdateHabitacionRequest;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.exception.HabitacionNumeroAlreadyExistsException;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
class HabitacionServiceIntegrationTest {

	@Autowired
	private HabitacionRepository habitacionRepository;

	@Autowired
	private ReservaRepository reservaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private HabitacionService habitacionService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		reservaRepository.deleteAll();
		habitacionRepository.deleteAll();
		usuarioRepository.deleteAll();
		usuario = usuarioRepository.save(Usuario.builder()
				.nombre("Cliente Habitaciones")
				.email("habitaciones@correo.com")
				.password("password")
				.rol(Rol.CLIENTE)
				.build());
		habitacionRepository.save(buildHabitacion("101", TipoHabitacion.SIMPLE, EstadoHabitacion.DISPONIBLE, 1, "80.00"));
		habitacionRepository.save(buildHabitacion("201", TipoHabitacion.DOBLE, EstadoHabitacion.DISPONIBLE, 2, "150.00"));
		habitacionRepository.save(buildHabitacion("501", TipoHabitacion.SUITE, EstadoHabitacion.DISPONIBLE, 3, "280.00"));
		habitacionRepository.save(buildHabitacion("502", TipoHabitacion.SUITE, EstadoHabitacion.MANTENIMIENTO, 3, "260.00"));
	}

	@Test
	void shouldApplySimpleFiltersTogether() {
		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				TipoHabitacion.SUITE,
				EstadoHabitacion.DISPONIBLE,
				2,
				new BigDecimal("200.00"),
				new BigDecimal("300.00"),
				0,
				10,
				"precioPorNoche",
				"ASC"
		);

		assertThat(response.totalElements()).isEqualTo(1);
		assertThat(response.content()).extracting(HabitacionResponse::numero).containsExactly("501");
	}

	@Test
	void shouldApplyCapacityAndPriceFiltersWithoutTipoOrEstado() {
		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				null,
				null,
				2,
				new BigDecimal("100.00"),
				new BigDecimal("260.00"),
				0,
				10,
				"numero",
				"ASC"
		);

		assertThat(response.content()).extracting(HabitacionResponse::numero).containsExactly("201", "502");
	}

	@Test
	void shouldFilterHabitacionesAvailableByDateRange() {
		Habitacion habitacionOcupada = habitacionRepository.findByNumero("201").orElseThrow();
		Habitacion habitacionConReservaCancelada = habitacionRepository.findByNumero("501").orElseThrow();
		reservaRepository.save(buildReserva(
				habitacionOcupada,
				LocalDate.of(2026, 9, 10),
				LocalDate.of(2026, 9, 15),
				EstadoReserva.CONFIRMADA
		));
		reservaRepository.save(buildReserva(
				habitacionConReservaCancelada,
				LocalDate.of(2026, 9, 11),
				LocalDate.of(2026, 9, 14),
				EstadoReserva.CANCELADA
		));

		PageResponse<HabitacionResponse> response = habitacionService.findAll(
				null,
				EstadoHabitacion.DISPONIBLE,
				2,
				null,
				null,
				LocalDate.of(2026, 9, 12),
				LocalDate.of(2026, 9, 13),
				0,
				10,
				"numero",
				"ASC"
		);

		assertThat(response.content()).extracting(HabitacionResponse::numero).containsExactly("501");
	}

	@Test
	void shouldCreateHabitacionAndPersistDefaultEstado() {
		CreateHabitacionRequest request = new CreateHabitacionRequest(
				"601",
				6,
				TipoHabitacion.MATRIMONIAL,
				null,
				2,
				new BigDecimal("220.00"),
				"Habitacion matrimonial"
		);

		HabitacionResponse response = habitacionService.create(request);

		assertThat(response.id()).isNotNull();
		assertThat(response.numero()).isEqualTo("601");
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
		assertThat(habitacionRepository.findByNumero("601")).isPresent()
				.get()
				.extracting(Habitacion::getPrecioPorNoche)
				.isEqualTo(new BigDecimal("220.00"));
	}

	@Test
	void shouldRejectDuplicatedNumeroOnCreate() {
		CreateHabitacionRequest request = new CreateHabitacionRequest(
				"101",
				1,
				TipoHabitacion.SIMPLE,
				EstadoHabitacion.DISPONIBLE,
				1,
				new BigDecimal("90.00"),
				null
		);

		assertThatThrownBy(() -> habitacionService.create(request))
				.isInstanceOf(HabitacionNumeroAlreadyExistsException.class)
				.hasMessage("Ya existe una habitacion con el numero: 101");
	}

	@Test
	void shouldUpdateHabitacionAndPersistChanges() {
		Habitacion habitacion = habitacionRepository.findByNumero("201").orElseThrow();
		UpdateHabitacionRequest request = new UpdateHabitacionRequest(
				"202",
				2,
				TipoHabitacion.FAMILIAR,
				EstadoHabitacion.MANTENIMIENTO,
				4,
				new BigDecimal("320.00"),
				"Habitacion familiar actualizada"
		);

		HabitacionResponse response = habitacionService.update(habitacion.getId(), request);

		assertThat(response.numero()).isEqualTo("202");
		assertThat(response.tipo()).isEqualTo(TipoHabitacion.FAMILIAR);
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.MANTENIMIENTO);
		assertThat(response.capacidad()).isEqualTo(4);
		assertThat(response.precioPorNoche()).isEqualByComparingTo("320.00");

		Habitacion persisted = habitacionRepository.findById(habitacion.getId()).orElseThrow();
		assertThat(persisted.getNumero()).isEqualTo("202");
		assertThat(persisted.getDescripcion()).isEqualTo("Habitacion familiar actualizada");
	}

	@Test
	void shouldPatchHabitacionAndKeepUnsentFields() {
		Habitacion habitacion = habitacionRepository.findByNumero("101").orElseThrow();
		UpdateHabitacionRequest request = new UpdateHabitacionRequest(
				null,
				null,
				null,
				EstadoHabitacion.OCUPADA,
				null,
				null,
				null
		);

		HabitacionResponse response = habitacionService.update(habitacion.getId(), request);

		assertThat(response.numero()).isEqualTo("101");
		assertThat(response.tipo()).isEqualTo(TipoHabitacion.SIMPLE);
		assertThat(response.estado()).isEqualTo(EstadoHabitacion.OCUPADA);
		assertThat(response.precioPorNoche()).isEqualByComparingTo("80.00");
	}

	@Test
	void shouldRejectDuplicatedNumeroOnUpdate() {
		Habitacion habitacion = habitacionRepository.findByNumero("201").orElseThrow();
		UpdateHabitacionRequest request = new UpdateHabitacionRequest(
				"101",
				null,
				null,
				null,
				null,
				null,
				null
		);

		assertThatThrownBy(() -> habitacionService.update(habitacion.getId(), request))
				.isInstanceOf(HabitacionNumeroAlreadyExistsException.class)
				.hasMessage("Ya existe una habitacion con el numero: 101");
	}

	@Test
	void shouldDeleteHabitacionAndRejectSecondDelete() {
		Habitacion habitacion = habitacionRepository.findByNumero("502").orElseThrow();

		habitacionService.delete(habitacion.getId());

		assertThat(habitacionRepository.existsById(habitacion.getId())).isFalse();
		assertThatThrownBy(() -> habitacionService.delete(habitacion.getId()))
				.isInstanceOf(HabitacionNotFoundException.class)
				.hasMessage("Habitacion no encontrada con id: " + habitacion.getId());
	}

	private Habitacion buildHabitacion(
			String numero,
			TipoHabitacion tipo,
			EstadoHabitacion estado,
			Integer capacidad,
			String precioPorNoche
	) {
		return Habitacion.builder()
				.numero(numero)
				.piso(1)
				.tipo(tipo)
				.estado(estado)
				.capacidad(capacidad)
				.precioPorNoche(new BigDecimal(precioPorNoche))
				.descripcion("Habitacion de prueba")
				.build();
	}

	private Reserva buildReserva(
			Habitacion habitacion,
			LocalDate fechaEntrada,
			LocalDate fechaSalida,
			EstadoReserva estado
	) {
		return Reserva.builder()
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(fechaEntrada)
				.fechaSalida(fechaSalida)
				.cantidadHuespedes(1)
				.precioTotal(habitacion.getPrecioPorNoche())
				.estado(estado)
				.build();
	}
}
