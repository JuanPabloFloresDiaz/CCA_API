# Principios SOLID con Spring Boot, JPA e Hibernate

## 🎯 ¿Qué son los Principios SOLID?

Los principios SOLID son cinco principios de diseño de software que ayudan a crear código más mantenible, flexible y escalable. Fueron introducidos por Robert C. Martin (Uncle Bob).

---

## 🔤 S - Single Responsibility Principle (SRP)

### Definición
**Una clase debe tener una sola razón para cambiar**, es decir, debe tener una única responsabilidad.

### ❌ Ejemplo MALO - Violando SRP

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    private String password;
    
    // ❌ MALO: La entidad no debería manejar validaciones complejas
    public boolean validarEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    // ❌ MALO: La entidad no debería manejar encriptación
    public void encriptarPassword() {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    // ❌ MALO: La entidad no debería enviar emails
    public void enviarEmailBienvenida() {
        // Lógica para enviar email
        System.out.println("Enviando email a: " + email);
    }
    
    // getters y setters...
}
```

### ✅ Ejemplo BUENO - Siguiendo SRP

```java
// ✅ ENTIDAD: Solo representa datos
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    // Solo getters, setters y constructores
    public Usuario() {}
    
    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }
    
    // getters y setters...
}

// ✅ SERVICIO: Maneja validaciones de negocio
@Service
public class ValidadorUsuario {
    
    public boolean validarEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
    
    public boolean validarPassword(String password) {
        return password != null && password.length() >= 8;
    }
}

// ✅ SERVICIO: Maneja encriptación
@Service
public class ServicioEncriptacion {
    
    public String encriptar(String textoPlano) {
        return BCrypt.hashpw(textoPlano, BCrypt.gensalt());
    }
    
    public boolean verificar(String textoPlano, String hash) {
        return BCrypt.checkpw(textoPlano, hash);
    }
}

// ✅ SERVICIO: Maneja envío de emails
@Service
public class ServicioEmail {
    
    public void enviarEmailBienvenida(Usuario usuario) {
        // Lógica específica para envío de emails
        System.out.println("Enviando email de bienvenida a: " + usuario.getEmail());
    }
}
```

---

## 🔓 O - Open/Closed Principle (OCP)

### Definición
**Las clases deben estar abiertas para extensión pero cerradas para modificación**. Deberías poder añadir nuevas funcionalidades sin cambiar el código existente.

### ❌ Ejemplo MALO - Violando OCP

```java
@Service
public class CalculadoraDescuento {
    
    public double calcularDescuento(String tipoCliente, double precio) {
        // ❌ MALO: Cada nuevo tipo requiere modificar este método
        switch (tipoCliente) {
            case "REGULAR":
                return precio * 0.05; // 5% descuento
            case "VIP":
                return precio * 0.15; // 15% descuento
            case "PREMIUM":
                return precio * 0.25; // 25% descuento
            default:
                return 0;
        }
        // Si quiero agregar "ESTUDIANTE", debo modificar este código ❌
    }
}
```

### ✅ Ejemplo BUENO - Siguiendo OCP

```java
// ✅ INTERFAZ: Define el contrato
public interface EstrategiaDescuento {
    double calcularDescuento(double precio);
    String getTipoCliente();
}

// ✅ IMPLEMENTACIONES: Cada tipo de cliente
@Component
public class DescuentoClienteRegular implements EstrategiaDescuento {
    @Override
    public double calcularDescuento(double precio) {
        return precio * 0.05; // 5%
    }
    
    @Override
    public String getTipoCliente() {
        return "REGULAR";
    }
}

@Component
public class DescuentoClienteVip implements EstrategiaDescuento {
    @Override
    public double calcularDescuento(double precio) {
        return precio * 0.15; // 15%
    }
    
    @Override
    public String getTipoCliente() {
        return "VIP";
    }
}

@Component
public class DescuentoClientePremium implements EstrategiaDescuento {
    @Override
    public double calcularDescuento(double precio) {
        return precio * 0.25; // 25%
    }
    
