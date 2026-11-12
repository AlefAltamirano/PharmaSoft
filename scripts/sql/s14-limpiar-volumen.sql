WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
-- Retira exclusivamente filas reservadas de carga; ejecutar después de las mediciones.
DELETE FROM ventas WHERE creado_por='carga' AND NOT EXISTS (SELECT 1 FROM detalle_ventas d WHERE d.venta_id=ventas.id);
DELETE FROM productos WHERE creado_por='carga' AND nombre LIKE 'S14-CARGA-%' AND NOT EXISTS (SELECT 1 FROM detalle_ventas d WHERE d.producto_id=productos.id);
COMMIT;
EXIT
