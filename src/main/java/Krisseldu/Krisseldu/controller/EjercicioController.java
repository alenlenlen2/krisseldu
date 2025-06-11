package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.*;
import Krisseldu.Krisseldu.service.AsistenciaService;
import Krisseldu.Krisseldu.service.EjercicioService;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class EjercicioController {

    @Autowired
    private EjercicioService ejercicioService;

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MensajeService mensajeService;

    // Mostrar la página de ejercicios
    @GetMapping("/ejercicios")
    public String mostrarEjerciciosPage(Model model, HttpSession session) {
        String dni = (String) session.getAttribute("dni");

        if (dni == null || dni.isBlank()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        String nombreUsuario = usuario.getNombre() + " " + usuario.getApellido();
        List<Long> condicionesIds = usuarioService.obtenerCondicionesPorUsuario(usuario.getId())
                .stream()
                .map(Condicion::getId)
                .collect(Collectors.toList());

        List<Ejercicio> ejercicios = ejercicioService.obtenerEjerciciosPorCondicionesYUsuario(condicionesIds, usuario.getId());

        List<NotificacionDTO> notificaciones = mensajeService.obtenerNotificacionesUsuario(usuario.getId());
        long sinLeerCount = notificaciones.stream().filter(n -> !n.isLeido()).count();

        model.addAttribute("nombre", nombreUsuario);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesSinLeer", sinLeerCount);

        return "ejercicios"; // Vista con los ejercicios
    }

    // Iniciar un ejercicio y redirigir a la página de asistencias
    @GetMapping("/iniciarEjercicio")
    public String iniciarEjercicio(@RequestParam Long ejercicioId, HttpSession session) {
        String dni = (String) session.getAttribute("dni");

        if (dni == null || dni.isBlank()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Obtener las condiciones del usuario
        List<Condicion> condiciones = usuarioService.obtenerCondicionesPorUsuario(usuario.getId());
        String videoLink = generarVideoLinkPorCondicion(condiciones);

        // Verificar si la asistencia ya existe para este ejercicio
        Asistencia asistenciaExistente = asistenciaService.obtenerAsistencia(ejercicioId, usuario.getId());
        if (asistenciaExistente == null) {
            // Si no existe, registrar una nueva asistencia con estado "presente"
            Asistencia asistencia = new Asistencia();
            asistencia.setEjercicioId(ejercicioId);
            asistencia.setUsuarioId(usuario.getId());
            asistencia.setAsistencia("presente");
            asistencia.setVideo(videoLink);
            asistenciaService.registrarAsistencia(asistencia);
        } else {
            // Si ya existe, actualizar la asistencia
            asistenciaService.actualizarAsistencia(asistenciaExistente.getId(), "presente", videoLink);
        }

        // Marcar el ejercicio como pendiente
        ejercicioService.marcarEjercicioPendiente(usuario.getId(), ejercicioId);

        // Redirigir a la vista de asistencias con el ejercicioId
        return "redirect:/asistencias?ejercicioId=" + ejercicioId;
    }

    // Método auxiliar para generar el enlace del video según la condición
    private String generarVideoLinkPorCondicion(List<Condicion> condiciones) {
        for (Condicion c : condiciones) {
            if ("esguince".equalsIgnoreCase(c.getNombre())) {
                return "https://videoserver.com/videos/esguince.mp4";
            } else if ("lesion tobillo".equalsIgnoreCase(c.getNombre())) {
                return "https://videoserver.com/videos/lesion_tobillo.mp4";
            }
        }
        return "https://videoserver.com/videos/video_general.mp4"; // Video por defecto
    }

    // Método para finalizar la rutina, marcar ejercicio como realizado y redirigir
    @PostMapping("/finalizarRutina")
    public String finalizarRutina(@RequestParam Long ejercicioId,
                                  @RequestParam(required = false) String video,
                                  HttpSession session,
                                  Model model) {
        String dni = (String) session.getAttribute("dni");

        if (dni == null || dni.isBlank()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorDni(dni);
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Marcar el ejercicio como realizado y actualizar la asistencia
        ejercicioService.marcarEjercicioRealizadoParaUsuario(ejercicioId, usuario.getId(), video);

        // Pasar el mensaje de éxito al modelo
        model.addAttribute("mensajeExito", "Rutina enviada exitosamente.");

        // Redirigir al usuario a la página de ejercicios
        return "redirect:/ejercicios";
    }
}
