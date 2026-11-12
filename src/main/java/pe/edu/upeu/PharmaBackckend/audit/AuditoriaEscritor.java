package pe.edu.upeu.PharmaBackckend.audit;
import pe.edu.upeu.PharmaBackckend.entity.AuditoriaEvento;
import pe.edu.upeu.PharmaBackckend.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class AuditoriaEscritor {
 private final AuditoriaRepository repository;
 public AuditoriaEscritor(AuditoriaRepository repository) {this.repository=repository;}
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public void guardar(EventoAuditoria e) {
  var a=new AuditoriaEvento();a.setFecha(java.time.LocalDateTime.now());
  a.setUsuario(limitar(e.usuario(),100));a.setAccion(limitar(e.accion(),60));a.setEntidad(limitar(e.entidad(),60));
  a.setEntidadId(e.entidadId());a.setDetalle(limitar(e.detalle(),500));a.setRequestId(limitar(e.requestId(),36));
  repository.saveAndFlush(a);
 }
 private String limitar(String s,int max) {return s==null?null:s.substring(0,Math.min(s.length(),max));}
}
