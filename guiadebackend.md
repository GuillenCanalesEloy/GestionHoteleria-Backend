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
2. KAN-26 Crear base de datos HotelDB
3. KAN-25 Configurar conexion MySQL
4. KAN-27 Configurar JPA/Hibernate
5. KAN-31 Crear entidad Usuario
6. KAN-34 Configurar Spring Security
7. KAN-30 Implementar sistema JWT
8. KAN-32 Crear endpoint Login
9. KAN-33 Crear endpoint Register
10. KAN-28 Configurar variables de entorno
```

Este orden es correcto: primero se define la arquitectura de paquetes, luego se crea la base de datos `HotelDB` y despues se configura la conexion MySQL del backend apuntando a esa base. Asi se evita que Spring intente conectarse a una base que todavia no existe.

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

## KAN-26 Crear base de datos HotelDB

Crear la base de datos en MySQL:

```sql
CREATE DATABASE HotelDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Verificar que exista:

```sql
SHOW DATABASES;
```

Durante desarrollo, se recomienda dejar que JPA/Hibernate cree o actualice las tablas automaticamente.

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
jdbc:mysql://localhost:3306/HotelDB?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

Nota: la conexion debe probarse despues de crear `HotelDB`.

## KAN-27 Configurar JPA/Hibernate

Agregar configuracion JPA en `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

Recomendacion:

```text
Desarrollo:  spring.jpa.hibernate.ddl-auto=update
Produccion:  spring.jpa.hibernate.ddl-auto=validate
```

Detalles importantes:

```text
open-in-view=false evita consultas tardias desde la capa web.
Hibernate detecta automaticamente el dialecto de MySQL desde la conexion.
hibernate.jdbc.time_zone=UTC mantiene fechas consistentes entre Java y MySQL.
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
Usar columnas explicitas created_at y updated_at para mantener consistencia en MySQL.
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

Resultado esperado:

