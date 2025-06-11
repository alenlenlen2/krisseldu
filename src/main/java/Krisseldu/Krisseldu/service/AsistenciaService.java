package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.Asistencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsistenciaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Método para registrar asistencia
    public void registrarAsistencia(Asistencia asistencia) {
        String sql = "INSERT INTO asistencia (usuario_id, ejercicio_id, asistencia, video) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, asistencia.getUsuarioId(), asistencia.getEjercicioId(), asistencia.getAsistencia(), asistencia.getVideo());
    }

    // Método para actualizar la asistencia (sin video)
    public void actualizarAsistencia(Long asistenciaId, String estadoAsistencia) {
        String sql = "UPDATE asistencia SET asistencia = ? WHERE id = ?";
        jdbcTemplate.update(sql, estadoAsistencia, asistenciaId);
    }

    // Método actualizado para incluir video (este método está relacionado con el HTML)
    public void actualizarAsistencia(Long asistenciaId, String estadoAsistencia, String video) {
        String sql = "UPDATE asistencia SET asistencia = ?, video = ? WHERE id = ?";
        jdbcTemplate.update(sql, estadoAsistencia, video, asistenciaId);
    }

    // Obtener asistencia por ejercicio y usuario
    public Asistencia obtenerAsistencia(Long ejercicioId, Long usuarioId) {
        String sql = "SELECT id, usuario_id, ejercicio_id, asistencia, video FROM asistencia WHERE ejercicio_id = ? AND usuario_id = ?";
        List<Asistencia> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Asistencia a = new Asistencia();
            a.setId(rs.getLong("id"));
            a.setUsuarioId(rs.getLong("usuario_id"));
            a.setEjercicioId(rs.getLong("ejercicio_id"));
            a.setAsistencia(rs.getString("asistencia"));
            a.setVideo(rs.getString("video"));
            return a;
        }, ejercicioId, usuarioId);

        return lista.isEmpty() ? null : lista.get(0);
    }

    // Método para registrar o actualizar la asistencia de un ejercicio (en este caso, incluye el video)
    public void registrarOActualizarAsistencia(Long ejercicioId, Long usuarioId, String estadoAsistencia, String video) {
        Asistencia asistencia = obtenerAsistencia(ejercicioId, usuarioId);

        // Si la asistencia no existe, registramos una nueva
        if (asistencia == null) {
            asistencia = new Asistencia();
            asistencia.setUsuarioId(usuarioId);
            asistencia.setEjercicioId(ejercicioId);
            asistencia.setAsistencia(estadoAsistencia);
            asistencia.setVideo(video);
            registrarAsistencia(asistencia);
        } else {
            // Si ya existe, actualizamos la asistencia
            actualizarAsistencia(asistencia.getId(), estadoAsistencia, video);
        }
    }

    // Método para marcar asistencia como "realizado"
    public void marcarAsistenciaRealizada(Long ejercicioId, Long usuarioId, String video) {
        Asistencia asistenciaExistente = obtenerAsistencia(ejercicioId, usuarioId);
        if (asistenciaExistente != null) {
            // Actualizamos la asistencia para marcarla como realizada
            actualizarAsistencia(asistenciaExistente.getId(), "realizado", video);
        } else {
            // Si no existe, registramos una nueva asistencia como realizada
            Asistencia asistencia = new Asistencia();
            asistencia.setEjercicioId(ejercicioId);
            asistencia.setUsuarioId(usuarioId);
            asistencia.setAsistencia("realizado");
            asistencia.setVideo(video);
            registrarAsistencia(asistencia);
        }
    }
}
