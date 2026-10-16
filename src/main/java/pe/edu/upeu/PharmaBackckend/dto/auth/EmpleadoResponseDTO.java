package pe.edu.upeu.PharmaBackckend.dto.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponseDTO {
    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String cargo;
    private String email;
    private String telefono;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
