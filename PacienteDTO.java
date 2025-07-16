package Krisseldu.Krisseldu.model;

import java.sql.Timestamp;

/**
 * DTO que combina la información básica del paciente,
 * su condición principal y el último estado de asistencia.
 */
public class PacienteDTO {

    private Long id;
    private String nombre;
    private String dni;
    private int edad;

    private Long condicionId;     // FK a condiciones (puede ser null si no asignada)
    private String condicion;     // Nombre de la condición

    private String asistenciaEstado;
    private Timestamp asistenciaFecha;
    private String video;

    // ==== GETTERS Y SETTERS ====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public Long getCondicionId() { return condicionId; }
    public void setCondicionId(Long condicionId) { this.condicionId = condicionId; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public String getAsistenciaEstado() { return asistenciaEstado; }
    public void setAsistenciaEstado(String asistenciaEstado) { this.asistenciaEstado = asistenciaEstado; }

    public Timestamp getAsistenciaFecha() { return asistenciaFecha; }
    public void setAsistenciaFecha(Timestamp asistenciaFecha) { this.asistenciaFecha = asistenciaFecha; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    /**
     * Devuelve true si el paciente tiene una condición válida asignada.
     */
    public boolean tieneCondicion() {
        return condicionId != null && condicionId > 0;
    }

    @Override
    public String toString() {
        return "PacienteDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", dni='" + dni + '\'' +
                ", edad=" + edad +
                ", condicionId=" + condicionId +
                ", condicion='" + condicion + '\'' +
                ", asistenciaEstado='" + asistenciaEstado + '\'' +
                ", asistenciaFecha=" + asistenciaFecha +
                ", video='" + video + '\'' +
                '}';
    }
}
