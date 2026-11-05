package pe.edu.upeu.PharmaBackckend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPerfilResponseDTO {
    private String username;
    private String nombreCompleto;
    private List<String> roles;
    private List<String> permisos;
}
