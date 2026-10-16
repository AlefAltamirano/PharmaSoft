package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteVentaCategoriaDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    List<ReporteVentaCategoriaDTO> obtenerVentasPorCategoria(LocalDate desde, LocalDate hasta);

    List<ReporteProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDate desde, LocalDate hasta);
}
