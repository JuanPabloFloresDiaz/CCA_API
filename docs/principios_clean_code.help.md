# Principios de Clean Code (Código Limpio)

## 🎯 Introducción

Los principios de Clean Code de Robert C. Martin (Uncle Bob) son fundamentales para escribir código que sea fácil de leer, mantener y modificar. Este documento incluye ejemplos prácticos con Spring Boot.

---

## 📚 1. Nombres Significativos

### Definición
**Usa nombres que revelen intención**: Los nombres deben decir por qué existe algo, qué hace y cómo se usa.

### ❌ Ejemplos MALOS

```java
@Entity
public class U {  // ❌ ¿Qué es U?
    private String n;  // ❌ ¿n de qué?
    private String e;  // ❌ ¿e de qué?
    private int t;     // ❌ ¿t de qué?
    
    public String getN() { return n; }  // ❌ Sin significado
    public void setN(String n) { this.n = n; }
}

@Service
public class S {  // ❌ Nombre sin significado
    
    public List<U> getList() {  // ❌ ¿Lista de qué?
        return null;
    }
    
    public void doStuff(U u, String s) {  // ❌ ¿Qué hace doStuff?
        // ❌ Variables con nombres confusos
        List<U> list1 = new ArrayList<>();
        List<U> list2 = new ArrayList<>();
        
        for (U item : list1) {  // ❌ ¿item de qué?
            if (item.getN().equals(s)) {
                list2.add(item);
            }
        }
    }
}
```

### ✅ Ejemplos BUENOS

```java
@Entity
@Table(name = "usuarios")
public class Usuario {  // ✅ Nombre claro y específico
    
    @Column(nullable = false)
    private String nombre;  // ✅ Revela propósito
    
    @Column(nullable = false, unique = true)
    private String email;   // ✅ Específico y claro
    
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;  // ✅ Indica qué tipo de información contiene
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;  // ✅ Específico sobre qué fecha
    
    // ✅ Getters y setters con nombres descriptivos
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // ✅ Métodos que revelan intención
    public boolean esUsuarioActivo() {
        return tipo != TipoUsuario.INACTIVO;
    }
    
    public boolean fueCreadoEnLosUltimos30Dias() {
        return fechaCreacion.isAfter(LocalDateTime.now().minusDays(30));
    }
}

@Service
public class ServicioGestionUsuarios {  // ✅ Nombre que describe responsabilidad
    
    @Autowired
    private RepositorioUsuario repositorioUsuario;
    
    @Autowired
    private ValidadorEmail validadorEmail;
    
    public List<Usuario> obtenerUsuariosActivos() {  // ✅ Método auto-documentado
        return repositorioUsuario.findAll()
            .stream()
            .filter(Usuario::esUsuarioActivo)
            .collect(Collectors.toList());
    }
    
    public List<Usuario> buscarUsuariosPorCriterio(String criterioBusqueda) {
        List<Usuario> todosLosUsuarios = repositorioUsuario.findAll();
        List<Usuario> usuariosEncontrados = new ArrayList<>();
        
        for (Usuario usuarioActual : todosLosUsuarios) {  // ✅ Variables descriptivas
            if (usuarioCoincideConCriterio(usuarioActual, criterioBusqueda)) {
                usuariosEncontrados.add(usuarioActual);
            }
        }
        
        return usuariosEncontrados;
    }
    
    private boolean usuarioCoincideConCriterio(Usuario usuario, String criterio) {
        return usuario.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
               usuario.getEmail().toLowerCase().contains(criterio.toLowerCase());
    }
}
```

---

## 🔧 2. Funciones

### Principios de Funciones Limpias

#### ✅ Pequeñas
**Las funciones deben ser pequeñas. Y después, deben ser más pequeñas aún.**

#### ✅ Hacen Una Sola Cosa
**Las funciones deben hacer una cosa, hacerla bien y ser lo único que hagan.**

#### ✅ Un Nivel de Abstracción
**Todos los statements en una función deben estar al mismo nivel de abstracción.**

