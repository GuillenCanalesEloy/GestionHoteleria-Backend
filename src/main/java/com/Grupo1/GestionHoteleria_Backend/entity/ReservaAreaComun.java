package com.Grupo1.GestionHoteleria_Backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
		name = "reservas_areas_comunes",
		indexes = {
				@Index(name = "idx_rac_usuario_id", columnList = "usuario_id"),
				@Index(name = "idx_rac_area_comun_id", columnList = "area_comun_id"),
				@Index(name = "idx_rac_fecha", columnList = "fecha"),
				@Index(name = "idx_rac_estado", columnList = "estado"),
				@Index(name = "idx_rac_area_fecha_estado", columnList = "area_comun_id,fecha,estado"),
				@Index(name = "idx_rac_usuario_fecha", columnList = "usuario_id,fecha")
		}
)
public class ReservaAreaComun {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "area_comun_id", nullable = false)
	private AreaComun areaComun;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;

	@Column(name = "hora_inicio", nullable = false)
	private LocalTime horaInicio;

	@Column(name = "hora_fin", nullable = false)
	private LocalTime horaFin;

	@Column(name = "duracion_minutos", nullable = false)
	private Integer duracionMinutos;

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
