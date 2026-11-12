package pe.edu.upeu.PharmaBackckend.audit;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.*;
import org.slf4j.*;
@Component
public class AuditoriaEventos {
 private static final Logger LOG=LoggerFactory.getLogger(AuditoriaEventos.class);
 private final ApplicationEventPublisher publisher;
 private final UsuarioAuditor auditor;
 private final AuditoriaEscritor escritor;
 public AuditoriaEventos(ApplicationEventPublisher publisher,UsuarioAuditor auditor,AuditoriaEscritor escritor) {this.publisher=publisher;this.auditor=auditor;this.escritor=escritor;}
 public void publicar(String accion,String entidad,Long id,String detalle) {
  publisher.publishEvent(new EventoAuditoria(auditor.getCurrentAuditor().orElse("sistema"),accion,entidad,id,detalle,MDC.get("requestId")));
 }
 public void login(String usuario,boolean correcto) {
  persistir(new EventoAuditoria(usuario,correcto?"LOGIN_EXITOSO":"LOGIN_FALLIDO","Usuario",null,"Autenticación "+(correcto?"aceptada":"rechazada"),MDC.get("requestId")));
 }
 @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
 public void persistir(EventoAuditoria evento) {
  try {escritor.guardar(evento);} catch(RuntimeException ex) {LOG.error("No se pudo persistir auditoría accion={} requestId={}",evento.accion(),evento.requestId(),ex);}
 }
}
