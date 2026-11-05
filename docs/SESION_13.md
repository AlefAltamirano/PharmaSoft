# Sesión 13: optimización y preparación para producción

Rama: `feature/optimizacion-produccion-altamirano` en ambos repositorios.

## Ejecutar

1. Java 21 y Oracle local. Compilar con `./mvnw.cmd package`.
2. En frontend: `npm run build -- --stats-json` y
   `docker compose -f deploy/docker-compose.yml up -d`.
3. Backend: configurar `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`,
   `DB_PASSWORD`, `JWT_SECRET` (Base64 de al menos 32 bytes aleatorios) y
   `REDIS_HOST=localhost`. Ejecutar `java -jar target/PharmaBackckend-0.0.1-SNAPSHOT.jar`.
4. Abrir `http://localhost:8081`. Salud pública en
   `http://localhost:8080/actuator/health`; métricas requieren token administrativo.

Los secretos llegan desde el entorno y no se incluyen en los archivos de entrega.
No usar credenciales de demostración en una instalación pública.

## Comprobación

`./mvnw.cmd test`: 48 pruebas. `npm test -- --watch=false`: 16 pruebas.
`scripts/verify-s13.cjs` ejecuta comprobaciones locales con Edge/Playwright y Docker:
crea y elimina una categoría; registra y anula una venta, restaurando stock;
detiene y reinicia Redis. Los registros de venta anulados quedan como auditoría.
Configurar S13_ADMIN_PASSWORD y S13_SELLER_PASSWORD en el entorno antes de ejecutar; no se guardan contraseñas en el script.

La prueba de contexto desactiva Flyway, valida el esquema y simula el inicializador
de cuentas. Hibernate puede gestionar tablas temporales internas.

## Evidencias y límites

Se conservaron salidas de build y dos llamadas a categorías antes de optimizar.
Tras los cambios: 14 comprobaciones aprobadas. La caché HTTP se midió sin
interceptar peticiones (Playwright desactiva caché al instalar interceptores).
La primera entrada a productos posterior al login transfirió 12562 bytes;
recarga normal: 3044 bytes con 17 recursos desde caché.

El paquete inicial creció de 298,13 a 309,97 kB al incorporar soporte defer;
el reporte separado ocupa 1,74 kB. No se afirma reducción del paquete inicial.
La caché de categorías evitó SQL en el segundo GET. Antes: 23 ms; después: 21 ms
(muestras individuales del perfil dev, no benchmark estadístico).

La entrega local contiene PDF, evidencias originales y capturas. Los PR permanecen
abiertos hacia develop. No se realizó merge ni despliegue público.
