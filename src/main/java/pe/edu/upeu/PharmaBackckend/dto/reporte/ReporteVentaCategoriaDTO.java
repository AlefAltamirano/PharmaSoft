package pe.edu.upeu.PharmaBackckend.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaCategoriaDTO {
    private Long categoriaId;
    private String categoriaNombre;
    private Long cantidadVendida;
    private BigDecimal montoTotal;
}
