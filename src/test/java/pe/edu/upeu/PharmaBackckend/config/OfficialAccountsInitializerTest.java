package pe.edu.upeu.PharmaBackckend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.repository.*;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OfficialAccountsInitializerTest {
    @Test
    void reiniciarNoReescribeCuentasExistentes() {
        var empleados = mock(EmpleadoRepository.class);
        var roles = mock(RolRepository.class);
        var usuarios = mock(UsuarioRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(usuarios.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.of(new Usuario()));

        new OfficialAccountsInitializer(empleados, roles, usuarios, encoder).run();

        verifyNoInteractions(empleados, roles, encoder);
        verify(usuarios, never()).save(any());
    }
}
