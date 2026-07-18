# Sistema de Gestión Hotelera — Backend

API REST para administrar clientes, habitaciones, reservas, áreas comunes, pagos y métricas de un hotel. El proyecto utiliza Spring Boot, PostgreSQL, autenticación JWT y control de acceso por roles.

## Funcionalidades principales

- Registro e inicio de sesión con JWT.
- Administración de clientes y roles `ADMIN` y `CLIENTE`.
- CRUD, filtros, paginación e imágenes de habitaciones.
- Gestión de reservas de habitaciones y áreas comunes.
- Registro y consulta de pagos.
- Dashboard administrativo con ocupación, ingresos y estado de reservas.
- Documentación interactiva con OpenAPI/Swagger.
- Pruebas unitarias, de seguridad e integración con JUnit 5 y H2.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Seguridad | Spring Security + JWT |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL |
| Documentación API | Springdoc OpenAPI / Swagger UI |
| Pruebas | JUnit 5, Spring Security Test y H2 |
| Construcción | Maven Wrapper 3.9.15 |

## Requisitos

Para la ejecución local se necesita:

- JDK 21.
- PostgreSQL 14 o superior, local o alojado (por ejemplo, Supabase).
- Git.
- Docker, únicamente si se usará la ejecución en contenedor.

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

## Configuración

1. Clona el repositorio y entra al directorio:

   ```bash
   git clone https://github.com/GuillenCanalesEloy/GestionHoteleria-Backend.git
   cd GestionHoteleria-Backend
   ```

2. Crea una base de datos PostgreSQL. Para una instalación local:

   ```sql
   CREATE DATABASE gestion_hoteleria;
   ```

3. Crea el archivo de variables a partir de la plantilla:

   En Windows PowerShell:

   ```powershell
   Copy-Item .env.example .env
   ```

   En Linux o macOS:

   ```bash
   cp .env.example .env
   ```

4. Edita `.env` con los datos de tu entorno. Las variables mínimas son:

   | Variable | Descripción | Ejemplo |
   | --- | --- | --- |
   | `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gestion_hoteleria` |
   | `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
   | `DB_PASSWORD` | Contraseña de PostgreSQL | `tu_password` |
   | `JWT_SECRET` | Clave para firmar JWT, mínimo 32 caracteres | `cambia_esta_clave_por_una_muy_segura` |
   | `PORT` | Puerto HTTP de la aplicación | `10000` |

   `FRONTEND_URL`, `JWT_EXPIRATION_MS`, `JPA_DDL_AUTO`, `JPA_SHOW_SQL` y `UPLOAD_DIR` son configurables en la misma plantilla. Las variables de Supabase Storage son opcionales; si se omiten, las imágenes se guardan localmente en `uploads/habitaciones`.

> Spring Boot no carga archivos `.env` de forma automática. Antes de iniciar la aplicación hay que importar sus variables en la terminal, usar la configuración de variables del IDE o ejecutar con Docker y `--env-file`.

## Ejecución local

### Windows PowerShell

Importa las variables del archivo `.env` en la sesión actual:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
```

Inicia la API:

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux o macOS

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

La aplicación estará disponible, por defecto, en `http://localhost:10000`.

## Ejecución con Docker

Construye la imagen:

```bash
docker build -t gestion-hoteleria-backend .
```

Ejecuta el contenedor usando las variables de `.env`:

```bash
docker run --rm --name gestion-hoteleria-backend --env-file .env -p 10000:10000 gestion-hoteleria-backend
```

Si cambias `PORT` en `.env`, ajusta también ambos puertos del parámetro `-p`.

## Comprobar la API

Con la aplicación iniciada:

- Swagger UI: `http://localhost:10000/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:10000/api-docs`
- Registro: `POST /api/auth/register`
- Inicio de sesión: `POST /api/auth/login`

Los endpoints protegidos esperan el encabezado:

```http
Authorization: Bearer <token>
```

En Swagger, usa el botón **Authorize** e introduce el token JWT obtenido al iniciar sesión.

## Módulos de la API

| Ruta base | Responsabilidad |
| --- | --- |
| `/api/auth` | Registro e inicio de sesión |
| `/api/clientes` | Administración de clientes |
| `/api/habitaciones` | Habitaciones, disponibilidad e imágenes |
| `/api/reservas` | Reservas de habitaciones |
| `/api/areas-comunes` | Catálogo de áreas comunes |
| `/api/reservas-areas-comunes` | Reservas de áreas comunes |
| `/api/pagos` | Pagos asociados a reservas |
| `/api/dashboard` | Métricas administrativas |

Las operaciones públicas y los permisos de cada rol pueden consultarse en Swagger. En términos generales, las consultas de habitaciones y áreas comunes son públicas; las operaciones administrativas requieren `ADMIN`; y las reservas y pagos requieren `ADMIN` o `CLIENTE`.

## Pruebas y construcción

Ejecuta toda la suite:

```powershell
.\mvnw.cmd test
```

En Linux o macOS:

```bash
./mvnw test
```

Genera el archivo JAR:

```powershell
.\mvnw.cmd clean package
```

El artefacto se crea en `target/GestionHoteleria-Backend-0.0.1-SNAPSHOT.jar`.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/com/Grupo1/GestionHoteleria_Backend/
│   │   ├── config/       # Seguridad, CORS, OpenAPI y recursos estáticos
│   │   ├── controller/   # Endpoints REST
│   │   ├── dto/          # Objetos de entrada y respuesta
│   │   ├── entity/       # Entidades JPA
│   │   ├── exception/    # Manejo centralizado de errores
│   │   ├── repository/   # Acceso a datos
│   │   ├── security/     # Filtro y servicios JWT
│   │   └── service/      # Lógica de negocio
│   └── resources/        # Configuración de Spring Boot
└── test/                 # Pruebas unitarias y de integración
```

## Solución de problemas

- **La aplicación no conecta con PostgreSQL:** comprueba `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, y confirma que la base de datos acepta conexiones desde tu equipo.
- **El frontend recibe un error CORS:** configura `FRONTEND_URL` con el origen exacto del frontend, incluido el puerto.
- **Los endpoints responden 401:** inicia sesión y envía el token en `Authorization: Bearer <token>`.
- **El puerto está ocupado:** cambia `PORT` antes de ejecutar la aplicación.
- **Las imágenes no se suben a Supabase:** revisa `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY` y que `SUPABASE_STORAGE_BUCKET` exista y sea público.

## Seguridad

- No confirmes el archivo `.env` ni credenciales reales en Git.
- Usa un `JWT_SECRET` distinto por entorno y suficientemente largo.
- La clave `SUPABASE_SERVICE_ROLE_KEY` es privada y solo debe existir en el backend.
- Para producción, configura secretos mediante variables del proveedor de despliegue.

La guía técnica ampliada del proyecto está disponible en [`guiadebackend.md`](guiadebackend.md).