    @Override
    public String getTipoCliente() {
        return "PREMIUM";
    }
}

// ✅ NUEVA IMPLEMENTACIÓN: Sin modificar código existente
@Component
public class DescuentoEstudiante implements EstrategiaDescuento {
    @Override
    public double calcularDescuento(double precio) {
        return precio * 0.30; // 30% para estudiantes
    }
    
    @Override
    public String getTipoCliente() {
        return "ESTUDIANTE";
    }
}

// ✅ SERVICIO: Usa las estrategias sin conocer implementaciones específicas
@Service
public class ServicioDescuento {
    
    private final Map<String, EstrategiaDescuento> estrategias;
    
    public ServicioDescuento(List<EstrategiaDescuento> estrategiasList) {
        this.estrategias = estrategiasList.stream()
            .collect(Collectors.toMap(
                EstrategiaDescuento::getTipoCliente,
                Function.identity()
            ));
    }
    
    public double aplicarDescuento(String tipoCliente, double precio) {
        EstrategiaDescuento estrategia = estrategias.get(tipoCliente);
        return estrategia != null ? estrategia.calcularDescuento(precio) : 0;
    }
}
```

---

## 🔄 L - Liskov Substitution Principle (LSP)

### Definición
**Los objetos de una superclase deben ser reemplazables por objetos de sus subclases sin alterar el funcionamiento del programa**.

### ❌ Ejemplo MALO - Violando LSP

```java
// ❌ CLASE BASE
@MappedSuperclass
public abstract class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    protected String marca;
    protected String modelo;
    
    public abstract void acelerar();
    public abstract void frenar();
    public abstract void encender(); // ❌ Problemático para vehículos eléctricos
}

@Entity
@Table(name = "autos")
public class Auto extends Vehiculo {
    private String tipoMotor;
    
    @Override
    public void acelerar() {
        System.out.println("Auto acelerando con motor de combustión");
    }
    
    @Override
    public void frenar() {
        System.out.println("Auto frenando");
    }
    
    @Override
    public void encender() {
        System.out.println("Encendiendo motor de combustión");
    }
}

@Entity
@Table(name = "bicicletas")
public class Bicicleta extends Vehiculo {
    private int numeroMarchas;
    
    @Override
    public void acelerar() {
        System.out.println("Pedaleando más fuerte");
    }
    
    @Override
    public void frenar() {
        System.out.println("Usando frenos de la bicicleta");
    }
    
    @Override
    public void encender() {
        // ❌ VIOLA LSP: Una bicicleta no se "enciende"
        throw new UnsupportedOperationException("Una bicicleta no se enciende");
    }
}
```

### ✅ Ejemplo BUENO - Siguiendo LSP

```java
// ✅ CLASE BASE: Solo comportamientos comunes
@MappedSuperclass
public abstract class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    @Column(nullable = false)
    protected String marca;
    
    @Column(nullable = false)
    protected String modelo;
    
    // Comportamientos que TODOS los vehículos pueden hacer
    public abstract void acelerar();
    public abstract void frenar();
    public abstract void obtenerVelocidadMaxima();
    
    // getters y setters...
}

// ✅ INTERFAZ: Para vehículos con motor
public interface VehiculoConMotor {
    void encender();
    void apagar();
    String getTipoMotor();
}

// ✅ INTERFAZ: Para vehículos eléctricos
public interface VehiculoElectrico {
    void cargarBateria();
    int getNivelBateria();
}

@Entity
@Table(name = "autos")
public class Auto extends Vehiculo implements VehiculoConMotor {
    
    @Column(name = "tipo_motor")
    private String tipoMotor;
    
    @Override
    public void acelerar() {
        System.out.println("Auto acelerando");
    }
    
    @Override
    public void frenar() {
        System.out.println("Auto frenando");
    }
    
    @Override
    public void obtenerVelocidadMaxima() {
        System.out.println("Velocidad máxima: 200 km/h");
    }
    
    @Override
    public void encender() {
        System.out.println("Motor encendido");
    }
    
