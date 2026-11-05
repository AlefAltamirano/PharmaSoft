package pe.edu.upeu.PharmaBackckend.dto;
import java.io.Serializable;
import java.util.List;
public record PaginaResponseDTO<T>(List<T> contenido, int pagina, int tamanio,
 long totalElementos, int totalPaginas, boolean ultima) implements Serializable {}