```text
La entidad Usuario queda mapeada a la tabla usuarios.
El email queda protegido con una restriccion unique.
El rol por defecto para nuevos registros es CLIENTE.
El repositorio permite buscar y validar usuarios por email.
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

Resultado esperado:

```text
La API trabaja sin sesiones de servidor usando SessionCreationPolicy.STATELESS.
POST /api/auth/login y POST /api/auth/register quedan publicos.
GET /api/habitaciones/** queda publico para el catalogo del frontend.
El resto de endpoints requiere autenticacion.
CORS permite llamadas desde FRONTEND_URL e incluye el header Authorization.
El filtro JWT se ejecuta antes de UsernamePasswordAuthenticationFilter.
```

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

Resultado esperado:

```text
JwtService genera tokens firmados con el email como subject.
JwtService valida tokens validos y rechaza tokens malformados o de otro usuario.
JwtAuthenticationFilter autentica peticiones con header Bearer valido.
JwtAuthenticationFilter deja la peticion sin autenticar si el token es invalido o el usuario no existe.
CustomUserDetailsService carga usuarios desde UsuarioRepository usando el email.
```

Pruebas recomendadas:

```text
Generar token y extraer email.
Validar token para el mismo usuario.
Rechazar token para un usuario diferente.
Rechazar token malformado.
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

Resultado esperado:

```text
El endpoint POST /api/auth/login recibe email y password.
El request se valida con Bean Validation.
AuthService autentica las credenciales usando AuthenticationManager.
Si las credenciales son correctas, se genera un JWT.
La respuesta incluye token, type, email, nombre y rol.
Si las credenciales son incorrectas, se responde 401.
```

Pruebas recomendadas:

```text
AuthController debe responder 200 con AuthResponse en login correcto.
AuthService debe invocar AuthenticationManager y generar JWT.
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

Resultado esperado:

```text
El endpoint POST /api/auth/register recibe nombre, email y password.
El request se valida con Bean Validation.
AuthService verifica que el email no exista.
La password se guarda encriptada con BCryptPasswordEncoder.
El usuario se guarda con rol CLIENTE.
La respuesta devuelve token JWT y datos basicos del usuario.
Si el email ya existe, se responde 409.
```

Pruebas recomendadas:

```text
AuthController debe responder 201 con AuthResponse en registro correcto.
AuthService debe encriptar password y guardar usuario con rol CLIENTE.
AuthService debe rechazar emails duplicados.
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

Variables opcionales para desarrollo:

```text
JPA_DDL_AUTO
JPA_SHOW_SQL
```

Ejemplo local basado en `.env.example`:

```text
DB_URL=jdbc:mysql://localhost:3306/HotelDB?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=admin
JWT_SECRET=clave_super_segura_de_minimo_32_caracteres
JWT_EXPIRATION_MS=86400000
FRONTEND_URL=http://localhost:5173
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
```

Importante: `JWT_SECRET` debe ser largo. Usar minimo 32 caracteres.

El archivo `.env.example` se versiona como plantilla. El archivo `.env` real no se debe subir al repositorio porque puede contener credenciales locales.

En PowerShell se pueden definir variables para una ejecucion local asi:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/HotelDB?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="admin"
$env:JWT_SECRET="clave_super_segura_de_minimo_32_caracteres"
$env:JWT_EXPIRATION_MS="86400000"
$env:FRONTEND_URL="http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

Tambien se pueden configurar en el IDE dentro de la configuracion de ejecucion de Spring Boot.

Resultado esperado:

```text
application.properties lee configuracion desde variables de entorno.
.env.example documenta las variables necesarias.
.env queda ignorado por Git.
No se versionan credenciales reales como password o JWT_SECRET.
```

## Orden recomendado de implementacion

1. `KAN-29` Crear paquetes base.
2. `KAN-26` Crear base de datos `HotelDB`.
3. `KAN-25` Configurar propiedades de conexion MySQL.
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

## Orden recomendado para las siguientes tareas

Los KAN desarrollados hasta ahora llegan a `KAN-46` CRUD de habitaciones. El siguiente bloque debe cerrar habitaciones a nivel de validaciones, errores, endpoints, paginacion y pruebas; despues debe abrir reservas y dashboard con tareas propias para no esconder trabajo grande dentro de testing u optimizacion.

### Bloque completado: usuarios y habitaciones base

1. `KAN-36` Implementar roles `ADMIN`/`CLIENTE`.
2. `KAN-37` Proteger endpoints por rol.
3. `KAN-38` "Crear DTOs de Usuario."
4. `KAN-39` CRUD de Usuario.
5. `KAN-40` Crear endpoints para usuario.
6. `KAN-41` Testing seguridad usuarios/roles.
7. `KAN-42` Crear tipos de habitacion.
8. `KAN-43` Crear entidad Habitacion.
9. `KAN-44` Crear repositorios y servicios de habitaciones.
10. `KAN-46` CRUD de habitaciones.

### Nuevo bloque en Jira: habitaciones, reservas y dashboard

1. `KAN-47` Validaciones backend habitaciones.
2. `KAN-48` Manejo de errores `NotFound`.
3. `KAN-49` Crear endpoints de habitaciones.
4. `KAN-50` Paginacion y ordenamiento.
5. `KAN-51` Implementar filtros simples de habitaciones.
6. `KAN-52` Testing CRUD habitaciones.
7. `KAN-53` Crear entidad Reserva y enum EstadoReserva.
8. `KAN-54` Crear repositorio, DTOs y servicio de reservas.
9. `KAN-55` Crear endpoints de reservas.
10. `KAN-56` Validar disponibilidad por rango de fechas.
11. `KAN-58` Evitar reservas duplicadas o solapadas.
12. `KAN-59` Definir permisos de reservas por rol.
13. `KAN-60` Testing sistema reservas.
14. `KAN-61` Crear endpoints de dashboard administrativo.
15. `KAN-62` Crear metricas y reportes basicos.
16. `KAN-63` Documentar contrato de API de habitaciones y reservas.
17. `KAN-64` Optimizacion consultas SQL.
18. `KAN-65` Correccion errores finales sprint.

Nota sobre `KAN-57`:

```text
KAN-57 fue eliminado del tablero.
El orden continua correctamente desde KAN-56 a KAN-58.
No crear una tarea nueva con ese numero salvo que Jira vuelva a reservarlo.
```

### Alcance recomendado por tarea

`KAN-47` Validaciones backend habitaciones:

```text
Validar numero obligatorio y unico.
Validar piso positivo.
Validar capacidad mayor que cero.
Validar precioPorNoche mayor que cero.
Validar tipo y estado con enums permitidos.
Validar descripcion con longitud maxima razonable.
Responder 400 con mensajes claros cuando falle Bean Validation.
```

`KAN-48` Manejo de errores `NotFound`:

```text
Centralizar excepciones en GlobalExceptionHandler.
Responder 404 cuando no exista una habitacion, usuario o reserva.
Mantener una estructura estable de error: timestamp, status, error, message, path.
No exponer stack traces ni mensajes internos.
```

`KAN-49` Crear endpoints de habitaciones:

```text
GET /api/habitaciones
GET /api/habitaciones/{id}
POST /api/habitaciones
PUT /api/habitaciones/{id}
PATCH /api/habitaciones/{id}
DELETE /api/habitaciones/{id}
```

Reglas:

```text
GET puede ser publico para catalogo.
POST, PUT, PATCH y DELETE deben requerir rol ADMIN.
Las respuestas deben usar DTOs, no entidades JPA directas.
```

`KAN-50` Paginacion y ordenamiento:

```text
Agregar parametros page, size, sortBy y direction en GET /api/habitaciones.
Usar Pageable/PageRequest de Spring Data.
Devolver metadata de paginacion: page, size, totalElements, totalPages.
Limitar size maximo para evitar consultas pesadas.
Ordenar por campos permitidos: id, numero, piso, tipo, estado, capacidad, precioPorNoche.
```

`KAN-51` Implementar filtros simples de habitaciones:

```text
Filtrar por tipo.
Filtrar por estado.
Filtrar por capacidad minima.
Filtrar por precio minimo.
Filtrar por precio maximo.
Permitir combinar filtros con paginacion y ordenamiento.
```

Nota:

```text
No incluir disponibilidad real por fechas en KAN-51.
Esa regla depende de Reserva y debe quedar en KAN-56.
```

`KAN-52` Testing CRUD habitaciones:

```text
Tests unitarios de HabitacionService.
Tests de HabitacionController con MockMvc.
Tests de validacion 400.
Tests de NotFound 404.
Tests de seguridad: GET publico y escritura solo ADMIN.
Tests de paginacion, ordenamiento y filtros.
```

`KAN-53` Crear entidad Reserva y enum EstadoReserva:

```text
Crear Reserva con relacion a Usuario y Habitacion.
Definir fechaEntrada y fechaSalida.
Definir cantidadHuespedes, precioTotal y estado.
Crear EstadoReserva con valores iniciales: PENDIENTE, CONFIRMADA, CANCELADA, FINALIZADA.
Agregar timestamps createdAt y updatedAt.
Agregar indices para habitacion_id, usuario_id, fecha_entrada y fecha_salida.
```

`KAN-54` Crear repositorio, DTOs y servicio de reservas:

```text
Crear ReservaRepository.
Crear CreateReservaRequest, UpdateReservaRequest, ReservaResponse y filtros.
Crear ReservaService.
Calcular precioTotal segun noches y precioPorNoche.
Validar que fechaSalida sea posterior a fechaEntrada.
Validar que cantidadHuespedes no supere capacidad de la habitacion.
```

`KAN-55` Crear endpoints de reservas:

```text
GET /api/reservas
GET /api/reservas/{id}
POST /api/reservas
PATCH /api/reservas/{id}/estado
DELETE /api/reservas/{id}
```

Reglas:

```text
CLIENTE puede crear reservas y consultar sus propias reservas.
ADMIN puede consultar todas las reservas y cambiar estados.
Las respuestas deben usar DTOs, no entidades JPA directas.
```

`KAN-56` Validar disponibilidad por rango de fechas:

```text
Recibir fechaEntrada y fechaSalida.
Buscar reservas existentes de la misma habitacion.
Excluir reservas CANCELADAS.
Rechazar solapes cuando una reserva existente cruza el rango solicitado.
Exponer filtro de habitaciones disponibles por rango de fechas.
```

`KAN-58` Evitar reservas duplicadas o solapadas:

```text
Impedir dos reservas activas para la misma habitacion en fechas cruzadas.
Agregar test unitario para solapes exactos, parciales e internos.
Considerar restriccion transaccional para evitar doble reserva en solicitudes simultaneas.
```

`KAN-59` Definir permisos de reservas por rol:

```text
CLIENTE solo puede ver, crear o cancelar sus propias reservas.
ADMIN puede ver, modificar estado y cancelar cualquier reserva.
Bloquear cambios no autorizados con 403.
Mantener /api/reservas/** protegido por JWT.
```

`KAN-60` Testing sistema reservas:

```text
Debe ejecutarse despues de implementar entidad, servicio y endpoints de reservas.
Cubrir creacion de reserva, validacion de fechas, disponibilidad y reservas solapadas.
Cubrir permisos: CLIENTE puede crear/ver sus reservas, ADMIN puede consultar todas.
```

`KAN-61` Crear endpoints de dashboard administrativo:

```text
GET /api/dashboard/resumen
GET /api/dashboard/reservas
GET /api/dashboard/ocupacion
GET /api/dashboard/ingresos
Proteger todos los endpoints con rol ADMIN.
```

`KAN-62` Crear metricas y reportes basicos:

```text
Total de habitaciones.
Habitaciones disponibles, ocupadas y en mantenimiento.
Reservas activas por estado.
Ingresos por rango de fechas.
Ocupacion por tipo de habitacion.
```

`KAN-63` Documentar contrato de API de habitaciones y reservas:

```text
Documentar parametros aceptados.
Documentar ejemplos request/response.
Documentar codigos HTTP esperados.
Documentar estructura de errores.
Actualizar notas para integracion con frontend.
```

`KAN-64` Optimizacion consultas SQL:

```text
Revisar consultas de habitaciones, reservas y dashboard.
Agregar indices necesarios en numero, estado, tipo, habitacion_id, usuario_id, fecha_entrada y fecha_salida.
Evitar N+1 queries con fetch join, EntityGraph o DTO projections cuando aplique.
Optimizar solo despues de tener consultas reales y tests funcionales.
```

`KAN-65` Correccion errores finales sprint:

```text
Corregir bugs encontrados en pruebas manuales y automatizadas.
Validar respuestas HTTP y mensajes de error.
Ejecutar test suite completa.
Revisar integracion con frontend.
Actualizar .env.example si aparecen nuevas variables.
```

### Analisis del orden

Este orden evita problemas porque primero termina el modulo de habitaciones, que es dependencia directa de reservas. Despues crea el modelo de reservas, sus DTOs, servicio y endpoints; recien entonces implementa disponibilidad por fechas, bloqueo de duplicados, permisos y pruebas del sistema de reservas. Dashboard, reportes, documentacion y optimizacion quedan al final porque dependen de consultas reales de habitaciones y reservas.

### Tareas que no se deben duplicar

No agregar como tareas separadas si ya quedan cubiertas asi:

```text
CRUD de habitaciones                 -> incluido en KAN-46
Endpoints de habitaciones            -> incluido en KAN-49
NotFound de habitaciones             -> incluido en KAN-48
Validaciones de habitaciones         -> incluido en KAN-47
Paginacion y ordenamiento            -> incluido en KAN-50
Filtros simples de habitaciones      -> incluido en KAN-51
Disponibilidad por fechas            -> incluido en KAN-56
Reservas duplicadas o solapadas      -> incluido en KAN-58
Testing CRUD habitaciones            -> incluido en KAN-52
Testing sistema reservas             -> incluido en KAN-60
Dashboard administrativo             -> incluido en KAN-61
Metricas y reportes basicos          -> incluido en KAN-62
Contrato de API                      -> incluido en KAN-63
Optimizacion de consultas SQL        -> incluido en KAN-64
```

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
