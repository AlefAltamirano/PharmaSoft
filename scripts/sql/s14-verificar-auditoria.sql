WHENEVER SQLERROR EXIT SQL.SQLCODE
SET LINESIZE 220 PAGESIZE 100
SELECT "version","description","type","success" FROM "flyway_schema_history" ORDER BY "installed_rank";
SELECT table_name,column_name FROM user_tab_columns WHERE column_name IN ('CREADO_POR','MODIFICADO_POR') ORDER BY table_name,column_name;
SELECT index_name,table_name FROM user_indexes WHERE index_name LIKE 'IX_%';
SELECT trigger_name,status FROM user_triggers WHERE trigger_name='TR_AUDITORIA_INMUTABLE';
SELECT id,usuario,accion,entidad,entidad_id,request_id FROM auditoria_eventos ORDER BY id DESC FETCH FIRST 8 ROWS ONLY;
EXIT
