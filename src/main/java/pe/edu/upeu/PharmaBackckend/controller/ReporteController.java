package pe.edu.upeu.PharmaBackckend.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteVentaCategoriaDTO;
import pe.edu.upeu.PharmaBackckend.service.service.ReporteService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasAuthority('REPORTES_VER')")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ventas-por-categoria")
    public List<ReporteVentaCategoriaDTO> ventasPorCategoria(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.obtenerVentasPorCategoria(desde, hasta);
    }

    @GetMapping("/productos-mas-vendidos")
    public List<ReporteProductoMasVendidoDTO> productosMasVendidos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.obtenerProductosMasVendidos(desde, hasta);
    }
}
