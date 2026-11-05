package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import pe.edu.upeu.PharmaBackckend.entity.Venta;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteProductoMasVendidoDTO;
import pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteVentaCategoriaDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venta v where v.id = :id")
    Optional<Venta> buscarParaActualizar(Long id);
    @Query("""
            select new pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteVentaCategoriaDTO(
                c.id,
                c.nombre,
                sum(d.cantidad),
                sum(d.subtotal)
            )
            from DetalleVenta d
            join d.venta v
            join d.producto p
            join p.categoria c
            where v.estado = pe.edu.upeu.PharmaBackckend.enums.EstadoVenta.REGISTRADA
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha < :hasta)
            group by c.id, c.nombre
            order by sum(d.subtotal) desc
            """)
    List<ReporteVentaCategoriaDTO> obtenerVentasPorCategoria(
            LocalDateTime desde, LocalDateTime hasta);

    @Query("""
            select new pe.edu.upeu.PharmaBackckend.dto.reporte.ReporteProductoMasVendidoDTO(
                p.id,
                p.nombre,
                p.categoria.nombre,
                sum(d.cantidad),
                sum(d.subtotal)
            )
            from DetalleVenta d
            join d.venta v
            join d.producto p
            where v.estado = pe.edu.upeu.PharmaBackckend.enums.EstadoVenta.REGISTRADA
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha < :hasta)
            group by p.id, p.nombre, p.categoria.nombre
            order by sum(d.cantidad) desc, sum(d.subtotal) desc
            """)
    List<ReporteProductoMasVendidoDTO> obtenerProductosMasVendidos(
            LocalDateTime desde, LocalDateTime hasta);
}
