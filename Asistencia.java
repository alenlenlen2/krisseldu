package Krisseldu.Krisseldu.model;

public class Asistencia {
    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private String asistencia;  // 'presente' o 'ausente'
    private java.sql.Timestamp fecha;
    private String video;       // Nuevo campo para el link de video

    public Asistencia() {}

    public Asistencia(Long id, Long usuarioId, Long ejercicioId, String asistencia, java.sql.Timestamp fecha, String video) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.ejercicioId = ejercicioId;
        this.asistencia = asistencia;
        this.fecha = fecha;
        this.video = video;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public String getAsistencia() { return asistencia; }
    public void setAsistencia(String asistencia) { this.asistencia = asistencia; }

    public java.sql.Timestamp getFecha() { return fecha; }
    public void setFecha(java.sql.Timestamp fecha) { this.fecha = fecha; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }
}
