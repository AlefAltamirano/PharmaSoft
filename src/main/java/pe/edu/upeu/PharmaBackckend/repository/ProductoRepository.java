package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import pe.edu.upeu.PharmaBackckend.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizar(Long id);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    org.springframework.data.domain.Page<Producto> findByCategoriaId(Long categoriaId, org.springframework.data.domain.Pageable pageable);
    // Revisa si existe un producto con el mismo nombre (para evitar duplicados)
    boolean existsByNombreIgnoreCase(String nombre);

    // Consulta derivada para buscar todos los productos pertenecientes a una categoría
    List<Producto> findByCategoriaId(Long categoriaId);
}
