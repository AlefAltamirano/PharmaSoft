package pe.edu.upeu.PharmaBackckend.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
@Configuration
@EnableJpaAuditing(auditorAwareRef="usuarioAuditor")
public class AuditoriaConfig {}
