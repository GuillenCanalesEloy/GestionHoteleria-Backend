# Sistema de Gestión Hotelera - Backend

## Descripción General

El backend del Sistema de Gestión Hotelera es una API REST desarrollada con Spring Boot, diseñada para gestionar los procesos principales de un hotel y brindar servicios tanto al portal de clientes como al panel administrativo.

**Grupo:** Grupo1
**Artifact:** GestionHoteleria-Backend
**Versión:** 0.0.1-SNAPSHOT

---

# Tecnologías Utilizadas

## Tecnologías Principales

* Java 21
* Spring Boot 4.0.6
* Maven

## Dependencias Implementadas

* Spring Boot Starter Data JPA (persistencia y acceso a datos mediante ORM)
* Spring Boot Starter Security (autenticación y autorización)
* MySQL Connector J (conexión con la base de datos MySQL)
* Lombok (reducción de código repetitivo)

## Dependencias por Implementar

* Spring Boot Starter Web (desarrollo de servicios REST)
* JJWT (gestión de tokens JWT)
* Spring Boot Starter Validation (validación de datos)
* SpringDoc OpenAPI (documentación mediante Swagger)

---

# Módulos del Sistema

## Autenticación

Encargado de la gestión de acceso al sistema mediante autenticación basada en JWT.

Funciones principales:

* Inicio de sesión
* Registro de usuarios
* Renovación de tokens
* Control de acceso según roles

Roles disponibles:

* ADMIN
* RECEPCIONISTA
* GERENTE
* CLIENTE

---

## Habitaciones

Permite administrar las habitaciones del hotel.

Funciones principales:

* Registro de habitaciones
* Actualización de información
* Eliminación de registros
* Consulta y búsqueda de habitaciones

Estados disponibles:

* DISPONIBLE
* OCUPADA
* MANTENIMIENTO

Filtros:

* Precio
* Tipo de habitación
* Disponibilidad

---

## Clientes

Gestiona la información de los clientes registrados en el sistema.

Funciones principales:

* Registro de clientes
* Actualización de datos
* Consulta de historial de reservas

---

## Reservas

Módulo encargado de la gestión de reservas realizadas por los clientes.

Funciones principales:

* Crear reservas
* Modificar reservas
* Cancelar reservas
* Registrar check-in y check-out

Estados disponibles:

* CONFIRMADA
* CANCELADA
* PENDIENTE

---

## Pagos

Administra los pagos asociados a las reservas.

Funciones principales:

* Registro de pagos
* Consulta de historial de pagos por reserva

---

## Empleados

Gestiona el personal interno del hotel.

Funciones principales:

* Registro de empleados
* Administración de usuarios internos
* Asignación y gestión de roles

---

## Reportes

Genera información útil para la toma de decisiones administrativas.

Reportes disponibles:

* Ingresos por período
* Tasa de ocupación
* Clientes frecuentes

---

# Estructura del Proyecto

```text
com.Grupo1.GestionHoteleria_Backend/
 ├── config/          # Configuraciones generales y seguridad
 ├── controller/      # Controladores REST
 ├── service/         # Lógica de negocio
 ├── repository/      # Acceso a datos mediante JPA
 ├── model/           # Entidades del sistema
 ├── dto/             # Objetos de transferencia de datos
 ├── exception/       # Manejo global de excepciones
 ├── security/        # Componentes de autenticación JWT
 └── GestionHoteleriaBackendApplication.java
```

---

# Seguridad

La seguridad del sistema estará basada en JWT (JSON Web Token).

Características principales:

* Rutas públicas para autenticación y consulta de habitaciones.
* Protección de recursos mediante token válido.
* Control de acceso según roles de usuario.
* Restricción de operaciones según permisos asignados.

---

# Base de Datos

* Motor de base de datos: MySQL
* Persistencia mediante Spring Data JPA

Tablas principales:

* usuarios
* habitaciones
* clientes
* reservas
* pagos
* empleados

---

# Estado Actual del Proyecto

Actualmente se cuenta con los siguientes avances:

* Proyecto inicializado con Spring Boot.
* Dependencias principales configuradas.
* Integración de JPA, Security, MySQL y Lombok.
* Repositorio GitHub organizado con 12 ramas de trabajo.

Pendiente de implementación:

* Definición de entidades JPA.
* Desarrollo de endpoints REST.
* Configuración completa de JWT.
* Conexión con la base de datos.
* Documentación de la API mediante Swagger.

---

# Próximas Actividades

Las siguientes tareas corresponden a la siguiente fase de desarrollo:

1. Configurar la conexión a la base de datos mediante application.properties.
2. Crear las entidades principales del sistema.
3. Implementar la autenticación y autorización con JWT.
4. Desarrollar los servicios y controladores de cada módulo.
5. Realizar pruebas funcionales.
6. Generar la documentación de la API con Swagger.


Como ves el readmi ahora
