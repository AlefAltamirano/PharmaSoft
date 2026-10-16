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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    @Override
    @Transactional
    public UsuarioResponseDTO asignarRoles(Long id, AsignarRolesRequestDTO request, String usernameAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));

        if (request == null || request.getRolIds() == null || request.getRolIds().isEmpty()) {
            throw new ReglaNegocioException("Debe asignar al menos un rol");
        }

        Set<Rol> rolesEncontrados = new HashSet<>();
        for (Long rolId : request.getRolIds()) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con id: " + rolId));
            rolesEncontrados.add(rol);
        }

        boolean tieneRolAdmin = rolesEncontrados.stream()
                .anyMatch(r -> Long.valueOf(1L).equals(r.getId()) || "ADMIN".equalsIgnoreCase(r.getNombre()));

        if (usernameAdmin != null && usuario.getUsername() != null
                && usuario.getUsername().equalsIgnoreCase(usernameAdmin) && !tieneRolAdmin) {
            throw new ReglaNegocioException("Un administrador no puede quitarse a sí mismo el rol ADMIN");
        }

        usuario.setRoles(new HashSet<>(rolesEncontrados));
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapper.toResponse(usuarioActualizado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Long id, EstadoUsuarioRequestDTO request, String usernameAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));

        if (request == null || request.getEstado() == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }

        if (usernameAdmin != null && usuario.getUsername() != null
                && usuario.getUsername().equalsIgnoreCase(usernameAdmin)
                && Boolean.FALSE.equals(request.getEstado())) {
            throw new ReglaNegocioException("Un administrador no puede desactivarse a sí mismo");
        }

        usuario.setEstado(request.getEstado());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapper.toResponse(usuarioActualizado);
    }
}