### ❌ Función MALA

```java
@Service
public class ServicioPedido {
    
    // ❌ Función larga que hace muchas cosas
    public void procesarPedido(PedidoRequest request) {
        // Validar datos del pedido
        if (request.getProductoId() == null) {
            throw new IllegalArgumentException("Producto ID requerido");
        }
        if (request.getCantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser positiva");
        }
        if (request.getClienteId() == null) {
            throw new IllegalArgumentException("Cliente ID requerido");
        }
        
        // Buscar producto
        Producto producto = productoRepository.findById(request.getProductoId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Verificar stock
        if (producto.getStock() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente");
        }
        
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        // Calcular precio
        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));
        BigDecimal descuento = BigDecimal.ZERO;
        
        if (cliente.getTipo().equals("VIP")) {
            descuento = subtotal.multiply(BigDecimal.valueOf(0.1));
        } else if (cliente.getTipo().equals("PREMIUM")) {
            descuento = subtotal.multiply(BigDecimal.valueOf(0.05));
        }
        
        BigDecimal total = subtotal.subtract(descuento);
        
        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setClienteId(request.getClienteId());
        pedido.setProductoId(request.getProductoId());
        pedido.setCantidad(request.getCantidad());
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setTotal(total);
        pedido.setFecha(LocalDateTime.now());
        
        // Actualizar stock
        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);
        
        // Guardar pedido
        pedidoRepository.save(pedido);
        
        // Enviar notificación
        String mensaje = "Su pedido de " + producto.getNombre() + 
                        " por cantidad " + request.getCantidad() + 
                        " ha sido procesado. Total: $" + total;
        
        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            emailService.send(cliente.getEmail(), "Confirmación de Pedido", mensaje);
        }
        
        if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
            smsService.send(cliente.getTelefono(), mensaje);
        }
        
        // Log
        logger.info("Pedido procesado para cliente {} con total {}", 
                   cliente.getId(), total);
    }
}
```

### ✅ Funciones BUENAS

```java
@Service
@Transactional
public class ServicioPedido {
    
    @Autowired
    private ValidadorPedido validadorPedido;
    
    @Autowired
    private RepositorioProducto repositorioProducto;
    
    @Autowired
    private RepositorioCliente repositorioCliente;
    
    @Autowired
    private CalculadoraPrecio calculadoraPrecio;
    
    @Autowired
    private ServicioNotificacion servicioNotificacion;
    
    // ✅ Función principal que orquesta el proceso
    public Pedido procesarPedido(PedidoRequest request) {
        validarSolicitudPedido(request);
        
        Producto producto = obtenerProductoValidado(request.getProductoId());
        Cliente cliente = obtenerClienteValidado(request.getClienteId());
        
        verificarDisponibilidadStock(producto, request.getCantidad());
        
        CalculoPrecios calculo = calcularPrecios(producto, cliente, request.getCantidad());
        
        Pedido pedido = crearPedido(request, calculo);
        actualizarStock(producto, request.getCantidad());
        
        enviarNotificaciones(cliente, producto, pedido);
        
        return pedido;
    }
    
    // ✅ Función pequeña con una responsabilidad
    private void validarSolicitudPedido(PedidoRequest request) {
        validadorPedido.validar(request);
    }
    
    // ✅ Función que hace una sola cosa
    private Producto obtenerProductoValidado(Long productoId) {
        return repositorioProducto.findById(productoId)
            .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado: " + productoId));
    }
    
    // ✅ Función específica y clara
    private Cliente obtenerClienteValidado(Long clienteId) {
        return repositorioCliente.findById(clienteId)
            .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado: " + clienteId));
    }
    
    // ✅ Función con nombre que describe exactamente qué hace
    private void verificarDisponibilidadStock(Producto producto, Integer cantidadSolicitada) {
        if (producto.getStock() < cantidadSolicitada) {
            throw new StockInsuficienteException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", 
                             producto.getStock(), cantidadSolicitada));
        }
    }
    
    // ✅ Función delegada a especialista
    private CalculoPrecios calcularPrecios(Producto producto, Cliente cliente, Integer cantidad) {
        return calculadoraPrecio.calcular(producto, cliente, cantidad);
    }
    
    // ✅ Función constructora específica
    private Pedido crearPedido(PedidoRequest request, CalculoPrecios calculo) {
        return Pedido.builder()
            .clienteId(request.getClienteId())
            .productoId(request.getProductoId())
            .cantidad(request.getCantidad())
            .subtotal(calculo.getSubtotal())
            .descuento(calculo.getDescuento())
            .total(calculo.getTotal())
            .fecha(LocalDateTime.now())
            .build();
    }
    
    // ✅ Función con propósito específico
    private void actualizarStock(Producto producto, Integer cantidad) {
        producto.reducirStock(cantidad);
        repositorioProducto.save(producto);
    }
    
    // ✅ Función coordinadora de notificaciones
    private void enviarNotificaciones(Cliente cliente, Producto producto, Pedido pedido) {
        MensajeNotificacion mensaje = construirMensajeConfirmacion(producto, pedido);
        servicioNotificacion.notificarPedidoProcesado(cliente, mensaje);
    }
    
    // ✅ Función específica para construcción de mensaje
    private MensajeNotificacion construirMensajeConfirmacion(Producto producto, Pedido pedido) {
        String contenido = String.format(
            "Su pedido de %s por cantidad %d ha sido procesado. Total: $%.2f",
            producto.getNombre(), 
            pedido.getCantidad(), 
            pedido.getTotal()
        );
        
        return new MensajeNotificacion("Confirmación de Pedido", contenido);
    }
}
```

