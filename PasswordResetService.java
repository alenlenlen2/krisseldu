package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.PasswordResetToken;
import Krisseldu.Krisseldu.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UsuarioService usuarioService) {
        this.tokenRepository = tokenRepository;
        this.usuarioService  = usuarioService;
    }

    /* =========================================================
       1️⃣  Crear y guardar token de 6 dígitos (válido 30 min)
       ========================================================= */
    public String generateResetToken(String emailOrDni) {

        Long usuarioId = usuarioService.obtenerIdPorEmail(emailOrDni);
        if (usuarioId == null)
            usuarioId = usuarioService.obtenerIdPorDni(emailOrDni);

        if (usuarioId == null)
            throw new IllegalArgumentException("Usuario no encontrado");

        String token = generateSixDigitToken();                   // «123456»
        LocalDateTime expiracion = LocalDateTime.now()
                .plusMinutes(30); // 30 min

        tokenRepository.saveToken(usuarioId, token.trim(), expiracion);
        return token;
    }

    private String generateSixDigitToken() {
        return String.valueOf(100_000 + new Random().nextInt(900_000));
    }

    /* =========================================================
       2️⃣  Validar token (única fuente: la base de datos)
       ========================================================= */
    public boolean validateResetToken(String token) {
        if (token == null || token.isBlank()) return false;

        Optional<PasswordResetToken> opt =
                tokenRepository.findByToken(token.trim());

        return opt.isPresent();          // Solo existe y no está expirado/ usado
    }

    /* =========================================================
       3️⃣  Marcar token como usado (tras cambiar la contraseña)
       ========================================================= */
    public void markTokenAsUsed(String token) {
        tokenRepository.markAsUsed(token.trim());
    }

    /* =========================================================
       4️⃣  Obtener ID de usuario a partir del token
       ========================================================= */
    public Long getUsuarioIdFromToken(String token) {
        return tokenRepository.findByToken(token.trim())
                .map(PasswordResetToken::getUsuarioId)
                .orElse(null);
    }
}
