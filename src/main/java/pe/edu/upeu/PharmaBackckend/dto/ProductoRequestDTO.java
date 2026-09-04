package pe.edu.upeu.PharmaBackckend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoRequestDTO {
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(
            min = 3,
            max = 100,
            message = "El nombre debe tener entre 3 y 100 caracteres"
    )
    private String nombre;

    @Size(
            max = 200,
            message = "La descripcion no debe superar los 200 caracteres"
    )
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no debe ser negativo")
    private Integer stock;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    @NotNull(message = "El ID de la categoria es obligatorio")
    private Long categoriaId;
}
