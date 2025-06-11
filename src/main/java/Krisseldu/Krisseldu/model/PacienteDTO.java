package Krisseldu.Krisseldu.model;

import java.sql.Timestamp;

public class PacienteDTO {
    private Long id;
    private String nombre;
    private int edad;
    private String condicion;
    private String asistenciaEstado;
    private Timestamp asistenciaFecha;
    private String video;
    private String dni;  // Añadido el campo dni

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getAsistenciaEstado() {
        return asistenciaEstado;
    }

    public void setAsistenciaEstado(String asistenciaEstado) {
        this.asistenciaEstado = asistenciaEstado;
    }

    public Timestamp getAsistenciaFecha() {
        return asistenciaFecha;
    }

    public void setAsistenciaFecha(Timestamp asistenciaFecha) {
        this.asistenciaFecha = asistenciaFecha;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getDni() {  // Getter para dni
        return dni;
    }

    public void setDni(String dni) {  // Setter para dni
        this.dni = dni;
    }
}
