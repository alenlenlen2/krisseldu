// EjercicioController.java
package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.*;
import Krisseldu.Krisseldu.service.EjercicioService;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EjercicioController {

    @Autowired private EjercicioService ejercicioService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private MensajeService mensajeService;

    @GetMapping("/ejercicios")
    public String mostrarEjerciciosAsignadosPage(Model model, HttpSession session) {
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isBlank()) return "redirect:/login";
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) return "redirect:/login";

        String nombreUsuario = usuario.getNombre() + " " + usuario.getApellido();
        List<Ejercicio> ejercicios = ejercicioService.obtenerEjerciciosAsignadosAUsuario(usuario.getId());
        List<NotificacionDTO> notificaciones = mensajeService.obtenerNotificacionesUsuario(usuario.getId());
        long sinLeerCount = notificaciones.stream().filter(n -> !n.isLeido()).count();

        model.addAttribute("nombre", nombreUsuario);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesSinLeer", sinLeerCount);
        return "ejercicios";
    }

    @GetMapping("/iniciarEjercicio")
    public String iniciarEjercicio(@RequestParam Long ejercicioId, HttpSession session) {
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isBlank()) return "redirect:/login";
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) return "redirect:/login";
        ejercicioService.asignarEjercicioAUsuario(ejercicioId, usuario.getId());
        return "redirect:/asistencias?ejercicioId=" + ejercicioId;
    }

    // ========== ADMINISTRACIÓN ==========
    @PostMapping("/admin/asignarEjercicioUsuario")
    public String asignarEjercicioUsuario(
            @RequestParam Long usuarioId,
            @RequestParam Long ejercicioId,
            RedirectAttributes redirectAttributes
    ) {
        ejercicioService.asignarEjercicioExistente(ejercicioId, usuarioId);
        redirectAttributes.addFlashAttribute("msg", "Ejercicio asignado exitosamente.");
        return "redirect:/admin/gestionarPacientes";
    }

    @PostMapping("/admin/quitarEjercicioUsuario")
    public String quitarEjercicioUsuario(
            @RequestParam Long usuarioId,
            @RequestParam Long ejercicioId,
            RedirectAttributes redirectAttributes
    ) {
        ejercicioService.quitarEjercicioDeUsuario(ejercicioId, usuarioId);
        redirectAttributes.addFlashAttribute("msg", "Ejercicio eliminado para el usuario.");
        return "redirect:/admin/gestionarPacientes";
    }
}
