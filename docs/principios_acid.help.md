# Principios ACID con Spring Boot, JPA, Hibernate y MariaDB

## 🎯 ¿Qué son los Principios ACID?

Los principios ACID son las propiedades fundamentales que garantizan que las transacciones en bases de datos sean procesadas de manera confiable. Son esenciales para mantener la integridad de los datos en sistemas de gestión de bases de datos.

---

## ⚛️ A - Atomicity (Atomicidad)

### Definición
**Una transacción es indivisible**: todas las operaciones se ejecutan completamente o no se ejecuta ninguna. Si falla una operación, toda la transacción se revierte (rollback).

### ❌ Ejemplo SIN Atomicidad

```java
@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String numeroCuenta;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;
    
    // constructores, getters y setters...
}

@Entity
@Table(name = "transferencias")
public class Transferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cuenta_origen", nullable = false)
    private Long cuentaOrigen;
    
    @Column(name = "cuenta_destino", nullable = false)
    private Long cuentaDestino;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "fecha_transferencia", nullable = false)
    private LocalDateTime fechaTransferencia;
    
    // constructores, getters y setters...
}

// ❌ SIN TRANSACCIÓN: Viola Atomicidad
@Service
public class ServicioTransferenciaMalo {
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    
    // ❌ PELIGROSO: Sin @Transactional
    public void transferirDinero(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto) {
        
        // Paso 1: Buscar cuentas
        Cuenta cuentaOrigen = cuentaRepository.findById(cuentaOrigenId)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
        
        Cuenta cuentaDestino = cuentaRepository.findById(cuentaDestinoId)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));
        
        // Paso 2: Verificar saldo suficiente
        if (cuentaOrigen.getSaldo().compareTo(monto) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }
        
        // Paso 3: Debitar cuenta origen
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
        cuentaRepository.save(cuentaOrigen); // ✅ Se guarda
        
        // ❌ AQUÍ PUEDE FALLAR (corte de luz, error de red, etc.)
        // Si falla aquí, el dinero se perdió de la cuenta origen
        // pero nunca llegó a la cuenta destino
        
        // Paso 4: Acreditar cuenta destino
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(monto));
        cuentaRepository.save(cuentaDestino); // ❌ Puede no ejecutarse
        
        // Paso 5: Registrar transferencia
        Transferencia transferencia = new Transferencia(
            cuentaOrigenId, cuentaDestinoId, monto, LocalDateTime.now());
        transferenciaRepository.save(transferencia); // ❌ Puede no ejecutarse
    }
}
```

### ✅ Ejemplo CON Atomicidad

