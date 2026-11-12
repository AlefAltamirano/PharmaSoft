package pe.edu.upeu.PharmaBackckend.controller;
import pe.edu.upeu.PharmaBackckend.repository.AuditoriaRepository;
import pe.edu.upeu.PharmaBackckend.entity.AuditoriaEvento;
import pe.edu.upeu.PharmaBackckend.dto.PaginaResponseDTO;
import pe.edu.upeu.PharmaBackckend.util.PaginacionUtil;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
@RestController @RequestMapping("/api/v1/auditoria")
@PreAuthorize("hasAuthority('USUARIOS_ADMINISTRAR')")
public class AuditoriaController {
 private final AuditoriaRepository repository;
 public AuditoriaController(AuditoriaRepository repository) {this.repository=repository;}
 @GetMapping
 public PaginaResponseDTO<AuditoriaEvento> buscar(@RequestParam(required=false) String usuario,@RequestParam(required=false) String accion,
 @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,
 @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta,
 @RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {
  if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw new ReglaNegocioException("Rango de fechas inválido");
  var p=repository.buscar(limpiar(usuario),limpiar(accion),desde==null?null:desde.atStartOfDay(),hasta==null?null:hasta.plusDays(1).atStartOfDay(),PaginacionUtil.construir(pagina,tamanio,"fecha","desc",java.util.Set.of("fecha")));
  return new PaginaResponseDTO<>(p.getContent(),pagina,tamanio,p.getTotalElements(),p.getTotalPages(),p.isLast());
 }
 private String limpiar(String v) {return v==null||v.isBlank()?null:v.trim();}
}
