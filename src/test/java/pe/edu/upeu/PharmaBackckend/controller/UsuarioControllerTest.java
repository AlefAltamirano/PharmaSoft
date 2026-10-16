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
import pe.edu.upeu.PharmaBackckend.dto.auth.AsignarRolesRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.auth.EstadoUsuarioRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.PharmaBackckend.exception.GlobalExceptionHandler;
import pe.edu.upeu.PharmaBackckend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.service.service.UsuarioService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        auth = new UsernamePasswordAuthenticationToken("ADMIN", null,
                List.of(new SimpleGrantedAuthority("USUARIOS_ADMINISTRAR")));
    }

    // ==========================================
    // PUT /api/v1/usuarios/{id}/roles
    // ==========================================

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id}/roles retorna 200 OK cuando es exitoso")
    void asignarRoles_Exitoso_Retorna200() throws Exception {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(1L, 2L));

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(2L);
        response.setUsername("VENDEDOR_1");

        when(usuarioService.asignarRoles(eq(2L), any(AsignarRolesRequestDTO.class), eq("ADMIN")))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/2/roles")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value("VENDEDOR_1"));

        verify(usuarioService).asignarRoles(eq(2L), any(AsignarRolesRequestDTO.class), eq("ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id}/roles retorna 404 Not Found si el usuario o rol no existe")
    void asignarRoles_NoEncontrado_Retorna404() throws Exception {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(99L));

        when(usuarioService.asignarRoles(eq(2L), any(AsignarRolesRequestDTO.class), eq("ADMIN")))
                .thenThrow(new RecursoNoEncontradoException("Rol no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/usuarios/2/roles")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Rol no encontrado con id: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id}/roles retorna 409 Conflict si admin se quita rol ADMIN")
    void asignarRoles_AdminSeQuitaRol_Retorna409() throws Exception {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(2L));

        when(usuarioService.asignarRoles(eq(1L), any(AsignarRolesRequestDTO.class), eq("ADMIN")))
                .thenThrow(new ReglaNegocioException("Un administrador no puede quitarse a sí mismo el rol ADMIN"));

        mockMvc.perform(put("/api/v1/usuarios/1/roles")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Un administrador no puede quitarse a sí mismo el rol ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id}/roles retorna 400 Bad Request si rolIds está vacío")
    void asignarRoles_DtoVacio_Retorna400() throws Exception {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Collections.emptySet());

        mockMvc.perform(put("/api/v1/usuarios/1/roles")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.rolIds").exists());

        verify(usuarioService, never()).asignarRoles(any(), any(), any());
    }

    // ==========================================
    // PATCH /api/v1/usuarios/{id}/estado
    // ==========================================

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/estado retorna 200 OK cuando es exitoso")
    void cambiarEstado_Exitoso_Retorna200() throws Exception {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(2L);
        response.setEstado(false);

        when(usuarioService.cambiarEstado(eq(2L), any(EstadoUsuarioRequestDTO.class), eq("ADMIN")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/usuarios/2/estado")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.estado").value(false));

        verify(usuarioService).cambiarEstado(eq(2L), any(EstadoUsuarioRequestDTO.class), eq("ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/estado retorna 404 Not Found si el usuario no existe")
    void cambiarEstado_UsuarioNoExiste_Retorna404() throws Exception {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        when(usuarioService.cambiarEstado(eq(99L), any(EstadoUsuarioRequestDTO.class), eq("ADMIN")))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado con id: 99"));

        mockMvc.perform(patch("/api/v1/usuarios/99/estado")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado con id: 99"));
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/estado retorna 409 Conflict si admin intenta desactivarse a sí mismo")
    void cambiarEstado_AdminSeDesactiva_Retorna409() throws Exception {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        when(usuarioService.cambiarEstado(eq(1L), any(EstadoUsuarioRequestDTO.class), eq("ADMIN")))
                .thenThrow(new ReglaNegocioException("Un administrador no puede desactivarse a sí mismo"));

        mockMvc.perform(patch("/api/v1/usuarios/1/estado")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Un administrador no puede desactivarse a sí mismo"));
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/{id}/estado retorna 400 Bad Request si estado es null")
    void cambiarEstado_DtoNull_Retorna400() throws Exception {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(null);

        mockMvc.perform(patch("/api/v1/usuarios/1/estado")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.estado").exists());

        verify(usuarioService, never()).cambiarEstado(any(), any(), any());
    }
}

