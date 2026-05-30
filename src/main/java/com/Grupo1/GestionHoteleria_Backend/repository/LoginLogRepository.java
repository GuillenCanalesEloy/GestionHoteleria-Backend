package com.Grupo1.GestionHoteleria_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Grupo1.GestionHoteleria_Backend.entity.LoginLog;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
}