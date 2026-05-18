# Guia de desarrollo del backend

Esta guia organiza las tareas de configuracion base y seguridad del backend de Gestion Hoteleria. Esta pensada para integrarse con el frontend `GuillenCanalesEloy/GestionHoteleria-Frontend`, que espera una API bajo `/api`.

Endpoints iniciales recomendados:

```text
POST /api/auth/register
POST /api/auth/login
```

Endpoints previstos por el frontend:

```text
/api/auth
/api/habitaciones
/api/reservas
/api/pagos
/api/clientes
```

## Analisis del nuevo orden

Nuevo orden de tareas:

```text
1. KAN-29 Configurar arquitectura de paquetes
2. KAN-25 Configurar conexion MySQL
3. KAN-26 Crear base de datos hotelera
4. KAN-27 Configurar JPA/Hibernate
5. KAN-31 Crear entidad Usuario
6. KAN-34 Configurar Spring Security
7. KAN-30 Implementar sistema JWT
8. KAN-32 Crear endpoint Login
9. KAN-33 Crear endpoint Register
10. KAN-28 Configurar variables de entorno
```

Este orden es valido, pero con una observacion importante: `KAN-25` puede hacerse antes de `KAN-26` solo como configuracion de archivos. No se debe intentar levantar la aplicacion contra MySQL hasta crear la base de datos `hotelera`, porque la conexion fallara si la base aun no existe.

La parte mas delicada es que `KAN-34 Configurar Spring Security` aparece antes que `KAN-30 Implementar sistema JWT`. Es aceptable si en `KAN-34` se deja primero una configuracion base con rutas publicas, `PasswordEncoder`, `AuthenticationManager` y CORS. Luego, en `KAN-30`, se agrega el filtro JWT a esa configuracion.

Recomendacion practica:

```text
KAN-34 debe crear la estructura de seguridad base.
KAN-30 debe completar la seguridad agregando JWT.
```

## KAN-29 Configurar arquitectura de paquetes

Paquetes base del backend:

```text
src/main/java/com/Grupo1/GestionHoteleria_Backend/config/
src/main/java/com/Grupo1/GestionHoteleria_Backend/controller/
src/main/java/com/Grupo1/GestionHoteleria_Backend/service/
src/main/java/com/Grupo1/GestionHoteleria_Backend/repository/
src/main/java/com/Grupo1/GestionHoteleria_Backend/entity/
src/main/java/com/Grupo1/GestionHoteleria_Backend/dto/
src/main/java/com/Grupo1/GestionHoteleria_Backend/security/
src/main/java/com/Grupo1/GestionHoteleria_Backend/exception/
```

Uso recomendado:

```text
config      -> configuracion general, CORS, SecurityFilterChain
controller  -> endpoints REST
service     -> logica de negocio
repository  -> interfaces JpaRepository
entity      -> clases JPA
dto         -> requests y responses
security    -> JWT, filtros, UserDetails
exception   -> manejo global de errores
```

Resultado esperado:

```text
La aplicacion mantiene una arquitectura profesional por capas.
Cada archivo nuevo tiene un paquete claro donde vivir.
```

## KAN-25 Configurar conexion MySQL

Configurar `src/main/resources/application.properties` usando variables de entorno:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Ejemplo de `DB_URL`:

```text
jdbc:mysql://localhost:3306/hotelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

Nota: en este punto se configura la conexion, pero la prueba real de conexion debe hacerse despues de `KAN-26`.

## KAN-26 Crear base de datos hotelera

Crear la base de datos en MySQL:

```sql
CREATE DATABASE hotelera CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Durante desarrollo, se recomienda dejar que JPA/Hibernate cree o actualice las tablas automaticamente.

## KAN-27 Configurar JPA/Hibernate

