package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.PacienteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Obtener lista de pacientes asignados a un terapeuta específico, con condiciones y asistencia
    // También acepta un filtro (nombre o DNI) para filtrar los pacientes
    public List<PacienteDTO> obtenerPacientesPorTerapeuta(Long terapeutaId, String filtro) {
        String sql = """
            SELECT DISTINCT u.id, u.nombre, u.dni, u.edad, c.nombre AS condicion,
                   a.asistencia AS asistenciaEstado, a.fecha AS asistenciaFecha, a.video
            FROM usuario u
            JOIN usuario_condicion uc ON u.id = uc.usuario_id
            JOIN condicion_terapeuta ct ON uc.condicion_id = ct.condicion_id
            LEFT JOIN condiciones c ON uc.condicion_id = c.id
            LEFT JOIN asistencia a ON u.id = a.usuario_id
            WHERE u.rol = 'PACIENTE' AND ct.terapeuta_id = ?
            """ + (filtro != null && !filtro.isEmpty() ?
                " AND (u.nombre LIKE ? OR u.dni LIKE ?)" : "") + // Filtrar por nombre o DNI si el filtro no está vacío
                " ORDER BY u.nombre";

        // Si hay un filtro, se buscan pacientes con el filtro aplicado
        if (filtro != null && !filtro.isEmpty()) {
            return jdbcTemplate.query(sql, new Object[]{terapeutaId, "%" + filtro + "%", "%" + filtro + "%"}, (rs, rowNum) -> {
                PacienteDTO dto = new PacienteDTO();
                dto.setId(rs.getLong("id"));
                dto.setNombre(rs.getString("nombre"));
                dto.setDni(rs.getString("dni"));
                dto.setEdad(rs.getInt("edad"));
                dto.setCondicion(rs.getString("condicion"));
                dto.setAsistenciaEstado(rs.getString("asistenciaEstado"));
                dto.setAsistenciaFecha(rs.getTimestamp("asistenciaFecha"));
                dto.setVideo(rs.getString("video"));
                return dto;
            });
        } else {
            // Si no hay filtro, se obtienen todos los pacientes asignados al terapeuta
            return jdbcTemplate.query(sql, new Object[]{terapeutaId}, (rs, rowNum) -> {
                PacienteDTO dto = new PacienteDTO();
                dto.setId(rs.getLong("id"));
                dto.setNombre(rs.getString("nombre"));
                dto.setDni(rs.getString("dni"));
                dto.setEdad(rs.getInt("edad"));
                dto.setCondicion(rs.getString("condicion"));
                dto.setAsistenciaEstado(rs.getString("asistenciaEstado"));
                dto.setAsistenciaFecha(rs.getTimestamp("asistenciaFecha"));
                dto.setVideo(rs.getString("video"));
                return dto;
            });
        }
    }

    // Obtener un paciente específico por su id
    public PacienteDTO obtenerPacientePorId(Long id) {
        String sql = """
            SELECT u.id, u.nombre, u.dni, u.edad, c.nombre AS condicion,
                   a.asistencia AS asistenciaEstado, a.fecha AS asistenciaFecha, a.video
            FROM usuario u
            LEFT JOIN usuario_condicion uc ON u.id = uc.usuario_id
            LEFT JOIN condiciones c ON uc.condicion_id = c.id
            LEFT JOIN asistencia a ON u.id = a.usuario_id
            WHERE u.id = ?
            LIMIT 1
            """;

        List<PacienteDTO> pacientes = jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> {
            PacienteDTO dto = new PacienteDTO();
            dto.setId(rs.getLong("id"));
            dto.setNombre(rs.getString("nombre"));
            dto.setDni(rs.getString("dni"));
            dto.setEdad(rs.getInt("edad"));
            dto.setCondicion(rs.getString("condicion"));
            dto.setAsistenciaEstado(rs.getString("asistenciaEstado"));
            dto.setAsistenciaFecha(rs.getTimestamp("asistenciaFecha"));
            dto.setVideo(rs.getString("video"));
            return dto;
        });

        return pacientes.isEmpty() ? null : pacientes.get(0);
    }
}
