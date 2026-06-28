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
import jakarta.persistence.Index;
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
		name = "areas_comunes",
		indexes = {
				@Index(name = "idx_areas_comunes_nombre", columnList = "nombre"),
				@Index(name = "idx_areas_comunes_estado", columnList = "estado")
		}
)
public class AreaComun {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, unique = true, length = 100)
	private String nombre;

	@Column(name = "descripcion", length = 500)
	private String descripcion;

	@Column(name = "capacidad_maxima", nullable = false)
	private Integer capacidadMaxima;

	@Column(name = "precio_por_hora", nullable = false, precision = 10, scale = 2)
	private BigDecimal precioPorHora;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EstadoAreaComun estado = EstadoAreaComun.DISPONIBLE;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		if (estado == null) {
			estado = EstadoAreaComun.DISPONIBLE;
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
