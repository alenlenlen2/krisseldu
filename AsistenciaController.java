package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.*;
import Krisseldu.Krisseldu.service.AsistenciaService;
import Krisseldu.Krisseldu.service.EjercicioService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EjercicioService ejercicioService;  // Para obtener info de ejercicios y videos

    // Página de asistencias (no se modifica)
    @GetMapping("/asistencias")
    public String mostrarAsistencias(@RequestParam(required = false) Long ejercicioId,
                                     Model model, HttpSession session) {
        model.addAttribute("ejercicioId", ejercicioId);

        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario != null) {
            model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido());
        } else {
            model.addAttribute("nombre", "Usuario desconocido");
        }

        // Obtener ejercicio y videos personalizados
        if (ejercicioId != null && usuario != null) {
            Ejercicio ejercicio = ejercicioService.obtenerEjercicioPorId(ejercicioId);
            if (ejercicio != null) {
                model.addAttribute("ejercicio", ejercicio); // video referencial

                // Buscar si el usuario ya tiene un video personalizado grabado para ese ejercicio
                String videoPersonalizado = ejercicioService.obtenerVideoDeUsuarioPorEjercicio(usuario.getId(), ejercicioId);
                model.addAttribute("videoPersonalizado", videoPersonalizado); // puede ser null si no hay
            }
        }

        return "asistencias";
    }

    // =============================
    // NUEVO: Finalizar rutina del usuario (grabar video, marcar como realizado, guardar asistencia)
    // =============================
    @PostMapping("/finalizarRutina")
    public String finalizarRutina(@RequestParam Long ejercicioId,
                                  @RequestParam(name = "video", required = false) String videoBase64,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        // Tomamos usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null) return "redirect:/login";
        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) return "redirect:/login";

        Long usuarioId = usuario.getId();

        // Guardar o actualizar la asistencia como "realizado" con video
        asistenciaService.marcarAsistenciaRealizada(ejercicioId, usuarioId, videoBase64);

        // Opcional: también puedes guardar el video asociado al ejercicio/usuario
        if (videoBase64 != null && !videoBase64.isBlank()) {
            ejercicioService.guardarVideoDeUsuarioPorEjercicio(usuarioId, ejercicioId, videoBase64);
        }

        redirectAttributes.addFlashAttribute("successMessage", "¡Se registró tu rutina correctamente!");
        return "redirect:/ejercicios";
    }

    // (Puedes dejar estos métodos extra por si los necesitas)
    @PostMapping("/registrarAsistencia")
    public String registrarAsistencia(@RequestParam Long ejercicioId,
                                      @RequestParam Long usuarioId,
                                      @RequestParam(required = false) String video,
                                      RedirectAttributes redirectAttributes) {
        Asistencia asistencia = new Asistencia();
        asistencia.setEjercicioId(ejercicioId);
        asistencia.setUsuarioId(usuarioId);
        asistencia.setAsistencia("ausente");
        asistencia.setVideo(video);

        asistenciaService.registrarAsistencia(asistencia);

        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia registrada correctamente.");
        return "redirect:/asistencias?ejercicioId=" + ejercicioId;
    }

    @PostMapping("/marcarAsistenciaRealizada")
    public String marcarAsistenciaRealizada(@RequestParam Long asistenciaId,
                                            @RequestParam Long ejercicioId,
                                            @RequestParam Long usuarioId,
                                            @RequestParam(required = false) String video,
                                            RedirectAttributes redirectAttributes) {
        asistenciaService.actualizarAsistencia(asistenciaId, "presente", video);

        // Guardar el video grabado por el usuario para ese ejercicio
        if (video != null && !video.isBlank()) {
            ejercicioService.guardarVideoDeUsuarioPorEjercicio(usuarioId, ejercicioId, video);
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Asistencia marcada como realizada.");
        return "redirect:/ejercicios";
    }
}
