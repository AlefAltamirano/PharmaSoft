package pe.edu.upeu.PharmaBackckend.config;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
  String id = UUID.randomUUID().toString();
  MDC.put("requestId",id); res.setHeader("X-Request-Id",id);
  long inicio=System.nanoTime();
  try { chain.doFilter(req,res); } finally {
   if (!req.getRequestURI().startsWith("/actuator")) LoggerFactory.getLogger(RequestIdFilter.class).info("{} {} -> {} duracionMs={}",req.getMethod(),req.getRequestURI(),res.getStatus(),(System.nanoTime()-inicio)/1000000);
   MDC.remove("requestId");
  }
 }
}
