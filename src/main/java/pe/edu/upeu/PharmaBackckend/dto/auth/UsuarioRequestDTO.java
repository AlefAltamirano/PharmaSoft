package pe.edu.upeu.PharmaBackckend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;

    @NotEmpty(message = "Debe asignar al menos un rol")
    private Set<Long> rolIds;
}
