package Krisseldu.Krisseldu.model;

public class UsuarioEjercicioVideo {
    private Long usuarioId;
    private Long ejercicioId;
    private String videoPath;

    public UsuarioEjercicioVideo() {}

    public UsuarioEjercicioVideo(Long usuarioId, Long ejercicioId, String videoPath) {
        this.usuarioId = usuarioId;
        this.ejercicioId = ejercicioId;
        this.videoPath = videoPath;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
}
