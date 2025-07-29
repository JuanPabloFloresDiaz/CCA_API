# Códigos de Estado HTTP para APIs REST

## 🎯 Introducción

Esta guía define los códigos de estado HTTP correctos que deben usarse en los endpoints de nuestra API REST, siguiendo las mejores prácticas y estándares RFC.

---

## ✅ Códigos de Éxito (2xx)

### 200 OK
**Uso**: Operación exitosa que devuelve datos
- **GET** - Obtener recursos existentes
- **PUT** - Actualización exitosa
- **PATCH** - Modificación parcial exitosa

**Ejemplos:**
```http
GET /api/aplicaciones/123 → 200 + datos de la aplicación
PUT /api/aplicaciones/123 → 200 + aplicación actualizada
PATCH /api/aplicaciones/123/estado → 200 + aplicación con nuevo estado
```

### 201 Created
**Uso**: Recurso creado exitosamente
- **POST** - Creación de nuevos recursos

**Ejemplos:**
```http
POST /api/aplicaciones → 201 + datos de la aplicación creada
POST /api/secciones → 201 + datos de la sección creada
```

### 204 No Content
**Uso**: Operación exitosa sin contenido de respuesta
- **DELETE** - Eliminación exitosa
- **PUT/PATCH** - Actualización sin respuesta de datos

**Ejemplos:**
```http
DELETE /api/aplicaciones/123 → 204 (sin cuerpo)
PATCH /api/aplicaciones/123/activar → 204 (si no devuelve datos)
```

---

## ❌ Códigos de Error del Cliente (4xx)

### 400 Bad Request
**Uso**: Solicitud malformada o datos inválidos
- Datos de entrada que fallan validación
- JSON malformado
- Parámetros inválidos

**Ejemplos:**
```http
POST /api/aplicaciones con datos inválidos → 400
PUT /api/aplicaciones/123 con email inválido → 400
```

### 401 Unauthorized
**Uso**: Autenticación requerida o fallida
- Token de acceso inválido o expirado
- Credenciales incorrectas

**Ejemplos:**
```http
GET /api/aplicaciones sin token → 401
POST /api/aplicaciones con token expirado → 401
```

### 403 Forbidden
**Uso**: Usuario autenticado pero sin permisos
- Rol insuficiente para la operación
- Recurso protegido

**Ejemplos:**
```http
DELETE /api/aplicaciones/123 (usuario normal) → 403
PUT /api/configuracion (solo admin) → 403
```

### 404 Not Found
**Uso**: Recurso no encontrado
- ID no existe en base de datos
- Endpoint no existe

**Ejemplos:**
```http
GET /api/aplicaciones/999 → 404
PUT /api/aplicaciones/inexistente → 404
```

### 409 Conflict
**Uso**: Conflicto con el estado actual del recurso
- Violación de constraints únicos
- Estado del recurso impide la operación

**Ejemplos:**
```http
POST /api/aplicaciones con nombre duplicado → 409
DELETE /api/aplicaciones/123 (tiene dependencias) → 409
```

### 422 Unprocessable Entity
**Uso**: Entidad válida pero no procesable
- Violaciones de reglas de negocio
- Datos semánticamente incorrectos

**Ejemplos:**
```http
POST /api/aplicaciones (fecha fin < fecha inicio) → 422
PUT /api/aplicaciones/123 (estado inválido para transición) → 422
```

---

## 🚨 Códigos de Error del Servidor (5xx)

### 500 Internal Server Error
**Uso**: Error interno no manejado
- Excepciones no capturadas
- Errores de base de datos
- Fallos del sistema

### 503 Service Unavailable
**Uso**: Servicio temporalmente no disponible
- Base de datos no accesible
- Sistema en mantenimiento

---

## 📋 Guía por Operación CRUD

### CREATE (POST)
```http
✅ 201 Created - Recurso creado exitosamente
❌ 400 Bad Request - Datos inválidos
❌ 409 Conflict - Recurso ya existe (duplicado)
❌ 422 Unprocessable Entity - Violación reglas de negocio
```

### READ (GET)
```http
✅ 200 OK - Recurso(s) encontrado(s)
❌ 404 Not Found - Recurso no existe
❌ 401 Unauthorized - Sin autenticación
❌ 403 Forbidden - Sin permisos
```

### UPDATE (PUT/PATCH)
```http
✅ 200 OK - Actualización exitosa con respuesta
✅ 204 No Content - Actualización exitosa sin respuesta
❌ 400 Bad Request - Datos inválidos
❌ 404 Not Found - Recurso no existe
❌ 409 Conflict - Conflicto con otros recursos
❌ 422 Unprocessable Entity - Violación reglas de negocio
```

