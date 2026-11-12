package pe.edu.upeu.PharmaBackckend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.auth.*;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackckend.security.JwtService;
import pe.edu.upeu.PharmaBackckend.security.PermisosPorRol;
import pe.edu.upeu.PharmaBackckend.service.service.AuthService;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private final pe.edu.upeu.PharmaBackckend.audit.AuditoriaEventos auditoria;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                           UsuarioRepository usuarioRepository,
                           UsuarioMapper usuarioMapper,
                           PasswordEncoder passwordEncoder, pe.edu.upeu.PharmaBackckend.audit.AuditoriaEventos auditoria) {
        this.auditoria=auditoria;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        org.springframework.security.core.Authentication authentication;
        try {
            authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername().trim(),request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            auditoria.login(request.getUsername().trim(),false);
            throw ex;
        }
        auditoria.login(authentication.getName(),true);
        var usuario = usuarioRepository.findByUsernameForAuthentication(authentication.getName())
                .orElseThrow(() -> new RecursosNoEncontradosException("Usuario no encontrado"));
        var token = jwtService.generateToken(authentication.getName(), authentication.getAuthorities());
        var roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre().toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
        var permisos = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> !authority.startsWith("ROLE_"))
                .distinct()
                .toList();
        var empleado = usuario.getEmpleado();
        var nombreCompleto = (empleado.getNombres() + " " + empleado.getApellidos()).trim();
        return new LoginResponseDTO(token, "Bearer", jwtService.getExpirationSeconds(),
                usuario.getUsername(), nombreCompleto, roles, permisos);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioPerfilResponseDTO usuarioActual(String username) {
        String normalizedUsername = username == null ? "" : username.trim();
        Usuario usuario = usuarioRepository.findByUsernameForAuthentication(normalizedUsername)
                .orElseThrow(() -> new RecursosNoEncontradosException("Usuario no encontrado"));
        var roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre().toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
        var permisos = PermisosPorRol.authorities(usuario.getRoles()).stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> !authority.startsWith("ROLE_"))
                .distinct()
                .sorted()
                .toList();
        var empleado = usuario.getEmpleado();
        var nombreCompleto = (empleado.getNombres() + " " + empleado.getApellidos()).trim();
        return new UsuarioPerfilResponseDTO(usuario.getUsername(), nombreCompleto, roles, permisos);
    }

    @Override
    @Transactional
    public void cambiarPassword(String username, CambioPasswordRequestDTO request) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursosNoEncontradosException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta");
        }

        if (request.getPasswordActual().equals(request.getPasswordNueva())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada para el usuario: {}", username);
    }
}
