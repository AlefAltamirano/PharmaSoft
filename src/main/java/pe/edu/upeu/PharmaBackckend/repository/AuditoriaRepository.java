package pe.edu.upeu.PharmaBackckend.repository;
import pe.edu.upeu.PharmaBackckend.entity.AuditoriaEvento;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface AuditoriaRepository extends JpaRepository<AuditoriaEvento,Long> {
 @Query("""
 select a from AuditoriaEvento a where (:usuario is null or lower(a.usuario) like lower(concat('%',:usuario,'%')))
 and (:accion is null or a.accion=:accion) and (:desde is null or a.fecha>=:desde) and (:hasta is null or a.fecha<:hasta)
 """)
 Page<AuditoriaEvento> buscar(String usuario,String accion,LocalDateTime desde,LocalDateTime hasta,Pageable pagina);
}