---

## 💬 3. Comentarios

### Principios sobre Comentarios

#### ✅ Los Comentarios Buenos
- **Comentarios legales** (copyright, licencias)
- **Comentarios informativos** (formato de regex, etc.)
- **Explicación de intención** (por qué se tomó una decisión)
- **Advertencias** (sobre consecuencias)
- **TODO comments** (tareas pendientes)

#### ❌ Los Comentarios Malos
- **Comentarios redundantes** (explican código obvio)
- **Comentarios engañosos** (no coinciden con el código)
- **Comentarios mandatorios** (por cada función)
- **Comentarios de ruido** (sin valor)

### ❌ Comentarios MALOS

```java
@Service
public class ServicioUsuario {
    
    // ❌ Comentario redundante - el código ya lo dice
    // Incrementa el contador en 1
    private int incrementarContador(int contador) {
        return contador + 1;  // ❌ Comentario innecesario
    }
    
    // ❌ Comentario mandatorio sin valor
    /**
     * Método que obtiene usuario por ID
     * @param id el ID del usuario
     * @return el usuario
     */
    public Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    
    // ❌ Comentario engañoso - no refleja lo que hace realmente
    // Valida el email del usuario
    public boolean validarDatos(Usuario usuario) {
        return usuario.getNombre() != null && 
               usuario.getEmail() != null && 
               usuario.getTelefono() != null;  // ❌ También valida teléfono
    }
    
    // ❌ Código comentado - confunde y molesta
    public void procesarUsuario(Usuario usuario) {
        usuario.setActivo(true);
        // usuario.setFechaUltimaConexion(LocalDateTime.now());
        // emailService.enviarBienvenida(usuario.getEmail());
        usuarioRepository.save(usuario);
    }
}
```

### ✅ Comentarios BUENOS

