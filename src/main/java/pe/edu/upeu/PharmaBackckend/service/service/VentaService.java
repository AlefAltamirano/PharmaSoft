package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.VentaResponseDTO;

import java.util.List;

public interface VentaService {
    VentaResponseDTO registrar(VentaRequestDTO request);
    VentaResponseDTO buscar(Long id);
    VentaResponseDTO anular(Long id);
    List<VentaResponseDTO> listar();
    pe.edu.upeu.PharmaBackckend.dto.PaginaResponseDTO<VentaResponseDTO> buscar(Long clienteId,String estado,java.time.LocalDate desde,java.time.LocalDate hasta,int pagina,int tamanio);

}