    @Override
    public void apagar() {
        System.out.println("Motor apagado");
    }
    
    @Override
    public String getTipoMotor() {
        return tipoMotor;
    }
}

@Entity
@Table(name = "bicicletas")
public class Bicicleta extends Vehiculo {
    
    @Column(name = "numero_marchas")
    private int numeroMarchas;
    
    @Override
    public void acelerar() {
        System.out.println("Pedaleando más fuerte");
    }
    
    @Override
    public void frenar() {
        System.out.println("Usando frenos de bicicleta");
    }
    
    @Override
    public void obtenerVelocidadMaxima() {
        System.out.println("Velocidad máxima: 50 km/h");
    }
    
    // ✅ No implementa VehiculoConMotor porque no tiene motor
}

@Entity
@Table(name = "autos_electricos")
public class AutoElectrico extends Vehiculo implements VehiculoElectrico {
    
    @Column(name = "nivel_bateria")
    private int nivelBateria;
    
    @Override
    public void acelerar() {
        System.out.println("Auto eléctrico acelerando silenciosamente");
    }
    
    @Override
    public void frenar() {
        System.out.println("Frenando y recuperando energía");
    }
    
    @Override
    public void obtenerVelocidadMaxima() {
        System.out.println("Velocidad máxima: 250 km/h");
    }
    
    @Override
    public void cargarBateria() {
        this.nivelBateria = 100;
        System.out.println("Batería cargada al 100%");
    }
    
    @Override
    public int getNivelBateria() {
        return nivelBateria;
    }
}

// ✅ SERVICIO: Puede trabajar con cualquier vehículo sin problemas
@Service
public class ServicioVehiculo {
    
    public void probarVehiculo(Vehiculo vehiculo) {
        // ✅ Funciona con CUALQUIER subtipo de Vehículo
        vehiculo.acelerar();
        vehiculo.frenar();
        vehiculo.obtenerVelocidadMaxima();
        
        // ✅ Comportamientos específicos solo si los implementa
        if (vehiculo instanceof VehiculoConMotor) {
            ((VehiculoConMotor) vehiculo).encender();
        }
        
        if (vehiculo instanceof VehiculoElectrico) {
            ((VehiculoElectrico) vehiculo).cargarBateria();
        }
    }
}
```

---

## 🔌 I - Interface Segregation Principle (ISP)

### Definición
**Los clientes no deben verse obligados a depender de interfaces que no usan**. Es mejor tener muchas interfaces específicas que una interfaz general.

### ❌ Ejemplo MALO - Violando ISP

```java
// ❌ INTERFAZ GRANDE: Obliga a implementar métodos innecesarios
public interface RepositorioCompleto<T> {
    // Operaciones básicas
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    void deleteById(Long id);
    
    // Operaciones de búsqueda avanzada
    List<T> findWithPagination(int page, int size);
    List<T> findByCustomCriteria(Map<String, Object> criteria);
    
    // Operaciones de cache
    void cacheEntity(T entity);
    void evictCache(Long id);
    void clearAllCache();
    
    // Operaciones de auditoria
    void logOperation(String operation, T entity);
    List<String> getAuditHistory(Long id);
    
    // Operaciones de exportación
    byte[] exportToCsv();
    byte[] exportToPdf();
    byte[] exportToExcel();
}

// ❌ IMPLEMENTACIÓN: Debe implementar TODO, aunque no lo necesite
@Repository
public class RepositorioUsuarioSimple implements RepositorioCompleto<Usuario> {
    
    @Autowired
    private EntityManager entityManager;
    
    @Override
    public Usuario save(Usuario entity) {
        return entityManager.merge(entity);
    }
    
