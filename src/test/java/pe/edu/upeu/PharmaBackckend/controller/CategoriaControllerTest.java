package pe.edu.upeu.PharmaBackckend.controller;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.PharmaBackckend.service.service.CategoriaService;
import pe.edu.upeu.PharmaBackckend.dto.CategoriaRequestDTO;
import static org.mockito.Mockito.*;
class CategoriaControllerTest {
 @Test void actualizarNoCreaOtraCategoria() {
  var servicio=mock(CategoriaService.class);
  var request=new CategoriaRequestDTO("Nombre nuevo","Descripción",true);
  new CategoriaController(servicio).update(12L,request);
  verify(servicio).update(12L,request);
  verify(servicio,never()).create(any());
 }
}
