package pe.edu.upeu.PharmaBackckend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.auth.*;
import pe.edu.upeu.PharmaBackckend.entity.*;
import pe.edu.upeu.PharmaBackckend.exception.*;
import pe.edu.upeu.PharmaBackckend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackckend.repository.*;
import pe.edu.upeu.PharmaBackckend.service.service.UsuarioService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, EmpleadoRepository empleadoRepository,
                               RolRepository rolRepository, PasswordEncoder passwordEncoder, UsuarioMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        String username = request.getUsername().trim().toUpperCase(Locale.ROOT);
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ReglaNegocioException("Ya existe el usuario: " + username);
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La clave no puede superar 72 bytes UTF-8");
        }
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                .orElseThrow(() -> new RecursosNoEncontradosException("Empleado no encontrado"));
        if (usuarioRepository.existsByEmpleadoId(empleado.getId())) {
            throw new ReglaNegocioException("El empleado ya tiene un usuario asignado");
        }
        var roles = request.getRolIds().stream()
                .map(rolId -> rolRepository.findById(rolId)
                        .orElseThrow(() -> new RecursosNoEncontradosException("Rol no encontrado con id: " + rolId)))
                .collect(java.util.stream.Collectors.toSet());
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmpleado(empleado);
        usuario.setRoles(roles);
        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarEmpleados() {
        var ids = usuarioRepository.findEmpleadoIdsConUsuario();
        return (ids.isEmpty() ? empleadoRepository.findAll() : empleadoRepository.findByIdNotIn(ids))
                .stream().map(mapper::toEmpleadoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponseDTO> listarRoles() {
        return rolRepository.findAll().stream().map(mapper::toRolResponse).toList();
    }
}
