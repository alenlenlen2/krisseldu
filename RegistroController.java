package Krisseldu.Krisseldu.controller;

import Krisseldu.Krisseldu.model.Usuario;
import Krisseldu.Krisseldu.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    // Mostrar formulario de registro
    @GetMapping("/registro")
    public String showRegistrationForm(Model model,
                                       @RequestParam(value = "successMessage", required = false) String successMessage,
                                       @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        if (successMessage != null) model.addAttribute("successMessage", successMessage);
        if (errorMessage != null) model.addAttribute("errorMessage", errorMessage);
        return "registro";  // Vista del formulario de registro
    }

    // Procesar el registro del usuario
    @PostMapping("/registro")
    public String registerUser(@RequestParam String nombre,
                               @RequestParam String apellido,
                               @RequestParam int edad,
                               @RequestParam String condicion,
                               @RequestParam String dni,  // Usamos el DNI como identificador
                               @RequestParam String email, // Correo electrónico agregado
                               @RequestParam String password,
                               Model model) {
        try {
            // Validar si el correo electrónico ya existe
            if (usuarioService.obtenerUsuarioPorEmail(email) != null) {
                model.addAttribute("errorMessage", "El correo electrónico ya está registrado.");
                return "registro";
            }

            // Validar si el DNI ya está registrado
            if (usuarioService.obtenerUsuarioPorDni(dni) != null) {
                model.addAttribute("errorMessage", "El DNI ya está registrado.");
                return "registro";
            }

            // Crear el objeto Usuario
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEdad(edad);
            usuario.setDni(dni); // Guardar DNI como identificador
            usuario.setEmail(email); // Guardar correo electrónico
            usuario.setPassword(password);  // Asegúrate de encriptar la contraseña antes de guardarla
            usuario.setRol("PACIENTE");

            // Guardar usuario en la base de datos
            usuarioService.guardarUsuario(usuario);

            // Buscar usuario guardado para obtener su ID
            Usuario usuarioGuardado = usuarioService.obtenerUsuarioPorEmail(email);

            // Obtener el ID de la condición seleccionada
            Long condicionId = usuarioService.obtenerIdCondicionPorNombre(condicion);
            if (condicionId == null) {
                model.addAttribute("errorMessage", "Condición no válida.");
                return "registro";
            }

            // Guardar la relación usuario-condición
            usuarioService.guardarUsuarioCondicion(usuarioGuardado.getId(), condicionId);

            // Redirigir con un mensaje de éxito
            return "redirect:/registro?successMessage=Usuario registrado exitosamente";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Hubo un error al registrar el usuario. Intenta de nuevo.");
            return "registro";  // En caso de error, muestra mensaje en el formulario
        }
    }
}
