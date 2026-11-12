package pe.edu.upeu.PharmaBackckend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
@MappedSuperclass @Getter @Setter @EntityListeners(AuditingEntityListener.class)
public abstract class CreacionAuditada {
 @CreatedBy @Column(name="creado_por",length=100,nullable=false,updatable=false)
 private String creadoPor;
}