    @Override
    public Optional<Usuario> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Usuario.class, id));
    }
    
    @Override
    public List<Usuario> findAll() {
        return entityManager.createQuery("SELECT u FROM Usuario u", Usuario.class)
                          .getResultList();
    }
    
    @Override
    public void deleteById(Long id) {
        Usuario usuario = entityManager.find(Usuario.class, id);
        if (usuario != null) {
            entityManager.remove(usuario);
        }
    }
    
    // ❌ FORZADO a implementar métodos que no necesita
    @Override
    public List<Usuario> findWithPagination(int page, int size) {
        throw new UnsupportedOperationException("No implementado");
    }
    
    @Override
    public void cacheEntity(Usuario entity) {
        throw new UnsupportedOperationException("No implementado");
    }
    
    @Override
    public byte[] exportToCsv() {
        throw new UnsupportedOperationException("No implementado");
    }
    
    // ... más métodos sin implementar
}
```

### ✅ Ejemplo BUENO - Siguiendo ISP

```java
// ✅ INTERFACES SEGREGADAS: Cada una con responsabilidad específica

// Operaciones básicas CRUD
public interface RepositorioBasico<T> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    void deleteById(Long id);
}

// Operaciones de búsqueda avanzada
public interface RepositorioBusquedaAvanzada<T> {
    List<T> findWithPagination(int page, int size);
    List<T> findByCustomCriteria(Map<String, Object> criteria);
}

// Operaciones de cache
public interface RepositorioConCache<T> {
    void cacheEntity(T entity);
    void evictCache(Long id);
    void clearAllCache();
}

// Operaciones de auditoría
public interface RepositorioAuditable<T> {
    void logOperation(String operation, T entity);
    List<String> getAuditHistory(Long id);
}

// Operaciones de exportación
public interface RepositorioExportable {
    byte[] exportToCsv();
    byte[] exportToPdf();
    byte[] exportToExcel();
}

// ✅ IMPLEMENTACIÓN SIMPLE: Solo lo que necesita
@Repository
public class RepositorioUsuarioBasico implements RepositorioBasico<Usuario> {
    
    @Autowired
    private EntityManager entityManager;
    
    @Override
    public Usuario save(Usuario entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }
    
    @Override
    public Optional<Usuario> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Usuario.class, id));
    }
    
    @Override
    public List<Usuario> findAll() {
        return entityManager.createQuery(
            "SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
            .getResultList();
    }
    
    @Override
    public void deleteById(Long id) {
        Usuario usuario = entityManager.find(Usuario.class, id);
        if (usuario != null) {
            entityManager.remove(usuario);
        }
    }
}

