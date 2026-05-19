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

Este orden separa el trabajo de esta semana, enfocado en usuarios y habitaciones, del trabajo de la siguiente semana, enfocado en reservas y dashboard. La idea es evitar tareas repetidas y dejar cada bloque con dependencias claras.

### Esta semana: usuarios y habitaciones

1. `KAN-36` Implementar roles `ADMIN`/`CLIENTE`.
2. `KAN-37` Proteger endpoints por rol.
3. `KAN-38` Crear DTOs de Usuario.
4. `KAN-39` CRUD de Usuario.
5. `KAN-40` Crear endpoints para usuario.
6. `KAN-41` Testing seguridad usuarios/roles.
7. `KAN-42` Crear tipos de habitacion.
8. `KAN-43` Crear entidad Habitacion.
9. `KAN-44` Crear repositorios y servicios de habitaciones.
10. `KAN-46` CRUD de habitaciones.
11. Nueva tarea: Validaciones backend habitaciones.
12. Nueva tarea: Manejo de errores `NotFound`.
13. Nueva tarea: Crear endpoints de habitaciones.
14. Nueva tarea: Paginacion y ordenamiento.
15. Nueva tarea: Implementar filtros simples de habitaciones.
16. Nueva tarea: Testing CRUD habitaciones.

Notas:

```text
La tarea de filtros simples de habitaciones debe limitarse a:
- tipo de habitacion
- precio
- capacidad
- estado: DISPONIBLE, OCUPADA, MANTENIMIENTO

La disponibilidad real por rango de fechas no debe hacerse en la tarea de filtros simples.
Esa validacion pertenece a KAN-48, dentro del sistema de reservas.
```

### Siguiente semana: reservas y dashboard

1. `KAN-46` Sistema de reservas.
2. `KAN-47` Creacion de endpoints de reservas.
3. `KAN-48` Validar disponibilidad de fechas.
4. `KAN-49` Evitar reservas duplicadas.
5. `KAN-50` Dashboard administrativo.
6. `KAN-51` Crear reportes.
7. `KAN-52` Integrar frontend con reservas.
8. `KAN-53` Testing sistema reservas.
9. `KAN-54` Optimizacion consultas SQL.
10. `KAN-55` Correccion errores finales sprint.

Notas:

```text
KAN-46 debe incluir el modelo principal de reservas y la relacion con habitaciones.
KAN-48 debe validar disponibilidad usando fechas de entrada y salida.
KAN-49 debe impedir reservas duplicadas o solapadas para la misma habitacion.
KAN-54 debe dejarse despues de tener consultas reales de reservas, dashboard y reportes.
```

### Tareas que no se deben duplicar

No agregar como tareas separadas en esta semana:

```text
Crear modelo base de reservas       -> incluido en KAN-46
Relacionar reservas con habitaciones -> incluido en KAN-46
Validar disponibilidad por fechas    -> incluido en KAN-48
Evitar reservas duplicadas           -> incluido en KAN-49
Optimizacion de consultas SQL        -> incluido en KAN-54
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
