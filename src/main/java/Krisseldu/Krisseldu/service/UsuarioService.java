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
    private EjercicioService ejercicioService;  // Inyección perezosa para evitar ciclo

    // Guardar usuario con DNI como clave primaria, no email
    public void guardarUsuario(Usuario usuario) {
        String insertSql = "INSERT INTO usuario (nombre, apellido, edad, dni, email, password, rol, terapeuta_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEdad(),
                usuario.getDni(),  // Usando DNI como clave principal para autenticación
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getRol(),
                usuario.getTerapeutaId());
    }

    // Verificar si el DNI ya está registrado
    public Usuario obtenerUsuarioPorDni(String dni) {
        try {
            String sql = "SELECT * FROM usuario WHERE dni = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{dni}, (rs, rowNum) -> {
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
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Verificar si el email ya está registrado
    public Usuario obtenerUsuarioPorEmail(String email) {
        try {
            String sql = "SELECT * FROM usuario WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
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
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Obtener el nombre completo del usuario
    public String obtenerNombreCompleto(Long usuarioId) {
        String sql = "SELECT nombre, apellido FROM usuario WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{usuarioId}, (rs, rowNum) -> {
            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            return nombre + " " + apellido;  // Concatenamos el nombre y el apellido
        });
    }

    // Método unificado de autenticación usando DNI
    public Usuario authenticateByDni(String dni, String contrasena) {
        // Obtener el usuario por el DNI
        Usuario usuario = obtenerUsuarioPorDni(dni);

        // Si no se encuentra el usuario
        if (usuario == null) {
            throw new RuntimeException("Usuario no existe.");
        }

        // Verificar si la contraseña es correcta
        if (!usuario.getPassword().equals(contrasena)) {
            throw new RuntimeException("Contraseña incorrecta.");
        }

        return usuario;  // Retorna el usuario autenticado
    }

    // Método para obtener el ID del usuario por su DNI
    public Long obtenerIdPorDni(String dni) {
        String sql = "SELECT id FROM usuario WHERE dni = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{dni}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Método para obtener el ID del usuario por su email o DNI (si se usa para otras validaciones)
    public Long obtenerIdPorEmail(String emailOrDni) {
        // Primero intenta obtenerlo por email
        String sqlEmail = "SELECT id FROM usuario WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sqlEmail, new Object[]{emailOrDni}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            // Si no se encuentra, intenta obtenerlo por DNI
            String sqlDni = "SELECT id FROM usuario WHERE dni = ?";
            try {
                return jdbcTemplate.queryForObject(sqlDni, new Object[]{emailOrDni}, Long.class);
            } catch (EmptyResultDataAccessException ex) {
                return null;
            }
        }
    }

    // Crear usuario con condiciones y asignar ejercicios pendientes automáticamente
    public void crearUsuarioConCondicionesYEjercicios(Usuario usuario, List<Long> condicionesIds) {
        // Guardar usuario
        guardarUsuario(usuario);

        // Obtener ID del usuario recién guardado
        Long usuarioId = obtenerIdPorDni(usuario.getDni());  // Usamos DNI para obtener el ID
        if (usuarioId == null) {
            throw new RuntimeException("Error al obtener ID del usuario recién creado.");
        }

        // Guardar relaciones usuario-condición
        for (Long condicionId : condicionesIds) {
            guardarUsuarioCondicion(usuarioId, condicionId);
        }

        // Obtener ejercicios asociados a esas condiciones y asignar como pendientes
        List<Long> ejercicioIds = ejercicioService.obtenerEjerciciosIdsPorCondiciones(condicionesIds);
        for (Long ejercicioId : ejercicioIds) {
            ejercicioService.marcarEjercicioPendiente(usuarioId, ejercicioId);
        }
    }

    // Guardar relación usuario-condición
    public void guardarUsuarioCondicion(Long usuarioId, Long condicionId) {
        String sql = "INSERT INTO usuario_condicion (usuario_id, condicion_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, usuarioId, condicionId);
    }

    // Obtener lista de condiciones de un usuario por su ID
    public List<Condicion> obtenerCondicionesPorUsuario(Long usuarioId) {
        String sql = "SELECT c.id, c.nombre FROM condiciones c " +
                "JOIN usuario_condicion uc ON c.id = uc.condicion_id " +
                "WHERE uc.usuario_id = ?";
        return jdbcTemplate.query(sql, new Object[]{usuarioId}, (rs, rowNum) -> {
            Condicion c = new Condicion();
            c.setId(rs.getLong("id"));
            c.setNombre(rs.getString("nombre"));
            return c;
        });
    }

    // Método para obtener el ID de la condición por su nombre
    public Long obtenerIdCondicionPorNombre(String nombreCondicion) {
        String sql = "SELECT id FROM condiciones WHERE nombre = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{nombreCondicion}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;  // Si no se encuentra la condición, retorna null
        }
    }

    // Método para actualizar la contraseña del usuario mediante un token
    public void actualizarContrasenaPorToken(String token, String newPassword) {
        // Primero debes obtener el ID del usuario mediante el token
        Long usuarioId = obtenerUsuarioIdPorToken(token);
        if (usuarioId != null) {
            String sql = "UPDATE usuario SET password = ? WHERE id = ?";
            jdbcTemplate.update(sql, newPassword, usuarioId);  // Actualiza la contraseña
        } else {
            throw new RuntimeException("Token inválido o usuario no encontrado.");
        }
    }

    // Método ficticio para obtener el usuario por su ID utilizando el token
    private Long obtenerUsuarioIdPorToken(String token) {
        // Aquí deberías tener la lógica para obtener el ID del usuario utilizando el token
        // Podrías consultar la tabla de tokens si estás usando algún tipo de token para validar
        // ejemplo ficticio:
        return 1L;  // Esto es solo un ejemplo, debes adaptarlo
    }

    public Usuario obtenerUsuarioPorId(Long usuarioId) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{usuarioId}, (rs, rowNum) -> {
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
        });
    }
}
