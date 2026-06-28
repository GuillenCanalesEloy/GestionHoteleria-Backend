package com.Grupo1.GestionHoteleria_Backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Habitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Reserva;
import com.Grupo1.GestionHoteleria_Backend.entity.Rol;
import com.Grupo1.GestionHoteleria_Backend.entity.TipoHabitacion;
import com.Grupo1.GestionHoteleria_Backend.entity.Usuario;
import com.Grupo1.GestionHoteleria_Backend.repository.HabitacionRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.ReservaRepository;
import com.Grupo1.GestionHoteleria_Backend.repository.UsuarioRepository;
import com.Grupo1.GestionHoteleria_Backend.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
class ReservaSystemIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private JwtService jwtService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private HabitacionRepository habitacionRepository;

	@Autowired
	private ReservaRepository reservaRepository;

	private MockMvc mockMvc;
	private Usuario admin;
	private Usuario cliente;
	private Usuario otroCliente;
	private Habitacion habitacion;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		reservaRepository.deleteAll();
		habitacionRepository.deleteAll();
		usuarioRepository.deleteAll();

		admin = usuarioRepository.save(buildUsuario("Admin Reservas", "admin.reservas@correo.com", Rol.ADMIN));
		cliente = usuarioRepository.save(buildUsuario("Cliente Reservas", "cliente.reservas@correo.com", Rol.CLIENTE));
		otroCliente = usuarioRepository.save(buildUsuario("Otro Cliente", "otro.reservas@correo.com", Rol.CLIENTE));
		habitacion = habitacionRepository.save(buildHabitacion("810"));
	}

	@AfterEach
	void tearDown() {
		reservaRepository.deleteAll();
		habitacionRepository.deleteAll();
		usuarioRepository.deleteAll();
	}

	@Test
	void shouldCreateListAndReadOnlyOwnReservaAsCliente() throws Exception {
		Reserva reservaOtroCliente = reservaRepository.save(buildReserva(
				otroCliente,
				habitacion,
				LocalDate.of(2026, 9, 20),
				LocalDate.of(2026, 9, 22),
				EstadoReserva.PENDIENTE
		));

		MvcResult createResult = mockMvc.perform(post("/api/reservas")
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": %d,
								  "habitacionId": %d,
								  "fechaEntrada": "2026-09-10",
								  "fechaSalida": "2026-09-13",
								  "cantidadHuespedes": 2
								}
								""".formatted(cliente.getId(), habitacion.getId())))
				.andExpect(status().isCreated())
				.andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("/api/reservas/")))
				.andExpect(jsonPath("$.usuarioId").value(cliente.getId()))
				.andExpect(jsonPath("$.estado").value("PENDIENTE"))
				.andExpect(jsonPath("$.precioTotal").value(540.00))
				.andReturn();

		Long reservaClienteId = responseId(createResult);

		mockMvc.perform(get("/api/reservas")
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente))
						.param("usuarioId", otroCliente.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].id").value(reservaClienteId))
				.andExpect(jsonPath("$.content[0].usuarioId").value(cliente.getId()));

		mockMvc.perform(get("/api/reservas/{id}", reservaOtroCliente.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No tienes permisos para acceder a esta reserva"));
	}

	@Test
	void shouldRejectOverlappingReservaThroughEndpoint() throws Exception {
		reservaRepository.save(buildReserva(
				cliente,
				habitacion,
				LocalDate.of(2026, 10, 10),
				LocalDate.of(2026, 10, 15),
				EstadoReserva.CONFIRMADA
		));

		mockMvc.perform(post("/api/reservas")
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "usuarioId": %d,
								  "habitacionId": %d,
								  "fechaEntrada": "2026-10-12",
								  "fechaSalida": "2026-10-16",
								  "cantidadHuespedes": 1
								}
								""".formatted(cliente.getId(), habitacion.getId())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("La habitacion " + habitacion.getId()
						+ " no esta disponible entre 2026-10-12 y 2026-10-16"));

		assertThat(reservaRepository.findAll()).hasSize(1);
	}

	@Test
	void shouldAllowAdminToManageReservaEstadoAndClienteOnlyToCancelOwnReserva() throws Exception {
		Reserva reserva = reservaRepository.save(buildReserva(
				cliente,
				habitacion,
				LocalDate.of(2026, 11, 10),
				LocalDate.of(2026, 11, 12),
				EstadoReserva.PENDIENTE
		));

		mockMvc.perform(patch("/api/reservas/{id}/estado", reserva.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CONFIRMADA"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Solo puedes cancelar tus propias reservas"));

		mockMvc.perform(patch("/api/reservas/{id}/estado", reserva.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CONFIRMADA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CONFIRMADA"));

		mockMvc.perform(patch("/api/reservas/{id}/estado", reserva.getId())
						.header(HttpHeaders.AUTHORIZATION, bearer(cliente))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "estado": "CANCELADA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CANCELADA"));
	}

	private String bearer(Usuario usuario) {
		return "Bearer " + jwtService.generateToken(usuario);
	}

	private Long responseId(MvcResult result) throws Exception {
		JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
		return jsonNode.get("id").asLong();
	}

	private Usuario buildUsuario(String nombre, String email, Rol rol) {
		return Usuario.builder()
				.nombre(nombre)
				.email(email)
				.password("password")
				.rol(rol)
				.build();
	}

	private Habitacion buildHabitacion(String numero) {
		return Habitacion.builder()
				.numero(numero)
				.piso(8)
				.tipo(TipoHabitacion.DOBLE)
				.estado(EstadoHabitacion.DISPONIBLE)
				.capacidad(2)
				.precioPorNoche(new BigDecimal("180.00"))
				.descripcion("Habitacion para prueba de sistema")
				.build();
	}

	private Reserva buildReserva(
			Usuario usuario,
			Habitacion habitacion,
			LocalDate fechaEntrada,
			LocalDate fechaSalida,
			EstadoReserva estado
	) {
		long nights = java.time.temporal.ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
		return Reserva.builder()
				.usuario(usuario)
				.habitacion(habitacion)
				.fechaEntrada(fechaEntrada)
				.fechaSalida(fechaSalida)
				.cantidadHuespedes(1)
				.precioTotal(habitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(nights)))
				.estado(estado)
				.build();
	}
}