```java
@Service
public class ServicioTransferencia {
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    
    // ✅ CON @Transactional: Garantiza Atomicidad
    @Transactional(rollbackFor = Exception.class)
    public TransferenciaResultado transferirDinero(
            Long cuentaOrigenId, 
            Long cuentaDestinoId, 
            BigDecimal monto) throws TransferenciaException {
        
        try {
            // Paso 1: Buscar cuentas con bloqueo pessimista
            Cuenta cuentaOrigen = cuentaRepository.findByIdWithLock(cuentaOrigenId)
                .orElseThrow(() -> new TransferenciaException("Cuenta origen no encontrada"));
            
            Cuenta cuentaDestino = cuentaRepository.findByIdWithLock(cuentaDestinoId)
                .orElseThrow(() -> new TransferenciaException("Cuenta destino no encontrada"));
            
            // Paso 2: Validaciones de negocio
            validarTransferencia(cuentaOrigen, cuentaDestino, monto);
            
            // Paso 3: Debitar cuenta origen
            cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
            cuentaRepository.save(cuentaOrigen);
            
            // Paso 4: Acreditar cuenta destino
            cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(monto));
            cuentaRepository.save(cuentaDestino);
            
            // Paso 5: Registrar transferencia
            Transferencia transferencia = new Transferencia(
                cuentaOrigenId, cuentaDestinoId, monto, LocalDateTime.now());
            transferencia = transferenciaRepository.save(transferencia);
            
            // ✅ Si llegamos aquí, TODO se ejecutó correctamente
            return new TransferenciaResultado(transferencia.getId(), "Transferencia exitosa");
            
        } catch (Exception e) {
            // ✅ Si hay cualquier error, se hace ROLLBACK automático
            // NINGUNA de las operaciones se confirma
            throw new TransferenciaException("Error en transferencia: " + e.getMessage(), e);
        }
    }
    
    // Método auxiliar para validaciones
    private void validarTransferencia(Cuenta origen, Cuenta destino, BigDecimal monto) 
            throws TransferenciaException {
        
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaException("El monto debe ser mayor a cero");
        }
        
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new TransferenciaException("Saldo insuficiente");
        }
        
        if (origen.getId().equals(destino.getId())) {
            throw new TransferenciaException("No se puede transferir a la misma cuenta");
        }
    }
}

// Repository con bloqueo pessimista
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    
    @Query("SELECT c FROM Cuenta c WHERE c.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cuenta> findByIdWithLock(@Param("id") Long id);
}

// Configuración en application.yml para MariaDB
/*
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/banco_db
    username: usuario
    password: password
    driver-class-name: org.mariadb.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
    show-sql: true
    
  transaction:
    default-timeout: 30
    rollback-on-commit-failure: true
*/
```

---

## 🔄 C - Consistency (Consistencia)

### Definición
**Los datos deben mantenerse en un estado válido** antes y después de cada transacción, respetando todas las reglas de integridad, constraints y triggers de la base de datos.

### ❌ Ejemplo SIN Consistencia

```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private Integer stock; // ❌ Sin validación de rango
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio; // ❌ Sin validación de rango
    
    // getters y setters...
}

@Entity
@Table(name = "ordenes")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "producto_id", nullable = false)
    private Long productoId;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(nullable = false)
    private String estado; // ❌ Sin enum, cualquier string es válido
    
    // getters y setters...
}

// ❌ SIN VALIDACIONES: Permite estados inconsistentes
@Service
public class ServicioOrdenMalo {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private OrdenRepository ordenRepository;
    
    @Transactional
    public void procesarOrden(Long productoId, Integer cantidad) {
        
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // ❌ PROBLEMA: No valida que el stock no sea negativo
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
        
        // ❌ PROBLEMA: Puede crear orden con estado inválido
        Orden orden = new Orden();
        orden.setProductoId(productoId);
        orden.setCantidad(cantidad);
        orden.setEstado("ESTADO_INEXISTENTE"); // ❌ Estado inválido
        ordenRepository.save(orden);
        
        // ❌ RESULTADO: Base de datos en estado inconsistente
        // - Stock negativo
        // - Orden con estado inválido
    }
}
```

### ✅ Ejemplo CON Consistencia

