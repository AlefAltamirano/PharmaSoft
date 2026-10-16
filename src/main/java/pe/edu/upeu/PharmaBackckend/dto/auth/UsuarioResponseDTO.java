package pe.edu.upeu.PharmaBackckend.dto.auth;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private Boolean estado;
    private EmpleadoResponseDTO empleado;
    private List<RolResponseDTO> roles;
}
