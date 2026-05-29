package com.Grupo1.GestionHoteleria_Backend.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DatabaseHealthController {

    private final DataSource dataSource;

    @GetMapping("/api/health/database")
    public ResponseEntity<String> checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return ResponseEntity.ok("Conexion a base de datos exitosa: " + connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error de conexion a base de datos: " + e.getMessage());
        }
    }
}