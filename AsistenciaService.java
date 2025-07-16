package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.Asistencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsistenciaService {

    @Autowired private JdbcTemplate jdbcTemplate;

    // INSERTAR nueva asistencia
    public void registrarAsistencia(Asistencia a){
        String sql = "INSERT INTO asistencia (usuario_id, ejercicio_id, asistencia, video) VALUES (?,?,?,?)";
        jdbcTemplate.update(sql, a.getUsuarioId(), a.getEjercicioId(), a.getAsistencia(), a.getVideo());
    }

    // ACTUALIZAR solo estado
    public void actualizarAsistencia(Long id, String estado){
        jdbcTemplate.update("UPDATE asistencia SET asistencia=? WHERE id=?", estado, id);
    }

    // ACTUALIZAR estado y video
    public void actualizarAsistencia(Long id, String estado, String video){
        jdbcTemplate.update("UPDATE asistencia SET asistencia=?, video=? WHERE id=?", estado, video, id);
    }

    // OBTENER asistencia por ejercicio y usuario (debe devolver máximo UNA)
    public Asistencia obtenerAsistencia(Long ejercicioId, Long usuarioId){
        String sql = "SELECT id, usuario_id, ejercicio_id, asistencia, video FROM asistencia WHERE ejercicio_id=? AND usuario_id=? ORDER BY id DESC";
        List<Asistencia> l = jdbcTemplate.query(sql, (rs, r) -> {
            Asistencia a = new Asistencia();
            a.setId(rs.getLong("id"));
            a.setUsuarioId(rs.getLong("usuario_id"));
            a.setEjercicioId(rs.getLong("ejercicio_id"));
            a.setAsistencia(rs.getString("asistencia"));
            a.setVideo(rs.getString("video"));
            return a;
        }, ejercicioId, usuarioId);
        // Si hay más de uno, solo toma el más reciente y considera limpiar tu BD!
        return l.isEmpty() ? null : l.get(0);
    }

    // MARCAR asistencia como realizada (actualiza o inserta)
    public void marcarAsistenciaRealizada(Long ejercicioId, Long usuarioId, String video){
        Asistencia a = obtenerAsistencia(ejercicioId, usuarioId);
        if(a == null){
            a = new Asistencia();
            a.setEjercicioId(ejercicioId);
            a.setUsuarioId(usuarioId);
            a.setAsistencia("realizado");
            a.setVideo(video);
            registrarAsistencia(a);
        } else {
            actualizarAsistencia(a.getId(), "realizado", video);
        }
    }

    // (Opcional) BORRAR asistencia por usuario y ejercicio
    public void eliminarAsistencia(Long ejercicioId, Long usuarioId){
        jdbcTemplate.update("DELETE FROM asistencia WHERE ejercicio_id=? AND usuario_id=?", ejercicioId, usuarioId);
    }
}
