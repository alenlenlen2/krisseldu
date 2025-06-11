package Krisseldu.Krisseldu.repository;

import Krisseldu.Krisseldu.model.PacienteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PacienteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PacienteDTO> findAllPacientesConAsistenciaYCondicion() {
        String sql = """
            SELECT 
                u.id,
                u.nombre,
                u.edad,
                c.nombre AS condicion,
                a.asistencia AS asistenciaEstado,
                a.fecha AS asistenciaFecha,
                a.video
            FROM usuario u
            LEFT JOIN usuario_condicion uc ON u.id = uc.usuario_id
            LEFT JOIN condiciones c ON uc.condicion_id = c.id
            LEFT JOIN asistencia a ON u.id = a.usuario_id
            WHERE u.rol = 'PACIENTE'
            ORDER BY u.nombre
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PacienteDTO paciente = new PacienteDTO();
            paciente.setId(rs.getLong("id"));
            paciente.setNombre(rs.getString("nombre"));
            paciente.setEdad(rs.getInt("edad"));
            paciente.setCondicion(rs.getString("condicion"));
            paciente.setAsistenciaEstado(rs.getString("asistenciaEstado"));
            paciente.setAsistenciaFecha(rs.getTimestamp("asistenciaFecha"));
            paciente.setVideo(rs.getString("video"));
            return paciente;
        });
    }
}
