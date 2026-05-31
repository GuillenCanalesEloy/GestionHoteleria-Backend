package com.Grupo1.GestionHoteleria_Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.entity.ReservaAreaComun;

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
public class ReservaAreaComunResponse {

	private Long id;
	private Long usuarioId;
	private String usuarioNombre;
	private Long areaComunId;
	private String areaComunNombre;
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;
	private Integer duracionMinutos;
	private BigDecimal precioTotal;
	private EstadoReserva estado;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ReservaAreaComunResponse fromEntity(ReservaAreaComun entity) {
		return ReservaAreaComunResponse.builder()
				.id(entity.getId())
				.usuarioId(entity.getUsuario().getId())
				.usuarioNombre(entity.getUsuario().getNombre())
				.areaComunId(entity.getAreaComun().getId())
				.areaComunNombre(entity.getAreaComun().getNombre())
				.fecha(entity.getFecha())
				.horaInicio(entity.getHoraInicio())
				.horaFin(entity.getHoraFin())
				.duracionMinutos(entity.getDuracionMinutos())
				.precioTotal(entity.getPrecioTotal())
				.estado(entity.getEstado())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}
}
