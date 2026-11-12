package pe.edu.upeu.PharmaBackckend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedBy;
@MappedSuperclass @Getter @Setter
public abstract class EntidadAuditada extends CreacionAuditada {
 @LastModifiedBy @Column(name="modificado_por",length=100)
 private String modificadoPor;
}
