package Krisseldu.Krisseldu.model;

import java.sql.Timestamp;

public class NotificacionDTO {
    private Long id;
    private Long usuarioId;
    private Long mensajeId;
    private String vista;
    private boolean leido;
    private Timestamp fecha;
    private String contenidoMensaje;

    // Constructor vacío
    public NotificacionDTO() {}

    // Constructor con todos los campos
    public NotificacionDTO(Long id, Long usuarioId, Long mensajeId, String vista, boolean leido, Timestamp fecha, String contenidoMensaje) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.mensajeId = mensajeId;
        this.vista = vista;
        this.leido = leido;
        this.fecha = fecha;
        this.contenidoMensaje = contenidoMensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getMensajeId() {
        return mensajeId;
    }

    public void setMensajeId(Long mensajeId) {
        this.mensajeId = mensajeId;
    }

    public String getVista() {
        return vista;
    }

    public void setVista(String vista) {
        this.vista = vista;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getContenidoMensaje() {
        return contenidoMensaje;
    }

    public void setContenidoMensaje(String contenidoMensaje) {
        this.contenidoMensaje = contenidoMensaje;
    }
}
