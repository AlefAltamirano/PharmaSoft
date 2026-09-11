package pe.edu.upeu.PharmaBackckend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.VentaPorCategoriaDTO;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.VentaRepository;
import pe.edu.upeu.PharmaBackckend.service.service.ReporteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteServiceImpl.class);

    private final VentaRepository ventaRepository;

    public ReporteServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaPorCategoriaDTO> reporteVentasPorCategoria(LocalDate desde, LocalDate hasta) {
        long inicio = System.currentTimeMillis();

        log.info("Inicio reporte ventas por categoria | desde={} | hasta={}", desde, hasta);

        validarRangoFechas(desde, hasta);

        LocalDateTime desdeHora = (desde == null) ? null : desde.atStartOfDay();
        LocalDateTime hastaHora = (hasta == null) ? null : hasta.atTime(LocalTime.MAX);

        List<VentaPorCategoriaDTO> resultado = ventaRepository.reporteVentasPorCategoria(desdeHora, hastaHora);

        log.info("Fin reporte ventas por categoria | desde={} | hasta={} | filas={} | duracionMs={}",
                desde, hasta, resultado.size(), System.currentTimeMillis() - inicio);

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoMasVendidoDTO> reporteProductosMasVendidos(LocalDate desde, LocalDate hasta) {
        long inicio = System.currentTimeMillis();

        log.info("Inicio reporte productos mas vendidos | desde={} | hasta={}", desde, hasta);

        validarRangoFechas(desde, hasta);

        LocalDateTime desdeHora = (desde == null) ? null : desde.atStartOfDay();
        LocalDateTime hastaHora = (hasta == null) ? null : hasta.atTime(LocalTime.MAX);

        List<ProductoMasVendidoDTO> resultado = ventaRepository.reporteProductosMasVendidos(desdeHora, hastaHora);

        log.info("Fin reporte productos mas vendidos | desde={} | hasta={} | filas={} | duracionMs={}",
                desde, hasta, resultado.size(), System.currentTimeMillis() - inicio);

        return resultado;
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("El rango de fechas es invalido: 'desde' (" + desde + ") es posterior a 'hasta' (" + hasta + ")");
        }
    }
}