package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.service.PasswordResetService;
import Krisseldu.Krisseldu.service.EmailService;
import Krisseldu.Krisseldu.service.UsuarioService;
import Krisseldu.Krisseldu.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {

    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final UsuarioService usuarioService;

    @Autowired
    public PasswordController(PasswordResetService passwordResetService,
                              EmailService emailService,
                              UsuarioService usuarioService) {
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
        this.usuarioService = usuarioService;
    }

    // Paso 1: Formulario para ingresar el correo o DNI
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";  // Página HTML donde el usuario ingresa su correo o DNI
    }

    // Paso 2: Enviar el token de verificación al correo
    @PostMapping("/send-token")
    public String sendResetToken(@RequestParam String emailOrDni, Model model) {
        try {
            Long usuarioId = getUserIdByEmailOrDni(emailOrDni);

            if (usuarioId != null) {
                String token = passwordResetService.generateResetToken(emailOrDni);

                Usuario usuario = null;
                if (usuarioService.obtenerUsuarioPorEmail(emailOrDni) != null) {
                    usuario = usuarioService.obtenerUsuarioPorEmail(emailOrDni); // Si encontramos por email
                } else if (usuarioService.obtenerUsuarioPorDni(emailOrDni) != null) {
                    usuario = usuarioService.obtenerUsuarioPorDni(emailOrDni); // Si encontramos por DNI
                }

                if (usuario != null) {
                    // Enviar el token por correo electrónico
                    emailService.sendResetTokenEmail(usuario.getEmail(), token);
                    model.addAttribute("token", token);
                    return "verify-token";  // Página HTML para ingresar el token
                } else {
                    model.addAttribute("errorMessage", "Usuario no encontrado");
                    return "forgot-password";
                }
            } else {
                model.addAttribute("errorMessage", "Correo o DNI no encontrado");
                return "forgot-password";
            }
        } catch (Exception e) {
            // Log de excepción detallado
            e.printStackTrace();
            model.addAttribute("errorMessage", "Hubo un error al enviar el token: " + e.getMessage());
            return "forgot-password";
        }
    }

    // Paso 3: Verificar el token
    @GetMapping("/verify-token")
    public String verifyToken(@RequestParam("token") String token, Model model) {
        if (passwordResetService.validateResetToken(token)) {
            model.addAttribute("token", token);  // Pasamos el token a la página de restablecimiento
            return "reset-password";  // Página HTML para restablecer la contraseña
        } else {
            model.addAttribute("errorMessage", "Token inválido o expirado");
            return "forgot-password";
        }
    }

    // Paso 4: Restablecer la contraseña
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword, Model model) {
        try {
            // Verificar que el token sea válido
            if (passwordResetService.validateResetToken(token)) {
                // Restablecer la contraseña en la base de datos
                passwordResetService.markTokenAsUsed(token);
                usuarioService.actualizarContrasenaPorToken(token, newPassword);  // Método para actualizar la contraseña

                model.addAttribute("successMessage", "Contraseña restablecida exitosamente");
                return "login";  // Página de inicio de sesión
            } else {
                model.addAttribute("errorMessage", "Token inválido o expirado");
                return "reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Hubo un error al restablecer la contraseña: " + e.getMessage());
            return "reset-password";
        }
    }

    // Método para obtener el usuario por correo o DNI
    private Long getUserIdByEmailOrDni(String emailOrDni) {
        Long usuarioId = usuarioService.obtenerIdPorEmail(emailOrDni);

        // Si no se encuentra por email, intentar por DNI
        if (usuarioId == null) {
            usuarioId = usuarioService.obtenerIdPorDni(emailOrDni);
        }

        return usuarioId;  // Devuelve el ID si lo encuentra, o null si no
    }
}
