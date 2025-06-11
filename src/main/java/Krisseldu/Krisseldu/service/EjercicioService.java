package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.Asistencia;
import Krisseldu.Krisseldu.model.Ejercicio;
import Krisseldu.Krisseldu.model.Condicion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EjercicioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    @Lazy
    private UsuarioService usuarioService;

    // Obtener ejercicios de acuerdo con las condiciones del usuario
    public List<Ejercicio> obtenerEjerciciosPorCondicionesYUsuario(List<Long> condicionesIds, Long usuarioId) {
        if (condicionesIds == null || condicionesIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = condicionesIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT DISTINCT e.id, e.nombre, e.descripcion, e.horarios, " +
                "COALESCE(ue.realizado, FALSE) AS realizado " +
                "FROM ejercicios e " +
                "JOIN condicion_ejercicio ce ON e.id = ce.ejercicio_id " +
                "JOIN usuario_condicion uc ON ce.condicion_id = uc.condicion_id AND uc.usuario_id = ? " +
                "LEFT JOIN usuario_ejercicio ue ON e.id = ue.ejercicio_id AND ue.usuario_id = ? " +
                "WHERE ce.condicion_id IN (" + inSql + ")";

        Object[] params = new Object[condicionesIds.size() + 2];
        params[0] = usuarioId;
        params[1] = usuarioId;
        for (int i = 0; i < condicionesIds.size(); i++) {
            params[i + 2] = condicionesIds.get(i);
        }

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Ejercicio ejercicio = new Ejercicio();
            ejercicio.setId(rs.getLong("id"));
            ejercicio.setNombre(rs.getString("nombre"));
            ejercicio.setDescripcion(rs.getString("descripcion"));
            ejercicio.setHorarios(rs.getString("horarios"));
            ejercicio.setRealizado(rs.getBoolean("realizado"));
            return ejercicio;
        });
    }

    // Obtener los IDs de los ejercicios según las condiciones
    public List<Long> obtenerEjerciciosIdsPorCondiciones(List<Long> condicionesIds) {
        if (condicionesIds == null || condicionesIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = condicionesIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT DISTINCT e.id FROM ejercicios e " +
                "JOIN condicion_ejercicio ce ON e.id = ce.ejercicio_id " +
                "WHERE ce.condicion_id IN (" + inSql + ")";

        Object[] params = condicionesIds.toArray();

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("id"));
    }

    // Obtener ejercicios no realizados por un usuario
    public List<Ejercicio> obtenerEjerciciosNoRealizadosPorUsuario(Long usuarioId) {
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.horarios
            FROM ejercicios e
            JOIN usuario_ejercicio ue ON e.id = ue.ejercicio_id
            WHERE ue.usuario_id = ? AND (ue.realizado = FALSE OR ue.realizado IS NULL)
            ORDER BY e.nombre
            """;

        return jdbcTemplate.query(sql, new Object[]{usuarioId}, (rs, rowNum) -> {
            Ejercicio ejercicio = new Ejercicio();
            ejercicio.setId(rs.getLong("id"));
            ejercicio.setNombre(rs.getString("nombre"));
            ejercicio.setDescripcion(rs.getString("descripcion"));
            ejercicio.setHorarios(rs.getString("horarios"));
            return ejercicio;
        });
    }

    // Marcar un ejercicio como pendiente para un usuario
    public void marcarEjercicioPendiente(Long usuarioId, Long ejercicioId) {
        String sqlUpdate = "UPDATE usuario_ejercicio SET realizado = FALSE WHERE usuario_id = ? AND ejercicio_id = ?";
        int updated = jdbcTemplate.update(sqlUpdate, usuarioId, ejercicioId);
        if (updated == 0) {
            String sqlInsert = "INSERT INTO usuario_ejercicio (usuario_id, ejercicio_id, realizado) VALUES (?, ?, FALSE)";
            jdbcTemplate.update(sqlInsert, usuarioId, ejercicioId);
        }

        // Asistencia también se marca como "ausente"
        Asistencia asistencia = asistenciaService.obtenerAsistencia(ejercicioId, usuarioId);
        if (asistencia == null) {
            asistencia = new Asistencia();
            asistencia.setEjercicioId(ejercicioId);
            asistencia.setUsuarioId(usuarioId);
            asistencia.setAsistencia("ausente");
            asistenciaService.registrarAsistencia(asistencia);
        } else {
            asistenciaService.actualizarAsistencia(asistencia.getId(), "ausente");
        }
    }

    // Marcar un ejercicio como realizado para un usuario
    public void marcarEjercicioRealizadoParaUsuario(Long ejercicioId, Long usuarioId, String video) {
        String sqlCheck = "SELECT COUNT(*) FROM usuario_ejercicio WHERE usuario_id = ? AND ejercicio_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, new Object[]{usuarioId, ejercicioId}, Integer.class);

        if (count != null && count > 0) {
            String sqlUpdate = "UPDATE usuario_ejercicio SET realizado = TRUE WHERE usuario_id = ? AND ejercicio_id = ?";
            jdbcTemplate.update(sqlUpdate, usuarioId, ejercicioId);
        } else {
            String sqlInsert = "INSERT INTO usuario_ejercicio (usuario_id, ejercicio_id, realizado) VALUES (?, ?, TRUE)";
            jdbcTemplate.update(sqlInsert, usuarioId, ejercicioId);
        }

        // Ahora, marcamos la asistencia como "realizado"
        asistenciaService.marcarAsistenciaRealizada(ejercicioId, usuarioId, video);
    }

    // Inicializar ejercicios pendientes para un usuario (cuando no están asignados)
    public void inicializarEjerciciosPendientesParaUsuario(Long usuarioId) {
        List<Condicion> condiciones = usuarioService.obtenerCondicionesPorUsuario(usuarioId);
        if (condiciones == null || condiciones.isEmpty()) {
            return;
        }

        List<Long> condicionesIds = condiciones.stream()
                .map(Condicion::getId)
                .collect(Collectors.toList());

        List<Long> ejercicioIds = obtenerEjerciciosIdsPorCondiciones(condicionesIds);

        for (Long ejercicioId : ejercicioIds) {
            String sqlCheck = "SELECT COUNT(*) FROM usuario_ejercicio WHERE usuario_id = ? AND ejercicio_id = ?";
            Integer count = jdbcTemplate.queryForObject(sqlCheck, new Object[]{usuarioId, ejercicioId}, Integer.class);

            if (count == null || count == 0) {
                String sqlInsert = "INSERT INTO usuario_ejercicio (usuario_id, ejercicio_id, realizado) VALUES (?, ?, FALSE)";
                jdbcTemplate.update(sqlInsert, usuarioId, ejercicioId);
            }
        }
    }
}
