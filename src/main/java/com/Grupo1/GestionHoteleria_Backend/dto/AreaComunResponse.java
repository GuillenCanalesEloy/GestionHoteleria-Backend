package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaComunResponse {

	private Long id;
	private String nombre;
	private String descripcion;
	private Integer capacidadMaxima;
	private BigDecimal precioPorHora;
	private EstadoAreaComun estado;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static AreaComunResponse fromEntity(AreaComun entity) {
		return AreaComunResponse.builder()
				.id(entity.getId())
				.nombre(entity.getNombre())
				.descripcion(entity.getDescripcion())
				.capacidadMaxima(entity.getCapacidadMaxima())
				.precioPorHora(entity.getPrecioPorHora())
				.estado(entity.getEstado())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}
}
