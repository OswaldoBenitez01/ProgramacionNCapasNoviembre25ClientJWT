
package OBenitez.ProgramacionNCapasNoviembre25.Controller;

import OBenitez.ProgramacionNCapasNoviembre25.ML.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private static final String urlBase = "http://localhost:8080/api";

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute @Valid LoginRequest loginRequest, 
                       BindingResult bindingResult, 
                       HttpSession session, 
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                urlBase + "/auth/login",
                loginRequest,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                session.setAttribute("jwtToken", response.getBody());
                return "redirect:/usuario";
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos");
                return "redirect:/login";
            }
            
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("error", "Error al conectarse al servidor");
            return "redirect:/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error interno del sistema");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("info", "Sesión cerrada correctamente");
        return "redirect:/login";
    }
}


