package com.Grupo1.GestionHoteleria_Backend.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Grupo1.GestionHoteleria_Backend.dto.DashboardIngresosResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardMetricasResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardOcupacionResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.DashboardResumenResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.PageResponse;
import com.Grupo1.GestionHoteleria_Backend.dto.ReservaResponse;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoReserva;
import com.Grupo1.GestionHoteleria_Backend.service.DashboardService;
import com.Grupo1.GestionHoteleria_Backend.service.ReservaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;
	private final ReservaService reservaService;

	@GetMapping("/resumen")
	public ResponseEntity<DashboardResumenResponse> getResumen() {
		return ResponseEntity.ok(dashboardService.getResumen());
	}

	@GetMapping("/reservas")
	public ResponseEntity<PageResponse<ReservaResponse>> getReservas(
			@RequestParam(required = false) Long usuarioId,
			@RequestParam(required = false) Long habitacionId,
			@RequestParam(required = false) EstadoReserva estado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntradaDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntradaHasta,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "fechaEntrada") String sortBy,
			@RequestParam(defaultValue = "DESC") String direction
	) {
		return ResponseEntity.ok(reservaService.findAll(
				usuarioId,
				habitacionId,
				estado,
				fechaEntradaDesde,
				fechaEntradaHasta,
				page,
				size,
				sortBy,
				direction
		));
	}

	@GetMapping("/ocupacion")
	public ResponseEntity<DashboardOcupacionResponse> getOcupacion(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida
	) {
		return ResponseEntity.ok(dashboardService.getOcupacion(fechaEntrada, fechaSalida));
	}

	@GetMapping("/ingresos")
	public ResponseEntity<DashboardIngresosResponse> getIngresos(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
	) {
		return ResponseEntity.ok(dashboardService.getIngresos(fechaDesde, fechaHasta));
	}

	@GetMapping("/metricas")
	public ResponseEntity<DashboardMetricasResponse> getMetricas(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ingresosFechaDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ingresosFechaHasta,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ocupacionFechaEntrada,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ocupacionFechaSalida
	) {
		return ResponseEntity.ok(dashboardService.getMetricas(
				ingresosFechaDesde,
				ingresosFechaHasta,
				ocupacionFechaEntrada,
				ocupacionFechaSalida
		));
	}
}
