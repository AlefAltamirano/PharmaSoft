package pe.edu.upeu.PharmaBackckend.audit;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.MDC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuditoriaEventosTest {
 @AfterEach void limpiar(){SecurityContextHolder.clearContext();MDC.clear();}
 @Test void excluyeAnonimoYUsaSistema(){
  var auditor=new UsuarioAuditor();
  assertEquals("sistema",auditor.getCurrentAuditor().orElseThrow());
  SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key","anonymousUser",java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
  assertEquals("sistema",auditor.getCurrentAuditor().orElseThrow());
 }
 @Test void capturaUsuarioYRequestAntesDelCommit(){
  var publisher=mock(ApplicationEventPublisher.class);var escritor=mock(AuditoriaEscritor.class);
  var servicio=new AuditoriaEventos(publisher,new UsuarioAuditor(),escritor);
  SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated("vendedor",null,java.util.List.of()));
  MDC.put("requestId","solicitud-123");servicio.publicar("VENTA_REGISTRADA","Venta",12L,"Total=20");
  verify(publisher).publishEvent(new EventoAuditoria("vendedor","VENTA_REGISTRADA","Venta",12L,"Total=20","solicitud-123"));
  verifyNoInteractions(escritor);
 }
 @Test void falloBitacoraNoSePropagaALaVentaConfirmada(){
  var escritor=mock(AuditoriaEscritor.class);doThrow(new IllegalStateException("BD de auditoría no disponible")).when(escritor).guardar(any());
  var servicio=new AuditoriaEventos(mock(ApplicationEventPublisher.class),new UsuarioAuditor(),escritor);
  assertDoesNotThrow(()->servicio.persistir(new EventoAuditoria("vendedor","VENTA_REGISTRADA","Venta",12L,"Total=20","123")));
 }
}
