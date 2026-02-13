package OBenitez.ProgramacionNCapasNoviembre25.Controller;

import OBenitez.ProgramacionNCapasNoviembre25.ML.Direccion;
import OBenitez.ProgramacionNCapasNoviembre25.ML.Pais;
import OBenitez.ProgramacionNCapasNoviembre25.ML.Result;
import OBenitez.ProgramacionNCapasNoviembre25.ML.Rol;
import OBenitez.ProgramacionNCapasNoviembre25.ML.Usuario;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("usuario")
public class UsuarioController {

    private static final String urlBase = "http://localhost:8080/api";
    
    @GetMapping
    public String GetAll(Model model, HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Result<Usuario>> responseEntity = restTemplate.exchange(
                urlBase + "/usuario",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Result<Usuario>>() {}
            );

            ResponseEntity<Result<Rol>> responseEntityRoles = restTemplate.exchange(
                urlBase + "/rol",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Result<Rol>>() {}
            );

            model.addAttribute("Usuarios", responseEntity.getBody().Objects);
            model.addAttribute("Roles", responseEntityRoles.getBody().Objects);
            model.addAttribute("usuarioBusqueda", new Usuario());
            model.addAttribute("tokenValido", true);
            model.addAttribute("jwtToken", token);

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                session.invalidate();
                return "redirect:/login";
            } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                session.invalidate();
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Error: " + ex.getMessage());
                model.addAttribute("Usuarios", new ArrayList<>());
                model.addAttribute("Roles", new ArrayList<>());
                model.addAttribute("usuarioBusqueda", new Usuario());
                model.addAttribute("tokenValido", false);
            }
        } catch (HttpServerErrorException ex) {
            model.addAttribute("error", "Error del servidor");
            model.addAttribute("Usuarios", new ArrayList<>());
            model.addAttribute("Roles", new ArrayList<>());
            model.addAttribute("usuarioBusqueda", new Usuario());
            model.addAttribute("tokenValido", false);
        } catch (Exception ex) {
            model.addAttribute("error", "Error de conexión");
            model.addAttribute("Usuarios", new ArrayList<>());
            model.addAttribute("Roles", new ArrayList<>());
            model.addAttribute("usuarioBusqueda", new Usuario());
            model.addAttribute("tokenValido", false);
        }

        return "Index";
    }


    
    @PostMapping("busqueda")
    public String Busqueda(@ModelAttribute("usuario") Usuario usuario, Model model, HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate(); 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        try {
            HttpEntity<Usuario> requestEntity = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result<Usuario>> responseEntityBusqueda = restTemplate.exchange(
                    urlBase + "/usuario/busqueda",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Result<Usuario>>() {}
            );
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Result<Rol>> responseEntityRoles = restTemplate.exchange(
                    urlBase + "/rol",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<Rol>>() {}
            );
            
            if (responseEntityBusqueda.getStatusCode().is2xxSuccessful()) {
                Result resultBusqueda = responseEntityBusqueda.getBody();
                model.addAttribute("Usuarios", resultBusqueda.Objects);
            } else{
                model.addAttribute("Usuarios", new ArrayList<>());
                if (responseEntityBusqueda.getStatusCodeValue() == 401 || responseEntityBusqueda.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
            if (responseEntityRoles.getStatusCode().is2xxSuccessful()) {
                Result resultRoles = responseEntityRoles.getBody();
                model.addAttribute("Roles", resultRoles.Objects);
            } else{
                model.addAttribute("Roles", new ArrayList<>());
            }
            
            model.addAttribute("usuarioBusqueda", usuario);
        } catch (Exception ex) {
            model.addAttribute("Usuarios", new ArrayList<>());
            model.addAttribute("Roles", new ArrayList<>());
            model.addAttribute("usuarioBusqueda", new Usuario());
        }
        return "Index";
    }
    
    @GetMapping("detail/{IdUsuario}")
    public String Detail(@PathVariable("IdUsuario") int IdUsuario, Model model, HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }
       
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Result<Usuario>> responseEntity = restTemplate.exchange(
                urlBase + "/usuario/" + IdUsuario,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Result<Usuario>>() {}
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Result result = responseEntity.getBody();
                model.addAttribute("Usuario", result.Object);
                try {
                    ResponseEntity<Result<Rol>> responseEntityRoles = restTemplate.exchange(
                        urlBase + "/rol",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<Rol>>() {}
                    );
                    ResponseEntity<Result<Pais>> responseEntityPais = restTemplate.exchange(
                        urlBase + "/pais",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<Pais>>() {}
                    );
                    
                    if (responseEntityRoles.getBody() != null) {
                        model.addAttribute("Roles", responseEntityRoles.getBody().Objects);
                    }
                    if (responseEntityPais.getBody() != null) {
                        model.addAttribute("Paises", responseEntityPais.getBody().Objects);
                    }
                    model.addAttribute("jwtToken", token);
                } catch (Exception ex) {
                    model.addAttribute("Roles", new ArrayList<>());
                    model.addAttribute("Paises", new ArrayList<>());
                }
            } else {
                if (responseEntity.getStatusCodeValue() == 401 || responseEntity.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            return "redirect:/usuario";
        }
        return "UsuarioDetail";
    }
    
    @GetMapping("deleteAddress/{IdDireccion}/{IdUsuario}")
    public String DeleteAddress(@PathVariable("IdDireccion") int IdDireccion, @PathVariable("IdUsuario") int IdUsuario, RedirectAttributes redirectAttributes, HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Result result = new Result();
        try {
            ResponseEntity<Result<Direccion>> responseEntityDeleteAddress = restTemplate.exchange(
                urlBase + "/direccion/" + IdDireccion,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<Result<Direccion>>() {}
            );
        
            if (responseEntityDeleteAddress.getStatusCode().is2xxSuccessful()) {
                result.Correct = true;
                result.Object = "La direccion fue eliminada";
            } else {
                result.Correct = false;
                result.Object = "No fue posible eliminar la direccion :c";
                if (responseEntityDeleteAddress.getStatusCodeValue() == 401 || responseEntityDeleteAddress.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            result.Correct = false;
            result.Object = ex.getLocalizedMessage();
            result.ex = ex;
        }
        
        redirectAttributes.addFlashAttribute("resultDeleteAddress", result);
        return "redirect:/usuario/detail/"+IdUsuario;
    }
    
    @PostMapping("/updatePhoto")
    public String updatePhoto(@ModelAttribute Usuario usuario,
                              @RequestParam("imagenUsuario") MultipartFile imagenUsuario,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate(); 
        Result result = new Result();
        
        if (imagenUsuario.isEmpty()) {
            result.Correct = false;
            result.Object = "No se seleccionó ninguna imagen";
            redirectAttributes.addFlashAttribute("resultUpdatePhoto", result);
            return "redirect:/usuario/detail/" + usuario.getIdUsuario();
        } 
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        try {
            String encodedString = Base64.getEncoder().encodeToString(imagenUsuario.getBytes());
            HttpEntity<String> requestEntity = new HttpEntity<>(encodedString, headers);

            ResponseEntity<Result> responseEntityUpdatePhoto = restTemplate.exchange(
                    urlBase + "/usuario/"+usuario.getIdUsuario()+"/updatePhoto",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Result>() {}
            );
            if (responseEntityUpdatePhoto.getStatusCode().is2xxSuccessful()) {
                result.Correct = true;
                result.Object = "Se actualizó correctamente la foto";
            } else {
                result.Correct = false;
                result.Object = "No se pudo actualizar la foto :c";
                if (responseEntityUpdatePhoto.getStatusCodeValue() == 401 || responseEntityUpdatePhoto.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (IOException ex) {
            result.Correct = false;
            result.ErrorMessage = ex.getLocalizedMessage();
            result.Object = "No pudo procesarse la imagen";
            result.ex = ex;
        }
        redirectAttributes.addFlashAttribute("resultUpdatePhoto", result);
        return "redirect:/usuario/detail/" + usuario.getIdUsuario();
    }

    @PostMapping("deletePhoto/{IdUsuario}")
    public String deletePhoto(@PathVariable int IdUsuario, RedirectAttributes redirectAttributes, HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Result result = new Result();

        try {
            ResponseEntity<Result> responseEntityDeletePhoto = restTemplate.exchange(
                    urlBase + "/usuario/" + IdUsuario + "/photo",
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<Result>() {}
            );
            if (responseEntityDeletePhoto.getStatusCode().is2xxSuccessful()) {
                result.Correct = true;
                result.Object = "Se eliminó correctamente la foto";
            } else {
                result.Correct = false;
                result.Object = "No se pudo eliminar la foto :c";
                if (responseEntityDeletePhoto.getStatusCodeValue() == 401 || responseEntityDeletePhoto.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            result.Correct = false;
            result.ErrorMessage = ex.getLocalizedMessage();
            result.Object = "No se pudo eliminar la foto :c";
            result.ex = ex;
        }
        redirectAttributes.addFlashAttribute("resultUpdatePhoto", result);
        return "redirect:/usuario/detail/" + IdUsuario;
    }
    
    @GetMapping("form")
    public String Form(Model model, HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Result<Rol>> responseEntityRoles = restTemplate.exchange(
                    urlBase + "/rol",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<Rol>>() {}
            );
            ResponseEntity<Result<Pais>> responseEntityPaises = restTemplate.exchange(
                    urlBase + "/pais",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<Pais>>() {}
            );
            
            if (responseEntityRoles.getStatusCode().is2xxSuccessful()) {
                Result resultRoles = responseEntityRoles.getBody();
                model.addAttribute("Roles", resultRoles.Objects);
            } else {
                model.addAttribute("Roles", new ArrayList<>());
                if (responseEntityRoles.getStatusCodeValue() == 401 || responseEntityRoles.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
            
            if (responseEntityPaises.getStatusCode().is2xxSuccessful()) {
                Result resultPais = responseEntityPaises.getBody();
                model.addAttribute("Paises", resultPais.Objects);
            } else {
                model.addAttribute("Paises", new ArrayList<>());
            }
        } catch (Exception ex) {
            model.addAttribute("Roles", new ArrayList<>());
            model.addAttribute("Paises", new ArrayList<>());
        }
        
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(0);
        model.addAttribute("Usuario", usuario);
     
        return "UsuarioForm";
    }
    
    @PostMapping("add")
    public String Add(@Valid @ModelAttribute("Usuario") Usuario usuario, BindingResult bindingResult, Model model, @RequestParam("imagenUsuario") MultipartFile imagenUsuario, RedirectAttributes redirectAttributes, HttpSession session) throws IOException{
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("Usuario", usuario);
            return "UsuarioForm"; 
        }
        
        RestTemplate restTemplate = new RestTemplate(); 
        Result result = new Result();

        if (imagenUsuario.isEmpty()) {
            usuario.setImagen(null);
        } else {
            String encodedString = Base64.getEncoder().encodeToString(imagenUsuario.getBytes());
            usuario.setImagen(encodedString);
        }
        usuario.setStatus(1);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<Usuario> requestEntity = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result> responseEntityAddUser = restTemplate.exchange(
                    urlBase + "/usuario",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Result>() {}
            );
            Result resultAddUser = responseEntityAddUser.getBody();

            if (resultAddUser != null && responseEntityAddUser.getStatusCode().is2xxSuccessful()) {
                result.Correct = true;
                result.Object = "El usuario se agregó correctamente";
            } else {
                result.Correct = false;
                result.Object = "No fue posible agregar al usuario";
                if (responseEntityAddUser.getStatusCodeValue() == 401 || responseEntityAddUser.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            result.Correct = false;
            result.Object = "No fue posible agregar al usuario";
            result.ErrorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }
        redirectAttributes.addFlashAttribute("resultAddUserFull", result);
        return "redirect:/usuario";
    }
    
    @PostMapping("formEditable")
    public String Form(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes, HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        if (usuario.getDirecciones().get(0).getIdDireccion() == -1) {
            Result result = new Result();
            usuario.Direcciones.remove(0);
            
            try {
                HttpEntity<Usuario> requestEntity = new HttpEntity<>(usuario, headers);

                ResponseEntity<Result> responseEntityUpdateUser = restTemplate.exchange(
                        urlBase + "/usuario",
                        HttpMethod.PUT,
                        requestEntity,
                        new ParameterizedTypeReference<Result>() {}
                );
                Result resultUpdateUser = responseEntityUpdateUser.getBody();

                if (resultUpdateUser != null && resultUpdateUser.Correct) {
                    result.Correct = true;
                    result.Object = "El usuario se actualizó correctamente";
                } else {
                    result.Correct = false;
                    result.Object = "No fue posible actualizar al usuario";
                    if (responseEntityUpdateUser.getStatusCodeValue() == 401 || responseEntityUpdateUser.getStatusCodeValue() == 403) {
                        session.invalidate();
                        return "redirect:/login";
                    }
                }
            } catch (Exception ex) {
                result.Correct = false;
                result.Object = "No fue posible actualizar al usuario";
                result.ErrorMessage = ex.getLocalizedMessage();
                result.ex = ex;
            }

            redirectAttributes.addFlashAttribute("resultEditUserBasic", result);
            return "redirect:/usuario/detail/" + usuario.getIdUsuario();
        }else if(usuario.Direcciones.get(0).getIdDireccion() == 0){
            Result result = new Result();
            try {
                Direccion direccionNueva = usuario.Direcciones.get(0);
                direccionNueva.Usuario = new Usuario();
                direccionNueva.Usuario.setIdUsuario(usuario.getIdUsuario());
                HttpEntity<Direccion> requestEntity = new HttpEntity<>(direccionNueva, headers);

                ResponseEntity<Result> responseEntityAddAddress = restTemplate.exchange(
                        urlBase + "/direccion",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Result>() {}
                );
                Result resultAddAddress = responseEntityAddAddress.getBody();

                if (resultAddAddress != null && responseEntityAddAddress.getStatusCode().is2xxSuccessful()) {
                    result.Correct = true;
                    result.Object = "La direccion se agregó correctamente";
                } else {
                    result.Correct = false;
                    result.Object = "No fue posible agregar la direccion";
                    if (responseEntityAddAddress.getStatusCodeValue() == 401 || responseEntityAddAddress.getStatusCodeValue() == 403) {
                        session.invalidate();
                        return "redirect:/login";
                    }
                }
            } catch (Exception ex) {
                result.Correct = false;
                result.Object = "No fue posible agregar la direccion";
                result.ErrorMessage = ex.getLocalizedMessage();
                result.ex = ex;
            }
            redirectAttributes.addFlashAttribute("resultAddAddress", result);
            return "redirect:/usuario/detail/"+usuario.getIdUsuario();
        }else{
            Result result = new Result();
            try {
                Direccion dirreccion = usuario.Direcciones.get(0);
                dirreccion.Usuario = new Usuario();
                dirreccion.Usuario.setIdUsuario(usuario.getIdUsuario());
                HttpEntity<Direccion> requestEntity = new HttpEntity<>(dirreccion, headers);

                ResponseEntity<Result> responseEntityUpdateAddress = restTemplate.exchange(
                        urlBase + "/direccion",
                        HttpMethod.PUT,
                        requestEntity,
                        new ParameterizedTypeReference<Result>() {}
                );
                Result resultUpdateAddress = responseEntityUpdateAddress.getBody();

                if (resultUpdateAddress != null && resultUpdateAddress.Correct) {
                    result.Correct = true;
                    result.Object = "La direccion se actualizó correctamente";
                } else {
                    result.Correct = false;
                    result.Object = "No fue posible actualizar la direccion";
                    if (responseEntityUpdateAddress.getStatusCodeValue() == 401 || responseEntityUpdateAddress.getStatusCodeValue() == 403) {
                        session.invalidate();
                        return "redirect:/login";
                    }
                }
            } catch (Exception ex) {
                result.Correct = false;
                result.Object = "No fue posible actualizar la direccion";
                result.ErrorMessage = ex.getLocalizedMessage();
                result.ex = ex;
            }
            redirectAttributes.addFlashAttribute("resultEditAddress", result);
            return "redirect:/usuario/detail/"+usuario.getIdUsuario();
        }
    }
    
    @GetMapping("cargaMasiva")
    public String CargaMasiva(HttpSession session){
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        return "CargaMasiva";
    }
   
    @PostMapping("cargaMasiva")
    public String CargaMasiva(@RequestParam("archivo") MultipartFile archivo, Model model, HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");

        if (token == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        
        if (archivo.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar un archivo");
            return "CargaMasiva";
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(token);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", archivo.getResource());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Result> resposeEntity = restTemplate.exchange(
                    urlBase + "/usuario/cargaMasiva/validar", 
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Result>(){}
            );
            Result result = resposeEntity.getBody();
            
            if (resposeEntity.getStatusCode().is2xxSuccessful()) {
                session.setAttribute("token", result.Object);
                model.addAttribute("exito", result.ErrorMessage);
                model.addAttribute("mostrarProcesar", true);
            } else {
                model.addAttribute("error", "Error al validar");
                if (resposeEntity.getStatusCodeValue() == 401 || resposeEntity.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Error: " + ex.getMessage());
        }
        return "CargaMasiva";
    }

    @GetMapping("cargaMasiva/procesar")
    public String ProcesarArchivo(HttpSession session, RedirectAttributes redirectAttributes){
        String jwtToken = (String) session.getAttribute("jwtToken");

        if (jwtToken == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Result result = new Result();
        
        String token = (String) session.getAttribute("token");
        
        if (token == null) {
            result.Correct = false;
            result.Object = "No hay archivos pendientes";
            redirectAttributes.addFlashAttribute("resultCargaMasiva", result);
            return "redirect:/usuario";
        }
        
        try {
            ResponseEntity<Result> resposeEntity = restTemplate.exchange(
                urlBase + "/usuario/cargaMasiva/procesar/" + token, 
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Result>(){}
            );
            
            if (resposeEntity.getStatusCode().is2xxSuccessful()) {
                result = resposeEntity.getBody();
                session.removeAttribute("token");
            } else {
                result.Correct = false;
                result.Object = "Error al procesar";
                if (resposeEntity.getStatusCodeValue() == 401 || resposeEntity.getStatusCodeValue() == 403) {
                    session.invalidate();
                    return "redirect:/login";
                }
            }
        } catch (Exception ex) {
            result.Correct = false;
            result.Object = "Error al procesar: " + ex.getMessage();
        }
        
        redirectAttributes.addFlashAttribute("resultCargaMasiva", result);
        return "redirect:/usuario";
    }   
}