```java
@Service
public class ServicioCalculoImpuestos {
    
    // ✅ Comentario legal
    /*
     * Copyright (c) 2025 Mi Empresa
     * Licenciado bajo Apache License 2.0
     */
    
    // ✅ Comentario informativo - explica formato complejo
    // Regex para validar RFC mexicano: 4 letras, 6 dígitos, 3 caracteres alfanuméricos
    private static final Pattern RFC_PATTERN = 
        Pattern.compile("^[A-ZÑ&]{4}[0-9]{6}[A-Z0-9]{3}$");
    
    // ✅ Comentario de intención - explica POR QUÉ
    public BigDecimal calcularImpuesto(BigDecimal monto, String tipoCliente) {
        // Aplicamos descuento especial a clientes gubernamentales
        // debido a acuerdo comercial firmado en enero 2025
        if ("GOBIERNO".equals(tipoCliente)) {
            return monto.multiply(BigDecimal.valueOf(0.08)); // 8% en lugar de 16%
        }
        
        return monto.multiply(BigDecimal.valueOf(0.16));
    }
    
    // ✅ Comentario de advertencia
    public void procesarFacturacionMasiva(List<Cliente> clientes) {
        // ADVERTENCIA: Este proceso puede tomar varias horas con más de 10,000 clientes
        // Asegúrate de ejecutar durante horas de menor carga
        
        if (clientes.size() > 10000) {
            logger.warn("Procesando {} clientes - esto tomará tiempo considerable", clientes.size());
        }
        
        // ... lógica de procesamiento
    }
    
    // ✅ TODO comment válido
    public BigDecimal calcularDescuentoEspecial(Cliente cliente, BigDecimal monto) {
        // TODO: Implementar algoritmo de descuentos dinámicos basado en ML
        // Ticket: FEAT-2025-ML-DISCOUNTS
        // Fecha límite: 31/03/2025
        
        // Por ahora usamos lógica simple
        return cliente.esVip() ? monto.multiply(BigDecimal.valueOf(0.1)) : BigDecimal.ZERO;
    }
    
    // ✅ Comentario que explica decisión de algoritmo no obvio
    private BigDecimal aplicarAlgoritmoEspecial(BigDecimal base) {
        // Usamos el algoritmo de Luhn modificado para este cálculo
        // porque los tests A/B mostraron 15% mejor precisión
        // vs algoritmo estándar para nuestro dominio específico
        
        return base.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP);
    }
}
```

---

## 🎨 4. Formato

### Principios de Formato

#### ✅ Consistencia
**El formato debe ser consistente en todo el proyecto**

#### ✅ Legibilidad
**El código debe ser fácil de leer y navegar**

### ✅ Ejemplo de Buen Formato

```java
@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {
    
    private final ServicioUsuario servicioUsuario;
    private final ValidadorUsuario validadorUsuario;
    
    public UsuarioController(ServicioUsuario servicioUsuario, 
                           ValidadorUsuario validadorUsuario) {
        this.servicioUsuario = servicioUsuario;
        this.validadorUsuario = validadorUsuario;
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamaño) {
        
        List<Usuario> usuarios = servicioUsuario.obtenerUsuariosPaginados(pagina, tamaño);
        List<UsuarioResponse> response = usuarios.stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request) {
        
        validadorUsuario.validarCreacion(request);
        
        Usuario usuario = servicioUsuario.crearUsuario(
            request.getNombre(),
            request.getEmail(),
            request.getPassword()
        );
        
        UsuarioResponse response = convertirAResponse(usuario);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = servicioUsuario.obtenerPorId(id);
        UsuarioResponse response = convertirAResponse(usuario);
        
        return ResponseEntity.ok(response);
    }
    
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return UsuarioResponse.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .fechaCreacion(usuario.getFechaCreacion())
            .activo(usuario.isActivo())
            .build();
    }
}
```

---

## 🏗️ 5. Objetos y Estructuras de Datos

### Principios

#### ✅ Abstracción de Datos
**Los objetos exponen comportamiento y ocultan datos**

#### ✅ Ley de Demeter
**Un objeto solo debe conocer a sus colaboradores inmediatos**

### ✅ Ejemplo de Buena Abstracción

