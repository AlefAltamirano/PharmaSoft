package pe.edu.upeu.PharmaBackckend.util;
import java.util.Set;
import org.springframework.data.domain.*;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
public final class PaginacionUtil {
 private PaginacionUtil() {}
 public static Pageable construir(int pagina,int tamanio,String campo,String direccion,Set<String> campos) {
  if(pagina<0 || tamanio<1 || tamanio>100 || !campos.contains(campo) || !("asc".equals(direccion)||"desc".equals(direccion)))
   throw new ReglaNegocioException("Página, tamaño (1 a 100) u orden inválido");
  return PageRequest.of(pagina,tamanio,Sort.by(Sort.Direction.fromString(direccion),campo).and(Sort.by("id")));
 }
}
