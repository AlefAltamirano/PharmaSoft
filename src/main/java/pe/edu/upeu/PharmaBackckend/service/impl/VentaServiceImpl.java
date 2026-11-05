package pe.edu.upeu.PharmaBackckend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackckend.dto.DetalleVentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.DetalleVentaResponseDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaResponseDTO;
import pe.edu.upeu.PharmaBackckend.entity.Cliente;
import pe.edu.upeu.PharmaBackckend.entity.DetalleVenta;
import pe.edu.upeu.PharmaBackckend.entity.Producto;
import pe.edu.upeu.PharmaBackckend.entity.Venta;
import pe.edu.upeu.PharmaBackckend.enums.EstadoVenta;
import pe.edu.upeu.PharmaBackckend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.ClienteRepository;
import pe.edu.upeu.PharmaBackckend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackckend.repository.VentaRepository;
import pe.edu.upeu.PharmaBackckend.service.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
@Service
public class VentaServiceImpl implements VentaService {
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {

        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO registrar(VentaRequestDTO request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() ->new RecursosNoEncontradosException("Cliente no encontrado con id: "+ request.getClienteId()));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new ReglaNegocioException("No se puede registrar una venta para un cliente inactivo");
        }
        Venta venta = new Venta();

        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO item: request.getDetalles().stream()
                .sorted(Comparator.comparing(DetalleVentaRequestDTO::getProductoId)).toList()) {
            Producto producto = productoRepository.buscarParaActualizar(item.getProductoId()).orElseThrow(() ->
                    new RecursosNoEncontradosException("Producto no encontrado con id: "+ item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new ReglaNegocioException("El producto "+ producto.getNombre()+ " se encuentra inactivo");
            }

            if (producto.getStock()< item.getCantidad()) {

                throw new ReglaNegocioException("Stock insuficiente para "+ producto.getNombre()+ ". Disponible: "+ producto.getStock()
                        + ", solicitado: "+ item.getCantidad());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();

            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            total = total.add(subtotal);

            producto.setStock(producto.getStock()- item.getCantidad());
        }

        venta.setTotal(total);

        Venta guardada =ventaRepository.save(venta);

        return convertirResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {

        Venta venta = ventaRepository.findById(id).orElseThrow(() ->
                new RecursosNoEncontradosException("Venta no encontrada con id: "+ id));
        return convertirResponse(venta);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "productos", allEntries = true)
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepository.buscarParaActualizar(id)
                .orElseThrow(() -> new RecursosNoEncontradosException("Venta no encontrada con id: " + id));
        if (venta.getEstado() != EstadoVenta.REGISTRADA) {
            throw new ReglaNegocioException("La venta ya fue anulada");
        }
        venta.getDetalles().stream()
                .sorted(Comparator.comparing(detalle -> detalle.getProducto().getId()))
                .forEach(detalle -> {
                    Producto producto = productoRepository.buscarParaActualizar(detalle.getProducto().getId())
                            .orElseThrow(() -> new RecursosNoEncontradosException("Producto de la venta no encontrado"));
                    producto.setStock(Math.addExact(producto.getStock(), detalle.getCantidad()));
                });
        venta.setEstado(EstadoVenta.ANULADA);
        return convertirResponse(ventaRepository.save(venta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listar() {
        return ventaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    private VentaResponseDTO convertirResponse(Venta venta) {

        List<DetalleVentaResponseDTO> detalles =
                venta.getDetalles()
                        .stream()
                        .map(detalle ->
                                new DetalleVentaResponseDTO(
                                        detalle.getProducto().getId(),
                                        detalle.getProducto().getNombre(),
                                        detalle.getCantidad(),
                                        detalle.getPrecio(),
                                        detalle.getSubtotal()
                                )
                        ).toList();

        String clienteNombre = venta.getCliente().getNombres()+ " "+ venta.getCliente().getApellidos();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getCliente().getId(),
                clienteNombre,
                venta.getEstado().name(),
                venta.getTotal(),
                detalles
        );
    }
}
