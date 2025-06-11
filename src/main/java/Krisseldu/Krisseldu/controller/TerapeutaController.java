package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.PacienteDTO;
import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.MensajeService;
import Krisseldu.Krisseldu.service.PacienteService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/terapeuta")
public class TerapeutaController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MensajeService mensajeService;

    // Mostrar panel principal terapeuta con lista de pacientes
    @GetMapping
    public String mostrarVistaTerapeuta(@RequestParam(required = false) String filtro, Model model, HttpSession session) {
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isBlank()) {
            return "redirect:/login";  // Redirige a login si no hay sesión válida
        }

        // Obtener los datos del terapeuta
        Usuario terapeuta = usuarioService.obtenerUsuarioPorDni(dni);
        if (terapeuta == null) {
            return "redirect:/login";  // Si el terapeuta no existe, redirige a login
        }

        // Obtener el nombre completo del terapeuta y asignarlo al modelo
        String nombreCompleto = terapeuta.getNombre() + " " + terapeuta.getApellido();
        model.addAttribute("nombre", nombreCompleto);

        // Obtener la lista de pacientes asignados al terapeuta, filtrados por nombre o DNI
        List<PacienteDTO> pacientes = pacienteService.obtenerPacientesPorTerapeuta(terapeuta.getId(), filtro);
        model.addAttribute("pacientes", pacientes);

        return "terapeuta";  // Vista principal del terapeuta
    }

    // Ver detalles de paciente y formulario para enviar mensaje
    @GetMapping("/verPaciente/{pacienteId}")
    public String verPaciente(@PathVariable Long pacienteId, Model model, HttpSession session) {
        // Verificar si la sesión tiene un dni válido
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isBlank()) {
            return "redirect:/login";  // Redirige a login si no hay sesión válida
        }

        // Obtener los datos del terapeuta a partir del dni
        Usuario terapeuta = usuarioService.obtenerUsuarioPorDni(dni);
        if (terapeuta == null) {
            return "redirect:/login";  // Si el terapeuta no existe, redirige a login
        }

        // Obtener el nombre completo del terapeuta y asignarlo al modelo
        String nombreCompleto = terapeuta.getNombre() + " " + terapeuta.getApellido();
        model.addAttribute("nombre", nombreCompleto);

        // Obtener los datos del paciente
        PacienteDTO paciente = pacienteService.obtenerPacientePorId(pacienteId);
        if (paciente == null) {
            model.addAttribute("errorMessage", "Paciente no encontrado.");
            return "error";  // Redirige a una página de error si el paciente no existe
        }

        // Asignar el paciente al modelo para la vista
        model.addAttribute("paciente", paciente);

        return "verPaciente";  // Vista con los datos del paciente
    }

    // Enviar mensaje al paciente
    @PostMapping("/enviarMensaje")
    public String enviarMensaje(@RequestParam Long pacienteId,
                                @RequestParam String mensaje,
                                HttpSession session) {
        try {
            // Verificar si la sesión tiene un dni válido
            String dni = (String) session.getAttribute("dni");
            if (dni == null || dni.isBlank()) {
                return "redirect:/login";  // Redirige a login si no hay sesión válida
            }

            // Obtener el ID del terapeuta usando el DNI
            Usuario terapeuta = usuarioService.obtenerUsuarioPorDni(dni);
            if (terapeuta == null) {
                return "redirect:/login";  // Si el terapeuta no existe, redirige a login
            }

            // Enviar el mensaje al paciente
            mensajeService.enviarMensaje(terapeuta.getId(), pacienteId, mensaje);

            // Redirige al panel principal del terapeuta después de enviar el mensaje
            return "redirect:/terapeuta";
        } catch (Exception e) {
            e.printStackTrace();  // Imprimir el error completo en los logs
            return "redirect:/terapeuta?error=true";  // Redirige con un error si algo sale mal
        }
    }
}
