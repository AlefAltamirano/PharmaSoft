package pe.edu.upeu.PharmaBackckend.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoUsuarioRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    public EstadoUsuarioRequestDTO() {
    }

    public EstadoUsuarioRequestDTO(Boolean estado) {
        this.estado = estado;
    }
}

