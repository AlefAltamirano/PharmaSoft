package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PharmaBackckend.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Revisa si existe un producto con el mismo nombre (para evitar duplicados)
    boolean existsByNombreIgnoreCase(String nombre);

    // Consulta derivada para buscar todos los productos pertenecientes a una categoría
    List<Producto> findByCategoriaId(Long categoriaId);
}
