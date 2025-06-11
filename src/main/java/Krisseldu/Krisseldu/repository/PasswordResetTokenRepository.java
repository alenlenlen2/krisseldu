package Krisseldu.Krisseldu.repository;

import Krisseldu.Krisseldu.model.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public PasswordResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Guardar el token
    public void saveToken(Long usuarioId, String token, LocalDateTime fechaExpiracion) {
        String sql = "INSERT INTO password_reset_token (usuario_id, token, fecha_expiracion) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, usuarioId, token, fechaExpiracion);
    }

    // Validar el token
    public Optional<PasswordResetToken> findByToken(String token) {
        String sql = "SELECT * FROM password_reset_token WHERE token = ? AND usado = FALSE AND fecha_expiracion > NOW()";
        try {
            PasswordResetToken passwordResetToken = jdbcTemplate.queryForObject(sql, new Object[]{token}, (rs, rowNum) -> {
                PasswordResetToken prt = new PasswordResetToken();
                prt.setId(rs.getLong("id"));
                prt.setUsuarioId(rs.getLong("usuario_id"));
                prt.setToken(rs.getString("token"));
                prt.setFechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime());
                prt.setUsado(rs.getBoolean("usado"));
                return prt;
            });
            return Optional.ofNullable(passwordResetToken);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Marcar token como usado
    public void markAsUsed(String token) {
        String sql = "UPDATE password_reset_token SET usado = TRUE WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}
