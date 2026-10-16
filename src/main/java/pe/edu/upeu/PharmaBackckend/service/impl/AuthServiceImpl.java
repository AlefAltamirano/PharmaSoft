package pe.edu.upeu.PharmaBackckend.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.auth.*;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackckend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackckend.security.JwtService;
import pe.edu.upeu.PharmaBackckend.service.service.AuthService;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                           UsuarioRepository usuarioRepository,
                           UsuarioMapper usuarioMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));
        var usuario = usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
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
    public UsuarioResponseDTO usuarioActual(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursosNoEncontradosException("Usuario no encontrado"));
        return usuarioMapper.toResponse(usuario);
    }
}
