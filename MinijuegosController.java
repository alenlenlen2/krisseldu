package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.NotificacionDTO;
import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.UsuarioService;
import Krisseldu.Krisseldu.service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class MinijuegosController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MensajeService mensajeService;

    // Controlador para la ruta /minijuegos
    @GetMapping("/minijuegos")
    public String mostrarMinijuegos(HttpSession session, Model model) {
        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");

        // Si no hay sesión o el DNI es inválido, redirigimos al login
        if (dni == null || dni.isBlank()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        // Obtener el usuario completo usando el DNI (la variable 'dni' es suficiente para la búsqueda)
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Obtener nombre completo desde la sesión (se guarda en login)
        String nombreCompleto = (String) session.getAttribute("nombre");

        // Obtener los mensajes enviados por el terapeuta al paciente (utilizando MensajeDTO)
        List<Krisseldu.Krisseldu.dto.MensajeDTO> mensajes = mensajeService.obtenerMensajesPorPaciente(usuario.getId());

        // Obtener notificaciones del paciente (utilizando NotificacionDTO)
        List<NotificacionDTO> notificaciones = mensajeService.obtenerNotificacionesUsuario(usuario.getId());

        // Contar notificaciones no leídas
        long sinLeerCount = notificaciones.stream()
                .filter(notificacion -> !notificacion.isLeido())
                .count();

        // Pasar el nombre del usuario (nombre completo)
        model.addAttribute("nombre", nombreCompleto); // Pasamos el nombre completo a la vista
        model.addAttribute("mensajes", mensajes); // Pasar los mensajes al modelo
        model.addAttribute("notificaciones", notificaciones); // Pasar las notificaciones
        model.addAttribute("notificacionesSinLeer", sinLeerCount); // Contar notificaciones no leídas

        return "minijuegos"; // Retorna la vista minijuegos.html con todos los datos cargados
    }

    // Método para cerrar sesión
    @GetMapping("/cerrar-sesion")
    public String cerrarSesion(HttpSession session) {
        // Eliminar el atributo de sesión 'dni' y otros posibles atributos al cerrar sesión
        session.invalidate();
        return "redirect:/login"; // Redirige al login
    }
}
