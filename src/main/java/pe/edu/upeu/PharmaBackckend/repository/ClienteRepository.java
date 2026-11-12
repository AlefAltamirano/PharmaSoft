package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PharmaBackckend.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @org.springframework.data.jpa.repository.Query("select c from Cliente c where c.estado=true and (lower(concat(concat(c.nombres,' '),c.apellidos)) like lower(concat('%',:texto,'%')) or c.dni like concat('%',:texto,'%'))")
    org.springframework.data.domain.Page<Cliente> buscarActivos(@org.springframework.data.repository.query.Param("texto") String texto,org.springframework.data.domain.Pageable pageable);
    boolean existsByDni(String dni);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}