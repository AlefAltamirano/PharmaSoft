package pe.edu.upeu.PharmaBackckend.service.service;

import pe.edu.upeu.PharmaBackckend.dto.ClienteRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.ClienteResponseDTO;
import pe.edu.upeu.PharmaBackckend.service.generic.CrutService;

public interface ClienteService extends CrutService<ClienteRequestDTO, ClienteResponseDTO, Long> {
    java.util.List<ClienteResponseDTO> buscarActivos(String texto);
}
