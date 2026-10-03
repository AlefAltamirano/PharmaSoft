package pe.edu.upeu.PharmaBackckend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.PharmaBackckend.dto.ClienteRequestDTO;
import pe.edu.upeu.PharmaBackckend.dto.ClienteResponseDTO;
import pe.edu.upeu.PharmaBackckend.service.service.ClienteService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<Iterable<ClienteResponseDTO>> findAll() {
        return ResponseEntity.ok(clienteService.readAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.read(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clienteService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity.ok(clienteService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
