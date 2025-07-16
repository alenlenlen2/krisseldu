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

    /* ------------------------------------------------------------
     * 1) Lista completa de pacientes (admin / debug)
     * ------------------------------------------------------------ */
    public List<PacienteDTO> findAllPacientesConAsistenciaYCondicion() {

        String sql = """
            SELECT
                u.id,
                u.nombre,
                u.dni,
                u.edad,
                uc.condicion_id AS condicionId,
                c.nombre        AS condicion,
                a.asistencia    AS asistenciaEstado,
                a.fecha         AS asistenciaFecha,
                a.video
            FROM usuario u
            LEFT JOIN usuario_condicion uc ON uc.usuario_id = u.id
            LEFT JOIN condiciones       c  ON c.id         = uc.condicion_id
            LEFT JOIN asistencia        a  ON a.usuario_id = u.id
            WHERE u.rol = 'PACIENTE'
            ORDER BY u.nombre
            """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    /* ------------------------------------------------------------
     * 2) Pacientes asignados a un terapeuta  (+ filtro opcional)
     * ------------------------------------------------------------ */
    public List<PacienteDTO> findPacientesByTerapeuta(Long terapeutaId, String filtro) {

        String base = """
            SELECT
                u.id, u.nombre, u.dni, u.edad,
                uc.condicion_id AS condicionId,
                c.nombre        AS condicion,
                a.asistencia    AS asistenciaEstado,
                a.fecha         AS asistenciaFecha,
                a.video
            FROM usuario u
            JOIN usuario_condicion uc ON uc.usuario_id = u.id
            JOIN condicion_terapeuta ct ON ct.condicion_id = uc.condicion_id
                                         AND ct.terapeuta_id = ?
            LEFT JOIN condiciones  c ON c.id = uc.condicion_id
            LEFT JOIN asistencia   a ON a.usuario_id = u.id
            WHERE u.rol = 'PACIENTE'
            """;

        StringBuilder sql = new StringBuilder(base);

        if (filtro != null && !filtro.isBlank()) {
            sql.append(" AND (u.nombre LIKE ? OR u.dni LIKE ?) ");
            filtro = "%" + filtro + "%";
            return jdbcTemplate.query(sql.toString(),
                    new Object[]{terapeutaId, filtro, filtro},
                    this::mapRow);
        }
        return jdbcTemplate.query(sql.append(" ORDER BY u.nombre").toString(),
                new Object[]{terapeutaId},
                this::mapRow);
    }

    /* ------------------------------------------------------------
     * 3) Obtener un paciente por su ID
     * ------------------------------------------------------------ */
    public PacienteDTO findPacienteById(Long id) {

        String sql = """
            SELECT
                u.id, u.nombre, u.dni, u.edad,
                uc.condicion_id AS condicionId,
                c.nombre        AS condicion,
                a.asistencia    AS asistenciaEstado,
                a.fecha         AS asistenciaFecha,
                a.video
            FROM usuario u
            LEFT JOIN usuario_condicion uc ON uc.usuario_id = u.id
            LEFT JOIN condiciones       c  ON c.id         = uc.condicion_id
            LEFT JOIN asistencia        a  ON a.usuario_id = u.id
            WHERE u.id = ?
            LIMIT 1
            """;

        return jdbcTemplate.query(sql, new Object[]{id}, this::mapRow)
                .stream().findFirst().orElse(null);
    }

    /* ------------------------------------------------------------
     * Mapper común
     * ------------------------------------------------------------ */
    private PacienteDTO mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        PacienteDTO p = new PacienteDTO();
        p.setId              (rs.getLong("id"));
        p.setNombre          (rs.getString("nombre"));
        p.setDni             (rs.getString("dni"));
        p.setEdad            (rs.getInt   ("edad"));
        p.setCondicionId     (rs.getLong  ("condicionId"));
        p.setCondicion       (rs.getString("condicion"));
        p.setAsistenciaEstado(rs.getString("asistenciaEstado"));
        p.setAsistenciaFecha (rs.getTimestamp("asistenciaFecha"));
        p.setVideo           (rs.getString("video"));
        return p;
    }
}
