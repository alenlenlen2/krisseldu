package Krisseldu.Krisseldu.dto;

import java.sql.Timestamp;

public class MensajeDTO {
    private Long id;
    private Long terapeutaId;
    private Long pacienteId;
    private String contenido;
    private Timestamp fecha;
    private boolean leido;
    private String video;  // URL o path del video asociado (puede ser null)

    // Constructor vacío
    public MensajeDTO() {}

    // Constructor con todos los campos
    public MensajeDTO(Long id, Long terapeutaId, Long pacienteId, String contenido, Timestamp fecha, boolean leido, String video) {
        this.id = id;
        this.terapeutaId = terapeutaId;
        this.pacienteId = pacienteId;
        this.contenido = contenido;
        this.fecha = fecha;
        this.leido = leido;
        this.video = video;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTerapeutaId() {
        return terapeutaId;
    }

    public void setTerapeutaId(Long terapeutaId) {
        this.terapeutaId = terapeutaId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
