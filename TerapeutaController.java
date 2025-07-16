package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.Ejercicio;
import Krisseldu.Krisseldu.model.PacienteDTO;
import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.EjercicioService;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.PacienteService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/terapeuta")
public class TerapeutaController {

    @Autowired private PacienteService  pacienteService;
    @Autowired private UsuarioService   usuarioService;
    @Autowired private MensajeService   mensajeService;
    @Autowired private EjercicioService ejercicioService;

    private static final String VIDEO_UPLOAD_DIR = "videos";

    // ========== VISTAS ==========
    @GetMapping
    public String listaPacientes(@RequestParam(required = false) String filtro,
                                 Model model, HttpSession session) {
        Usuario t = validarSesion(session);
        if (t == null) return "redirect:/login";

        model.addAttribute("nombre", t.getNombre() + " " + t.getApellido());
        model.addAttribute("pacientes", pacienteService.obtenerPacientesPorTerapeuta(t.getId(), filtro));
        var notif = mensajeService.obtenerNotificacionesUsuario(t.getId());
        model.addAttribute("notificaciones", notif);
        model.addAttribute("notificacionesSinLeer", notif.stream().filter(n -> !n.isLeido()).count());

        return "terapeuta";
    }

    @GetMapping("/asignar")
    public String formAsignar(@RequestParam(required = false) String filtro,
                              Model model, HttpSession session) {
        Usuario t = validarSesion(session);
        if (t == null) return "redirect:/login";

        model.addAttribute("nombre", t.getNombre() + " " + t.getApellido());
        model.addAttribute("pacientes", pacienteService.obtenerPacientesPorTerapeuta(t.getId(), filtro));
        var notif = mensajeService.obtenerNotificacionesUsuario(t.getId());
        model.addAttribute("notificaciones", notif);
        model.addAttribute("notificacionesSinLeer", notif.stream().filter(n -> !n.isLeido()).count());

        return "asignar";
    }

    // ========= API DE ASIGNACIÓN INDIVIDUAL =========

    // Listar ejercicios asignados a un paciente
    @ResponseBody
    @GetMapping("/paciente/{pacId}/ejercicios")
    public List<Ejercicio> ejerciciosPaciente(@PathVariable Long pacId) {
        return ejercicioService.obtenerEjerciciosAsignadosAUsuario(pacId);
    }

    // Crear + asignar nuevo ejercicio SOLO al paciente indicado
    @ResponseBody
    @PostMapping(value = "/paciente/{pacId}/ejercicios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearEjercicio(
            @PathVariable Long pacId,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String horarios,
            @RequestParam(value = "videoEjercicio", required = false) MultipartFile videoEjercicio,
            HttpSession session) throws IOException {

        Usuario t = validarSesion(session);
        if (t == null) return ResponseEntity.status(401).body("No hay sesión");

        PacienteDTO p = pacienteService.obtenerPacientePorId(pacId);
        if (p == null) return ResponseEntity.badRequest().body("Paciente no encontrado");

        if (nombre == null || nombre.trim().isEmpty())
            return ResponseEntity.badRequest().body("El nombre es obligatorio");

        if (descripcion == null) descripcion = "";
        if (horarios == null) horarios = "";

        String videoFileName = null;
        if (videoEjercicio != null && !videoEjercicio.isEmpty()) {
            String originalFileName = videoEjercicio.getOriginalFilename();
            videoFileName = "ej_" + System.currentTimeMillis() + "_" + originalFileName;
            Path videoDir = Paths.get(VIDEO_UPLOAD_DIR);
            if (!Files.exists(videoDir)) Files.createDirectories(videoDir);
            Path path = videoDir.resolve(videoFileName);
            Files.copy(videoEjercicio.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        // Crea ejercicio (en tabla ejercicios)
        Long nuevoEjId = ejercicioService.crearEjercicio(nombre, descripcion, horarios, videoFileName);
        // Lo asigna SOLO a ese paciente (tabla usuario_ejercicio)
        ejercicioService.asignarEjercicioAUsuario(nuevoEjId, pacId);

        return ResponseEntity.ok().build();
    }

    // Obtener ejercicio para edición
    @ResponseBody
    @GetMapping("/ejercicios/{id}")
    public ResponseEntity<Ejercicio> getEjercicio(@PathVariable Long id) {
        Ejercicio e = ejercicioService.obtenerEjercicioPorId(id);
        if (e == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(e);
    }

    // Actualizar ejercicio (opcionalmente video)
    @ResponseBody
    @PutMapping(value = "/ejercicios/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarEjercicio(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String horarios,
            @RequestParam(value = "videoEjercicio", required = false) MultipartFile videoEjercicio
    ) throws IOException {
        Ejercicio ejercicio = ejercicioService.obtenerEjercicioPorId(id);
        if (ejercicio == null) return ResponseEntity.notFound().build();

        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion == null ? "" : descripcion);
        ejercicio.setHorarios(horarios == null ? "" : horarios);

        if (videoEjercicio != null && !videoEjercicio.isEmpty()) {
            String originalFileName = videoEjercicio.getOriginalFilename();
            String videoFileName = "ej_" + System.currentTimeMillis() + "_" + originalFileName;
            Path videoDir = Paths.get(VIDEO_UPLOAD_DIR);
            if (!Files.exists(videoDir)) Files.createDirectories(videoDir);
            Path path = videoDir.resolve(videoFileName);
            Files.copy(videoEjercicio.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Borra el video anterior si existía
            if (ejercicio.getVideoPath() != null) {
                Path oldVideo = videoDir.resolve(ejercicio.getVideoPath());
                Files.deleteIfExists(oldVideo);
            }
            ejercicio.setVideoPath(videoFileName);
        }

        ejercicioService.actualizarEjercicio(id, ejercicio);
        return ResponseEntity.ok().build();
    }

    // Quitar ejercicio SOLO de un usuario (NO elimina el ejercicio globalmente)
    @ResponseBody
    @DeleteMapping("/paciente/{pacId}/ejercicios/{ejId}")
    public ResponseEntity<?> quitarEjercicioPaciente(
            @PathVariable Long pacId,
            @PathVariable Long ejId
    ) {
        ejercicioService.quitarEjercicioDeUsuario(ejId, pacId);
        return ResponseEntity.ok().build();
    }

    // Eliminar ejercicio completamente de la base (global, para todos)
    @ResponseBody
    @DeleteMapping("/ejercicios/{id}")
    public ResponseEntity<?> eliminarEjercicio(@PathVariable Long id) {
        ejercicioService.eliminarEjercicio(id);
        return ResponseEntity.ok().build();
    }

    // Mensajería
    @PostMapping("/enviarMensaje")
    public String enviarMensaje(@RequestParam Long pacienteId,
                                @RequestParam String mensaje,
                                HttpSession session) {
        Usuario t = validarSesion(session);
        if (t == null) return "redirect:/login";

        mensajeService.enviarMensaje(t.getId(), pacienteId, mensaje);
        return "redirect:/terapeuta";
    }

    // Validar sesión helper
    private Usuario validarSesion(HttpSession s) {
        String dni = (String) s.getAttribute("dni");
        return (dni == null || dni.isBlank())
                ? null
                : usuarioService.obtenerUsuarioPorDni(dni);
    }

    // Servir archivos de video subidos
    @ResponseBody
    @GetMapping("/videos/{fileName:.+}")
    public ResponseEntity<Resource> serveVideo(@PathVariable String fileName) throws IOException {
        Path filePath = Paths.get(VIDEO_UPLOAD_DIR, fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}
