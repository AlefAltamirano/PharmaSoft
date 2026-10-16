package pe.edu.upeu.PharmaBackckend.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.PharmaBackckend.security.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/health", "/swagger-ui/**",
                                "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**")
                            .hasAuthority("CATALOGO_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**", "/api/v1/categorias/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**", "/api/v1/categorias/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**", "/api/v1/categorias/**")
                            .hasAuthority("CATALOGO_ESCRIBIR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAuthority("CLIENTES_LEER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasAuthority("CLIENTES_ESCRIBIR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas/**").hasAuthority("VENTAS_REGISTRAR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ventas/**").hasAuthority("VENTAS_LEER")
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/empleados/**", "/api/v1/roles/**")
                            .hasAuthority("USUARIOS_ADMINISTRAR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
