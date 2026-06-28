package com.Grupo1.GestionHoteleria_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Grupo1.GestionHoteleria_Backend.entity.RegisterLog;

public interface RegisterLogRepository extends JpaRepository<RegisterLog, Long> {
}