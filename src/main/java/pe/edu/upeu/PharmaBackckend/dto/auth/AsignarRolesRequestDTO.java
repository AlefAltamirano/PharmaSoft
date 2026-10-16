package pe.edu.upeu.PharmaBackckend.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AsignarRolesRequestDTO {

    @NotEmpty(message = "Debe asignar al menos un rol")
    private Set<@NotNull Long> rolIds;

    public AsignarRolesRequestDTO() {
    }

    public AsignarRolesRequestDTO(Set<@NotNull Long> rolIds) {
        this.rolIds = rolIds;
    }
}

