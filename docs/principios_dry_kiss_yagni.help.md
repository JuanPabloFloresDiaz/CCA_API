# Principios DRY, KISS y YAGNI

## 🎯 Introducción

Estos son tres principios fundamentales en el desarrollo de software que, junto con SOLID, forman la base de un código limpio, mantenible y eficiente.

---

## 🔄 DRY - Don't Repeat Yourself

### Definición
**No te repitas**: Cada pieza de conocimiento debe tener una representación única, inequívoca y autoritativa dentro del sistema.

### ❌ Ejemplo VIOLANDO DRY

```java
@RestController
public class UsuarioController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @PostMapping("/usuarios")
    public ResponseEntity<String> crearUsuario(@RequestBody Usuario usuario) {
        // ❌ Validación repetida
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email es requerido");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body("Formato de email inválido");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nombre es requerido");
        }
        if (usuario.getNombre().length() < 2 || usuario.getNombre().length() > 100) {
            return ResponseEntity.badRequest().body("Nombre debe tener entre 2 y 100 caracteres");
        }
        
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario creado exitosamente");
    }
    
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<String> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        // ❌ MISMA VALIDACIÓN REPETIDA
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email es requerido");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body("Formato de email inválido");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nombre es requerido");
        }
        if (usuario.getNombre().length() < 2 || usuario.getNombre().length() > 100) {
            return ResponseEntity.badRequest().body("Nombre debe tener entre 2 y 100 caracteres");
        }
        
        usuario.setId(id);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario actualizado exitosamente");
    }
}

// ❌ Más repetición en el servicio
@Service
public class UsuarioService {
    
    public void enviarEmailBienvenida(Usuario usuario) {
        // ❌ Lógica de email repetida
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email es requerido");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        
        String mensaje = "Bienvenido " + usuario.getNombre() + "!";
        // Enviar email...
    }
    
    public void enviarEmailRecuperacion(Usuario usuario) {
        // ❌ MISMA VALIDACIÓN OTRA VEZ
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email es requerido");
        }
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        
        String mensaje = "Recupera tu contraseña " + usuario.getNombre();
        // Enviar email...
    }
}
```

### ✅ Ejemplo SIGUIENDO DRY

```java
// ✅ Validador centralizado
@Component
public class ValidadorUsuario {
    
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    public void validarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
        validarNombre(usuario.getNombre());
    }
    
    public void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email es requerido");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Formato de email inválido");
        }
    }
    
    public void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("Nombre es requerido");
        }
        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new ValidationException("Nombre debe tener entre 2 y 100 caracteres");
        }
    }
}

// ✅ DTO con validaciones Bean Validation
public class UsuarioRequest {
    @NotBlank(message = "Email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    // getters y setters...
}

// ✅ Controller limpio sin repetición
@RestController
@Validated
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @PostMapping("/usuarios")
    public ResponseEntity<ApiResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(new ApiResponse("Usuario creado exitosamente", usuario));
    }
    
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse> actualizarUsuario(
            @PathVariable Long id, 
            @Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(new ApiResponse("Usuario actualizado exitosamente", usuario));
    }
}

// ✅ Servicio base para emails
@Service
public class ServicioEmailBase {
    
    @Autowired
    private ValidadorUsuario validador;
    
    protected void enviarEmail(Usuario usuario, String asunto, String mensaje) {
        validador.validarEmail(usuario.getEmail()); // ✅ Una sola validación centralizada
        
        // Lógica común de envío de email
        EmailDto email = EmailDto.builder()
            .destinatario(usuario.getEmail())
            .asunto(asunto)
            .mensaje(mensaje)
            .build();
            
        // Enviar email...
    }
}

@Service
public class UsuarioService extends ServicioEmailBase {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ValidadorUsuario validador;
    
    @Transactional
    public Usuario crearUsuario(UsuarioRequest request) {
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail());
        usuario = usuarioRepository.save(usuario);
        
        enviarEmailBienvenida(usuario);
        return usuario;
    }
    
    public void enviarEmailBienvenida(Usuario usuario) {
        String mensaje = generarMensajeBienvenida(usuario.getNombre());
        enviarEmail(usuario, "¡Bienvenido!", mensaje); // ✅ Reutiliza método base
    }
    
    public void enviarEmailRecuperacion(Usuario usuario) {
        String mensaje = generarMensajeRecuperacion(usuario.getNombre());
        enviarEmail(usuario, "Recuperar contraseña", mensaje); // ✅ Reutiliza método base
    }
    
    // ✅ Métodos específicos para generar mensajes
    private String generarMensajeBienvenida(String nombre) {
        return "Bienvenido " + nombre + "! Tu cuenta ha sido creada exitosamente.";
    }
    
    private String generarMensajeRecuperacion(String nombre) {
        return "Hola " + nombre + ", haz clic en el enlace para recuperar tu contraseña.";
    }
}
```