// ✅ IMPLEMENTACIÓN AVANZADA: Solo implementa las interfaces que necesita
@Repository
public class RepositorioUsuarioAvanzado implements 
    RepositorioBasico<Usuario>, 
    RepositorioBusquedaAvanzada<Usuario>,
    RepositorioConCache<Usuario> {
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private CacheManager cacheManager;
    
    // Implementación de RepositorioBasico
    @Override
    public Usuario save(Usuario entity) {
        Usuario saved = entityManager.merge(entity);
        cacheEntity(saved); // Usar funcionalidad de cache
        return saved;
    }
    
    // ... otras implementaciones básicas
    
    // Implementación de RepositorioBusquedaAvanzada
    @Override
    public List<Usuario> findWithPagination(int page, int size) {
        return entityManager.createQuery(
            "SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }
    
    @Override
    public List<Usuario> findByCustomCriteria(Map<String, Object> criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usuario> query = cb.createQuery(Usuario.class);
        Root<Usuario> root = query.from(Usuario.class);
        
        // Lógica para construir predicados dinámicos
        List<Predicate> predicates = new ArrayList<>();
        
        criteria.forEach((key, value) -> {
            if (value != null) {
                predicates.add(cb.equal(root.get(key), value));
            }
        });
        
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
    
    // Implementación de RepositorioConCache
    @Override
    @Cacheable("usuarios")
    public void cacheEntity(Usuario entity) {
        // Spring maneja el cache automáticamente con @Cacheable
    }
    
    @Override
    @CacheEvict(value = "usuarios", key = "#id")
    public void evictCache(Long id) {
        // Spring maneja la evicción automáticamente
    }
    
    @Override
    @CacheEvict(value = "usuarios", allEntries = true)
    public void clearAllCache() {
        // Spring limpia todo el cache automáticamente
    }
}

// ✅ SERVICIO: Usa solo las interfaces que necesita
@Service
public class ServicioUsuario {
    
    private final RepositorioBasico<Usuario> repositorioBasico;
    private final RepositorioBusquedaAvanzada<Usuario> repositorioBusqueda;
    
    // ✅ Inyección específica según necesidades
    public ServicioUsuario(
        RepositorioBasico<Usuario> repositorioBasico,
        @Qualifier("repositorioUsuarioAvanzado") RepositorioBusquedaAvanzada<Usuario> repositorioBusqueda) {
        this.repositorioBasico = repositorioBasico;
        this.repositorioBusqueda = repositorioBusqueda;
    }
    
    public Usuario crearUsuario(Usuario usuario) {
        return repositorioBasico.save(usuario);
    }
    
    public List<Usuario> buscarUsuariosPaginados(int page, int size) {
        return repositorioBusqueda.findWithPagination(page, size);
    }
}
```

---

## ⬇️ D - Dependency Inversion Principle (DIP)

### Definición
**Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones**. Las abstracciones no deben depender de detalles, los detalles deben depender de abstracciones.

### ❌ Ejemplo MALO - Violando DIP

```java
// ❌ CLASE DE BAJO NIVEL: Implementación concreta
@Repository
public class RepositorioUsuarioJPA {
    
    @Autowired
    private EntityManager entityManager;
    
    public Usuario guardar(Usuario usuario) {
        return entityManager.merge(usuario);
    }
    
    public Usuario buscarPorEmail(String email) {
        return entityManager.createQuery(
            "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
            .setParameter("email", email)
            .getSingleResult();
    }
}

@Service
public class ServicioNotificacion {
    
    public void enviarEmail(String destinatario, String mensaje) {
        // Implementación específica de email
        System.out.println("Enviando email a: " + destinatario);
        System.out.println("Mensaje: " + mensaje);
    }
}

// ❌ CLASE DE ALTO NIVEL: Depende directamente de implementaciones concretas
@Service
public class ServicioRegistroUsuario {
    
    // ❌ Dependencia directa de implementación concreta
    private final RepositorioUsuarioJPA repositorio;
    private final ServicioNotificacion servicioNotificacion;
    
    public ServicioRegistroUsuario(
        RepositorioUsuarioJPA repositorio,
        ServicioNotificacion servicioNotificacion) {
        this.repositorio = repositorio;
        this.servicioNotificacion = servicioNotificacion;
    }
    
    public Usuario registrarUsuario(String nombre, String email, String password) {
        // ❌ Acoplado a implementaciones específicas
        Usuario usuario = new Usuario(nombre, email, password);
        Usuario usuarioGuardado = repositorio.guardar(usuario);
        
        servicioNotificacion.enviarEmail(
            email, 
            "Bienvenido " + nombre + "!"
        );
        
        return usuarioGuardado;
    }
}
```

### ✅ Ejemplo BUENO - Siguiendo DIP

```java
// ✅ ABSTRACCIONES: Interfaces que definen contratos

public interface RepositorioUsuario {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    List<Usuario> buscarTodos();
    void eliminar(Long id);
}

public interface ServicioNotificacion {
    void enviarNotificacion(String destinatario, String mensaje, TipoNotificacion tipo);
}

public enum TipoNotificacion {
    EMAIL, SMS, PUSH
}

// ✅ IMPLEMENTACIONES: Dependen de abstracciones

@Repository
public class RepositorioUsuarioJPA implements RepositorioUsuario {
    
    @Autowired
    private EntityManager entityManager;
    
    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() == null) {
            entityManager.persist(usuario);
            return usuario;
        } else {
            return entityManager.merge(usuario);
        }
    }
    
    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(entityManager.find(Usuario.class, id));
    }
    
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        try {
            Usuario usuario = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                .setParameter("email", email)
                .getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Usuario> buscarTodos() {
        return entityManager.createQuery(
            "SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
            .getResultList();
    }
    
    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = entityManager.find(Usuario.class, id);
        if (usuario != null) {
            entityManager.remove(usuario);
        }
    }
}

// ✅ IMPLEMENTACIÓN ALTERNATIVA: Puede ser fácilmente intercambiada
@Repository
@Profile("test")
public class RepositorioUsuarioMemoria implements RepositorioUsuario {
    
    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(idGenerator.getAndIncrement());
        }
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }
    
    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }
    
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.values().stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst();
    }
    
    @Override
    public List<Usuario> buscarTodos() {
        return new ArrayList<>(usuarios.values());
    }
    
    @Override
    public void eliminar(Long id) {
        usuarios.remove(id);
    }
}

