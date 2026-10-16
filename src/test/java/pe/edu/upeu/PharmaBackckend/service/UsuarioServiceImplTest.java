package pe.edu.upeu.PharmaBackckend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PharmaBackckend.dto.auth.AsignarRolesRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.auth.EstadoUsuarioRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.PharmaBackckend.entity.Empleado;
import pe.edu.upeu.PharmaBackckend.entity.Rol;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackckend.repository.EmpleadoRepository;
import pe.edu.upeu.PharmaBackckend.repository.RolRepository;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackckend.service.impl.UsuarioServiceImpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper mapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario adminUser;
    private Usuario standardUser;
    private Rol rolAdmin;
    private Rol rolVendedor;
    private Rol rolAlmacenero;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol(1L, "ADMIN", "Administrador");
        rolVendedor = new Rol(2L, "VENDEDOR", "Vendedor");
        rolAlmacenero = new Rol(3L, "ALMACENERO", "Almacenero");

        adminUser = new Usuario();
        adminUser.setId(1L);
        adminUser.setUsername("ADMIN");
        adminUser.setEstado(true);
        adminUser.setRoles(new HashSet<>(Set.of(rolAdmin)));

        standardUser = new Usuario();
        standardUser.setId(2L);
        standardUser.setUsername("VENDEDOR_1");
        standardUser.setEstado(true);
        standardUser.setRoles(new HashSet<>(Set.of(rolVendedor)));
    }

    // ==========================================
    // Test: Asignar Roles (B2)
    // ==========================================

    @Test
    @DisplayName("asignarRoles exitoso cuando admin actualiza roles de otro usuario")
    void asignarRoles_Exitoso_OtroUsuario() {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(2L, 3L));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(standardUser));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolVendedor));
        when(rolRepository.findById(3L)).thenReturn(Optional.of(rolAlmacenero));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(standardUser);

        UsuarioResponseDTO expectedResponse = new UsuarioResponseDTO();
        expectedResponse.setId(2L);
        expectedResponse.setUsername("VENDEDOR_1");
        when(mapper.toResponse(any(Usuario.class))).thenReturn(expectedResponse);

        UsuarioResponseDTO response = usuarioService.asignarRoles(2L, request, "ADMIN");

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(2, standardUser.getRoles().size());
        verify(usuarioRepository).save(standardUser);
    }

    @Test
    @DisplayName("asignarRoles lanza RecursoNoEncontradoException si el usuario no existe")
    void asignarRoles_UsuarioNoExiste_LanzaRecursoNoEncontradoException() {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(2L));

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                usuarioService.asignarRoles(99L, request, "ADMIN")
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("asignarRoles lanza RecursoNoEncontradoException si un rol no existe")
    void asignarRoles_RolNoExiste_LanzaRecursoNoEncontradoException() {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(99L));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(standardUser));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                usuarioService.asignarRoles(2L, request, "ADMIN")
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("asignarRoles lanza ReglaNegocioException si el admin intenta quitarse su propio rol ADMIN")
    void asignarRoles_AdminSeQuitaRolAdmin_LanzaReglaNegocioException() {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(2L)); // Solo rol VENDEDOR, sin ADMIN (ID 1)

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolVendedor));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                usuarioService.asignarRoles(1L, request, "admin")
        );

        assertEquals("Un administrador no puede quitarse a sí mismo el rol ADMIN", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("asignarRoles exitoso si el admin se asigna roles manteniendo el rol ADMIN")
    void asignarRoles_AdminMantieneRolAdmin_Exitoso() {
        AsignarRolesRequestDTO request = new AsignarRolesRequestDTO(Set.of(1L, 2L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolVendedor));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(adminUser);

        UsuarioResponseDTO expectedResponse = new UsuarioResponseDTO();
        expectedResponse.setId(1L);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(expectedResponse);

        UsuarioResponseDTO response = usuarioService.asignarRoles(1L, request, "ADMIN");

        assertNotNull(response);
        assertEquals(2, adminUser.getRoles().size());
        verify(usuarioRepository).save(adminUser);
    }

    // ==========================================
    // Test: Cambiar Estado (B3)
    // ==========================================

    @Test
    @DisplayName("cambiarEstado exitoso cuando desactiva a otro usuario")
    void cambiarEstado_DesactivarOtroUsuario_Exitoso() {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(standardUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(standardUser);

        UsuarioResponseDTO expectedResponse = new UsuarioResponseDTO();
        expectedResponse.setId(2L);
        expectedResponse.setEstado(false);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(expectedResponse);

        UsuarioResponseDTO response = usuarioService.cambiarEstado(2L, request, "ADMIN");

        assertNotNull(response);
        assertFalse(standardUser.getEstado());
        verify(usuarioRepository).save(standardUser);
    }

    @Test
    @DisplayName("cambiarEstado lanza RecursoNoEncontradoException si el usuario no existe")
    void cambiarEstado_UsuarioNoExiste_LanzaRecursoNoEncontradoException() {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                usuarioService.cambiarEstado(99L, request, "ADMIN")
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado lanza ReglaNegocioException si el admin intenta desactivarse a sí mismo")
    void cambiarEstado_AdminSeDesactivaASiMismo_LanzaReglaNegocioException() {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(false);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                usuarioService.cambiarEstado(1L, request, "admin")
        );

        assertEquals("Un administrador no puede desactivarse a sí mismo", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado exitoso si el admin se activa a sí mismo (estado true)")
    void cambiarEstado_AdminSeActivaASiMismo_Exitoso() {
        EstadoUsuarioRequestDTO request = new EstadoUsuarioRequestDTO(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(adminUser);

        UsuarioResponseDTO expectedResponse = new UsuarioResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setEstado(true);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(expectedResponse);

        UsuarioResponseDTO response = usuarioService.cambiarEstado(1L, request, "ADMIN");

        assertNotNull(response);
        assertTrue(adminUser.getEstado());
        verify(usuarioRepository).save(adminUser);
    }
}

