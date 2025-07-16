package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.PacienteDTO;
import Krisseldu.Krisseldu.service.PacienteService;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/verPaciente/{id}")
    public String verPaciente(@PathVariable Long id, Model model, HttpSession session) {
        // Obtener el DNI del usuario desde la sesión
        String dni = (String) session.getAttribute("dni");
        if (dni == null || dni.isEmpty()) {
            return "redirect:/login"; // Redirige a login si no hay sesión válida
        }

        // Obtener el usuario por su DNI
        var usuario = usuarioService.obtenerUsuarioPorEmail(dni);
        if (usuario == null) {
            return "redirect:/login"; // Redirige a login si no se encuentra el usuario
        }

        // Obtener el paciente por ID
        PacienteDTO paciente = pacienteService.obtenerPacientePorId(id);
        if (paciente == null) {
            return "redirect:/terapeuta"; // Redirige si no se encuentra el paciente
        }

        // Pasar el paciente y el nombre del usuario al modelo
        model.addAttribute("nombre", usuario.getNombre() + " " + usuario.getApellido()); // Nombre completo
        model.addAttribute("paciente", paciente);

        return "verPaciente"; // Nombre de la vista Thymeleaf
    }
}
