# SGCCA API - Sistema de Gestión y Control de Calidad de Agua

API REST para el Sistema de Gestión y Control de Calidad de Agua desarrollada con Spring Boot 3.5.4 y Java 21.

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21
- Maven 3.8+
- Docker y Docker Compose
- PostgreSQL (para producción)

### Instalación
```bash
# Clonar el repositorio
git clone <repository-url>
cd api

# Instalar dependencias
mvn clean install

# Ejecutar con Docker
docker-compose up -d

# Ejecutar en modo desarrollo
./scripts/dev.sh

# Despliegue
./scripts/deploy.sh
```

## 🧪 Testing
```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest="SeccionServiceTest,SeccionDtoTest"

# Probar APIs con scripts
./scripts/test_api.sh           # API de Secciones
./scripts/test_aplicaciones_api.sh  # API de Aplicaciones
```

## 📚 Documentación

Toda la documentación del proyecto se encuentra en la carpeta [`docs/`](./docs/):

- **[Guías de Testing](./docs/TESTS_README.md)** - Información completa sobre testing
- **[Implementación de Secciones](./docs/RESUMEN_SECCIONES.md)** - CRUD completo de Secciones
- **[Configuración Docker](./docs/DOCKER_README.md)** - Setup y configuración
- **[Principios de Desarrollo](./docs/)** - Referencias y mejores prácticas
- **[Códigos de Estado HTTP](./docs/http_status_codes.help.md)** - Guía de códigos HTTP correctos

## 🔧 Tecnologías

- **Backend**: Spring Boot 3.5.4, Java 21
- **Base de Datos**: PostgreSQL (prod), H2 (test)
- **Testing**: JUnit 5, Mockito, AssertJ
- **Documentación**: Swagger/OpenAPI
- **Contenerización**: Docker

## 📊 Estado del Proyecto

- ✅ **Secciones CRUD**: Implementado y probado (24/24 tests)
- ✅ **Aplicaciones CRUD**: Implementado y probado 
- ✅ **Arquitectura SOLID**: Principios implementados y validados
- ✅ **DTOs especializados**: CreateRequest, UpdateRequest, Response, Summary
- ✅ **Testing Suite**: Configurada y funcionando
- ✅ **Docker**: Configurado
- ✅ **API Response**: Patrón consistente en todos los endpoints

## 🤝 Contribución

1. Revisar la [documentación de desarrollo](./docs/)
2. Seguir los principios de [código limpio](./docs/principios_clean_code.help.md)
3. Escribir tests para nuevas funcionalidades
4. Mantener la documentación actualizada

---

**Versión**: 0.0.1-SNAPSHOT  
**Documentación completa**: [`docs/`](./docs/)
