package pe.edu.cibertec.adopta_patitas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import pe.edu.cibertec.adopta_patitas.response.AutenticacionResponse;
import pe.edu.cibertec.adopta_patitas.viewmodel.AutenticacionRequest;
import pe.edu.cibertec.adopta_patitas.viewmodel.LoginModel;

@Controller
@RequestMapping("/login")
public class LoginController {
    private RestTemplate restTemplate;

    @GetMapping("/inicio")
    public String inicio(Model model){
        LoginModel loginModel = new LoginModel("00","","");
        model.addAttribute("loginModel",loginModel); 
        return "inicio";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("tipoDocumento") String tipoDocumento,
                             @RequestParam("numeroDocumento") String numeroDocumento,
                             @RequestParam("password") String password, Model model) {
        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";
        }
        //CONSUMIR API
        String url = "http://localhost:8081/autenticacion/login";
        try {
            AutenticacionRequest authRequest = new AutenticacionRequest(tipoDocumento, numeroDocumento, password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AutenticacionRequest> requestEntity = new HttpEntity<>(authRequest, headers);

            ResponseEntity<AutenticacionResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, AutenticacionResponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                AutenticacionResponse authResponse = responseEntity.getBody();
                LoginModel loginModel = new LoginModel(authResponse.codigo(), authResponse.mensaje(), authResponse.nombreUsuario());
                model.addAttribute("loginModel", loginModel);
                return "principal";
            } else {
                LoginModel loginModel = new LoginModel("01", "Error: No se pudo autenticar", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";
            }
        } catch (Exception e) {
            LoginModel loginModel = new LoginModel("01", "Error: Ocurrió un problema con la autenticación", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";
        }
    }
}