```java
// ✅ ENTIDADES con validaciones y constraints
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @Column(nullable = false)
    @Min(value = 0, message = "El stock no puede ser negativo")
    @NotNull(message = "El stock es obligatorio")
    private Integer stock;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @NotNull(message = "El precio es obligatorio")
    private BigDecimal precio;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    // Constructor, getters y setters...
    
    // ✅ Método de negocio que mantiene consistencia
    public void reducirStock(Integer cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        
        if (this.stock < cantidad) {
            throw new StockInsuficienteException(
                "Stock insuficiente. Disponible: " + this.stock + ", Solicitado: " + cantidad);
        }
        
        this.stock -= cantidad;
    }
    
    public void aumentarStock(Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        this.stock += cantidad;
    }
}

// ✅ ENUM para estados válidos
public enum EstadoOrden {
    PENDIENTE("PENDIENTE", "Orden pendiente de procesamiento"),
    PROCESANDO("PROCESANDO", "Orden en proceso"),
    COMPLETADA("COMPLETADA", "Orden completada exitosamente"),
    CANCELADA("CANCELADA", "Orden cancelada"),
    FALLIDA("FALLIDA", "Orden fallida");
    
    private final String codigo;
    private final String descripcion;
    
    EstadoOrden(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    
    // getters...
}

@Entity
@Table(name = "ordenes")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @Column(nullable = false)
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    // ✅ Método que mantiene consistencia al cambiar estado
    public void cambiarEstado(EstadoOrden nuevoEstado) throws EstadoInvalidoException {
        validarTransicionEstado(this.estado, nuevoEstado);
        this.estado = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // ✅ Validación de transiciones de estado válidas
    private void validarTransicionEstado(EstadoOrden estadoActual, EstadoOrden nuevoEstado) 
            throws EstadoInvalidoException {
        
        switch (estadoActual) {
            case PENDIENTE:
                if (nuevoEstado != EstadoOrden.PROCESANDO && nuevoEstado != EstadoOrden.CANCELADA) {
                    throw new EstadoInvalidoException(
                        "Solo se puede cambiar de PENDIENTE a PROCESANDO o CANCELADA");
                }
                break;
            case PROCESANDO:
                if (nuevoEstado != EstadoOrden.COMPLETADA && nuevoEstado != EstadoOrden.FALLIDA) {
                    throw new EstadoInvalidoException(
                        "Solo se puede cambiar de PROCESANDO a COMPLETADA o FALLIDA");
                }
                break;
            case COMPLETADA:
            case CANCELADA:
            case FALLIDA:
                throw new EstadoInvalidoException(
                    "No se puede cambiar el estado desde: " + estadoActual);
            default:
                throw new EstadoInvalidoException("Estado no reconocido: " + estadoActual);
        }
    }
    
    // ✅ Calcular total manteniendo consistencia
    public void calcularTotal() {
        if (producto != null && cantidad != null) {
            this.total = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }
    
    // constructors, getters y setters...
}

// ✅ SERVICIO que mantiene consistencia
@Service
@Validated // Habilita validación de parámetros
public class ServicioOrden {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private OrdenRepository ordenRepository;
    
    @Transactional(rollbackFor = Exception.class)
    public Orden crearOrden(@Valid CrearOrdenRequest request) 
            throws ProductoNoEncontradoException, StockInsuficienteException {
        
        // ✅ Validar entrada
        validarRequest(request);
        
        // ✅ Buscar producto con bloqueo para evitar race conditions
        Producto producto = productoRepository.findByIdWithLock(request.getProductoId())
            .orElseThrow(() -> new ProductoNoEncontradoException(
                "Producto no encontrado: " + request.getProductoId()));
        
        // ✅ Validar que el producto esté activo
        if (!producto.getActivo()) {
            throw new ProductoNoDisponibleException("El producto no está disponible");
        }
        
        // ✅ Crear orden
        Orden orden = new Orden();
        orden.setProducto(producto);
        orden.setCantidad(request.getCantidad());
        orden.calcularTotal(); // Mantiene consistencia en el cálculo
        
        // ✅ Reducir stock de manera segura
        producto.reducirStock(request.getCantidad());
        
        // ✅ Guardar cambios
        productoRepository.save(producto);
        orden = ordenRepository.save(orden);
        
        // ✅ Cambiar estado a PROCESANDO
        orden.cambiarEstado(EstadoOrden.PROCESANDO);
        orden = ordenRepository.save(orden);
        
        return orden;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void completarOrden(Long ordenId) throws OrdenException {
        
        Orden orden = ordenRepository.findById(ordenId)
            .orElseThrow(() -> new OrdenException("Orden no encontrada: " + ordenId));
        
        // ✅ Solo se puede completar si está en procesamiento
        orden.cambiarEstado(EstadoOrden.COMPLETADA);
        ordenRepository.save(orden);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void cancelarOrden(Long ordenId) throws OrdenException {
        
        Orden orden = ordenRepository.findByIdWithLock(ordenId)
            .orElseThrow(() -> new OrdenException("Orden no encontrada: " + ordenId));
        
        // ✅ Solo se puede cancelar si está pendiente o procesando
        if (orden.getEstado() == EstadoOrden.PENDIENTE || 
            orden.getEstado() == EstadoOrden.PROCESANDO) {
            
            // ✅ Restaurar stock si se cancela
            Producto producto = orden.getProducto();
            producto.aumentarStock(orden.getCantidad());
            productoRepository.save(producto);
            
            // ✅ Cambiar estado
            orden.cambiarEstado(EstadoOrden.CANCELADA);
            ordenRepository.save(orden);
            
        } else {
            throw new OrdenException(
                "No se puede cancelar una orden en estado: " + orden.getEstado());
        }
    }
    
    private void validarRequest(CrearOrdenRequest request) {
        if (request.getProductoId() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }
}

// ✅ CONSTRAINTS en la base de datos (MariaDB)
/*
-- Script SQL para garantizar consistencia a nivel de BD
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    stock INT NOT NULL CHECK (stock >= 0), -- ✅ Constraint: stock no negativo
    precio DECIMAL(10,2) NOT NULL CHECK (precio > 0), -- ✅ Constraint: precio positivo
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    UNIQUE KEY uk_producto_nombre (nombre)
);

CREATE TABLE ordenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0), -- ✅ Constraint: cantidad positiva
    estado ENUM('PENDIENTE', 'PROCESANDO', 'COMPLETADA', 'CANCELADA', 'FALLIDA') NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    total DECIMAL(10,2) CHECK (total >= 0), -- ✅ Constraint: total no negativo
    
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    INDEX idx_orden_estado (estado),
    INDEX idx_orden_fecha (fecha_creacion)
);

-- ✅ Trigger para mantener consistencia
DELIMITER $$
CREATE TRIGGER tr_orden_before_insert
BEFORE INSERT ON ordenes
FOR EACH ROW
BEGIN
    -- Verificar que hay stock suficiente
    DECLARE stock_actual INT;
    SELECT stock INTO stock_actual FROM productos WHERE id = NEW.producto_id;
    
    IF stock_actual < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente';
    END IF;
END$$
DELIMITER ;
*/
```