---

## 💋 KISS - Keep It Simple, Stupid

### Definición
**Manténlo simple**: La mayoría de sistemas funcionan mejor si se mantienen simples en lugar de complicados. La simplicidad debe ser un objetivo clave en el diseño.

### ❌ Ejemplo VIOLANDO KISS

```java
// ❌ Sobrecomplejo e innecesario
@Service
public class CalculadoraDescuentoCompleja {
    
    @Autowired
    private Map<String, EstrategiaDescuento> estrategias;
    
    @Autowired
    private FactoryDescuentos factoryDescuentos;
    
    @Autowired
    private ValidadorDescuentos validadorDescuentos;
    
    @Autowired
    private CacheDescuentos cacheDescuentos;
    
    public ResultadoDescuento calcularDescuentoAvanzado(
            ParametrosDescuento parametros,
            ContextoDescuento contexto,
            MetadataDescuento metadata) throws DescuentoException {
        
        // ❌ Demasiada abstracción para algo simple
        ValidacionResultado validacion = validadorDescuentos
            .validarParametrosCompletos(parametros, contexto, metadata);
        
        if (!validacion.esValido()) {
            throw new DescuentoException(validacion.getMensajesError());
        }
        
        String claveCache = generarClaveCache(parametros, contexto, metadata);
        ResultadoDescuento resultadoCache = cacheDescuentos.obtener(claveCache);
        
        if (resultadoCache != null) {
            return resultadoCache;
        }
        
        EstrategiaDescuento estrategia = factoryDescuentos
            .crearEstrategia(parametros.getTipoCliente(), contexto.getCanal());
        
        CalculadoraBase calculadora = estrategia.obtenerCalculadora();
        ProcesadorDescuento procesador = calculadora.obtenerProcesador();
        
        ResultadoDescuento resultado = procesador.procesar(
            parametros, contexto, metadata, estrategia);
        
        cacheDescuentos.almacenar(claveCache, resultado);
        
        return resultado;
    }
    
    // ❌ Método innecesariamente complejo para generar una clave
    private String generarClaveCache(
            ParametrosDescuento parametros,
            ContextoDescuento contexto,
            MetadataDescuento metadata) {
        
        return new StringBuilder()
            .append(parametros.getTipoCliente())
            .append("_")
            .append(parametros.getMontoBase().toString())
            .append("_")
            .append(contexto.getCanal())
            .append("_")
            .append(contexto.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .append("_")
            .append(metadata.getVersion())
            .append("_")
            .append(metadata.getRegion())
            .toString();
    }
}
```

### ✅ Ejemplo SIGUIENDO KISS

```java
// ✅ Simple y directo
@Service
public class CalculadoraDescuento {
    
    public BigDecimal calcularDescuento(String tipoCliente, BigDecimal precio) {
        // ✅ Simple, claro y fácil de entender
        switch (tipoCliente.toUpperCase()) {
            case "VIP":
                return precio.multiply(BigDecimal.valueOf(0.20)); // 20%
            case "PREMIUM":
                return precio.multiply(BigDecimal.valueOf(0.15)); // 15%
            case "REGULAR":
                return precio.multiply(BigDecimal.valueOf(0.10)); // 10%
            default:
                return BigDecimal.ZERO;
        }
    }
    
    // ✅ Si necesitas más lógica, añádela gradualmente
    public BigDecimal calcularDescuentoConFecha(String tipoCliente, BigDecimal precio, LocalDate fecha) {
        BigDecimal descuentoBase = calcularDescuento(tipoCliente, precio);
        
        // ✅ Descuento adicional para fin de año
        if (fecha.getMonthValue() == 12) {
            descuentoBase = descuentoBase.multiply(BigDecimal.valueOf(1.1)); // 10% extra
        }
        
        return descuentoBase;
    }
}

// ✅ Si crece la complejidad, entonces extraer
@Service
public class CalculadoraDescuentoAvanzado {
    
    private final Map<String, BigDecimal> descuentosPorTipo = Map.of(
        "VIP", BigDecimal.valueOf(0.20),
        "PREMIUM", BigDecimal.valueOf(0.15),
        "REGULAR", BigDecimal.valueOf(0.10)
    );
    
    public BigDecimal calcularDescuento(String tipoCliente, BigDecimal precio, LocalDate fecha) {
        BigDecimal porcentajeDescuento = descuentosPorTipo.getOrDefault(
            tipoCliente.toUpperCase(), 
            BigDecimal.ZERO
        );
        
        BigDecimal descuento = precio.multiply(porcentajeDescuento);
        
        // ✅ Solo añadir complejidad cuando realmente se necesite
        if (esTemporadaAlta(fecha)) {
            descuento = descuento.multiply(BigDecimal.valueOf(1.1));
        }
        
        return descuento;
    }
    
    private boolean esTemporadaAlta(LocalDate fecha) {
        return fecha.getMonthValue() == 12 || fecha.getMonthValue() == 1;
    }
}
```

