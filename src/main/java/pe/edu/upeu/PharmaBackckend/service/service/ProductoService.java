package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackckend.service.generic.CrutService;

import java.util.List;

public interface ProductoService extends CrutService<ProductoRequestDTO, ProductoResponseDTO, Long> {
    pe.edu.upeu.PharmaBackckend.dto.PaginaResponseDTO<ProductoResponseDTO> listar(int pagina, int tamanio, String ordenarPor, String direccion, Long categoriaId);
    List<ProductoResponseDTO> buscarDisponibles(String texto);
    List<ProductoResponseDTO> findByCategoriaId(Long categoriaId);
}