---

## 🔒 I - Isolation (Aislamiento)

### Definición
**Las transacciones concurrentes no deben interferir entre sí**. Cada transacción debe ejecutarse como si fuera la única en el sistema, evitando problemas como dirty reads, phantom reads, etc.

### Niveles de Aislamiento en MariaDB

```java
// ✅ Configuración de niveles de aislamiento
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}
```

### ❌ Ejemplo SIN Aislamiento Adecuado

```java
@Service
public class ServicioCuentaBanco {
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    // ❌ PROBLEMA: Dirty Read
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public BigDecimal consultarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        // ❌ Puede leer datos no confirmados de otra transacción
        return cuenta.getSaldo();
    }
    
    // ❌ PROBLEMA: Non-repeatable Read
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String generarReporte(Long cuentaId) {
        
        // Primera lectura
        Cuenta cuenta1 = cuentaRepository.findById(cuentaId).get();
        BigDecimal saldo1 = cuenta1.getSaldo();
        
        // Simular procesamiento
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // Segunda lectura - ❌ Puede ser diferente si otra transacción modificó
        Cuenta cuenta2 = cuentaRepository.findById(cuentaId).get();
        BigDecimal saldo2 = cuenta2.getSaldo();
        
        // ❌ saldo1 != saldo2 (Non-repeatable read)
        return "Saldo inicial: " + saldo1 + ", Saldo final: " + saldo2;
    }
}
```

### ✅ Ejemplo CON Aislamiento Adecuado

