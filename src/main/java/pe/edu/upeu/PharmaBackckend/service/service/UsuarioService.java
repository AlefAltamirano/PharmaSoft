package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.auth.*;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crear(UsuarioRequestDTO request);
    List<UsuarioResponseDTO> listar();
    List<EmpleadoResponseDTO> listarEmpleados();
    List<RolResponseDTO> listarRoles();
}
