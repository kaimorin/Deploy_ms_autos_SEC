# Deploy ms-vehiculos (CI/CD seguro)

Microservicio REST de Vehículos (CRUD + Swagger) construido con Spring Boot, empaquetado en Docker y desplegado automáticamente en AWS EC2 mediante un pipeline de GitHub Actions que incluye análisis de seguridad (Snyk) y análisis estático (SonarCloud).


## Tecnologías
 
| Área | Herramienta |
|------|-------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.8 (Web, Data JPA, Validation) |
| Base de datos | MySQL 8.0 |
| Documentación API | springdoc-openapi (Swagger UI) 3.0.3 |
| Build | Maven (wrapper incluido) |
| Contenedores | Docker (multi-stage) + Docker Compose |
| Registro de imágenes | GitHub Container Registry (GHCR) |
| CI/CD | GitHub Actions |
| Seguridad / calidad | Snyk (SCA), SonarCloud (SAST), JaCoCo (cobertura) |
| Hosting | AWS EC2 |


## Estructura del repositorio
 
```
.
├── .github/workflows/ci-seguridad.yml   # Pipeline CI/CD
├── docker-compose.yml                   # MySQL + ms-vehiculos (usado en el despliegue)
└── ms-vehiculos/
    ├── Dockerfile                       # Build multi-stage (Maven -> JRE 21 Alpine)
    ├── pom.xml
    └── src/
        ├── main/java/cl/matiivilla/vehiculos/
        │   ├── controller/              # VehiculoController, HomeController
        │   ├── service/                 # VehiculoService (lógica de negocio)
        │   ├── repository/              # VehiculoRepository (JPA)
        │   ├── model/                   # Entidad Vehiculo
        │   └── dto/                     # VehiculoRequest, VehiculoResponse
        ├── main/resources/application.yml
        └── test/                        # Pruebas unitarias (VehiculoServiceTest)
```


## API
 
Base path: `/api/v1/vehiculos` (puerto por defecto **9004**).
 
| Método | Endpoint | Descripción | Respuesta |
|--------|----------|-------------|-----------|
| `GET` | `/` | Información básica de la API | `200` |
| `GET` | `/api/v1/vehiculos` | Listar todos los vehículos | `200` |
| `GET` | `/api/v1/vehiculos/{id}` | Buscar un vehículo por id | `200` / `404` |
| `POST` | `/api/v1/vehiculos` | Crear un vehículo | `201` / `409` (patente duplicada) |
| `PUT` | `/api/v1/vehiculos/{id}` | Actualizar un vehículo | `200` / `404` / `409` |
| `DELETE` | `/api/v1/vehiculos/{id}` | Eliminar un vehículo | `204` / `404` |
 
Swagger UI disponible en `http://localhost:9004/swagger-ui/index.html`.

### Validaciones
 
- `patente`, `marca`, `modelo` y `tipo`: obligatorios (no vacíos).
- `anio`: obligatorio, mayor o igual a 1950.
- `disponible`: obligatorio.
- `color`: opcional.
- La `patente` debe ser única.
## Ejecución local
 
### Requisitos
 
- Docker y Docker Compose, **o** JDK 21 + MySQL 8 para correr sin contenedores.
### Opción 1: Docker Compose
 
Levanta MySQL y el microservicio usando la imagen publicada en GHCR:
 
```bash
docker compose up -d
```
 
La API queda en `http://localhost:9004`

### Opción 2: Maven (desarrollo)
 
Con una base de datos MySQL disponible:
 
```bash
cd ms-vehiculos
 
export DB_URL="jdbc:mysql://localhost:3306/vehiculos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME=root
export DB_PASSWORD=tu_password
 
./mvnw spring-boot:run
```
 
### Pruebas y cobertura
 
```bash
cd ms-vehiculos
./mvnw clean verify
```
 
### Construir la imagen manualmente
 
```bash
docker build -t ms-vehiculos ./ms-vehiculos
```

### Variables de entorno
 
| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_URL` | URL JDBC de la base de datos | *(obligatoria)* |
| `DB_USERNAME` | Usuario de la base de datos | *(obligatoria)* |
| `DB_PASSWORD` | Contraseña de la base de datos | *(obligatoria)* |
| `SERVER_PORT` | Puerto HTTP del servicio | `9004` |
 
El esquema se gestiona con Hibernate (`ddl-auto: update`), por lo que la tabla `vehiculos` se crea automáticamente.
 
## Pipeline CI/CD
 
Archivo: [`.github/workflows/ci-seguridad.yml`](.github/workflows/ci-seguridad.yml)
 
Se ejecuta en cada `push` a `main` y manualmente (`workflow_dispatch`). Los jobs corren en secuencia; si uno falla, los siguientes no se ejecutan.
 
```
snyk-scan  →  test  →  sonar-analysis  →  build-jar  →  docker-build-and-deploy
```
 
| # | Job | Qué hace |
|---|-----|----------|
| 1 | **Snyk Security Scan** | Análisis de dependencias (SCA); falla con vulnerabilidades de severidad `high` o superior. Sube `snyk-report.json` como artefacto. |
| 2 | **Run Tests with JaCoCo** | Ejecuta pruebas unitarias y genera el reporte de cobertura (artefacto `jacoco-report`). |
| 3 | **SonarCloud Analysis** | Análisis estático de calidad y seguridad del código (SAST). |
| 4 | **Build Jar** | Empaqueta el JAR (artefacto `app-jar`). |
| 5 | **Build, Push & Deploy** | Construye la imagen Docker, la publica en GHCR (`latest` y el SHA del commit) y despliega en EC2 por SSH. |
 
## Secrets requeridos
 
Configúralos en **Settings → Secrets and variables → Actions**:
 
| Secret | Uso |
|--------|-----|
| `SNYK_TOKEN` | Autenticación de Snyk CLI |
| `SONAR_TOKEN` | Autenticación en SonarCloud |
| `EC2_HOST` | Host/IP de la instancia EC2 |
| `EC2_USER` | Usuario SSH de la instancia |
| `EC2_SSH_KEY` | Llave privada SSH para el despliegue |
 
`GITHUB_TOKEN` es provisto automáticamente por GitHub para publicar en GHCR.
 
## Despliegue
 
El último job se conecta por SSH a la instancia EC2 y ejecuta:
 
1. Crea el directorio `~/app`.
2. Descarga la última versión de `docker-compose.yml` desde `main`.
3. `docker compose pull` para obtener la imagen más reciente.
4. `docker compose up -d` para levantar los servicios.
5. `docker compose ps` para verificar el estado.
**Requisitos en la instancia EC2:** Docker y el plugin Docker Compose instalados, el puerto `9004` abierto en el Security Group y acceso a GHCR (login si la imagen es privada).
 
## Seguridad
 
- **SCA con Snyk:** el pipeline bloquea el despliegue si hay vulnerabilidades altas o críticas en dependencias.
- **Dependencias parcheadas:** el `pom.xml` fuerza versiones corregidas de Tomcat y Jackson, y excluye `jaxb-runtime` (no se usa en este servicio).
- **SAST con SonarCloud:** análisis de calidad y seguridad del código en cada ejecución.
- **Permisos mínimos:** el workflow usa `contents: read` y solo el job de publicación recibe `packages: write`.
- **Secretos fuera del código:** credenciales y llaves se inyectan vía GitHub Secrets y variables de entorno.
