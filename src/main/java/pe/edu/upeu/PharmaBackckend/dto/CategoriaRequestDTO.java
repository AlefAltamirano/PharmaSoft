package pe.edu.upeu.PharmaBackckend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoriaRequestDTO {
    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(
            min = 3,
            max = 50,
            message = "El nombre deve tener entre 3 y 50 caracteres"
    )
    private String nombre;
    @Size(
            max = 200,
            message = "La descripcion no deve superar los 200 caracteres"
    )
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
