package Krisseldu.Krisseldu.repository;

import Krisseldu.Krisseldu.model.PasswordResetToken;
import org.springframework.dao.EmptyResultDataAccessException;
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

    /* -----------------------------------------------------------
       1️⃣  GUARDAR TOKEN
       ----------------------------------------------------------- */
    public void saveToken(Long usuarioId, String token, LocalDateTime fechaExpiracion) {
        String sql = """
            INSERT INTO password_reset_token (usuario_id, token, fecha_expiracion, usado)
            VALUES (?, ?, ?, FALSE)
            """;
        jdbcTemplate.update(sql, usuarioId, token.trim(), fechaExpiracion);
    }

    /* -----------------------------------------------------------
       2️⃣  BUSCAR TOKEN  (valida expiración en Java)
       ----------------------------------------------------------- */
    public Optional<PasswordResetToken> findByToken(String token) {

        String sql = """
            SELECT * FROM password_reset_token
            WHERE token = ? AND usado = FALSE
            """;

        try {
            PasswordResetToken prt = jdbcTemplate.queryForObject(
                    sql,
                    new Object[] { token.trim() },
                    (rs, rowNum) -> {
                        PasswordResetToken t = new PasswordResetToken();
                        t.setId(rs.getLong("id"));
                        t.setUsuarioId(rs.getLong("usuario_id"));
                        t.setToken(rs.getString("token").trim());
                        t.setFechaExpiracion(
                                rs.getTimestamp("fecha_expiracion").toLocalDateTime()
                        );
                        t.setUsado(rs.getBoolean("usado"));
                        return t;
                    });

            /* ── Verificación de expiración ────────────────────── */
            if (prt != null && prt.getFechaExpiracion().isAfter(LocalDateTime.now())) {
                return Optional.of(prt);
            } else {
                return Optional.empty();      // expirado o nulo
            }

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();          // no encontrado
        }
    }

    /* -----------------------------------------------------------
       3️⃣  MARCAR TOKEN COMO USADO
       ----------------------------------------------------------- */
    public void markAsUsed(String token) {
        String sql = "UPDATE password_reset_token SET usado = TRUE WHERE token = ?";
        int rows = jdbcTemplate.update(sql, token.trim());

        if (rows > 0) {
            System.out.println("Token marcado como usado.");
        } else {
            System.out.println("Token no encontrado.");
        }
    }
}
