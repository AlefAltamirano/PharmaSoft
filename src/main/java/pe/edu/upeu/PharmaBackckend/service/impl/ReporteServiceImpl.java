package pe.edu.upeu.PharmaBackckend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteVentaCategoriaDTO;
import pe.edu.upeu.PharmaBackckend.repository.VentaRepository;
import pe.edu.upeu.PharmaBackckend.service.service.ReporteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {
    private final VentaRepository ventaRepository;

    public ReporteServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentaCategoriaDTO> obtenerVentasPorCategoria(LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeFecha = desde == null ? null : desde.atStartOfDay();
        LocalDateTime hastaFecha = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        return ventaRepository.obtenerVentasPorCategoria(desdeFecha, hastaFecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeFecha = desde == null ? null : desde.atStartOfDay();
        LocalDateTime hastaFecha = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        return ventaRepository.obtenerProductosMasVendidos(desdeFecha, hastaFecha);
    }
}
