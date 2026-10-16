package pe.edu.upeu.PharmaBackckend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String JWT_ERROR_ATTRIBUTE = "jwt.error";
    private final JwtService jwtService;
    private final UsuarioDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                String username = jwtService.extractUsername(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, user.getUsername()) && user.isEnabled()) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException | IllegalArgumentException | org.springframework.security.core.AuthenticationException ex) {
                request.setAttribute(JWT_ERROR_ATTRIBUTE, ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
