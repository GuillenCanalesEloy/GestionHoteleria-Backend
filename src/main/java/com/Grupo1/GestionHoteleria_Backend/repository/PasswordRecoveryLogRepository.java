package com.Grupo1.GestionHoteleria_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Grupo1.GestionHoteleria_Backend.entity.PasswordRecoveryLog;

public interface PasswordRecoveryLogRepository extends JpaRepository<PasswordRecoveryLog, Long> {
}