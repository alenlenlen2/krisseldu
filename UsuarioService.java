package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.Condicion;
import Krisseldu.Krisseldu.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Lazy
    @Autowired
    private EjercicioService ejercicioService;

    // ============= CRUD BÁSICO =============

    public void guardarUsuario(Usuario usuario) {
        String sql = """
            INSERT INTO usuario (nombre, apellido, edad, dni, email, password, rol, terapeuta_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
                usuario.getNombre(), usuario.getApellido(), usuario.getEdad(),
                usuario.getDni(), usuario.getEmail(), usuario.getPassword(),
                usuario.getRol(), usuario.getTerapeutaId());
    }

    public Usuario obtenerUsuarioPorDni(String dni) {
        String sql = "SELECT * FROM usuario WHERE dni = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{dni}, this::mapUsuario);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, this::mapUsuario);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Usuario obtenerUsuarioPorId(Long usuarioId) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{usuarioId}, this::mapUsuario);
    }

    private Usuario mapUsuario(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEdad(rs.getInt("edad"));
        u.setDni(rs.getString("dni"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setTerapeutaId(rs.getLong("terapeuta_id"));
        return u;
    }

    // ============= UTILIDADES =============

    public String obtenerNombreCompleto(Long usuarioId) {
        String sql = "SELECT nombre, apellido FROM usuario WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{usuarioId}, (rs, row) ->
                rs.getString("nombre") + " " + rs.getString("apellido"));
    }

    public Long obtenerIdPorDni(String dni) {
        String sql = "SELECT id FROM usuario WHERE dni = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{dni}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long obtenerIdPorEmail(String emailOrDni) {
        String sqlEmail = "SELECT id FROM usuario WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sqlEmail, new Object[]{emailOrDni}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return obtenerIdPorDni(emailOrDni);
        }
    }

    public Usuario authenticateByDni(String dni, String contrasena) {
        Usuario usuario = obtenerUsuarioPorDni(dni);
        if (usuario == null)
            throw new RuntimeException("Usuario no existe.");
        if (!usuario.getPassword().equals(contrasena))
            throw new RuntimeException("Contraseña incorrecta.");
        return usuario;
    }

    // ============= RELACIÓN usuario-condición / ejercicios =============

    /**
     * Ahora SOLO vincula condiciones al usuario.
     * Si quieres asignar ejercicios base de condición, llama manualmente a ejercicioService desde el admin.
     */
    public void crearUsuarioConCondicionesYEjercicios(Usuario usuario, List<Long> condicionesIds) {
        guardarUsuario(usuario);
        Long usuarioId = obtenerIdPorDni(usuario.getDni());
        if (usuarioId == null)
            throw new RuntimeException("Error al obtener ID del usuario recién creado.");

        // vincular condiciones
        condicionesIds.forEach(id -> guardarUsuarioCondicion(usuarioId, id));

        // **NO asigna ejercicios automáticamente**
        // Si algún día lo quieres:
        // List<Long> ejercicios = ejercicioService.obtenerEjerciciosIdsPorCondiciones(condicionesIds);
        // ejercicios.forEach(ejId -> ejercicioService.asignarEjercicioAUsuario(ejId, usuarioId));
    }

    public void guardarUsuarioCondicion(Long usuarioId, Long condicionId) {
        String sql = "INSERT INTO usuario_condicion (usuario_id, condicion_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, usuarioId, condicionId);
    }

    public List<Condicion> obtenerCondicionesPorUsuario(Long usuarioId) {
        String sql = """
            SELECT c.id, c.nombre
            FROM condiciones c
            JOIN usuario_condicion uc ON c.id = uc.condicion_id
            WHERE uc.usuario_id = ?""";
        return jdbcTemplate.query(sql, new Object[]{usuarioId}, (rs, row) -> {
            Condicion c = new Condicion();
            c.setId(rs.getLong("id"));
            c.setNombre(rs.getString("nombre"));
            return c;
        });
    }

    public Long obtenerIdCondicionPorNombre(String nombre) {
        String sql = "SELECT id FROM condiciones WHERE nombre = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{nombre}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ============= RECUPERACIÓN DE CONTRASEÑA =============

    private Long obtenerUsuarioIdPorToken(String token) {
        String sql = """
            SELECT usuario_id
            FROM password_reset_token
            WHERE token = ?
              AND usado = FALSE
              AND fecha_expiracion > CURRENT_TIMESTAMP
            """;
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{token.trim()}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void actualizarContrasenaPorToken(String token, String newPassword) {
        Long usuarioId = obtenerUsuarioIdPorToken(token);
        if (usuarioId == null)
            throw new RuntimeException("Token inválido o expirado");

        String sql = "UPDATE usuario SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newPassword, usuarioId);
    }
}
