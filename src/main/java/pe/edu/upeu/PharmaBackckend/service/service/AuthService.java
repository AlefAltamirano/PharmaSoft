package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.auth.*;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    UsuarioResponseDTO usuarioActual(String username);
}
