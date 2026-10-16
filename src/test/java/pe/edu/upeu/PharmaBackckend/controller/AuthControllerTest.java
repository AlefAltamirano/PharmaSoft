package pe.edu.upeu.PharmaBackckend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upeu.PharmaBackckend.dto.auth.CambioPasswordRequestDTO;
import pe.edu.upeu.PharmaBackckend.exception.GlobalExceptionHandler;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.service.service.AuthService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("PUT /api/v1/auth/password retorna 204 No Content cuando el cambio es exitoso")
    void cambiarPassword_Exitoso_Retorna204() throws Exception {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("OldPassword123", "NewPassword456");
        Authentication auth = new UsernamePasswordAuthenticationToken("ADMIN", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        doNothing().when(authService).cambiarPassword(eq("ADMIN"), any(CambioPasswordRequestDTO.class));

        mockMvc.perform(put("/api/v1/auth/password")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).cambiarPassword(eq("ADMIN"), any(CambioPasswordRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/password retorna 409 Conflict cuando falla regla de negocio")
    void cambiarPassword_ReglaNegocio_Retorna409() throws Exception {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("WrongPassword123", "NewPassword456");
        Authentication auth = new UsernamePasswordAuthenticationToken("ADMIN", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        doThrow(new ReglaNegocioException("La contraseña actual no es correcta"))
                .when(authService).cambiarPassword(eq("ADMIN"), any(CambioPasswordRequestDTO.class));

        mockMvc.perform(put("/api/v1/auth/password")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La contraseña actual no es correcta"));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/password retorna 400 Bad Request cuando el DTO es inválido")
    void cambiarPassword_DtoInvalido_Retorna400() throws Exception {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("", "corta");
        Authentication auth = new UsernamePasswordAuthenticationToken("ADMIN", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(put("/api/v1/auth/password")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.passwordActual").exists());

        verify(authService, never()).cambiarPassword(any(), any());
    }
}

