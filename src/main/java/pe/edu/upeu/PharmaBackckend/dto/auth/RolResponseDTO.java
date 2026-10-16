package pe.edu.upeu.PharmaBackckend.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
}