```java
// ✅ Objeto que expone comportamiento, no datos
@Entity
public class CuentaBancaria {
    
    @Id
    private String numeroCuenta;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal saldo;
    
    private Boolean activa;
    
    // ✅ Comportamiento, no getters/setters directos
    public void depositar(BigDecimal monto) {
        validarMontoPositivo(monto);
        validarCuentaActiva();
        
        this.saldo = this.saldo.add(monto);
    }
    
    public void retirar(BigDecimal monto) throws SaldoInsuficienteException {
        validarMontoPositivo(monto);
        validarCuentaActiva();
        validarSaldoSuficiente(monto);
        
        this.saldo = this.saldo.subtract(monto);
    }
    
    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        return this.saldo.compareTo(monto) >= 0;
    }
    
    public boolean esCuentaActiva() {
        return Boolean.TRUE.equals(this.activa);
    }
    
    // ✅ Solo expone información necesaria
    public String obtenerResumenCuenta() {
        return String.format("Cuenta: %s, Saldo: $%.2f, Estado: %s",
            numeroCuenta,
            saldo,
            activa ? "Activa" : "Inactiva"
        );
    }
    
    private void validarMontoPositivo(BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
    }
    
    private void validarCuentaActiva() {
        if (!esCuentaActiva()) {
            throw new CuentaInactivaException("La cuenta está inactiva");
        }
    }
    
    private void validarSaldoSuficiente(BigDecimal monto) throws SaldoInsuficienteException {
        if (!tieneSaldoSuficiente(monto)) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }
    }
}

// ✅ Servicio que respeta la Ley de Demeter
@Service
public class ServicioTransferencia {
    
    @Autowired
    private RepositorioCuenta repositorioCuenta;
    
    @Transactional
    public void transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        
        CuentaBancaria origen = obtenerCuenta(cuentaOrigen);
        CuentaBancaria destino = obtenerCuenta(cuentaDestino);
        
        // ✅ Usa comportamiento del objeto, no accede a datos directamente
        origen.retirar(monto);    // El objeto se encarga de sus validaciones
        destino.depositar(monto); // El objeto se encarga de sus validaciones
        
        // ✅ Guarda los objetos, que encapsulan su estado
        repositorioCuenta.save(origen);
        repositorioCuenta.save(destino);
    }
    
    private CuentaBancaria obtenerCuenta(String numeroCuenta) {
        return repositorioCuenta.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta));
    }
}
```

---

## 🔧 6. Manejo de Errores

### Principios

#### ✅ Usar Excepciones en lugar de Códigos de Error
#### ✅ Escribir try-catch-finally primero
#### ✅ Usar excepciones sin verificar para errores de lógica
#### ✅ Proporcionar contexto con excepciones

### ✅ Ejemplo de Buen Manejo de Errores

