package pe.edu.upeu.PharmaBackckend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiracion-minutos:60}") long expirationMinutes) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe contener al menos 256 bits");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
        expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(String username, java.util.Collection<? extends GrantedAuthority> authorities) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration.toMillis());
        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities.stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token))
                && extractAllClaims(token).getExpiration().after(new Date());
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