```java
@Service
public class ServicioCuentaSeguro {
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private MovimientoRepository movimientoRepository;
    
    // ✅ SERIALIZABLE: Máximo aislamiento para operaciones críticas
    @Transactional(
        isolation = Isolation.SERIALIZABLE,
        rollbackFor = Exception.class,
        timeout = 30
    )
    public void transferirDineroSeguro(Long origenId, Long destinoId, BigDecimal monto) 
            throws TransferenciaException {
        
        // ✅ Las cuentas se bloquean hasta el final de la transacción
        Cuenta origen = cuentaRepository.findByIdWithLock(origenId)
            .orElseThrow(() -> new TransferenciaException("Cuenta origen no encontrada"));
        
        Cuenta destino = cuentaRepository.findByIdWithLock(destinoId)
            .orElseThrow(() -> new TransferenciaException("Cuenta destino no encontrada"));
        
        // ✅ Validaciones
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new TransferenciaException("Saldo insuficiente");
        }
        
        // ✅ Operaciones atómicas
        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));
        
        // ✅ Registrar movimientos
        movimientoRepository.save(new Movimiento(origenId, monto.negate(), "TRANSFERENCIA_SALIDA"));
        movimientoRepository.save(new Movimiento(destinoId, monto, "TRANSFERENCIA_ENTRADA"));
        
        // ✅ Guardar cambios
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
    }
    
    // ✅ REPEATABLE READ: Para consultas que requieren consistencia
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        readOnly = true
    )
    public ReporteCuenta generarReporteConsistente(Long cuentaId) {
        
        // ✅ Primera lectura
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        BigDecimal saldoInicial = cuenta.getSaldo();
        
        // ✅ Obtener movimientos del mes
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1);
        List<Movimiento> movimientos = movimientoRepository
            .findByCuentaIdAndFechaBetween(cuentaId, inicioMes, LocalDateTime.now());
        
        // ✅ Segunda lectura - GARANTIZADA que será igual a la primera
        cuenta = cuentaRepository.findById(cuentaId).get();
        BigDecimal saldoFinal = cuenta.getSaldo();
        
        // ✅ saldoInicial == saldoFinal (Repeatable Read garantizado)
        
        return new ReporteCuenta(
            cuenta.getNumeroCuenta(),
            saldoInicial,
            saldoFinal,
            movimientos
        );
    }
    
    // ✅ READ COMMITTED: Para operaciones de consulta normales
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        readOnly = true
    )
    public List<Cuenta> listarCuentasActivas() {
        // ✅ Solo lee datos confirmados, pero permite lecturas no repetibles
        return cuentaRepository.findByActivaTrue();
    }
    
    // ✅ Manejo de concurrencia con optimistic locking
    @Transactional(rollbackFor = Exception.class)
    public void actualizarSaldoOptimista(Long cuentaId, BigDecimal nuevoSaldo) 
            throws ConcurrencyException {
        
        try {
            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            
            cuenta.setSaldo(nuevoSaldo);
            cuentaRepository.save(cuenta);
            
        } catch (OptimisticLockException e) {
            // ✅ Manejar conflicto de concurrencia
            throw new ConcurrencyException(
                "La cuenta fue modificada por otro usuario. Intente nuevamente.");
        }
    }
}

// ✅ Entidad con versionado para optimistic locking
@Entity
@Table(name = "cuentas")
public class CuentaConVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String numeroCuenta;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;
    
    // ✅ Versión para optimistic locking
    @Version
    private Long version;
    
    @Column(nullable = false)
    private Boolean activa = true;
    
    // constructors, getters y setters...
}

// ✅ Repository con diferentes tipos de bloqueo
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    
    // ✅ Bloqueo pessimista para escritura
    @Query("SELECT c FROM Cuenta c WHERE c.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cuenta> findByIdWithLock(@Param("id") Long id);
    
    // ✅ Bloqueo pessimista para lectura
    @Query("SELECT c FROM Cuenta c WHERE c.id = :id")
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Cuenta> findByIdWithReadLock(@Param("id") Long id);
    
    // ✅ Sin bloqueo para consultas normales
    List<Cuenta> findByActivaTrue();
}
```

