package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.dto.MensajeDTO;
import Krisseldu.Krisseldu.model.NotificacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensajeService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void enviarMensaje(Long terapeutaId, Long pacienteId, String contenido) {
        String sqlInsertMensaje = "INSERT INTO mensajes (terapeuta_id, paciente_id, contenido) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlInsertMensaje, terapeutaId, pacienteId, contenido);

        Long mensajeId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        String sqlInsertNotificacion = "INSERT INTO notificaciones (usuario_id, mensaje_id, vista) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlInsertNotificacion, pacienteId, mensajeId, "home");
    }

    public List<MensajeDTO> obtenerMensajesNoLeidosPaciente(Long pacienteId) {
        String sql = """
            SELECT id, terapeuta_id, paciente_id, contenido, fecha, leido, video
            FROM mensajes
            WHERE paciente_id = ? AND leido = FALSE
            ORDER BY fecha DESC
            """;

        return jdbcTemplate.query(sql, new Object[]{pacienteId}, (rs, rowNum) -> {
            MensajeDTO dto = new MensajeDTO();
            dto.setId(rs.getLong("id"));
            dto.setTerapeutaId(rs.getLong("terapeuta_id"));
            dto.setPacienteId(rs.getLong("paciente_id"));
            dto.setContenido(rs.getString("contenido"));
            dto.setFecha(rs.getTimestamp("fecha"));
            dto.setLeido(rs.getBoolean("leido"));
            dto.setVideo(rs.getString("video"));
            return dto;
        });
    }

    public List<MensajeDTO> obtenerMensajesPorPaciente(Long pacienteId) {
        String sql = """
            SELECT id, terapeuta_id, paciente_id, contenido, fecha, leido, video
            FROM mensajes
            WHERE paciente_id = ?
            ORDER BY fecha DESC
            """;

        return jdbcTemplate.query(sql, new Object[]{pacienteId}, (rs, rowNum) -> {
            MensajeDTO dto = new MensajeDTO();
            dto.setId(rs.getLong("id"));
            dto.setTerapeutaId(rs.getLong("terapeuta_id"));
            dto.setPacienteId(rs.getLong("paciente_id"));
            dto.setContenido(rs.getString("contenido"));
            dto.setFecha(rs.getTimestamp("fecha"));
            dto.setLeido(rs.getBoolean("leido"));
            dto.setVideo(rs.getString("video"));
            return dto;
        });
    }

    public List<NotificacionDTO> obtenerNotificacionesUsuario(Long usuarioId) {
        String sql = """
            SELECT n.id, n.usuario_id, n.mensaje_id, n.vista, n.leido, n.fecha,
                   m.contenido AS contenidoMensaje
            FROM notificaciones n
            JOIN mensajes m ON n.mensaje_id = m.id
            WHERE n.usuario_id = ? AND m.leido = FALSE
            ORDER BY n.fecha DESC
            """;

        return jdbcTemplate.query(sql, new Object[]{usuarioId}, (rs, rowNum) -> {
            NotificacionDTO dto = new NotificacionDTO();
            dto.setId(rs.getLong("id"));
            dto.setUsuarioId(rs.getLong("usuario_id"));
            dto.setMensajeId(rs.getLong("mensaje_id"));
            dto.setVista(rs.getString("vista"));
            dto.setLeido(rs.getBoolean("leido"));  // Este es el leido de la notificación, si existe
            dto.setFecha(rs.getTimestamp("fecha"));
            dto.setContenidoMensaje(rs.getString("contenidoMensaje"));
            return dto;
        });
    }

    public void marcarMensajeLeido(Long mensajeId) {
        String sql = "UPDATE mensajes SET leido = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, mensajeId);
    }

    public void marcarNotificacionLeida(Long notificacionId) {
        String sql = "UPDATE notificaciones SET leido = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, notificacionId);
    }

    public MensajeDTO obtenerMensajePorId(Long mensajeId) {
        String sql = """
            SELECT id, terapeuta_id, paciente_id, contenido, fecha, leido, video
            FROM mensajes
            WHERE id = ?
            """;

        List<MensajeDTO> mensajes = jdbcTemplate.query(sql, new Object[]{mensajeId}, (rs, rowNum) -> {
            MensajeDTO dto = new MensajeDTO();
            dto.setId(rs.getLong("id"));
            dto.setTerapeutaId(rs.getLong("terapeuta_id"));
            dto.setPacienteId(rs.getLong("paciente_id"));
            dto.setContenido(rs.getString("contenido"));
            dto.setFecha(rs.getTimestamp("fecha"));
            dto.setLeido(rs.getBoolean("leido"));
            dto.setVideo(rs.getString("video"));
            return dto;
        });

        return mensajes.isEmpty() ? null : mensajes.get(0);
    }
}
