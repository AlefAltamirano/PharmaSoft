WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET SERVEROUTPUT ON
-- Solo desarrollo. Idempotente: nombres reservados S14-CARGA y creado_por=carga.
-- Filas de ventas sintéticas ANULADAS, sin detalles: no alteran stock ni reportes de ventas registradas.
DECLARE
 c NUMBER; cliente NUMBER;
BEGIN
 SELECT MIN(id) INTO c FROM categorias WHERE estado=TRUE;
 SELECT MIN(id) INTO cliente FROM clientes WHERE estado=TRUE;
 IF c IS NULL OR cliente IS NULL THEN RAISE_APPLICATION_ERROR(-20002,'Se requiere categoría y cliente activos'); END IF;
 FOR i IN 1..5000 LOOP
  INSERT INTO productos(nombre,descripcion,precio,stock,estado,"Fecha de creacion",categoria_id,creado_por)
  SELECT 'S14-CARGA-'||LPAD(i,5,'0'),'Datos sintéticos para paginación',10,100,TRUE,SYSTIMESTAMP,c,'carga'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre='S14-CARGA-'||LPAD(i,5,'0'));
 END LOOP;
 SELECT COUNT(*) INTO c FROM ventas WHERE creado_por='carga';
 FOR i IN 1..GREATEST(0,20000-c) LOOP
  INSERT INTO ventas(fecha,total,estado,cliente_id,creado_por)
  VALUES (TIMESTAMP '2025-01-01 00:00:00'+NUMTODSINTERVAL(i,'SECOND'),0,'ANULADA',cliente,'carga');
 END LOOP;
 COMMIT;
END;
/
SELECT 'productos='||COUNT(*) FROM productos;
SELECT 'ventas='||COUNT(*) FROM ventas;
SELECT 'productos_carga='||COUNT(*) FROM productos WHERE creado_por='carga';
SELECT 'ventas_carga='||COUNT(*) FROM ventas WHERE creado_por='carga';
EXIT
