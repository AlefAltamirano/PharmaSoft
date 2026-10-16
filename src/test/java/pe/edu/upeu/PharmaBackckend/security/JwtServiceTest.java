package pe.edu.upeu.PharmaBackckend.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes());

    @Test
    void generaTokenYLeeClaims() {
        JwtService service = new JwtService(SECRET, 60);
        String token = service.generateToken("ADMIN",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertEquals("ADMIN", service.extractUsername(token));
        assertEquals("ADMIN", service.extractAllClaims(token).getSubject());
        assertNotNull(service.extractAllClaims(token).get("authorities"));
        assertTrue(service.isTokenValid(token, "ADMIN"));
    }

    @Test
    void rechazaFirmaAlterada() {
        JwtService service = new JwtService(SECRET, 60);
        String token = service.generateToken("ADMIN", List.of());
        String[] partes = token.split("\\.");
        String firmaAlterada = (partes[2].charAt(0) == 'a' ? "b" : "a")
                + partes[2].substring(1);
        String alterado = partes[0] + "." + partes[1] + "." + firmaAlterada;

        assertThrows(JwtException.class, () -> service.extractAllClaims(alterado));
    }

    @Test
    void rechazaClaveAjena() {
        JwtService service = new JwtService(SECRET, 60);
        JwtService otra = new JwtService(Base64.getEncoder().encodeToString(
                "abcdefghijklmnopqrstuvwxyz123456".getBytes()), 60);
        String token = service.generateToken("ADMIN", List.of());

        assertThrows(JwtException.class, () -> otra.extractAllClaims(token));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService service = new JwtService(SECRET, 0);
        String token = service.generateToken("ADMIN", List.of());

        assertThrows(JwtException.class, () -> service.extractAllClaims(token));
    }
}
