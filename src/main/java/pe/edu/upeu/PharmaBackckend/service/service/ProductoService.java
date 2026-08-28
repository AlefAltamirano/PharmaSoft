package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackckend.service.generic.CrutService;

import java.util.List;

public interface ProductoService extends CrutService<ProductoRequestDTO, ProductoResponseDTO, Long> {
    List<ProductoResponseDTO> findByCategoriaId(Long categoriaId);
}