```java
// ✅ Excepciones específicas del dominio
public class UsuarioException extends RuntimeException {
    public UsuarioException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

public class EmailYaRegistradoException extends UsuarioException {
    public EmailYaRegistradoException(String email) {
        super("El email ya está registrado: " + email);
    }
}

@Service
@Transactional
public class ServicioRegistroUsuario {
    
    private static final Logger logger = LoggerFactory.getLogger(ServicioRegistroUsuario.class);
    
    @Autowired
    private RepositorioUsuario repositorioUsuario;
    
    @Autowired
    private ServicioEmail servicioEmail;
    
    public Usuario registrarUsuario(String nombre, String email, String password) {
        try {
            // ✅ Validar primero
            validarDatosRegistro(nombre, email, password);
            
            // ✅ Verificar unicidad
            verificarEmailUnico(email);
            
            // ✅ Crear usuario
            Usuario usuario = crearNuevoUsuario(nombre, email, password);
            
            // ✅ Intentar enviar email de bienvenida
            enviarEmailBienvenidaSinFallar(usuario);
            
            logger.info("Usuario registrado exitosamente: {}", email);
            return usuario;
            
        } catch (UsuarioException e) {
            // ✅ Re-lanzar excepciones de dominio
            logger.warn("Error de validación en registro: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // ✅ Convertir excepciones inesperadas
            logger.error("Error inesperado registrando usuario: " + email, e);
            throw new UsuarioException("Error interno al registrar usuario", e);
        }
    }
    
    private void validarDatosRegistro(String nombre, String email, String password) {
        if (StringUtils.isBlank(nombre)) {
            throw new UsuarioException("El nombre es obligatorio");
        }
        
        if (StringUtils.isBlank(email)) {
            throw new UsuarioException("El email es obligatorio");
        }
        
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new UsuarioException("Formato de email inválido: " + email);
        }
        
        if (StringUtils.isBlank(password) || password.length() < 8) {
            throw new UsuarioException("La contraseña debe tener al menos 8 caracteres");
        }
    }
    
    private void verificarEmailUnico(String email) {
        if (repositorioUsuario.findByEmail(email).isPresent()) {
            throw new EmailYaRegistradoException(email);
        }
    }
    
    private Usuario crearNuevoUsuario(String nombre, String email, String password) {
        try {
            Usuario usuario = new Usuario(nombre, email, password);
            return repositorioUsuario.save(usuario);
            
        } catch (DataIntegrityViolationException e) {
            // ✅ Proporcionar contexto específico
            throw new UsuarioException(
                "Error de integridad al crear usuario con email: " + email, e);
        }
    }
    
    // ✅ Método que no falla la operación principal si hay error secundario
    private void enviarEmailBienvenidaSinFallar(Usuario usuario) {
        try {
            servicioEmail.enviarBienvenida(usuario.getEmail(), usuario.getNombre());
            
        } catch (Exception e) {
            // ✅ Log del error pero no interrumpe el flujo principal
            logger.warn("No se pudo enviar email de bienvenida a: {} - Error: {}", 
                       usuario.getEmail(), e.getMessage());
        }
    }
}

// ✅ Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EmailYaRegistradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse manejarEmailDuplicado(EmailYaRegistradoException e) {
        logger.warn("Intento de registro con email duplicado: {}", e.getMessage());
        
        return ErrorResponse.builder()
            .codigo("EMAIL_DUPLICADO")
            .mensaje(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    @ExceptionHandler(UsuarioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse manejarErrorUsuario(UsuarioException e) {
        logger.warn("Error de validación de usuario: {}", e.getMessage());
        
        return ErrorResponse.builder()
            .codigo("VALIDACION_USUARIO")
            .mensaje(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse manejarErrorGeneral(Exception e) {
        logger.error("Error inesperado en la aplicación", e);
        
        return ErrorResponse.builder()
            .codigo("ERROR_INTERNO")
            .mensaje("Ha ocurrido un error interno. Contacte al administrador.")
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

---

## 🎯 Resumen de Clean Code

### ✅ Checklist Rápido

#### Nombres ✅
- [ ] ¿Los nombres revelan intención?
- [ ] ¿Son pronunciables y buscables?
- [ ] ¿Evitan información engañosa?
- [ ] ¿Son distintivos y no tienen ruido mental?

#### Funciones ✅
- [ ] ¿Son pequeñas (< 20 líneas)?
- [ ] ¿Hacen una sola cosa?
- [ ] ¿Tienen un nivel de abstracción?
- [ ] ¿Nombre descriptivo?

#### Comentarios ✅
- [ ] ¿Son necesarios?
- [ ] ¿Añaden valor?
- [ ] ¿Están actualizados?
- [ ] ¿Explican POR QUÉ, no QUÉ?

#### Formato ✅
- [ ] ¿Es consistente?
- [ ] ¿Facilita la lectura?
- [ ] ¿Agrupa conceptos relacionados?

#### Objetos ✅
- [ ] ¿Exponen comportamiento?
- [ ] ¿Ocultan datos?
- [ ] ¿Respetan la Ley de Demeter?

#### Errores ✅
- [ ] ¿Usan excepciones apropiadas?
- [ ] ¿Proporcionan contexto?
- [ ] ¿No interrumpen flujo principal innecesariamente?

---

*Documento creado: 13/07/2025*
*Principios de Robert C. Martin aplicados a Spring Boot*