Agregar configuracion JPA en `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Recomendacion:

```text
Desarrollo:  spring.jpa.hibernate.ddl-auto=update
Produccion:  spring.jpa.hibernate.ddl-auto=validate
```

Mas adelante, para produccion, conviene manejar cambios de base de datos con Flyway o Liquibase.

## KAN-31 Crear entidad Usuario

Crear `entity/Usuario.java` con campos minimos:

```text
id
nombre
email
password
rol
createdAt
updatedAt
```

Recomendaciones:

```text
email debe ser unico.
password debe guardarse encriptado con BCrypt.
rol puede iniciar como CLIENTE.
createdAt y updatedAt pueden manejarse con @PrePersist y @PreUpdate.
```

Tambien crear `entity/Rol.java` como enum:

```text
ADMIN
CLIENTE
```

Crear tambien `repository/UsuarioRepository.java`:

```text
findByEmail(String email)
existsByEmail(String email)
```

## KAN-34 Configurar Spring Security

Crear `config/SecurityConfig.java`.

En esta etapa, como JWT todavia no esta implementado, configurar la base de seguridad:

```text
SecurityFilterChain
AuthenticationManager
AuthenticationProvider
PasswordEncoder
CorsConfigurationSource
```

Rutas publicas:

```text
POST /api/auth/login
POST /api/auth/register
GET /api/habitaciones/**
```

Rutas protegidas:

```text
/api/reservas/**
/api/pagos/**
/api/clientes/**
```

Configurar seguridad stateless:

```text
SessionCreationPolicy.STATELESS
```

Configurar CORS usando `FRONTEND_URL`:

```text
http://localhost:5173
```

Nota: el filtro JWT se conecta en `KAN-30`, cuando ya exista `JwtAuthenticationFilter`.

## KAN-30 Implementar sistema JWT

Crear en `security/`:

```text
JwtService.java
JwtAuthenticationFilter.java
CustomUserDetailsService.java
```

Responsabilidades:

```text
JwtService.java
- Generar tokens JWT.
- Validar tokens JWT.
- Extraer email o username del token.
- Leer secret y expiracion desde variables de entorno.

JwtAuthenticationFilter.java
- Leer el header Authorization.
- Validar tokens con formato Bearer <token>.
- Cargar usuario desde CustomUserDetailsService.
- Registrar autenticacion en el SecurityContext.

CustomUserDetailsService.java
- Buscar usuario por email.
- Adaptar Usuario al modelo UserDetails de Spring Security.
```

Header esperado:

```text
Authorization: Bearer <token>
```

## KAN-32 Crear endpoint Login

Crear `dto/LoginRequest.java`:

```text
email
password
```

Crear `dto/AuthResponse.java`:

```text
token
type
email
nombre
rol
```

Endpoint:

```text
POST /api/auth/login
```

Flujo:

```text
1. Recibir email y password.
2. Validar datos con Bean Validation.
3. Autenticar con AuthenticationManager.
4. Buscar usuario en base de datos.
5. Generar JWT.
6. Devolver token y datos basicos del usuario.
```

Respuesta sugerida:

```json
{
  "token": "jwt_generado",
  "type": "Bearer",
  "email": "usuario@correo.com",
  "nombre": "Usuario Demo",
  "rol": "CLIENTE"
}
```

## KAN-33 Crear endpoint Register

Crear `dto/RegisterRequest.java`:

```text
nombre
email
password
```

Endpoint:

```text
POST /api/auth/register
```

Flujo:

```text
1. Recibir nombre, email y password.
2. Validar datos con Bean Validation.
3. Verificar que el email no exista.
4. Encriptar password con BCryptPasswordEncoder.
5. Guardar usuario con rol CLIENTE.
6. Devolver respuesta de registro o token JWT.
```

Respuesta sugerida si se inicia sesion automaticamente:

```json
{
  "token": "jwt_generado",
  "type": "Bearer",
  "email": "nuevo@correo.com",
  "nombre": "Nuevo Usuario",
  "rol": "CLIENTE"
}
```

## KAN-28 Configurar variables de entorno

Variables minimas recomendadas:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MS
FRONTEND_URL
```

Ejemplo local:

```text
DB_URL=jdbc:mysql://localhost:3306/hotelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=tu_password
JWT_SECRET=clave_super_segura_de_minimo_32_caracteres
JWT_EXPIRATION_MS=86400000
FRONTEND_URL=http://localhost:5173
```

Importante: `JWT_SECRET` debe ser largo. Usar minimo 32 caracteres.

Aunque Jira lo deje al final, estas variables se pueden definir desde el inicio en `.env`, variables del sistema o configuracion del IDE. Lo importante es validarlas al final de la configuracion.

## Orden recomendado de implementacion

1. `KAN-29` Crear paquetes base.
2. `KAN-25` Configurar propiedades de conexion MySQL.
3. `KAN-26` Crear base de datos `hotelera`.
4. `KAN-27` Configurar JPA/Hibernate.
5. `KAN-31` Crear `Usuario`, `Rol` y `UsuarioRepository`.
6. `KAN-34` Configurar Spring Security base.
7. `KAN-30` Implementar `JwtService`, `JwtAuthenticationFilter` y `CustomUserDetailsService`.
8. `KAN-32` Crear endpoint `POST /api/auth/login`.
9. `KAN-33` Crear endpoint `POST /api/auth/register`.
10. `KAN-28` Validar variables de entorno finales.
11. Probar `POST /api/auth/register`.
12. Probar `POST /api/auth/login`.
13. Probar acceso a una ruta protegida con `Authorization: Bearer <token>`.
14. Conectar el frontend usando `VITE_API_URL`.

## Dependencias necesarias

Dependencias ya contempladas en `pom.xml`:

```text
Spring Web
Spring Data JPA
MySQL Driver
Lombok
Validation
Spring Security
Hibernate
JJWT
```

Para JWT se agregaron:

```text
io.jsonwebtoken:jjwt-api
io.jsonwebtoken:jjwt-impl
io.jsonwebtoken:jjwt-jackson
```

## Pruebas manuales sugeridas

Registro:

```http
POST /api/auth/register
Content-Type: application/json

{
  "nombre": "Usuario Demo",
  "email": "demo@correo.com",
  "password": "12345678"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "demo@correo.com",
  "password": "12345678"
}
```

Ruta protegida:

```http
GET /api/reservas
Authorization: Bearer <token>
```

## Criterios de aceptacion

```text
La aplicacion levanta sin errores.
La conexion MySQL funciona.
La tabla usuarios se crea correctamente.
Register crea usuarios con password encriptado.
Login devuelve JWT valido.
Rutas publicas funcionan sin token.
Rutas protegidas rechazan peticiones sin token.
Rutas protegidas aceptan peticiones con token valido.
CORS permite llamadas desde el frontend.
```
