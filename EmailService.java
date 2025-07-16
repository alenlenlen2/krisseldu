package Krisseldu.Krisseldu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetTokenEmail(String to, String token) {
        String subject = "Restablecimiento de Contraseña";
        String text = "Has solicitado restablecer tu contraseña. Usa el siguiente token para hacerlo: " + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("alfredoserpa64@gmail.com");  // Remitente
        message.setTo(to);  // Dirección de correo del destinatario
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);  // Enviar el mensaje
        } catch (Exception e) {
            e.printStackTrace();  // Imprimir cualquier error
            throw new RuntimeException("Error al enviar el correo electrónico", e);
        }
    }
}
