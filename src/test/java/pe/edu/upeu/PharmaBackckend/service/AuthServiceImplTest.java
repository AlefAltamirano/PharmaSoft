package pe.edu.upeu.PharmaBackckend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PharmaBackckend.dto.auth.CambioPasswordRequestDTO;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackckend.security.JwtService;
import pe.edu.upeu.PharmaBackckend.service.impl.AuthServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("USER_TEST");
        usuario.setPassword("$2a$10$oldHashedPassword");
        usuario.setEstado(true);
    }

    @Test
    @DisplayName("cambiarPassword exitoso actualiza la contraseña y guarda el usuario")
    void cambiarPassword_Exitoso() {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("OldPass123", "NewPass456");

        when(usuarioRepository.findByUsernameIgnoreCase("USER_TEST")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("OldPass123", "$2a$10$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass456")).thenReturn("$2a$10$newEncodedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        assertDoesNotThrow(() -> authService.cambiarPassword("USER_TEST", request));

        assertEquals("$2a$10$newEncodedPass", usuario.getPassword());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("cambiarPassword lanza excepción si el usuario no existe")
    void cambiarPassword_UsuarioNoExiste_LanzaExcepcion() {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("OldPass123", "NewPass456");

        when(usuarioRepository.findByUsernameIgnoreCase("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(RecursosNoEncontradosException.class, () ->
                authService.cambiarPassword("INEXISTENTE", request)
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarPassword lanza ReglaNegocioException si la contraseña actual no coincide")
    void cambiarPassword_PasswordActualIncorrecta_LanzaReglaNegocioException() {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("WrongPass123", "NewPass456");

        when(usuarioRepository.findByUsernameIgnoreCase("USER_TEST")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("WrongPass123", "$2a$10$oldHashedPassword")).thenReturn(false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                authService.cambiarPassword("USER_TEST", request)
        );

        assertEquals("La contraseña actual no es correcta", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarPassword lanza ReglaNegocioException si la nueva contraseña es igual a la actual")
    void cambiarPassword_PasswordNuevaIgualActual_LanzaReglaNegocioException() {
        CambioPasswordRequestDTO request = new CambioPasswordRequestDTO("SamePass123", "SamePass123");

        when(usuarioRepository.findByUsernameIgnoreCase("USER_TEST")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SamePass123", "$2a$10$oldHashedPassword")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                authService.cambiarPassword("USER_TEST", request)
        );

        assertEquals("La nueva contraseña debe ser distinta de la actual", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}

