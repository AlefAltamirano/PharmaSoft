package pe.edu.upeu.PharmaBackckend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private long expiraEnSegundos;
    private String username;
    private String nombreCompleto;
    private List<String> roles;
    private List<String> permisos;
}
