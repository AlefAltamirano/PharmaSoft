package pe.edu.upeu.PharmaBackckend.controller;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackckend.config.RequestIdFilter;
import pe.edu.upeu.PharmaBackckend.exception.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RequestIdErrorTest {
 @RestController static class Endpoint {
  @GetMapping("/prueba-error") public void fallar() { throw new ReglaNegocioException("Prueba de negocio"); }
 }
 @Test void errorContieneElMismoIdQueLaCabeceraYElFiltroLimpiaMdc() throws Exception {
  var mvc=MockMvcBuilders.standaloneSetup(new Endpoint()).setControllerAdvice(new GlobalExceptionHandler()).addFilters(new RequestIdFilter()).build();
  var response=mvc.perform(get("/prueba-error")).andExpect(status().isConflict()).andReturn().getResponse();
  String id=response.getHeader("X-Request-Id");
  assertNotNull(id);
  assertTrue(response.getContentAsString().contains("\"requestId\":\""+id+"\""));
  assertNull(org.slf4j.MDC.get("requestId"));
 }
}
