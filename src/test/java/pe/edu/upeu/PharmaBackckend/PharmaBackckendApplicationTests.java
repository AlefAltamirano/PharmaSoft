package pe.edu.upeu.PharmaBackckend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pe.edu.upeu.PharmaBackckend.config.OfficialAccountsInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.PharmaBackckend.service.service.VentaService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
class PharmaBackckendApplicationTests {
	// La comprobación de arranque no crea cuentas ni modifica el esquema real.
	@MockitoBean
	OfficialAccountsInitializer officialAccountsInitializer;
	@MockitoBean
	VentaService ventaService;
	@Autowired
	MockMvc mvc;

	@Test
	void contextLoads() {
	}

	@Test
	void anularSinSesionDevuelve401() throws Exception {
		mvc.perform(put("/api/v1/ventas/1/anular")).andExpect(status().isUnauthorized());
		verifyNoInteractions(ventaService);
	}

	@Test
	@WithMockUser(authorities = "CATALOGO_LEER")
	void anularSinPermisoDevuelve403() throws Exception {
		mvc.perform(put("/api/v1/ventas/1/anular")).andExpect(status().isForbidden());
		verifyNoInteractions(ventaService);
	}

	@Test
	@WithMockUser(authorities = "VENTAS_REGISTRAR")
	void anularConPermisoLlegaAlServicio() throws Exception {
		mvc.perform(put("/api/v1/ventas/1/anular")).andExpect(status().isOk());
		verify(ventaService).anular(1L);
	}

}
