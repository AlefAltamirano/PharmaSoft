package pe.edu.upeu.PharmaBackckend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.upeu.PharmaBackckend.entity.Rol;
import pe.edu.upeu.PharmaBackckend.enums.Permiso;

import java.util.*;

public final class PermisosPorRol {
    private static final Map<String, Set<Permiso>> PERMISOS = Map.of(
            "ADMIN", EnumSet.allOf(Permiso.class),
            "VENDEDOR", EnumSet.of(Permiso.CATALOGO_LEER, Permiso.CLIENTES_LEER,
                    Permiso.CLIENTES_ESCRIBIR, Permiso.VENTAS_LEER, Permiso.VENTAS_REGISTRAR),
            "ALMACENERO", EnumSet.of(Permiso.CATALOGO_LEER, Permiso.CATALOGO_ESCRIBIR)
    );

    private PermisosPorRol() {
    }

    public static List<GrantedAuthority> authorities(Collection<Rol> roles) {
        Set<String> authorities = new LinkedHashSet<>();
        for (Rol rol : roles) {
            String nombre = rol.getNombre().toUpperCase(Locale.ROOT);
            authorities.add("ROLE_" + nombre);
            PERMISOS.getOrDefault(nombre, Set.of()).stream()
                    .map(Enum::name)
                    .forEach(authorities::add);
        }
        return authorities.stream().map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority).toList();
    }
}
