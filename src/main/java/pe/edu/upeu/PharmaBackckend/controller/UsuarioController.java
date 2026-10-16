package pe.edu.upeu.PharmaBackckend.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackckend.dto.auth.*;
import pe.edu.upeu.PharmaBackckend.service.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/empleados")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarEmpleados() {
        return ResponseEntity.ok(usuarioService.listarEmpleados());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponseDTO>> listarRoles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }
}
