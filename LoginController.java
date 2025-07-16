package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar el formulario de login
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";  // Nombre del archivo HTML para login
    }

    // Procesar la autenticación del usuario
    @PostMapping("/login")
    public String loginUser(@RequestParam String dni, @RequestParam String contrasena, Model model, HttpSession session) {
        try {
            // Verificar si el usuario existe usando el servicio
            Usuario usuarioObj = usuarioService.authenticateByDni(dni, contrasena);

            // Si la autenticación es exitosa, guardar información en la sesión
            session.setAttribute("usuario", usuarioObj);  // Guardamos el usuario completo en la sesión
            session.setAttribute("dni", dni);  // Guardamos el dni del usuario también para referencia posterior

            // Obtener nombre completo y guardarlo en la sesión
            String nombreCompleto = usuarioService.obtenerNombreCompleto(usuarioObj.getId());
            session.setAttribute("nombre", nombreCompleto);  // Almacenar nombre completo en sesión

            // Agregar mensaje de éxito
            model.addAttribute("successMessage", "Se inició sesión correctamente.");

            // Redirigir a la página correspondiente
            if ("TERAPEUTA".equals(usuarioObj.getRol())) {
                return "redirect:/terapeuta";  // Redirigir a la vista de terapeuta
            } else if ("PACIENTE".equals(usuarioObj.getRol())) {
                return "redirect:/home";  // Redirigir a la vista de paciente (home)
            } else {
                model.addAttribute("errorMessage", "Rol desconocido.");
                return "login";  // Redirige al login con un mensaje de error
            }
        } catch (Exception e) {
            e.printStackTrace();  // Añadí esto para hacer debugging si ocurre un error
            model.addAttribute("errorMessage", "DNI o contraseña incorrectos.");
            return "login";  // Redirige al login con un mensaje de error
        }
    }

    // Ruta para cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Invalidar la sesión
        return "redirect:/login";  // Redirigir al login
    }
}