@Service
public class ServicioNotificacionEmail implements ServicioNotificacion {
    
    @Override
    public void enviarNotificacion(String destinatario, String mensaje, TipoNotificacion tipo) {
        if (tipo == TipoNotificacion.EMAIL) {
            System.out.println("📧 Enviando EMAIL a: " + destinatario);
            System.out.println("Mensaje: " + mensaje);
        } else {
            throw new UnsupportedOperationException("Solo soporta emails");
        }
    }
}

@Service
@Profile("sms")
public class ServicioNotificacionSMS implements ServicioNotificacion {
    
    @Override
    public void enviarNotificacion(String destinatario, String mensaje, TipoNotificacion tipo) {
        switch (tipo) {
            case SMS:
                System.out.println("📱 Enviando SMS a: " + destinatario);
                break;
            case EMAIL:
                System.out.println("📧 Enviando EMAIL a: " + destinatario);
                break;
            case PUSH:
                System.out.println("🔔 Enviando PUSH a: " + destinatario);
                break;
        }
        System.out.println("Mensaje: " + mensaje);
    }
}

// ✅ CLASE DE ALTO NIVEL: Depende solo de abstracciones
@Service
@Transactional
public class ServicioRegistroUsuario {
    
    // ✅ Dependencias de abstracciones, no de implementaciones concretas
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioNotificacion servicioNotificacion;
    private final ValidadorUsuario validador;
    private final ServicioEncriptacion encriptacion;
    
    public ServicioRegistroUsuario(
        RepositorioUsuario repositorioUsuario,
        ServicioNotificacion servicioNotificacion,
        ValidadorUsuario validador,
        ServicioEncriptacion encriptacion) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioNotificacion = servicioNotificacion;
        this.validador = validador;
        this.encriptacion = encriptacion;
    }
    
    public Usuario registrarUsuario(String nombre, String email, String password) 
            throws UsuarioException {
        
        // Validaciones usando abstracción
        if (!validador.validarEmail(email)) {
            throw new UsuarioException("Email inválido");
        }
        
        if (!validador.validarPassword(password)) {
            throw new UsuarioException("Password debe tener al menos 8 caracteres");
        }
        
        // Verificar si el usuario ya existe
        if (repositorioUsuario.buscarPorEmail(email).isPresent()) {
            throw new UsuarioException("El email ya está registrado");
        }
        
        // Crear usuario con password encriptado
        String passwordEncriptado = encriptacion.encriptar(password);
        Usuario usuario = new Usuario(nombre, email, passwordEncriptado);
        
        // Guardar usando abstracción
        Usuario usuarioGuardado = repositorioUsuario.guardar(usuario);
        
        // Notificar usando abstracción
        servicioNotificacion.enviarNotificacion(
            email,
            "¡Bienvenido " + nombre + "! Tu cuenta ha sido creada exitosamente.",
            TipoNotificacion.EMAIL
        );
        
        return usuarioGuardado;
    }
    
    public Optional<Usuario> buscarPorEmail(String email) {
        return repositorioUsuario.buscarPorEmail(email);
    }
    
    public List<Usuario> listarUsuarios() {
        return repositorioUsuario.buscarTodos();
    }
}

// ✅ CONFIGURACIÓN: Spring maneja las dependencias
@Configuration
public class ConfiguracionServicios {
    
    @Bean
    @Primary
    public ServicioNotificacion servicioNotificacionPrincipal() {
        return new ServicioNotificacionEmail();
    }
    
