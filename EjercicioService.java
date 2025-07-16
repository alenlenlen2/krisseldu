package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.Asistencia;
import Krisseldu.Krisseldu.model.Ejercicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Service
public class EjercicioService {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired @Lazy private UsuarioService usuarioService;

    // 1. Listar ejercicios asignados a un usuario
    public List<Ejercicio> obtenerEjerciciosAsignadosAUsuario(Long usuarioId) {
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.horarios, e.video_path, ue.realizado
            FROM ejercicios e
            JOIN usuario_ejercicio ue ON ue.ejercicio_id = e.id
            WHERE ue.usuario_id = ?
            ORDER BY e.nombre
        """;
        return jdbcTemplate.query(sql, new Object[]{usuarioId}, (rs, row) -> {
            Ejercicio e = new Ejercicio();
            e.setId(rs.getLong("id"));
            e.setNombre(rs.getString("nombre"));
            e.setDescripcion(rs.getString("descripcion"));
            e.setHorarios(rs.getString("horarios"));
            e.setVideoPath(rs.getString("video_path"));
            e.setRealizado(rs.getBoolean("realizado"));
            return e;
        });
    }

    // 2. Asignar ejercicio existente a usuario
    public void asignarEjercicioAUsuario(Long ejercicioId, Long usuarioId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario_ejercicio WHERE usuario_id=? AND ejercicio_id=?",
                new Object[]{usuarioId, ejercicioId}, Integer.class);
        if (c != null && c > 0) return; // Ya está asignado

        jdbcTemplate.update(
                "INSERT INTO usuario_ejercicio (usuario_id, ejercicio_id, realizado) VALUES (?,?,FALSE)",
                usuarioId, ejercicioId
        );

        // Registra asistencia inicial como "ausente"
        Asistencia a = asistenciaService.obtenerAsistencia(ejercicioId, usuarioId);
        if (a == null) {
            a = new Asistencia();
            a.setUsuarioId(usuarioId);
            a.setEjercicioId(ejercicioId);
            a.setAsistencia("ausente");
            asistenciaService.registrarAsistencia(a);
        } else {
            asistenciaService.actualizarAsistencia(a.getId(), "ausente");
        }
    }

    // 2.1 Alias para claridad
    public void asignarEjercicioExistente(Long ejercicioId, Long usuarioId) {
        asignarEjercicioAUsuario(ejercicioId, usuarioId);
    }

    // 3. Quitar/desasignar ejercicio de usuario (borrar relación)
    public void quitarEjercicioDeUsuario(Long ejercicioId, Long usuarioId) {
        jdbcTemplate.update(
                "DELETE FROM usuario_ejercicio WHERE usuario_id=? AND ejercicio_id=?",
                usuarioId, ejercicioId
        );
        jdbcTemplate.update(
                "DELETE FROM usuario_ejercicio_video WHERE usuario_id=? AND ejercicio_id=?",
                usuarioId, ejercicioId
        );
        // Si quieres eliminar también asistencia, descomenta:
        // asistenciaService.eliminarAsistencia(ejercicioId, usuarioId);
    }

    // 4. Crear ejercicio
    public Long crearEjercicio(String nombre, String descr, String horarios, String videoFileName) {
        String ins = "INSERT INTO ejercicios (nombre, descripcion, horarios, video_path) VALUES (?,?,?,?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, descr);
            ps.setString(3, horarios);
            ps.setString(4, videoFileName);
            return ps;
        }, kh);
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    // 5. Actualizar ejercicio
    public void actualizarEjercicio(Long id, Ejercicio dto) {
        jdbcTemplate.update(
                "UPDATE ejercicios SET nombre=?, horarios=?, descripcion=?, video_path=? WHERE id=?",
                dto.getNombre(), dto.getHorarios(), dto.getDescripcion(), dto.getVideoPath(), id
        );
    }

    // 6. Eliminar ejercicio COMPLETAMENTE (incluyendo relaciones)
    public void eliminarEjercicio(Long id) {
        jdbcTemplate.update("DELETE FROM usuario_ejercicio_video WHERE ejercicio_id = ?", id);
        jdbcTemplate.update("DELETE FROM usuario_ejercicio WHERE ejercicio_id = ?", id);
        jdbcTemplate.update("DELETE FROM condicion_ejercicio WHERE ejercicio_id = ?", id);
        jdbcTemplate.update("DELETE FROM asistencia WHERE ejercicio_id = ?", id); // Opcional, elimina asistencias
        jdbcTemplate.update("DELETE FROM ejercicios WHERE id = ?", id);
    }

    // 7. Obtener ejercicio por ID
    public Ejercicio obtenerEjercicioPorId(Long id) {
        String sql = "SELECT id, nombre, descripcion, horarios, video_path FROM ejercicios WHERE id = ?";
        List<Ejercicio> l = jdbcTemplate.query(sql, new Object[]{id}, (rs, row) -> {
            Ejercicio e = new Ejercicio();
            e.setId(rs.getLong("id"));
            e.setNombre(rs.getString("nombre"));
            e.setDescripcion(rs.getString("descripcion"));
            e.setHorarios(rs.getString("horarios"));
            e.setVideoPath(rs.getString("video_path"));
            return e;
        });
        return l.isEmpty() ? null : l.get(0);
    }

    // 8. Guardar o actualizar video grabado por el usuario para ese ejercicio
    public void guardarVideoDeUsuarioPorEjercicio(Long usuarioId, Long ejercicioId, String videoPath) {
        if (videoPath == null || videoPath.isBlank()) return;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario_ejercicio_video WHERE usuario_id=? AND ejercicio_id=?",
                new Object[]{usuarioId, ejercicioId}, Integer.class);

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE usuario_ejercicio_video SET video_path=? WHERE usuario_id=? AND ejercicio_id=?",
                    videoPath, usuarioId, ejercicioId);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO usuario_ejercicio_video (usuario_id, ejercicio_id, video_path) VALUES (?, ?, ?)",
                    usuarioId, ejercicioId, videoPath);
        }
    }

    // 9. Obtener video personalizado de un usuario para un ejercicio
    public String obtenerVideoDeUsuarioPorEjercicio(Long usuarioId, Long ejercicioId) {
        String sql = "SELECT video_path FROM usuario_ejercicio_video WHERE usuario_id=? AND ejercicio_id=? LIMIT 1";
        List<String> l = jdbcTemplate.query(sql, new Object[]{usuarioId, ejercicioId},
                (rs, row) -> rs.getString("video_path"));
        return l.isEmpty() ? null : l.get(0);
    }

    // 10. Marcar ejercicio como realizado para usuario y guardar video personalizado
    public void marcarEjercicioRealizadoParaUsuario(Long ejercicioId, Long usuarioId, String videoPathUsuario) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario_ejercicio WHERE usuario_id=? AND ejercicio_id=?",
                new Object[]{usuarioId, ejercicioId}, Integer.class);

        if (c != null && c > 0) {
            jdbcTemplate.update(
                    "UPDATE usuario_ejercicio SET realizado=TRUE WHERE usuario_id=? AND ejercicio_id=?",
                    usuarioId, ejercicioId);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO usuario_ejercicio (usuario_id, ejercicio_id, realizado) VALUES (?,?,TRUE)",
                    usuarioId, ejercicioId);
        }
        guardarVideoDeUsuarioPorEjercicio(usuarioId, ejercicioId, videoPathUsuario);
        asistenciaService.marcarAsistenciaRealizada(ejercicioId, usuarioId, videoPathUsuario);
    }

    // 11. Listar ejercicios NO realizados por usuario (pendientes)
    public List<Ejercicio> obtenerEjerciciosNoRealizadosPorUsuario(Long userId) {
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.horarios, e.video_path, ue.realizado
            FROM ejercicios e
            JOIN usuario_ejercicio ue ON ue.ejercicio_id = e.id
            WHERE ue.usuario_id = ? AND COALESCE(ue.realizado, FALSE) = FALSE
            ORDER BY e.nombre
        """;
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, row) -> {
            Ejercicio e = new Ejercicio();
            e.setId(rs.getLong("id"));
            e.setNombre(rs.getString("nombre"));
            e.setDescripcion(rs.getString("descripcion"));
            e.setHorarios(rs.getString("horarios"));
            e.setVideoPath(rs.getString("video_path"));
            e.setRealizado(rs.getBoolean("realizado"));
            return e;
        });
    }
}
