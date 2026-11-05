# Base corregida antes de la sesión 13

Rama: `codex/correcciones-base-s13-altamirano`, creada desde
`feature/seguridad-pruebas-altamirano`. Incluye los cambios locales de autenticación
y perfil que estaban pendientes al comenzar la revisión.

## Correcciones

- Las cuentas de demostración solo se inicializan en `dev`; las cuentas existentes
  conservan su contraseña, roles y estado al reiniciar.
- El JWT de desarrollo dura 60 minutos por defecto, configurable con
  `JWT_EXPIRACION_MINUTOS`.
- `PUT /api/v1/ventas/{id}/anular` requiere `VENTAS_REGISTRAR`, devuelve stock y
  rechaza una segunda anulación. Venta y productos se bloquean al modificar stock
  para evitar que dos operaciones consuman o repongan las mismas existencias.
- La baja de productos conserva el registro y su historial, cambiando su estado.
- La edición de categorías y productos rechaza nombres duplicados.
- Conflictos de integridad devuelven 409; JSON o parámetros mal formados, 400.
- Mockito se carga como agente al ejecutar pruebas para evitar el bloqueo por
  autoacoplamiento de la JVM en Windows.

## Verificación

Se requiere Java 21. Desde PowerShell:

```powershell
./mvnw.cmd test '-Dtest=!PharmaBackckendApplicationTests'
./mvnw.cmd test -Dtest=PharmaBackckendApplicationTests
```

La segunda prueba requiere Oracle local disponible según `application-dev.yaml`.
Valida el esquema existente, desactiva Flyway y simula el inicializador de cuentas;
no registra ventas ni modifica cuentas. Hibernate puede gestionar tablas temporales
internas al inicializar el motor de persistencia.

## Alcance

No se integraron automáticamente ramas anteriores, ni se incorporaron Redis,
Actuator, Nginx o configuración de despliegue de la sesión 13. Productos sigue
devolviendo un listado completo: la paginación actual se realiza en el frontend.
Las credenciales de demostración son solo para desarrollo.
