package pe.edu.upeu.PharmaBackckend.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        boolean activo = Boolean.TRUE.equals(usuario.getEstado())
                && usuario.getEmpleado() != null
                && Boolean.TRUE.equals(usuario.getEmpleado().getEstado());
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(PermisosPorRol.authorities(usuario.getRoles()))
                .disabled(!activo)
                .accountLocked(!activo)
                .build();
    }
}
