# Sistema de Gestión Hotelera - Backend

## Descripción

Backend desarrollado con Spring Boot para la gestión integral de un sistema hotelero. Proporciona una API REST segura para la administración de usuarios, habitaciones, reservas y áreas comunes, además de funcionalidades de autenticación mediante JWT y generación de métricas para paneles administrativos.

**Grupo:** Grupo1
**Artifact:** GestionHoteleria-Backend
**Versión:** 1.0.0-SNAPSHOT

---

## Tecnologías Utilizadas

| Tecnología        | Descripción                    |
| ----------------- | ------------------------------ |
| Java 21           | Lenguaje principal             |
| Spring Boot 4.0.6 | Framework Backend              |
| Spring Security   | Seguridad y autenticación      |
| Spring Data JPA   | Persistencia de datos          |
| PostgreSQL        | Base de datos                  |
| JWT               | Autenticación basada en tokens |
| Swagger/OpenAPI   | Documentación de la API        |
| Lombok            | Reducción de código repetitivo |
| JUnit 5           | Pruebas unitarias              |

---

## Funcionalidades

### Autenticación

* Registro de usuarios.
* Inicio de sesión.
* Autenticación mediante JWT.
* Control de acceso basado en roles.

### Gestión de Habitaciones

* Registro de habitaciones.
* Actualización de información.
* Consulta de habitaciones.
* Control de disponibilidad.

Estados:

* DISPONIBLE
* OCUPADA
* MANTENIMIENTO

### Gestión de Reservas

* Crear reservas.
* Modificar reservas.
* Cancelar reservas.
* Consultar historial de reservas.

Estados:

* PENDIENTE
* CONFIRMADA
* CANCELADA

### Gestión de Áreas Comunes

* Registro de áreas comunes.
* Administración de disponibilidad.
* Reserva de áreas comunes.

### Dashboard Administrativo

* Métricas generales.
* Ingresos.
* Ocupación hotelera.
* Estado de reservas.
* Estadísticas por tipo de habitación.

### Auditoría y Logs

* Registro de inicios de sesión.
* Registro de usuarios.
* Recuperación de contraseñas.

---

## Roles del Sistema

### Administrador

Tiene acceso completo a todas las funcionalidades del sistema, incluyendo la gestión de usuarios, habitaciones, reservas, áreas comunes y reportes.

### Cliente

Puede registrarse, iniciar sesión, consultar habitaciones disponibles, realizar reservas y visualizar su historial de reservas.

---

## Estructura del Proyecto

```text
src/main/java/com/Grupo1/GestionHoteleria_Backend

├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── GestionHoteleriaBackendApplication
```

---

## Arquitectura del Sistema

El backend sigue una arquitectura por capas que permite separar responsabilidades y facilitar el mantenimiento del código.

* **Controller:** expone los endpoints REST y recibe las solicitudes del cliente.
* **Service:** contiene la lógica de negocio de cada módulo.
* **Repository:** gestiona el acceso a la base de datos mediante JPA.
* **Entity:** representa las entidades y tablas de la base de datos.
* **DTO:** permite transferir información entre las diferentes capas.
* **Security:** administra la autenticación y autorización mediante JWT.
* **Exception:** centraliza el manejo de errores y excepciones.

---

## Organización del Repositorio

Para facilitar el trabajo colaborativo se implementó una estrategia de desarrollo basada en ramas de GitHub.

### Ramas principales

* main
* dev

### Ramas de desarrollo

* f-AreasComunes
* f-CRUDs
* f-HabBack
* f-confiback
* f-configSQL
* f-configpacks
* f-endpoints
* f-entidades
* f-roles
* f-swagger
* f-tests
* f-validaciones
* feature/migracion-db

Esta organización permitió desarrollar funcionalidades de forma independiente y posteriormente integrarlas al proyecto principal de manera controlada.

---

## Estado Actual del Proyecto

### Funcionalidades implementadas

* Configuración inicial del proyecto con Spring Boot.
* Integración con PostgreSQL.
* Implementación de Spring Security.
* Autenticación mediante JWT.
* Desarrollo de entidades principales.
* Implementación de endpoints REST.
* Configuración de Swagger para documentación.
* Desarrollo de pruebas unitarias e integración.
* Gestión de habitaciones, reservas y áreas comunes.
* Implementación de control de acceso por roles.

---

## Objetivo del Proyecto

Desarrollar un sistema de gestión hotelera que permita administrar de manera eficiente habitaciones, reservas, usuarios y áreas comunes, brindando una plataforma segura, escalable y fácil de mantener mediante una arquitectura basada en API REST.
