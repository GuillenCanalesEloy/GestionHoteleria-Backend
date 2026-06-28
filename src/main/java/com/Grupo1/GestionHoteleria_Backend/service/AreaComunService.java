package com.Grupo1.GestionHoteleria_Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Grupo1.GestionHoteleria_Backend.entity.AreaComun;
import com.Grupo1.GestionHoteleria_Backend.entity.EstadoAreaComun;
import com.Grupo1.GestionHoteleria_Backend.exception.ResourceNotFoundException;
import com.Grupo1.GestionHoteleria_Backend.repository.AreaComunRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AreaComunService {

	private final AreaComunRepository areaComunRepository;

	@Transactional(readOnly = true)
	public List<AreaComun> findAll() {
		return areaComunRepository.findAll();
	}

	@Transactional(readOnly = true)
	public AreaComun findById(Long id) {
		return areaComunRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Área común no encontrada con id: " + id));
	}

	@Transactional(readOnly = true)
	public List<AreaComun> findAllDisponibles() {
		return areaComunRepository.findAllDisponibles();
	}

	@Transactional(readOnly = true)
	public List<AreaComun> findByEstado(EstadoAreaComun estado) {
		return areaComunRepository.findByEstado(estado);
	}

	@Transactional
	public AreaComun create(AreaComun areaComun) {
		if (areaComunRepository.findByNombre(areaComun.getNombre()).isPresent()) {
			throw new IllegalArgumentException("Ya existe un área común con el nombre: " + areaComun.getNombre());
		}
		return areaComunRepository.save(areaComun);
	}

	@Transactional
	public AreaComun update(Long id, AreaComun areaComun) {
		AreaComun existing = findById(id);
		existing.setNombre(areaComun.getNombre());
		existing.setDescripcion(areaComun.getDescripcion());
		existing.setCapacidadMaxima(areaComun.getCapacidadMaxima());
		existing.setPrecioPorHora(areaComun.getPrecioPorHora());
		existing.setEstado(areaComun.getEstado());
		return areaComunRepository.save(existing);
	}

	@Transactional
	public void delete(Long id) {
		AreaComun areaComun = findById(id);
		areaComunRepository.delete(areaComun);
	}

	@Transactional
	public AreaComun updateEstado(Long id, EstadoAreaComun nuevoEstado) {
		AreaComun areaComun = findById(id);
		areaComun.setEstado(nuevoEstado);
		return areaComunRepository.save(areaComun);
	}
}
