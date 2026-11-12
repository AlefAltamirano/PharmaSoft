package pe.edu.upeu.PharmaBackckend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="auditoria_eventos") @Getter @Setter @NoArgsConstructor
public class AuditoriaEvento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,updatable=false) private LocalDateTime fecha;
 @Column(nullable=false,length=100,updatable=false) private String usuario;
 @Column(nullable=false,length=60,updatable=false) private String accion;
 @Column(nullable=false,length=60,updatable=false) private String entidad;
 @Column(name="entidad_id",updatable=false) private Long entidadId;
 @Column(length=500,updatable=false) private String detalle;
 @Column(name="request_id",length=36,updatable=false) private String requestId;
}