### Configuración de Aislamiento en MariaDB

```sql
-- ✅ Configuración a nivel de sesión
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- ✅ Configuración a nivel global
SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- ✅ Verificar nivel actual
SELECT @@SESSION.transaction_isolation;
SELECT @@GLOBAL.transaction_isolation;
```

---

## 💾 D - Durability (Durabilidad)

### Definición
**Una vez que una transacción se confirma (commit), los cambios deben persistir permanentemente**, incluso ante fallos del sistema, cortes de energía o crashes de la base de datos.

### ✅ Configuración para Garantizar Durabilidad

```yaml
# application.yml - Configuración Spring Boot con MariaDB
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/banco_db?useSSL=false&allowPublicKeyRetrieval=true
    username: usuario
    password: password
    driver-class-name: org.mariadb.jdbc.Driver
    
    # ✅ Configuraciones para durabilidad
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      leak-detection-threshold: 60000
      
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        jdbc:
          # ✅ Asegurar que los statements se ejecuten inmediatamente
          batch_size: 20
          batch_versioned_data: true
        connection:
          # ✅ Configuraciones de conexión para durabilidad
          autocommit: false
        order_inserts: true
        order_updates: true
        # ✅ Logging para auditoría
        generate_statistics: true
        format_sql: true
        
    # ✅ Mostrar SQL para auditoría
    show-sql: false
    
  # ✅ Configuración de transacciones
  transaction:
    default-timeout: 300  # 5 minutos
    rollback-on-commit-failure: true
```

```sql
-- ✅ Configuración MariaDB para máxima durabilidad
-- my.cnf o mariadb.conf

[mysqld]
# ✅ InnoDB settings para durabilidad
innodb_flush_log_at_trx_commit = 1    # Flush log en cada commit (máxima durabilidad)
innodb_doublewrite = ON               # Prevenir corrupción de páginas
innodb_file_per_table = ON            # Un archivo por tabla
innodb_log_file_size = 256M           # Tamaño del log para mejor performance
innodb_buffer_pool_size = 1G          # Buffer pool size
innodb_flush_method = O_DIRECT        # Evitar double buffering

# ✅ Configuraciones de seguridad
sync_binlog = 1                       # Sincronizar binary log en cada commit
binlog_format = ROW                   # Logging a nivel de fila

# ✅ Configuraciones de recovery
innodb_force_recovery = 0             # Recovery automático
innodb_fast_shutdown = 1              # Shutdown rápido pero seguro
```

### ✅ Ejemplo de Servicio con Durabilidad Garantizada

