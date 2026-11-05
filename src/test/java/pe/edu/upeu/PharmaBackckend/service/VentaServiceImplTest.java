package pe.edu.upeu.PharmaBackckend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.PharmaBackckend.dto.DetalleVentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.entity.*;
import pe.edu.upeu.PharmaBackckend.enums.EstadoVenta;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackckend.repository.*;
import pe.edu.upeu.PharmaBackckend.service.impl.VentaServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {
    @Mock VentaRepository ventas;
    @Mock ClienteRepository clientes;
    @Mock ProductoRepository productos;
    @InjectMocks VentaServiceImpl servicio;
    private Producto producto;
    private Venta venta;

    @BeforeEach
    void preparar() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto");
        producto.setPrecio(BigDecimal.TEN);
        producto.setStock(8);
        producto.setEstado(true);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombres("Cliente");
        cliente.setApellidos("Prueba");
        cliente.setEstado(true);
        venta = new Venta();
        venta.setCliente(cliente);
        venta.setEstado(EstadoVenta.REGISTRADA);
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        venta.agregarDetalle(detalle);
    }

    @Test
    void anularDevuelveStockUnaSolaVez() {
        when(ventas.buscarParaActualizar(1L)).thenReturn(Optional.of(venta));
        when(productos.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));
        when(ventas.save(venta)).thenReturn(venta);

        assertEquals("ANULADA", servicio.anular(1L).getEstado());
        assertEquals(10, producto.getStock());
        assertThrows(ReglaNegocioException.class, () -> servicio.anular(1L));
        assertEquals(10, producto.getStock());
        verify(productos, times(1)).buscarParaActualizar(1L);
    }

    @Test
    void registrarValidaStockYCalculaTotal() {
        when(clientes.findById(1L)).thenReturn(Optional.of(venta.getCliente()));
        when(productos.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));
        when(ventas.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = servicio.registrar(new VentaRequestDTO(1L,
                List.of(new DetalleVentaRequestDTO(1L, 3))));

        assertEquals(5, producto.getStock());
        assertEquals(new BigDecimal("30"), respuesta.getTotal());
    }

    @Test
    void stockInsuficienteNoGuardaVenta() {
        when(clientes.findById(1L)).thenReturn(Optional.of(venta.getCliente()));
        when(productos.buscarParaActualizar(1L)).thenReturn(Optional.of(producto));

        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(
                new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(1L, 9)))));
        verify(ventas, never()).save(any());
        assertEquals(8, producto.getStock());
    }
}
