package Krisseldu.Krisseldu.model;

public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private int edad;
    private String dni;  // Usamos DNI en lugar de username
    private String email;
    private String password;
    private String rol; // 'TERAPEUTA', 'ADMIN', 'PACIENTE'
    private Long terapeutaId;

    // Constructores, Getters y Setters
    public Usuario() {}

    public Usuario(Long id, String nombre, String apellido, int edad, String dni, String email, String password, String rol, Long terapeutaId) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.dni = dni;  // Cambiado a DNI
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.terapeutaId = terapeutaId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getDni() { return dni; }  // Ahora el campo que usamos es DNI
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Long getTerapeutaId() { return terapeutaId; }
    public void setTerapeutaId(Long terapeutaId) { this.terapeutaId = terapeutaId; }
}