    @Bean
    @ConditionalOnProperty(name = "notificacion.tipo", havingValue = "sms")
    public ServicioNotificacion servicioNotificacionSMS() {
        return new ServicioNotificacionSMS();
    }
}

// ✅ EXCEPCIÓN PERSONALIZADA
public class UsuarioException extends Exception {
    public UsuarioException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 🎯 Beneficios de Seguir SOLID

### ✅ Ventajas

1. **Mantenibilidad**: Código más fácil de mantener y modificar
2. **Escalabilidad**: Fácil agregar nuevas funcionalidades
3. **Testabilidad**: Fácil crear pruebas unitarias con mocks
4. **Reutilización**: Componentes reutilizables
5. **Flexibilidad**: Fácil intercambiar implementaciones

### 🧪 Ejemplo de Test con SOLID

```java
@ExtendWith(MockitoExtension.class)
class ServicioRegistroUsuarioTest {
    
    @Mock
    private RepositorioUsuario repositorioUsuario;
    
    @Mock
    private ServicioNotificacion servicioNotificacion;
    
    @Mock
    private ValidadorUsuario validador;
    
    @Mock
    private ServicioEncriptacion encriptacion;
    
    @InjectMocks
    private ServicioRegistroUsuario servicioRegistro;
    
    @Test
    void deberiaRegistrarUsuarioExitosamente() throws UsuarioException {
        // Given
        String nombre = "Juan Pérez";
        String email = "juan@example.com";
        String password = "password123";
        String passwordEncriptado = "encrypted_password";
        
        when(validador.validarEmail(email)).thenReturn(true);
        when(validador.validarPassword(password)).thenReturn(true);
        when(repositorioUsuario.buscarPorEmail(email)).thenReturn(Optional.empty());
        when(encriptacion.encriptar(password)).thenReturn(passwordEncriptado);
        
        Usuario usuarioEsperado = new Usuario(nombre, email, passwordEncriptado);
        usuarioEsperado.setId(1L);
        when(repositorioUsuario.guardar(any(Usuario.class))).thenReturn(usuarioEsperado);
        
        // When
        Usuario resultado = servicioRegistro.registrarUsuario(nombre, email, password);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo(nombre);
        assertThat(resultado.getEmail()).isEqualTo(email);
        
        verify(servicioNotificacion).enviarNotificacion(
            eq(email), 
            contains("Bienvenido"), 
            eq(TipoNotificacion.EMAIL)
        );
    }
    
    @Test
    void deberiaLanzarExcepcionSiEmailYaExiste() {
        // Given
        String email = "existente@example.com";
        when(validador.validarEmail(email)).thenReturn(true);
        when(validador.validarPassword(anyString())).thenReturn(true);
        when(repositorioUsuario.buscarPorEmail(email))
            .thenReturn(Optional.of(new Usuario()));
        
        // When & Then
        assertThrows(UsuarioException.class, () -> 
            servicioRegistro.registrarUsuario("Test", email, "password123")
        );
        
        verify(repositorioUsuario, never()).guardar(any());
        verify(servicioNotificacion, never()).enviarNotificacion(any(), any(), any());
    }
}
```

---

## 📚 Resumen

| Principio | Qué hace | Beneficio |
|-----------|----------|-----------|
| **S**RP | Una clase, una responsabilidad | Código más enfocado y mantenible |
| **O**CP | Abierto para extensión, cerrado para modificación | Fácil agregar funcionalidades |
| **L**SP | Subclases intercambiables | Polimorfismo confiable |
| **I**SP | Interfaces específicas | Sin dependencias innecesarias |
| **D**IP | Depender de abstracciones | Bajo acoplamiento, alta flexibilidad |

Los principios SOLID trabajando juntos crean un código robusto, mantenible y escalable, especialmente importante en aplicaciones Spring Boot complejas con JPA e Hibernate.

---

*Documento creado: 13/07/2025*
*Tecnologías: Spring Boot 3.0+, JPA, Hibernate*