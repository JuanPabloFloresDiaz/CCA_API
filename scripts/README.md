# Scripts del Proyecto

Esta carpeta contiene todos los scripts de automatización para el proyecto SGCCA API.

## 📂 Contenido

### 🚀 Despliegue y Desarrollo

- **`dev.sh`** - Ejecuta el proyecto en modo desarrollo
  ```bash
  ./scripts/dev.sh
  ```

- **`deploy.sh`** - Script de despliegue para producción
  ```bash
  ./scripts/deploy.sh
  ```

### 🧪 Testing de APIs

- **`test_api.sh`** - Pruebas completas de la API de Secciones
  ```bash
  ./scripts/test_api.sh
  ```

- **`test_aplicaciones_api.sh`** - Pruebas completas de la API de Aplicaciones
  ```bash
  ./scripts/test_aplicaciones_api.sh
  ```

## 📋 Uso

Todos los scripts deben ejecutarse desde la raíz del proyecto:

```bash
# Desarrollo
./scripts/dev.sh

# Pruebas de APIs
./scripts/test_api.sh
./scripts/test_aplicaciones_api.sh

# Despliegue
./scripts/deploy.sh
```

## ✅ Requisitos

- Los scripts requieren que el servidor esté ejecutándose en `http://localhost:8080`
- Para tests: curl debe estar instalado
- Para desarrollo: Java 21 y Maven deben estar disponibles

---

*Organización de scripts - Julio 2025*
