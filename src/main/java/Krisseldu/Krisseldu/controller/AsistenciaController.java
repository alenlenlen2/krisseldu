package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.*;
import Krisseldu.Krisseldu.service.AsistenciaService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar la página de asistencias
    @GetMapping("/asistencias")
    public String mostrarAsistencias(@RequestParam(required = false) Long ejercicioId,
                                     Model model, HttpSession session) {
        model.addAttribute("ejercicioId", ejercicioId);

        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null) {
            return "redirect:/login";  // Si no hay sesión, redirige a login
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario != null) {
            model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido());
        } else {
            model.addAttribute("nombre", "Usuario desconocido");
        }

        return "asistencias";  // Vista asistencias.html
    }

    // Registrar una nueva asistencia cuando se inicia la grabación
    @PostMapping("/registrarAsistencia")
    public String registrarAsistencia(@RequestParam Long ejercicioId,
                                      @RequestParam Long usuarioId,
                                      @RequestParam(required = false) String video,
                                      RedirectAttributes redirectAttributes) {
        Asistencia asistencia = new Asistencia();
        asistencia.setEjercicioId(ejercicioId);
        asistencia.setUsuarioId(usuarioId);
        asistencia.setAsistencia("ausente");  // Estado inicial
        asistencia.setVideo(video);  // Si se tiene el video, lo agregamos

        asistenciaService.registrarAsistencia(asistencia);  // Registrar la asistencia

        // Agregar mensaje de éxito
        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia registrada correctamente.");

        return "redirect:/asistencias?ejercicioId=" + ejercicioId;  // Redirige a la página de asistencias
    }

    // Marcar la asistencia como realizada
    @PostMapping("/marcarAsistenciaRealizada")
    public String marcarAsistenciaRealizada(@RequestParam Long asistenciaId,
                                            @RequestParam Long ejercicioId,
                                            @RequestParam(required = false) String video,
                                            RedirectAttributes redirectAttributes) {
        asistenciaService.actualizarAsistencia(asistenciaId, "presente", video);

        // Agregar mensaje de éxito
        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia marcada como realizada.");

        return "redirect:/ejercicios";  // Redirige a la vista de ejercicios
    }
}
