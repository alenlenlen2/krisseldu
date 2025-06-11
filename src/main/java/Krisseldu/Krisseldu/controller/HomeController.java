package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.dto.MensajeDTO;
import Krisseldu.Krisseldu.model.Ejercicio;
import Krisseldu.Krisseldu.model.NotificacionDTO;
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

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EjercicioService ejercicioService;

    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");

        // Si no hay sesión o el DNI es inválido, redirigimos al login
        if (dni == null || dni.isBlank()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        // Obtener el usuario completo usando el DNI (la variable 'dni' es suficiente para la búsqueda)
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni); // Aquí usamos el DNI para obtener el usuario
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        Long userId = usuario.getId();

        // Inicializar ejercicios pendientes para el usuario solo si no existen (para usuarios nuevos)
        ejercicioService.inicializarEjerciciosPendientesParaUsuario(userId);

        // Obtener mensajes enviados por el terapeuta al paciente (utilizando MensajeDTO)
        List<MensajeDTO> mensajes = mensajeService.obtenerMensajesPorPaciente(userId);

        // Obtener notificaciones para el usuario
        List<NotificacionDTO> notificaciones = mensajeService.obtenerNotificacionesUsuario(userId);

        // Contar notificaciones no leídas
        long sinLeerCount = notificaciones.stream()
                .filter(notificacion -> !notificacion.isLeido())
                .count();

        // Obtener SOLO ejercicios pendientes (no realizados)
        List<Ejercicio> ejerciciosPendientes = ejercicioService.obtenerEjerciciosNoRealizadosPorUsuario(userId);

        // Pasar el nombre del usuario (no solo el username)
        model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido());
        model.addAttribute("mensajes", mensajes); // Pasar los mensajes al modelo
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesSinLeer", sinLeerCount);
        model.addAttribute("ejerciciosPendientes", ejerciciosPendientes);

        return "home"; // Retorna la vista home.html con todos los datos cargados
    }
}
