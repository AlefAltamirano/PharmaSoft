package pe.edu.upeu.PharmaBackckend.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackckend.dto.auth.*;
import pe.edu.upeu.PharmaBackckend.entity.*;

@Component
public class UsuarioMapper {
    public UsuarioResponseDTO toResponse(Usuario usuario) {
        Empleado empleado = usuario.getEmpleado();
        EmpleadoResponseDTO empleadoDto = new EmpleadoResponseDTO(empleado.getId(), empleado.getDni(),
                empleado.getNombres(), empleado.getApellidos(), empleado.getCargo(), empleado.getEmail(),
                empleado.getTelefono(), empleado.getEstado(), empleado.getFechaCreacion(),
                empleado.getFechaModificacion());
        return new UsuarioResponseDTO(usuario.getId(), usuario.getUsername(), usuario.getEstado(),
                empleadoDto, usuario.getRoles().stream()
                        .map(rol -> new RolResponseDTO(rol.getId(), rol.getNombre(), rol.getDescripcion()))
                        .toList());
    }

    public EmpleadoResponseDTO toEmpleadoResponse(Empleado empleado) {
        return new EmpleadoResponseDTO(empleado.getId(), empleado.getDni(), empleado.getNombres(),
                empleado.getApellidos(), empleado.getCargo(), empleado.getEmail(), empleado.getTelefono(),
                empleado.getEstado(), empleado.getFechaCreacion(), empleado.getFechaModificacion());
    }

    public RolResponseDTO toRolResponse(Rol rol) {
        return new RolResponseDTO(rol.getId(), rol.getNombre(), rol.getDescripcion());
    }
}
