package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PharmaBackckend.entity.Usuario;

import java.util.Optional;
import java.util.Set;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("""
            select distinct u
            from Usuario u
            left join fetch u.roles
            left join fetch u.empleado
            where lower(u.username) = lower(:username)
            """)
    Optional<Usuario> findByUsernameForAuthentication(@Param("username") String username);

    Optional<Usuario> findByUsernameIgnoreCase(String username);
    Optional<Usuario> findByEmpleadoId(Long empleadoId);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmpleadoId(Long empleadoId);

    @Query("select u.empleado.id from Usuario u")
    Set<Long> findEmpleadoIdsConUsuario();
}