---

## 🚫 YAGNI - You Aren't Gonna Need It

### Definición
**No lo vas a necesitar**: No implementes funcionalidad hasta que realmente la necesites. No añadas funcionalidad basándote en especulaciones sobre necesidades futuras.

### ❌ Ejemplo VIOLANDO YAGNI

```java
// ❌ Sobrecargado con funcionalidades "por si acaso"
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    private String password;
    
    // ❌ Campos que "tal vez" se necesiten en el futuro
    private String segundoNombre;
    private String apellidoMaterno;
    private String apellidoPaterno;
    private LocalDate fechaNacimiento;
    private String numeroTelefono;
    private String telefonoAlternativo;
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String pais;
    private String genero;
    private String estadoCivil;
    private String profesion;
    private String empresa;
    private String cargoEmpresa;
    private BigDecimal salario;
    private String nivelEducacion;
    private String universidad;
    private String carrera;
    private Integer anoGraduacion;
    private String idiomas;
    private String aficiones;
    private String redesSociales;
    private String tipoSuscripcion;
    private LocalDate fechaVencimientoSuscripcion;
    private Boolean recibirNotificaciones;
    private Boolean recibirPromociones;
    private Boolean recibirNewsletter;
    private String preferenciasComunicacion;
    private String zonaHoraria;
    private String monedaPreferida;
    private String formatoFecha;
    private String tema;
    private String configuracionPrivacidad;
    
    // ❌ Métodos que "podrían" ser útiles
    public Integer calcularEdad() {
        return fechaNacimiento != null ? 
            Period.between(fechaNacimiento, LocalDate.now()).getYears() : null;
    }
    
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder(nombre);
        if (segundoNombre != null) sb.append(" ").append(segundoNombre);
        if (apellidoPaterno != null) sb.append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null) sb.append(" ").append(apellidoMaterno);
        return sb.toString();
    }
    
    public Boolean esMayorDeEdad() {
        return calcularEdad() != null && calcularEdad() >= 18;
    }
    
    public String getIniciales() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null) sb.append(nombre.charAt(0));
        if (segundoNombre != null) sb.append(segundoNombre.charAt(0));
        if (apellidoPaterno != null) sb.append(apellidoPaterno.charAt(0));
        return sb.toString();
    }
    
    // ... muchos más métodos "por si acaso"
}

// ❌ Repository con métodos que nadie ha pedido
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    List<Usuario> findByNombre(String nombre);
    List<Usuario> findByEmail(String email);
    
    // ❌ Métodos que "podrían" ser útiles algún día
    List<Usuario> findBySegundoNombre(String segundoNombre);
    List<Usuario> findByApellidoPaterno(String apellidoPaterno);
    List<Usuario> findByApellidoMaterno(String apellidoMaterno);
    List<Usuario> findByFechaNacimientoBetween(LocalDate inicio, LocalDate fin);
    List<Usuario> findByGenero(String genero);
    List<Usuario> findByEstadoCivil(String estadoCivil);
    List<Usuario> findByProfesion(String profesion);
    List<Usuario> findByEmpresa(String empresa);
    List<Usuario> findBySalarioBetween(BigDecimal min, BigDecimal max);
    List<Usuario> findByNivelEducacion(String nivel);
    List<Usuario> findByUniversidad(String universidad);
    List<Usuario> findByCarrera(String carrera);
    List<Usuario> findByAnoGraduacionBetween(Integer inicio, Integer fin);
    List<Usuario> findByPais(String pais);
    List<Usuario> findByCiudad(String ciudad);
    
    // ❌ Consultas complejas que nadie va a usar
    @Query("SELECT u FROM Usuario u WHERE u.salario > :salario AND u.nivelEducacion = :educacion")
    List<Usuario> findUsuariosCalificados(@Param("salario") BigDecimal salario, 
                                        @Param("educacion") String educacion);
}
```

