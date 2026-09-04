package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaResponseDTO;

import java.util.List;

public interface VentaService {
    VentaResponseDTO registrar(VentaRequestDTO request);
    VentaResponseDTO buscar(Long id);
    List<VentaResponseDTO> listar();
}
