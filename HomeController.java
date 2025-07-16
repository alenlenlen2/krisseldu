package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.dto.MensajeDTO;
import Krisseldu.Krisseldu.model.Ejercicio;
import Krisseldu.Krisseldu.model.NotificacionDTO;
import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.EjercicioService;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired private MensajeService mensajeService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private EjercicioService ejercicioService;

    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {

        // 1. Validar sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isBlank()) {
            return "redirect:/login";
        }

        // 2. Obtener usuario completo
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) {
            return "redirect:/login";
        }
        Long userId = usuario.getId();

        // 3. Obtener SOLO ejercicios asignados por el admin/terapeuta
        List<Ejercicio> ejerciciosPendientes = ejercicioService.obtenerEjerciciosNoRealizadosPorUsuario(userId);

        // 4. Notificaciones y mensajes
        List<MensajeDTO> mensajes = mensajeService.obtenerMensajesPorPaciente(userId);
        List<NotificacionDTO> notificaciones = mensajeService.obtenerNotificacionesUsuario(userId);
        long sinLeerCount = notificaciones.stream().filter(n -> !n.isLeido()).count();

        // 5. Atributos para la vista
        model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido());
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesSinLeer", sinLeerCount);
        model.addAttribute("ejerciciosPendientes", ejerciciosPendientes);
        model.addAttribute("successMessage", "Bienvenido " + usuario.getNombre() + ", sesión iniciada correctamente.");

        return "home"; // templates/home.html
    }
}
