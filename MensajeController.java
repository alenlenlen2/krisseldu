package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.dto.MensajeDTO;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar mensaje y marcar como leído inmediatamente al acceder
    @GetMapping("/verMensaje/{mensajeId}")
    public String verMensaje(@PathVariable Long mensajeId, Model model, HttpSession session) {
        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isEmpty()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        // Obtener el usuario por su DNI (se debe corregir el método para que use el DNI y no el email)
        var usuario = usuarioService.obtenerUsuarioPorDni(dni); // Correcto, usa el DNI, no el email
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Obtener el mensaje por su ID
        MensajeDTO mensaje = mensajeService.obtenerMensajePorId(mensajeId);
        if (mensaje == null) {
            return "redirect:/home"; // Si el mensaje no se encuentra, redirige al home
        }

        // Marcar el mensaje como leído
        mensajeService.marcarMensajeLeido(mensajeId);

        // Pasar el mensaje y el nombre del usuario al modelo
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido()); // Mostrar el nombre completo

        return "verMensaje"; // Nombre de la vista Thymeleaf
    }

    // Marcar mensaje como leído y redirigir al home (para usar desde enlace "Volver al inicio")
    @GetMapping("/marcarLeidoYVolver/{mensajeId}")
    public String marcarLeidoYVolver(@PathVariable Long mensajeId, HttpSession session) {
        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isEmpty()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        // Obtener el usuario por su DNI (se debe corregir el método para que use el DNI y no el email)
        var usuario = usuarioService.obtenerUsuarioPorDni(dni); // Correcto, usa el DNI, no el email
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Marcar el mensaje como leído
        mensajeService.marcarMensajeLeido(mensajeId);

        // Redirige al home para actualizar la lista de notificaciones
        return "redirect:/home";
    }
}