### DELETE
```http
✅ 204 No Content - Eliminación exitosa
✅ 200 OK - Eliminación con confirmación de datos
❌ 404 Not Found - Recurso no existe
❌ 409 Conflict - Recurso tiene dependencias
```

---

## 🔧 Implementación en Spring Boot

### Ejemplo Controlador Correcto

```java
@RestController
public class AplicacionController {

    // ✅ CREATE - 201 Created
    @PostMapping
    public ResponseEntity<ApiResponse<AplicacionResponse>> crear(@Valid @RequestBody AplicacionCreateRequest request) {
        try {
            AplicacionResponse aplicacion = aplicacionService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED)  // 201
                    .body(new ApiResponse<>("Aplicación creada exitosamente", aplicacion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()  // 400
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)  // 409
                    .body(new ApiResponse<>("Aplicación ya existe", null));
        }
    }

    // ✅ READ - 200 OK o 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AplicacionResponse>> obtenerPorId(@PathVariable UUID id) {
        try {
            AplicacionResponse aplicacion = aplicacionService.obtenerPorId(id);
            return ResponseEntity.ok(  // 200
                new ApiResponse<>("Aplicación encontrada", aplicacion));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        }
    }

    // ✅ UPDATE - 200 OK o 404 Not Found
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AplicacionResponse>> actualizar(
            @PathVariable UUID id, 
            @Valid @RequestBody AplicacionUpdateRequest request) {
        try {
            AplicacionResponse aplicacion = aplicacionService.actualizar(id, request);
            return ResponseEntity.ok(  // 200
                new ApiResponse<>("Aplicación actualizada", aplicacion));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()  // 400
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    // ✅ DELETE - 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        try {
            aplicacionService.eliminar(id);
            return ResponseEntity.noContent().build();  // 204
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        }
    }
}
```

---

## ⚠️ Errores Comunes a Evitar

### ❌ Usar 200 para DELETE
```java
// ❌ INCORRECTO
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<String>> eliminar(@PathVariable UUID id) {
    service.eliminar(id);
    return ResponseEntity.ok(new ApiResponse<>("Eliminado", "OK"));  // ❌ Debe ser 204
}

// ✅ CORRECTO
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();  // ✅ 204 No Content
}
```

### ❌ Usar 200 para CREATE
```java
// ❌ INCORRECTO
@PostMapping
public ResponseEntity<AplicacionResponse> crear(@RequestBody AplicacionCreateRequest request) {
    AplicacionResponse aplicacion = service.crear(request);
    return ResponseEntity.ok(aplicacion);  // ❌ Debe ser 201
}

// ✅ CORRECTO
@PostMapping
public ResponseEntity<AplicacionResponse> crear(@RequestBody AplicacionCreateRequest request) {
    AplicacionResponse aplicacion = service.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(aplicacion);  // ✅ 201 Created
}
```

### ❌ No distinguir entre 400 y 422
```java
// ❌ INCORRECTO - Todo es 400
@PostMapping
public ResponseEntity<String> crear(@RequestBody AplicacionCreateRequest request) {
    try {
        service.crear(request);
        return ResponseEntity.ok("Creado");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error");  // ❌ Muy genérico
    }
}

// ✅ CORRECTO - Distinguir tipos de error
@PostMapping
public ResponseEntity<ApiResponse<AplicacionResponse>> crear(@Valid @RequestBody AplicacionCreateRequest request) {
    try {
        AplicacionResponse aplicacion = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Creado", aplicacion));
    } catch (MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()  // 400 - Error de formato
                .body(new ApiResponse<>("Datos inválidos", null));
    } catch (BusinessRuleViolationException e) {
        return ResponseEntity.unprocessableEntity()  // 422 - Error de regla de negocio
                .body(new ApiResponse<>(e.getMessage(), null));
    } catch (DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)  // 409 - Conflicto
                .body(new ApiResponse<>("Recurso ya existe", null));
    }
}
```

---

## 📚 Referencias

- **RFC 7231**: HTTP/1.1 Semantics and Content
- **RFC 7232**: HTTP/1.1 Conditional Requests  
- **REST API Design Guidelines**: Microsoft, Google, GitHub
- **Spring Boot Documentation**: HTTP Status Codes

---

*Documento creado: 28/07/2025*  
*Última actualización: 28/07/2025*
