package com.Grupo1.GestionHoteleria_Backend.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;
import com.Grupo1.GestionHoteleria_Backend.repository.AreaComunRepository;

@Configuration
public class AreaComunDataInitializer {

	@Bean
	CommandLineRunner seedCommonAreas(AreaComunRepository areaComunRepository) {
		return args -> {
			if (areaComunRepository.count() > 0) {
				return;
			}

			areaComunRepository.saveAll(List.of(
					buildArea("Infinity Pool & Sun Deck",
							"Piscina panoramica con zona de descanso y atencion de bebidas.",
							24,
							"60.00",
							EstadoAreaComun.DISPONIBLE),
					buildArea("Wellness & Spa",
							"Cabinas privadas, sauna y ambiente terapeutico para sesiones de bienestar.",
							8,
							"85.00",
							EstadoAreaComun.DISPONIBLE),
					buildArea("High-Tech Gym",
							"Equipamiento moderno, zona cardiovascular y espacio funcional.",
							16,
							"25.00",
							EstadoAreaComun.DISPONIBLE),
					buildArea("The Azure Restaurant",
							"Salon gastronomico para cenas privadas y reuniones especiales.",
							40,
							"120.00",
							EstadoAreaComun.OCUPADA),
					buildArea("Private Lounge",
							"Lounge reservado con servicio de cafe, proyector y ambiente ejecutivo.",
							12,
							"75.00",
							EstadoAreaComun.DISPONIBLE)
			));
		};
	}

	private AreaComun buildArea(
			String nombre,
			String descripcion,
			Integer capacidadMaxima,
			String precioPorHora,
			EstadoAreaComun estado
	) {
		return AreaComun.builder()
				.nombre(nombre)
				.descripcion(descripcion)
				.capacidadMaxima(capacidadMaxima)
				.precioPorHora(new BigDecimal(precioPorHora))
				.estado(estado)
				.build();
	}
}
