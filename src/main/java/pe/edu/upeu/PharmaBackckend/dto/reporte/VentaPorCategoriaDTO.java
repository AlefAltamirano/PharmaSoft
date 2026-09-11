package pe.edu.upeu.PharmaBackckend.dto.reporte;

import java.math.BigDecimal;

public record VentaPorCategoriaDTO(
        Long categoriaId,
        String categoriaNombre,
        Long unidadesVendidas,
        BigDecimal montoTotal
) {}