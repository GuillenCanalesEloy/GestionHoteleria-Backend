# 📄 BACKEND - Sistema de Gestión Hotelera

## 📌 Descripción general

El backend del sistema de gestión hotelera es una **API REST** desarrollada con **Spring Boot**, diseñada para ser consumida por dos frontends: el portal de clientes y el panel administrativo.

Grupo: **Grupo1**
Artifact: `GestionHoteleria-Backend`
Versión: `0.0.1-SNAPSHOT`

---

# 🧩 Tecnologías del Backend

## Base principal

* Java 21
* Spring Boot 4.0.6
* Maven

## Dependencias actuales

* `spring-boot-starter-data-jpa` (acceso a base de datos con ORM)
* `spring-boot-starter-security` (autenticación y autorización)
* `mysql-connector-j` (base de datos MySQL)
* `lombok` (reducción de boilerplate)

## Por agregar

* `spring-boot-starter-web` (endpoints REST)
* `jjwt` (tokens JWT)
* `spring-boot-starter-validation` (validaciones)
* `springdoc-openapi` (documentación Swagger)

---

# 🔹 Módulos del Backend

## 1. Autenticación (`/api/auth`)
* Login con JWT
* Registro de usuarios
* Refresh token
* Control por roles: `ADMIN`, `RECEPCIONISTA`, `GERENTE`, `CLIENTE`

## 2. Habitaciones (`/api/habitaciones`)
* CRUD completo
* Estados: `DISPONIBLE`, `OCUPADA`, `MANTENIMIENTO`
* Filtros: precio, tipo, disponibilidad

## 3. Clientes (`/api/clientes`)
* Registro y edición
* Historial de reservas por cliente

## 4. Reservas (`/api/reservas`)
* Crear, editar, cancelar
* Check-in / Check-out
* Estado: `CONFIRMADA`, `CANCELADA`, `PENDIENTE`

## 5. Pagos (`/api/pagos`)
* Registrar pagos
* Historial por reserva

## 6. Empleados (`/api/empleados`)
* Gestión de usuarios internos
* Asignación de roles

## 7. Reportes (`/api/reportes`)
* Ingresos por período
* Tasa de ocupación
* Clientes frecuentes

---

# 🔹 Estructura de paquetes

```
com.Grupo1.GestionHoteleria_Backend/
 ├── config/          # SecurityConfig, CorsConfig, JwtConfig
 ├── controller/      # Endpoints REST
 ├── service/         # Lógica de negocio
 ├── repository/      # Interfaces JPA
 ├── model/           # Entidades JPA
 ├── dto/             # Objetos de transferencia
 ├── exception/       # Manejo de errores global
 ├── security/        # JWT filter, UserDetailsService
 └── GestionHoteleriaBackendApplication.java
```

---

# 🔹 Seguridad

* Autenticación con **JWT**
* Rutas públicas: `/api/auth/**`, `/api/habitaciones` (GET)
* Rutas protegidas: todo lo demás requiere token válido
* Control por roles en cada endpoint

---

# 🔹 Base de datos

* Motor: **MySQL**
* Acceso vía **Spring Data JPA**
* Tablas principales: `usuarios`, `habitaciones`, `clientes`, `reservas`, `pagos`, `empleados`

---

# 🔹 Estado actual del backend

✔ Proyecto inicializado con Spring Boot\
✔ Dependencias base configuradas (JPA, Security, MySQL, Lombok)\
✔ 12 ramas creadas en GitHub\
⬜ Entidades por definir\
⬜ Endpoints por implementar\
⬜ JWT por configurar\
⬜ Base de datos por conectar

---

# 🚀 Siguiente paso

* Configurar `application.properties` con la BD
* Crear entidades JPA (`Habitacion`, `Reserva`, `Cliente`, etc.)
* Implementar seguridad JWT
* Desarrollar controllers y services por módulo
* Documentar con Swagger
