package pe.edu.upeu.PharmaBackckend.service;
import org.junit.jupiter.api.Test;
import java.util.Set;
import pe.edu.upeu.PharmaBackckend.util.PaginacionUtil;
import pe.edu.upeu.PharmaBackckend.exception.ReglaNegocioException;
import static org.junit.jupiter.api.Assertions.*;
class PaginacionUtilTest {
 @Test void validaLimitesYListaBlanca() {
  for(int size:new int[]{0,-1,101,500}) assertThrows(ReglaNegocioException.class,()->PaginacionUtil.construir(0,size,"nombre","asc",Set.of("nombre")));
  assertThrows(ReglaNegocioException.class,()->PaginacionUtil.construir(0,20,"password","asc",Set.of("nombre")));
  assertThrows(ReglaNegocioException.class,()->PaginacionUtil.construir(-1,20,"nombre","asc",Set.of("nombre")));
 }
 @Test void aceptaMaximoYOrdenDeterminista() {
  var p=PaginacionUtil.construir(2,100,"nombre","desc",Set.of("nombre"));
  assertEquals(200,p.getOffset()); assertNotNull(p.getSort().getOrderFor("id"));
 }
}
