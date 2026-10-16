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
public class ReporteProductoMasVendidoDTO {
    private Long productoId;
    private String productoNombre;
    private Long cantidadVendida;
    private BigDecimal montoTotal;
}
