package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaResponseDTO;
import pe.edu.upeu.PharmaBackckend.enums.EstadoVenta;

import java.time.LocalDate;
import java.util.List;

public interface VentaService {

    VentaResponseDTO registrar(VentaRequestDTO request);

    VentaResponseDTO buscar(Long id);

    List<VentaResponseDTO> listar();

    List<VentaResponseDTO> buscar(
            Long clienteId,
            EstadoVenta estado,
            LocalDate desde,
            LocalDate hasta,
            String ordenarPor,
            String direccion
    );
}