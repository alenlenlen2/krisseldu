package Krisseldu.Krisseldu.service;

import Krisseldu.Krisseldu.model.PasswordResetToken;
import Krisseldu.Krisseldu.repository.PasswordResetTokenRepository;
import Krisseldu.Krisseldu.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;  // Añadimos el EmailService para enviar el correo con el token

    @Autowired
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UsuarioService usuarioService,
                                EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    // Generar un token de restablecimiento utilizando Email o DNI
    public String generateResetToken(String emailOrDni) {
        // Buscar al usuario por email o DNI
        Long usuarioId = null;

        // Intentamos buscar primero por email
        usuarioId = usuarioService.obtenerIdPorEmail(emailOrDni);

        // Si no se encuentra por email, buscamos por DNI
        if (usuarioId == null) {
            usuarioId = usuarioService.obtenerIdPorDni(emailOrDni);  // Método en UsuarioService para obtener por DNI
        }

        // Si no se encuentra el usuario, retornamos null
        if (usuarioId == null) {
            return null;
        }

        // Generamos el token de 6 dígitos
        String token = generateSixDigitToken();

        // Definimos la fecha de expiración como 2 minutos a partir de ahora
        LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(2); // Token válido por 2 minutos

        // Guardamos el token en la base de datos
        tokenRepository.saveToken(usuarioId, token, fechaExpiracion);

        // Obtenemos el correo del usuario para enviar el token
        String email = usuarioService.obtenerUsuarioPorId(usuarioId).getEmail();

        // Enviar el token por correo electrónico
        emailService.sendResetTokenEmail(email, token);

        return token;  // Devolvemos el token generado
    }

    // Método para generar un token de 6 dígitos aleatorios
    private String generateSixDigitToken() {
        Random random = new Random();
        int token = 100000 + random.nextInt(900000);  // Genera un número aleatorio de 6 dígitos
        return String.valueOf(token);
    }

    // Validar el token de restablecimiento
    public boolean validateResetToken(String token) {
        // Verificamos si el token existe y es válido
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        // Si el token está presente, verificamos la fecha de expiración
        if (resetToken.isPresent()) {
            LocalDateTime expirationDate = resetToken.get().getFechaExpiracion();
            LocalDateTime currentDate = LocalDateTime.now();

            // Depuración: muestra ambas fechas para asegurar que se comparan correctamente
            System.out.println("Fecha de expiración: " + expirationDate);
            System.out.println("Fecha actual: " + currentDate);

            // Verificar si la fecha de expiración es después de la fecha actual
            if (expirationDate.isAfter(currentDate)) {
                return true;
            } else {
                System.out.println("Token ha expirado.");
            }
        }

        return false;  // El token es inválido o ha expirado
    }

    // Marcar el token como usado
    public void markTokenAsUsed(String token) {
        // Marcamos el token como usado en la base de datos
        tokenRepository.markAsUsed(token);
    }

    // Método para obtener el usuario usando el token de restablecimiento
    public Long getUsuarioIdFromToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        // Si el token es válido, obtenemos el usuarioId asociado
        return resetToken.map(PasswordResetToken::getUsuarioId).orElse(null);
    }
}
