package pe.edu.upeu.PharmaBackckend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO implements java.io.Serializable {
 private static final long serialVersionUID = 1L;
    private  Long id;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
