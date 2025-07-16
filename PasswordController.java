package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {

    private final PasswordResetService passwordResetService;
    private final EmailService         emailService;
    private final UsuarioService       usuarioService;

    @Autowired
    public PasswordController(PasswordResetService passwordResetService,
                              EmailService emailService,
                              UsuarioService usuarioService) {
        this.passwordResetService = passwordResetService;
        this.emailService         = emailService;
        this.usuarioService       = usuarioService;
    }

    /* ---------- Paso 1 : formulario correo/DNI ---------- */
    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    /* ---------- Paso 2 : generar token y enviar ---------- */
    @PostMapping("/send-token")
    public String sendResetToken(@RequestParam String emailOrDni, Model m) {
        Long uid = getUserIdByEmailOrDni(emailOrDni);
        if (uid == null) {
            m.addAttribute("errorMessage", "Correo o DNI no encontrado");
            return "forgot-password";
        }

        String token = passwordResetService.generateResetToken(emailOrDni);
        Usuario u    = usuarioService.obtenerUsuarioPorId(uid);
        emailService.sendResetTokenEmail(u.getEmail(), token);

        m.addAttribute("infoMessage", "Hemos enviado un código de verificación a tu correo");
        return "verify-token";
    }

    /* ---------- Paso 3 : verificar token (POST) ---------- */
    @PostMapping("/verify-token")
    public String verifyToken(@RequestParam String token, Model m) {
        if (passwordResetService.validateResetToken(token)) {
            m.addAttribute("token", token.trim());
            return "reset-password";
        }
        m.addAttribute("errorMessage", "Token inválido o expirado");
        return "verify-token";
    }

    /* ---------- Paso 3B : mostrar form vía enlace GET ---------- */
    @GetMapping("/reset-password")
    public String showReset(@RequestParam(value = "token", required = false) String token,
                            Model m) {

        if (token != null && passwordResetService.validateResetToken(token)) {
            m.addAttribute("token", token.trim());
            return "reset-password";
        }
        m.addAttribute("errorMessage", "Debes solicitar un nuevo enlace de recuperación.");
        return "verify-token";
    }

    /* ---------- Paso 4 : guardar nueva contraseña (POST) ---------- */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model m) {

        if (!newPassword.equals(confirmPassword)) {
            m.addAttribute("errorMessage", "Las contraseñas no coinciden.");
            m.addAttribute("token", token);
            return "reset-password";
        }

        if (!passwordResetService.validateResetToken(token)) {
            m.addAttribute("errorMessage", "Token inválido o expirado");
            return "verify-token";
        }

        // 1. Cambia la contraseña
        usuarioService.actualizarContrasenaPorToken(token, newPassword);

        // 2. Marca el token como usado
        passwordResetService.markTokenAsUsed(token);

        // 3. Vuelve a la misma vista con mensaje de éxito
        m.addAttribute("successMessage", "¡Contraseña actualizada!");
        return "reset-password";
    }

    /* ---------- Helper ---------- */
    private Long getUserIdByEmailOrDni(String emailOrDni) {
        Long id = usuarioService.obtenerIdPorEmail(emailOrDni);
        return (id != null) ? id : usuarioService.obtenerIdPorDni(emailOrDni);
    }
}
