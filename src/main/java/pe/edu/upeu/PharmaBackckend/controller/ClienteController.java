package pe.edu.upeu.PharmaBackckend.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackckend.dto.*;
import pe.edu.upeu.PharmaBackckend.service.service.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(java.util.stream.StreamSupport
                .stream(clienteService.readAll().spliterator(), false).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.read(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
