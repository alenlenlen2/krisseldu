package Krisseldu.Krisseldu.repository;

import Krisseldu.Krisseldu.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

@Repository
public class UsuarioRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Obtener pacientes asignados a un terapeuta
    public List<Usuario> findPacientesByTerapeutaId(Long terapeutaId) {
        String sql = "SELECT * FROM usuario WHERE rol = 'PACIENTE' AND terapeuta_id = ?";
        return jdbcTemplate.query(sql, new Object[]{terapeutaId}, (rs, rowNum) -> mapUsuario(rs));
    }

    // Obtener usuario por email
    public Usuario findByEmail(String email) {
        try {
            String sql = "SELECT * FROM usuario WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> mapUsuario(rs));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Obtener usuario por dni
    public Usuario findByDni(String dni) {
        try {
            String sql = "SELECT * FROM usuario WHERE dni = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{dni}, (rs, rowNum) -> mapUsuario(rs));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Obtener usuario por id
    public Usuario findById(Long id) {
        try {
            String sql = "SELECT * FROM usuario WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> mapUsuario(rs));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Mapeo del usuario para incluir el correo electrónico
    private Usuario mapUsuario(java.sql.ResultSet rs) throws java.sql.SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEdad(rs.getInt("edad"));
        u.setDni(rs.getString("dni"));  // Usamos dni ahora en vez de username
        u.setEmail(rs.getString("email"));  // Email sigue presente
        u.setPassword(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setTerapeutaId(rs.getLong("terapeuta_id"));
        return u;
    }
}
