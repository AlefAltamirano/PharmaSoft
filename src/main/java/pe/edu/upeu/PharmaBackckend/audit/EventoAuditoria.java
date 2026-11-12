package pe.edu.upeu.PharmaBackckend.audit;
public record EventoAuditoria(String usuario,String accion,String entidad,Long entidadId,String detalle,String requestId) {}
