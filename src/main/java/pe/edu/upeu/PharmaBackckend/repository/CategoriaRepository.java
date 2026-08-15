package pe.edu.upeu.PharmaBackckend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PharmaBackckend.entity.Categoria;

public interface CategoriaRepository extends JpaRepository <Categoria, Long> {
}
