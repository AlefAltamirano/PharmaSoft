package pe.edu.upeu.PharmaBackckend.security;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.PharmaBackckend.entity.Rol;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermisosPorRolTest {
    @Test
    void asignaTodosLosPermisosAlAdmin() {
        Rol admin = new Rol(1L, "ADMIN", "Administrador");

        var authorities = PermisosPorRol.authorities(List.of(admin))
                .stream().map(Object::toString).toList();

        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("USUARIOS_ADMINISTRAR"));
        assertTrue(authorities.contains("REPORTES_VER"));
    }

    @Test
    void unePermisosDeMultiplesRolesSinDuplicados() {
        Rol vendedor = new Rol(1L, "VENDEDOR", "Vendedor");
        Rol almacenero = new Rol(2L, "ALMACENERO", "Almacenero");

        var authorities = PermisosPorRol.authorities(List.of(vendedor, almacenero))
                .stream().map(Object::toString).toList();

        assertTrue(authorities.contains("CATALOGO_LEER"));
        assertTrue(authorities.contains("CATALOGO_ESCRIBIR"));
        assertTrue(authorities.contains("VENTAS_REGISTRAR"));
        assertEquals(1, authorities.stream().filter("CATALOGO_LEER"::equals).count());
    }
}
