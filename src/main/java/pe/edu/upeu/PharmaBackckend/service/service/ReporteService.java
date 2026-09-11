package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.reporte.ProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.VentaPorCategoriaDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    List<VentaPorCategoriaDTO> reporteVentasPorCategoria(LocalDate desde, LocalDate hasta);

    List<ProductoMasVendidoDTO> reporteProductosMasVendidos(LocalDate desde, LocalDate hasta);
}