### ✅ Ejemplo SIGUIENDO YAGNI

```java
// ✅ Solo lo que realmente se necesita AHORA
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    // ✅ Solo constructores, getters y setters necesarios
    public Usuario() {}
    
    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }
    
    // getters y setters...
}

// ✅ Repository con solo los métodos que realmente se usan
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByActivoTrue();
    
    // ✅ Solo agregar métodos cuando realmente se necesiten
}

// ✅ Servicio simple que hace solo lo necesario
@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional
    public Usuario crearUsuario(String nombre, String email, String password) {
        // ✅ Solo validaciones que realmente se necesitan
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new UsuarioException("El email ya está registrado");
        }
        
        String passwordEncriptado = passwordEncoder.encode(password);
        Usuario usuario = new Usuario(nombre, email, passwordEncriptado);
        
        return usuarioRepository.save(usuario);
    }
    
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }
    
    // ✅ Solo agregar métodos cuando un caso de uso real los requiera
}
```

### ✅ Evolución YAGNI - Agregar funcionalidad cuando se necesite

```java
// ✅ Primera iteración: solo lo básico
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}

// ✅ Segunda iteración: se necesitó categorización
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    
    // ✅ Agregado cuando realmente se necesitó
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}

// ✅ Tercera iteración: se necesitó auditoría
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    
    // ✅ Agregado cuando el negocio lo requirió
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
}
```

---

## 🎯 Resumen y Mejores Prácticas

### ✅ Aplicando los Tres Principios Juntos

```java
// ✅ DRY + KISS + YAGNI en acción
@Service
public class ServicioAutenticacion {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ✅ KISS: Método simple y directo
    // ✅ YAGNI: Solo valida lo que necesita ahora
    public boolean autenticar(String email, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        
        if (usuario.isEmpty()) {
            return false;
        }
        
        return passwordEncoder.matches(password, usuario.get().getPassword());
    }
    
    // ✅ DRY: Reutiliza validación existente
    // ✅ KISS: Lógica clara y simple
    public Usuario registrar(String nombre, String email, String password) {
        validarDatosUsuario(nombre, email, password); // ✅ DRY
        
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new UsuarioExistenteException("Email ya registrado");
        }
        
        String passwordEncriptado = passwordEncoder.encode(password);
        Usuario usuario = new Usuario(nombre, email, passwordEncriptado);
        
        return usuarioRepository.save(usuario);
    }
    
    // ✅ DRY: Método centralizado para validación
    private void validarDatosUsuario(String nombre, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("Nombre es requerido");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email inválido");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password debe tener al menos 6 caracteres");
        }
    }
}
```

### 📋 Checklist para Aplicar los Principios

#### DRY ✅
- [ ] ¿Hay código duplicado que pueda extraerse?
- [ ] ¿Las validaciones están centralizadas?
- [ ] ¿Los algoritmos complejos están en un solo lugar?
- [ ] ¿Las configuraciones están externalizadas?

#### KISS ✅
- [ ] ¿El código es fácil de leer y entender?
- [ ] ¿Se puede simplificar sin perder funcionalidad?
- [ ] ¿Las abstracciones son necesarias?
- [ ] ¿Un desarrollador junior puede entenderlo?

#### YAGNI ✅
- [ ] ¿Cada línea de código resuelve un requisito actual?
- [ ] ¿Los métodos implementados se están usando?
- [ ] ¿Las configuraciones son para casos reales?
- [ ] ¿Se está especulando sobre el futuro?

---

## 🔧 Herramientas para Detectar Violaciones

### SonarQube Rules
```bash
# Detectar duplicación de código
sonar.cpd.minimum=100

# Detectar complejidad ciclomática alta
sonar.java.cyclomaticComplexity=10

# Detectar métodos no utilizados
sonar.java.unusedPrivateMethod=true
```

### Scripts de análisis
```bash
#!/bin/bash
# detect_duplicates.sh

echo "🔍 Buscando código duplicado..."
find . -name "*.java" -exec grep -l "if.*email.*null" {} \;

echo "🔍 Buscando métodos largos..."
find . -name "*.java" -exec awk '/^[[:space:]]*public|private|protected/ {start=NR} /^[[:space:]]*}/ {if(NR-start>30) print FILENAME":"start"-"NR}' {} \;

echo "🔍 Buscando clases con muchos campos..."
find . -name "*.java" -exec awk '/private.*String|Integer|Long/ {count++} END {if(count>10) print FILENAME":"count" fields"}' {} \;
```

---

*Documento creado: 13/07/2025*
*Principios fundamentales para código limpio y mantenible*
