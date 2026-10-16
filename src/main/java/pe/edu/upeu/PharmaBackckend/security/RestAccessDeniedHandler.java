package pe.edu.upeu.PharmaBackckend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        RespuestaErrorSeguridad.escribir(response, 403,
                "No tiene permisos para realizar esta operación");
    }
}
