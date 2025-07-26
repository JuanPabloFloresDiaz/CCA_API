# Centro de Control de Acceso - Configuración Docker

Este proyecto incluye configuración completa de Docker para ejecutar la aplicación Spring Boot junto con PostgreSQL, utilizando variables de entorno para mayor seguridad.

## 📋 Requisitos

- Docker 20.0+
- Docker Compose 2.0+

## 🔧 Configuración Inicial

Antes de ejecutar la aplicación, necesitas configurar las variables de entorno:

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Editar las variables según tu entorno
nano .env  # o tu editor preferido
```

### 🔐 Variables de Entorno Importantes

Las siguientes variables **DEBEN** ser configuradas antes del despliegue:

```env
# Seguridad JWT
TOKEN_SECRET=tu_clave_secreta_muy_larga_aqui_minimo_512_bits
TOKEN_SALT=tu_salt_en_base64_aqui

# Base de datos
DATABASE_PASSWORD=tu_password_seguro_aqui

# Llave de aplicación
APPLICATION_KEY=tu_clave_identificadora_aqui
```

## 🚀 Despliegue Completo

Para ejecutar toda la aplicación (API + Base de datos) en Docker:

```bash
# Opción 1: Usar el script automatizado (recomendado)
./deploy.sh

# Opción 2: Comandos manuales
docker-compose -f compose.yaml up --build -d
```

La aplicación estará disponible en:
- **API**: http://localhost:${API_EXTERNAL_PORT} (por defecto 8080)
- **Health Check**: http://localhost:${API_EXTERNAL_PORT}/actuator/health
- **Base de datos**: localhost:${DB_EXTERNAL_PORT} (por defecto 5432)

## 🔧 Desarrollo Local

Para desarrollo, puedes ejecutar solo PostgreSQL en Docker y la aplicación Spring Boot localmente:

```bash
# Opción 1: Usar el script de desarrollo
./dev.sh

# Opción 2: Comandos manuales
docker-compose -f compose.yaml up -d postgres
mvn spring-boot:run
```

## 🗄️ Configuración de Base de Datos

### Credenciales PostgreSQL
- **Host**: localhost
- **Puerto**: ${DB_EXTERNAL_PORT} (configurado en .env)
- **Base de datos**: ${DATABASE_NAME} (configurado en .env)
- **Usuario**: ${DATABASE_USERNAME} (configurado en .env)
- **Contraseña**: ${DATABASE_PASSWORD} (configurado en .env)

### Migraciones
Las migraciones de Flyway se ejecutan automáticamente al iniciar la aplicación en Docker. Los archivos de migración están en:
```
src/main/resources/db/migration/
```

## 🛠️ Comandos Útiles

```bash
# Ver logs en tiempo real
docker-compose -f compose.yaml logs -f

# Ver logs de un servicio específico
docker-compose -f compose.yaml logs -f api
docker-compose -f compose.yaml logs -f postgres

# Detener todos los servicios
docker-compose -f compose.yaml down

# Detener y eliminar volúmenes (⚠️ elimina datos de la BD)
docker-compose -f compose.yaml down -v

# Reiniciar un servicio específico
docker-compose -f compose.yaml restart api

# Ejecutar comandos en la base de datos
docker-compose -f compose.yaml exec postgres psql -U postgres -d cca

# Reconstruir la imagen de la API
docker-compose -f compose.yaml build api --no-cache
```

## 🏗️ Estructura de Archivos Docker

```
├── Dockerfile                    # Imagen de la aplicación Spring Boot
├── docker-compose.yaml           # Orquestación de servicios
├── .dockerignore                 # Archivos a ignorar en la construcción
├── deploy.sh                     # Script de despliegue automatizado
├── dev.sh                       # Script para desarrollo local
├── .env                         # Variables de entorno (Docker)
├── .env.development             # Variables de entorno (Desarrollo local)
├── .env.example                 # Plantilla de variables de entorno
└── src/main/resources/
    ├── application.properties    # Configuración unificada
    └── db/migration/            # Scripts de migración
```

## 🔄 Configuración Unificada

Ahora usamos un **único archivo** `application.properties` que se adapta automáticamente al entorno mediante variables de entorno:

- **Desarrollo local**: Usa `.env.development` (JPA auto-DDL, SQL verbose, localhost)
- **Docker/Producción**: Usa `.env` (Flyway, logs optimizados, servicios internos)

## 📝 Configuración por Entorno

### Para Desarrollo Local:
```bash
# El script dev.sh automáticamente usa .env.development
./dev.sh
mvn spring-boot:run
```

### Para Docker Completo:
```bash
# El script deploy.sh usa el archivo .env principal
./deploy.sh
```

## 🧪 Health Checks

Los servicios incluyen health checks:
- **PostgreSQL**: Verifica conexión con `pg_isready`
- **API**: Verifica endpoint `/actuator/health`

## 🚨 Solución de Problemas

### La aplicación no inicia
```bash
# Ver logs detallados
docker-compose -f compose.yaml logs api

# Verificar la base de datos
docker-compose -f compose.yaml logs postgres
```

### Error de conexión a la base de datos
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose -f compose.yaml ps postgres

# Probar conexión manual
docker-compose -f compose.yaml exec postgres pg_isready -U postgres -d cca
```

### Reiniciar todo desde cero
```bash
# Detener, eliminar y volver a crear
docker-compose -f compose.yaml down -v
docker-compose -f compose.yaml up --build -d
```

## ⚙️ Variables de Entorno

Las variables de entorno están configuradas en el archivo `.env`. Para configurarlas:

1. Copia el archivo de ejemplo: `cp .env.example .env`
2. Edita el archivo `.env` con tus valores específicos
3. **NUNCA** subas el archivo `.env` al repositorio (ya está en .gitignore)
4. Reinicia los servicios después de cambiar variables

### 🔒 Archivos de Configuración

```
├── .env                 # Variables de entorno (NO subir al repo)
├── .env.example         # Plantilla de variables (SÍ subir al repo)
├── compose.yaml         # Configuración Docker Compose
└── .gitignore          # Incluye .env para proteger secretos
```

### 🔑 Generación de Secretos

Para generar secretos seguros:

```bash
# Generar TOKEN_SECRET (512 bits en hexadecimal)
openssl rand -hex 64

# Generar TOKEN_SALT (Base64)
openssl rand -base64 32
```

## 🔐 Seguridad

- La aplicación se ejecuta con un usuario no-root
- PostgreSQL utiliza volúmenes persistentes
- Las credenciales están configuradas como variables de entorno

## 📊 Monitoreo

La aplicación expone endpoints de Actuator para monitoreo:
- `/actuator/health` - Estado de la aplicación
- `/actuator/info` - Información de la aplicación

Para más endpoints, modifica la configuración en `application-docker.properties`.
