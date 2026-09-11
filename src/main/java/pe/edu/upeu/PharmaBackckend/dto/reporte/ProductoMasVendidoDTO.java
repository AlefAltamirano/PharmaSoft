package pe.edu.upeu.PharmaBackckend.dto.reporte;

import java.math.BigDecimal;

public record ProductoMasVendidoDTO(
        Long productoId,
        String productoNombre,
        String categoriaNombre,
        Long unidadesVendidas,
        BigDecimal montoTotal
) {}