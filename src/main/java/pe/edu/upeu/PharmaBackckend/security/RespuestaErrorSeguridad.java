package pe.edu.upeu.PharmaBackckend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.upeu.PharmaBackckend.exception.dto.ErrorResponseDTO;

import java.io.IOException;
import java.time.LocalDateTime;

public final class RespuestaErrorSeguridad {
    private RespuestaErrorSeguridad() {
    }

    public static void escribir(HttpServletResponse response, int status, String message)
            throws IOException {
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDateTime.now(), status,
                status == 401 ? "Unauthorized" : "Forbidden", message, null, null);
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().findAndRegisterModules().writeValue(response.getOutputStream(), error);
    }
}
