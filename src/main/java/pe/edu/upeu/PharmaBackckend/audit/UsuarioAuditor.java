package pe.edu.upeu.PharmaBackckend.audit;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;
@Component("usuarioAuditor")
public class UsuarioAuditor implements AuditorAware<String> {
 public Optional<String> getCurrentAuditor() {
  var a=SecurityContextHolder.getContext().getAuthentication();
  return Optional.of(a!=null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)?a.getName():"sistema");
 }
}