```java
@Service
@Slf4j
public class ServicioBancoConDurabilidad {
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private AuditoriaRepository auditoriaRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // ✅ Transacción con configuración de durabilidad
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class,
        timeout = 60  // 60 segundos timeout
    )
    public ResultadoTransaccion realizarDeposito(Long cuentaId, BigDecimal monto) 
            throws TransaccionException {
        
        try {
            // ✅ Registrar inicio de transacción para auditoría
            RegistroAuditoria auditoriaInicio = new RegistroAuditoria(
                "DEPOSITO_INICIO",
                cuentaId,
                monto,
                LocalDateTime.now(),
                "Sistema"
            );
            auditoriaRepository.save(auditoriaInicio);
            
            // ✅ Buscar cuenta con bloqueo
            Cuenta cuenta = cuentaRepository.findByIdWithLock(cuentaId)
                .orElseThrow(() -> new TransaccionException("Cuenta no encontrada"));
            
            // ✅ Validaciones
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new TransaccionException("El monto debe ser positivo");
            }
            
            if (!cuenta.getActiva()) {
                throw new TransaccionException("La cuenta está inactiva");
            }
            
            // ✅ Guardar estado anterior para rollback manual si es necesario
            BigDecimal saldoAnterior = cuenta.getSaldo();
            
            // ✅ Realizar operación
            cuenta.setSaldo(cuenta.getSaldo().add(monto));
            cuenta.setFechaUltimaModificacion(LocalDateTime.now());
            
            // ✅ PUNTO CRÍTICO: Guardar cambios
            cuenta = cuentaRepository.save(cuenta);
            
            // ✅ Registrar éxito en auditoría
            RegistroAuditoria auditoriaExito = new RegistroAuditoria(
                "DEPOSITO_EXITOSO",
                cuentaId,
                monto,
                LocalDateTime.now(),
                "Sistema"
            );
            auditoriaExito.setSaldoAnterior(saldoAnterior);
            auditoriaExito.setSaldoNuevo(cuenta.getSaldo());
            auditoriaRepository.save(auditoriaExito);
            
            // ✅ Crear resultado
            ResultadoTransaccion resultado = new ResultadoTransaccion(
                true,
                "Depósito realizado exitosamente",
                cuenta.getSaldo(),
                LocalDateTime.now()
            );
            
            // ✅ Publicar evento (se ejecuta DESPUÉS del commit)
            eventPublisher.publishEvent(new DepositoRealizadoEvent(
                cuenta.getId(),
                monto,
                cuenta.getSaldo()
            ));
            
            log.info("Depósito exitoso - Cuenta: {}, Monto: {}, Nuevo saldo: {}", 
                cuentaId, monto, cuenta.getSaldo());
            
            return resultado;
            
        } catch (Exception e) {
            // ✅ Registrar error en auditoría
            RegistroAuditoria auditoriaError = new RegistroAuditoria(
                "DEPOSITO_ERROR",
                cuentaId,
                monto,
                LocalDateTime.now(),
                "Sistema"
            );
            auditoriaError.setMensajeError(e.getMessage());
            
            try {
                auditoriaRepository.save(auditoriaError);
            } catch (Exception auditException) {
                log.error("Error al guardar auditoría de error", auditException);
            }
            
            log.error("Error en depósito - Cuenta: {}, Monto: {}", cuentaId, monto, e);
            throw new TransaccionException("Error al procesar depósito: " + e.getMessage(), e);
        }
    }
    
    // ✅ Método para verificar integridad después de recovery
    @Transactional(readOnly = true)
    public VerificacionIntegridad verificarIntegridadCuenta(Long cuentaId) {
        
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        // ✅ Verificar que los movimientos cuadren con el saldo
        List<RegistroAuditoria> movimientos = auditoriaRepository
            .findByCuentaIdAndTipoIn(cuentaId, Arrays.asList(
                "DEPOSITO_EXITOSO", "RETIRO_EXITOSO", "TRANSFERENCIA_EXITOSO"
            ));
        
        BigDecimal saldoCalculado = movimientos.stream()
            .map(this::calcularImpactoSaldo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean integridadOk = saldoCalculado.compareTo(cuenta.getSaldo()) == 0;
        
        return new VerificacionIntegridad(
            cuenta.getId(),
            cuenta.getSaldo(),
            saldoCalculado,
            integridadOk,
            movimientos.size()
        );
    }
    
    private BigDecimal calcularImpactoSaldo(RegistroAuditoria registro) {
        switch (registro.getTipo()) {
            case "DEPOSITO_EXITOSO":
                return registro.getMonto();
            case "RETIRO_EXITOSO":
                return registro.getMonto().negate();
            default:
                return BigDecimal.ZERO;
        }
    }
}

// ✅ Entidad de auditoría para garantizar trazabilidad
@Entity
@Table(name = "auditoria_transacciones")
public class RegistroAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String tipo;
    
    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(nullable = false, length = 50)
    private String usuario;
    
    @Column(name = "saldo_anterior", precision = 19, scale = 2)
    private BigDecimal saldoAnterior;
    
    @Column(name = "saldo_nuevo", precision = 19, scale = 2)
    private BigDecimal saldoNuevo;
    
    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;
    
    // constructors, getters y setters...
}

// ✅ Event listener que se ejecuta DESPUÉS del commit
@Component
@Slf4j
public class TransaccionEventListener {
    
    @Autowired
    private ServicioNotificacion servicioNotificacion;
    
    // ✅ Se ejecuta solo si la transacción fue exitosa
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarDepositoRealizado(DepositoRealizadoEvent event) {
        try {
            // ✅ Enviar notificación solo después de que la transacción se persistió
            servicioNotificacion.enviarNotificacionDeposito(
                event.getCuentaId(),
                event.getMonto(),
                event.getNuevoSaldo()
            );
            
            log.info("Notificación enviada para depósito en cuenta: {}", event.getCuentaId());
            
        } catch (Exception e) {
            // ✅ Error en notificación no afecta la transacción ya confirmada
            log.error("Error al enviar notificación de depósito", e);
        }
    }
    
    // ✅ Se ejecuta solo si la transacción falló
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void manejarErrorTransaccion(TransaccionErrorEvent event) {
        log.warn("Transacción revertida para cuenta: {}, motivo: {}", 
            event.getCuentaId(), event.getMotivo());
    }
}

// ✅ Configuración de recovery y backup automático
@Component
@Scheduled(fixedRate = 300000) // Cada 5 minutos
public class VerificadorIntegridad {
    
    @Autowired
    private ServicioBancoConDurabilidad servicioBanco;
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Scheduled(cron = "0 0 2 * * ?") // Todos los días a las 2 AM
    public void verificarIntegridadCompleta() {
        log.info("Iniciando verificación de integridad completa");
        
        List<Cuenta> cuentas = cuentaRepository.findAll();
        int errores = 0;
        
        for (Cuenta cuenta : cuentas) {
            try {
                VerificacionIntegridad verificacion = servicioBanco
                    .verificarIntegridadCuenta(cuenta.getId());
                
                if (!verificacion.isIntegridadOk()) {
                    log.error("❌ Integridad comprometida en cuenta: {}", cuenta.getId());
                    errores++;
                }
                
            } catch (Exception e) {
                log.error("Error verificando cuenta: {}", cuenta.getId(), e);
                errores++;
            }
        }
        
        log.info("Verificación completa - Total cuentas: {}, Errores: {}", 
            cuentas.size(), errores);
    }
}
```

