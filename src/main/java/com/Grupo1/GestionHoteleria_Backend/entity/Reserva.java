package com.Grupo1.GestionHoteleria_Backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "reservas",
		indexes = {
				@Index(name = "idx_reservas_habitacion_id", columnList = "habitacion_id"),
				@Index(name = "idx_reservas_usuario_id", columnList = "usuario_id"),
				@Index(name = "idx_reservas_fecha_entrada", columnList = "fecha_entrada"),
				@Index(name = "idx_reservas_fecha_salida", columnList = "fecha_salida")
		}
)
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "habitacion_id", nullable = false)
	private Habitacion habitacion;

	@Column(name = "fecha_entrada", nullable = false)
	private LocalDate fechaEntrada;

	@Column(name = "fecha_salida", nullable = false)
	private LocalDate fechaSalida;

	@Column(name = "cantidad_huespedes", nullable = false)
	private Integer cantidadHuespedes;

	@Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioTotal;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EstadoReserva estado = EstadoReserva.PENDIENTE;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		if (estado == null) {
			estado = EstadoReserva.PENDIENTE;
		}
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
