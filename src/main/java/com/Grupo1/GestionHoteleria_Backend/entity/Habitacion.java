package com.Grupo1.GestionHoteleria_Backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
		name = "habitaciones",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_habitaciones_numero", columnNames = "numero")
		}
)
public class Habitacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String numero;

	@Column(nullable = false)
	private Integer piso;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoHabitacion tipo;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;

	@Column(nullable = false)
	private Integer capacidad;

	@Column(name = "precio_por_noche", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioPorNoche;

	@Column(length = 500)
	private String descripcion;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		if (estado == null) {
			estado = EstadoHabitacion.DISPONIBLE;
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

	public boolean isDisponible() {
		return EstadoHabitacion.DISPONIBLE.equals(estado);
	}
}