---

## 🎯 Resumen de Principios ACID

| Principio | Qué Garantiza | Implementación en Spring Boot |
|-----------|---------------|-------------------------------|
| **A**tomicity | Todo o nada | `@Transactional` con rollback |
| **C**onsistency | Estado válido | Validaciones + Constraints DB |
| **I**solation | Sin interferencias | Niveles de aislamiento + Locks |
| **D**urability | Persistencia permanente | Configuración DB + Auditoría |

### ✅ Mejores Prácticas

1. **Usar `@Transactional` apropiadamente**
   - Definir `rollbackFor = Exception.class`
   - Configurar timeouts apropiados
   - Usar el nivel de aislamiento correcto

2. **Implementar auditoría completa**
   - Registrar todas las operaciones críticas
   - Mantener trazabilidad de cambios
   - Verificar integridad periódicamente

3. **Configurar la base de datos correctamente**
   - `innodb_flush_log_at_trx_commit = 1`
   - Usar constraints apropiados
   - Configurar backups automáticos

4. **Manejar concurrencia adecuadamente**
   - Usar bloqueos cuando sea necesario
   - Implementar optimistic locking
   - Configurar pools de conexión apropiados

Los principios ACID son fundamentales para garantizar la confiabilidad de cualquier sistema que maneje datos críticos, especialmente en aplicaciones financieras, de e-commerce o cualquier dominio donde la integridad de los datos sea crucial.

---

*Documento creado: 13/07/2025*
*Tecnologías: Spring Boot 3.0+, JPA, Hibernate 6+, MariaDB 10.5+*