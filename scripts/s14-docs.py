from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import json,shutil,subprocess,datetime
r=Path(__file__).resolve().parents[1];f=r.parent/'pharma-frontend';d=r/'docs';d.mkdir(exist_ok=True)
def put(name,s):
 p=d/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s.strip()+'\n',encoding='utf-8')
def commit(repo):return subprocess.check_output(['git','rev-parse','--short','HEAD'],cwd=repo,text=True).strip()
put('README.md','''# PharmaSoft — Semana 14
Integración, auditoría y estabilización. Autor: Alef Altamirano. Fecha: 12/11/2026.

Backend: https://github.com/AlefAltamirano/PharmaSoft
Frontend: https://github.com/AlefAltamirano/PharmaSoft_Front-end
Rama de ambos: feature/integracion-estabilizacion-altamirano. Parte de la versión completa de Semana 13.

## Índice
- [Arquitectura](arquitectura.png)
- [Auditoría y seguridad](auditoria.md)
- [Incidencias](incidencias.md)
- [Resultados y matriz de integración](resultados-pruebas.md)
- [Guion de demostración](guion-demostracion.md)
- [Checklist de preparación](checklist-demostracion.md)
- [Defensa técnica](defensa-tecnica.md)
- [Evidencias](evidencias/README.md)
- [Colección Postman](postman/PharmaSoft-S14.postman_collection.json)
- [Compatibilidad y ejecución](ejecucion-s14.md)

Las evidencias pertenecen a la ejecución real local. No contienen contraseñas, tokens ni el respaldo privado de Oracle. Los datos de carga son sintéticos. La etiqueta v1.0-demo identifica el cierre de cada repositorio; la publicación y los PR se registran en enlaces-entrega.md.
''')
put('auditoria.md','''# Auditoría: identidad, eventos y trazabilidad

## Dos responsabilidades
Spring Data JPA registra creadoPor y modificadoPor en categorías, productos y clientes; las ventas conservan creadoPor. Se mantienen los callbacks de fechas ya existentes. CreacionAuditada y EntidadAuditada usan AuditingEntityListener; UsuarioAuditor obtiene el usuario autenticado, excluye AnonymousAuthenticationToken y usa sistema para trabajos sin sesión. Los DTO exponen esos campos sin alterar los campos anteriores.

La bitácora guarda un historial separado de acciones. AuditoriaEventos captura usuario y MDC.requestId antes de terminar la operación. La operación de negocio publica un EventoAuditoria dentro de su transacción. TransactionalEventListener(AFTER_COMMIT) llama a un bean separado AuditoriaEscritor con REQUIRES_NEW: un rollback de negocio no genera evento confirmado. Un fallo de escritura se registra en logs y no revierte una venta ya confirmada. Esta política puede perder un evento si Oracle falla justo después del commit; para entrega garantizada se necesitaría un outbox transaccional y reintentos supervisados.

## Eventos implementados
- VENTA_REGISTRADA: Venta, ID y total/líneas; solo tras confirmar la transacción.
- PRECIO_MODIFICADO: Producto, ID, precio anterior y nuevo; solo si cambia.
- PRODUCTO_ELIMINADO: Producto, ID; baja lógica.
- LOGIN_EXITOSO y LOGIN_FALLIDO: Usuario; escritura directa REQUIRES_NEW independiente de la autenticación.
- CLIENTE_REGISTRADO: Cliente, ID; evento adicional de la autónoma.
- VENTA_ANULADA: Venta, ID, restitución de stock; evento adicional de la autónoma.

No se escriben contraseñas, JWT, DNI ni correos en los detalles de eventos. Usuario y requestId permiten correlacionar respuesta HTTP, log y fila de Oracle. El detalle se limita a 500 caracteres y requestId a 36. El registro es append-only: no hay API de edición/borrado y un trigger Oracle rechaza UPDATE/DELETE. El propietario del esquema aún puede administrar o deshabilitar ese trigger; no se presenta como una protección criptográfica.

## Acceso y consultas
GET /api/v1/auditoria exige USUARIOS_ADMINISTRAR tanto en SecurityConfig como en PreAuthorize. El menú y la ruta Angular también exigen ese permiso. La API es la autoridad de seguridad incluso si se manipula la interfaz.

Filtros usuario parcial sin distinguir mayúsculas, acción exacta, desde y hasta inclusivos por día (límite superior exclusivo al día siguiente). Orden fecha descendente e ID estable, página desde cero, tamaño entre 1 y 100. Las búsquedas y cambios de tamaño vuelven a la página cero. La URL conserva filtros y paginación al recargar.

## Esquema existente y migraciones
El esquema real no tenía flyway_schema_history. Los archivos históricos V2_seguridad.sql, V3_actualizar_claves_usuarios.sql y V4_restaurar_supervisor.sql no siguen el separador doble de Flyway y no se ejecutaban. Además, Boot 4 requiere spring-boot-starter-flyway. Se preservaron los históricos, se respaldó PHARMADB mediante Data Pump y se estableció baseline 4 del esquema observado. V5__auditoria.sql fue aplicada realmente y consta en el historial. El baseline reconoce un esquema existente: no certifica que los archivos antiguos fueran ejecutados por Flyway.

Se usa V5 para no reutilizar la numeración histórica V4 mencionada en la guía. La base conserva sus columnas de fechas con nombres entre comillas y espacios. Se añadieron campos de usuario, auditoria_eventos, índices de fecha/usuario-fecha/nombre de producto y el trigger de inmutabilidad. Hibernate validate ya no modifica tablas implícitamente en desarrollo. Una base vacía necesita el esquema base preexistente; esta entrega está validada sobre la base del proyecto, no sobre una instalación vacía.

## Cómo verificar una venta
1. Abrir evidencias/s14-estabilizacion/trazabilidad-venta.json y tomar ventaId y requestId.
2. Consultar SELECT id,creado_por FROM ventas WHERE id=<ventaId>.
3. Consultar SELECT usuario,accion,entidad_id,request_id FROM auditoria_eventos WHERE entidad='Venta' AND entidad_id=<ventaId>.
4. Buscar ese requestId en el log del backend: POST /api/v1/ventas -> 201.
5. Abrir /auditoria con admin y filtrar VENTA_REGISTRADA.

La regresión de stock insuficiente comprueba que un 409 conserva stock, total de ventas y total de eventos VENTA_REGISTRADA. La prueba unitaria comprueba que un fallo del escritor de auditoría no se propaga al negocio confirmado.
''')
put('incidencias.md','''# Registro de incidencias — 12/11/2026

Se registran problemas observados al revisar la base de Semana 13 y durante la integración de Semana 14. Cada cierre identifica una regresión concreta. P1=alta, P2=media, P3=baja. Los avisos de dependencias se detallan aparte y no se ocultan como incidencias funcionales.

## S14-01 — Listado de ventas filtrado en el navegador — P1 — Cerrada
Módulo: ventas. Reproducir: abrir ventas y aplicar filtros con volumen. Esperado: una página desde Oracle. Observado: servicio descargaba el listado completo y filtraba/recortaba localmente. Causa: endpoint antiguo y filtrado frontend. Corrección: GET /ventas/buscar con filtros, Pageable y validación; frontend consume PaginaResponse. Commits backend e0a5737 / frontend f3638c5. Regresión: E2E 11, medición primera/última página con 20.000 ventas sintéticas.

## S14-02 — Formulario solicitaba hasta mil productos — P2 — Cerrada
Módulo: ventas/catálogo. Reproducir: abrir nueva venta. Esperado: búsquedas acotadas con stock y precio actuales. Observado: carga masiva inicial que se truncaba al crecer el catálogo y superaba el nuevo límite. Causa: búsqueda local sobre la carga de productos. Corrección: endpoints buscar de productos/clientes, activos y stock positivo, máximo veinte coincidencias; debounce 300 ms y switchMap. Commits e0a5737 / f3638c5. Regresión: E2E 07, 10 y 18; prueba de formulario Angular.

## S14-03 — El estado de listas se perdía al recargar — P2 — Cerrada
Módulo: paginación. Reproducir: ir a otra página, filtrar y pulsar F5. Esperado: conservar estado y volver a primera página cuando cambia un filtro. Observado: página y filtros solo existían en memoria. Causa: señales locales sin sincronización con router. Corrección: query params e inputs; paginador compartido de Productos, Ventas y Auditoría. Commit f3638c5 y 03104c6. Regresión: E2E 06, 11 y 12.

## S14-04 — Flyway no se activaba en Boot 4 — P1 — Cerrada
Módulo: persistencia. Reproducir: iniciar la configuración previa e inspeccionar user_tables. Esperado: migraciones administradas. Observado: no existía tabla de historial. Causa: flyway-core sin módulo de autoconfiguración de Boot 4; archivos históricos con nombres incompatibles. Corrección: starter compatible, respaldo previo, baseline explícito del esquema existente y V5 válida. Commit 688a457. Regresión: arranque real con Successfully applied y consulta del historial baseline 4 / V5 exitosa.

## S14-05 — Escrituras sin identidad ni historial de operación — P1 — Cerrada
Módulo: auditoría. Reproducir: registrar venta/cambiar precio en la versión previa. Esperado: usuario y evento correlacionados. Observado: no había campos de usuario ni tabla de eventos. Causa: auditoría aún no implementada. Corrección: entidades auditadas, eventos AFTER_COMMIT, escritor independiente, bitácora append-only y acceso administrativo. Commit 688a457. Regresión: E2E 07, 08, 13 y 16; AuditoriaEventosTest; consulta Oracle.

## S14-06 — Orden inválido al abrir URL con paginación — P2 — Cerrada
Módulo: productos/router. Reproducir: abrir /productos?pagina=0&tamanio=20. Esperado: veinte productos ordenados por nombre. Observado en la primera ronda: 409 y tabla vacía. Causa: withComponentInputBinding entrega undefined para parámetros ausentes, reemplazando los valores iniciales de orden/dirección. Corrección: transforms con fallback explícito. Commit 03104c6. Regresión: E2E 06 y tres repeticiones completas posteriores sin reintentos.

## S14-07 — Precio seleccionado conservaba un valor anterior — P2 — Cerrada
Módulo: venta. Reproducir: seleccionar producto y volver a buscar tras una actualización. Esperado: usar la nueva coincidencia del servidor. Observado en revisión: el merge conservaba prioritariamente el objeto seleccionado anterior. Causa: deduplicación a favor del objeto viejo. Corrección: preservar seleccionados solo si no vienen en la respuesta nueva. Commit 03104c6. Regresión: E2E 18 consulta precio vigente; el precio final siempre lo calcula Oracle/backend.

## Resumen funcional
Siete incidencias: tres P1, cuatro P2; todas cerradas. Ventas/catálogo 3, paginación/router 2, persistencia 1, auditoría 1. No hay P1 funcionales abiertas al cierre de las pruebas registradas. Las limitaciones operativas (outbox, esquema vacío y ensayo oral) figuran en la documentación, no como verificaciones realizadas.
''')
put('ejecucion-s14.md','''# Ejecución y compatibilidad

Base comprobada: Java 21, Spring Boot 4.0.7, Oracle local FREEPDB1 (host 1522), Redis 6379, Angular 22, TypeScript 6. Se conservó la base funcional de Semana 13. Los procesos de la versión anterior no se cerraron para hacer las pruebas: Semana 14 usa backend 8090 y frontend build 8081.

## Entorno local de pruebas
1. Iniciar Oracle y Redis. El respaldo privado de PHARMADB está fuera del repositorio en Escritorio/Semana 14/Respaldo_privado; contiene información sensible y no debe subirse a GitHub.
2. Backend: Java 21; ejecutar mvnw.cmd package en un equipo con Maven Wrapper configurado, luego java -jar target/PharmaBackckend-0.0.1-SNAPSHOT.jar --server.port=8090. En esta máquina se usó el Maven 3.9.16 ya almacenado porque el wrapper intenta descargar herramientas. No exportar secretos a los informes.
3. Frontend: npm ci, npm run build, node scripts/preview-s14.cjs. Abre http://localhost:8081; el proxy local envía /api al 8090. API_URL y PORT son configurables. Este script es una vista local, no el servidor de producción. Para Angular serve se mantiene el entorno development habitual contra 8080.
4. npm test -- --watch=false. Para E2E, instalar componentes con npx playwright install ffmpeg. La configuración local usa Edge (motor Chromium) instalado. Para Chromium descargado, instalar chromium y definir S14_BROWSER_CHANNEL=chromium.
5. Definir S14_ADMIN_PASSWORD, S14_VENDEDOR_PASSWORD y S14_ALMACEN_PASSWORD en el proceso local, luego npx playwright test --repeat-each=3. No guardar esas variables en Git. S14_BASE_URL y S14_API_URL permiten cambiar destinos.

Las pruebas crean clientes/productos sintéticos con prefijos E2E-S14 y anulan las ventas de prueba para restituir stock. Los productos se dan de baja lógica al final. No borran la bitácora. Los clientes se conservan por integridad de las ventas históricas de prueba.

## Carga y retirada de volumen
Con Oracle respaldado: scripts/s14-oracle.ps1 scripts/sql/s14-volumen.sql. Es idempotente por nombres de producto y cantidad marcada creado_por=carga. Añade 5.000 productos y 20.000 ventas ANULADAS sin detalles, fechadas en 2025: sirven para paginar, no simulan ventas comerciales ni modifican reportes de ventas registradas. No es una migración ni se ejecuta al iniciar la aplicación.

Para retirar solo esa carga: scripts/s14-oracle.ps1 scripts/sql/s14-limpiar-volumen.sql. Conserva filas que estén referenciadas por detalles. Tras SQL directo, invalidar únicamente las cachés productos/categorias o reiniciar con su TTL vencido; no usar FLUSHALL en un Redis compartido. Las creaciones API de fixtures invalidaron productos antes de las mediciones registradas.

## Límites conocidos
El esquema base del proyecto debe existir antes del baseline. La entrega no instala una base completamente vacía. No se garantizan métricas de producción ni concurrencia por una medición local individual. La pérdida de auditoría tras un fallo entre commit y escritura se registra pero requiere outbox para garantía de entrega. Los PR se dejan abiertos para revisión; no se mezclaron automáticamente con develop.
''')
put('guion-demostracion.md','''# Guion de demostración — objetivo 10 minutos

## 00:00–01:00 — Contexto (1 min)
PharmaSoft integra catálogo, clientes, ventas y reportes. Mostrar las ramas y el tag de la entrega. Explicar que Oracle es la fuente de verdad, Redis acelera lecturas y Angular conserva el estado en la URL.

## 01:00–04:30 — Flujo principal (3,5 min)
Ingresar como vendedor. Seleccionar un cliente preparado (o crear uno), buscar producto activo con stock, agregar dos unidades, comprobar total estimado y confirmar. Mostrar comprobante. Navegar a Productos sin F5 y comprobar el stock reducido en dos. Explicar que el backend usa precio actual, transacción y bloqueo de productos; la UI no decide el total definitivo.

## 04:30–06:00 — Seguridad (1,5 min)
Mostrar que el vendedor no puede editar catálogo ni consultar auditoría. La ruta directa también deniega acceso. Almacenero no accede a ventas. Explicar 401 frente a 403 y la recuperación de ruta después de expirar sesión.

## 06:00–07:30 — Volumen (1,5 min)
Mostrar veinte filas, siguiente/última página, URL, F5 y filtros que reinician la página. Citar las mediciones reales y la marca sintética de 5.000 productos/20.000 ventas de carga. No afirmar que la latencia local es un SLA de producción.

## 07:30–08:30 — Auditoría (1 min)
Ingresar como administrador, abrir la bitácora, filtrar VENTA_REGISTRADA. Seguir una venta con creadoPor, entidadId y requestId entre Oracle, respuesta y log. Mencionar los dos eventos adicionales: cliente registrado y venta anulada.

## 08:30–10:00 — Calidad y cierre (1,5 min)
Mostrar reporte Playwright con las repeticiones, pruebas unitarias e incidencias cerradas. Explicar AFTER_COMMIT/REQUIRES_NEW y su limitación de entrega; mostrar enlaces de los repositorios y evidencias. Tener a mano el video de respaldo.

## Ensayos y video
Se entregan dos recorridos reales automatizados cronometrados y un video sin narración oral. Ver evidencias/s14-estabilizacion/ensayos.json y la carpeta Videos del entregable. Estos tiempos miden la operación automatizada; no son dos ensayos orales humanos. Antes de la clase, el estudiante debe practicar dos veces este guion con narración y anotar sus tiempos y desvíos. Esa práctica personal no se declara realizada por el agente.

Tres flujos base para practicar: (1) venta de dos unidades y stock, (2) denegación por rol/ruta, (3) paginación y trazabilidad administrativa. No improvisar cambios de datos reales durante la exposición.
''')
put('checklist-demostracion.md','''# Checklist de demostración

- [ ] Oracle local iniciado y servicio FREEPDB1 disponible.
- [ ] Redis iniciado; /actuator/health responde UP.
- [ ] Backend de Semana 14 ejecutando el artefacto correcto en 8090.
- [ ] Frontend build de Semana 14 disponible en 8081; acceso directo a rutas SPA funciona.
- [ ] Elegir datos de demostración activos y suficiente stock; evitar datos comerciales reales.
- [ ] Verificar roles admin/vendedor/almacen; credenciales disponibles por canal privado.
- [ ] Probar nueva venta, navegación a catálogo y reporte del administrador.
- [ ] Abrir bitácora con filtro de acción y reporte HTML de pruebas.
- [ ] Cerrar pestañas ajenas y notificaciones; tamaño de pantalla legible.
- [ ] Tener PDFs, repositorios y video local de respaldo abiertos.
- [ ] Ensayar con narración dos veces, medir tres flujos base y anotar desviaciones.
- [ ] Subir PDFs y video al aula virtual según las instrucciones del docente.

Las casillas quedan para la revisión inmediata anterior a la exposición. Las verificaciones automatizadas realizadas se documentan en resultados-pruebas.md; no se presentan como una revisión manual del estudiante.
''')
put('defensa-tecnica.md','''# Defensa técnica — diez preguntas

1. **¿Por qué paginar en el servidor?** Para consultar solo una página y no transferir veinte mil ventas al navegador. Código: VentaController.buscar, VentaServiceImpl.buscar y VentaRepository.buscar. El DTO contiene filas y metadatos; PaginacionUtil limita tamaño/orden.
2. **¿Por qué guardar filtros en la URL?** Permite F5, enlaces reproducibles y atrás/adelante. ProductoList, VentaList y Auditoria usan inputs del router y navigate con queryParamsHandling. Un filtro nuevo reinicia pagina=0.
3. **¿Cómo se cancelan búsquedas anteriores?** switchMap en VentaForm cancela observables HTTP al cambiar el texto; debounceTime(300) reduce solicitudes. rxResource cancela la suscripción anterior cuando cambian los parámetros de lista. linkedSignal conserva los datos previos durante la carga.
4. **¿Auditoría JPA y bitácora son lo mismo?** No. CreacionAuditada/EntidadAuditada guardan quién creó/modificó la fila; auditoria_eventos guarda la acción, entidad, ID, detalle y requestId. Ambas se necesitan para responder qué pasó y quién lo hizo.
5. **¿Por qué AFTER_COMMIT y REQUIRES_NEW?** El primer mecanismo evita eventos confirmados de negocios revertidos. El segundo escribe en una transacción independiente después del commit. AuditoriaEventos y AuditoriaEscritor son beans separados para que se aplique el proxy transaccional.
6. **¿Qué pasa si falla la auditoría?** Se registra el fallo y la venta ya confirmada permanece. AuditoriaEventosTest comprueba que el fallo no se propaga. La solución garantizada sería outbox con reintentos, pendiente de un alcance superior.
7. **¿Por qué no confiar en ocultar botones?** Un atacante puede llamar a la API directamente. SecurityConfig y PreAuthorize validan autoridad en backend. La guarda de Angular y el menú solo complementan esa seguridad. E2E 13 verifica UI y 403 real.
8. **¿Cómo se evita vender más stock del disponible?** VentaServiceImpl.registrar consulta productos con bloqueo de escritura, valida activo/stock y actualiza dentro de una transacción. Ante insuficiencia lanza ReglaNegocioException, responde 409 y no confirma ni venta ni evento. E2E 08 verifica stock y contadores.
9. **¿Por qué V5 y baseline 4?** La base ya existía, no tenía historial y conservaba archivos históricos numerados hasta V4 con nombres no válidos para Flyway. Se respaldó y se adoptó baseline del esquema observado, preservando históricos. V5 añade la auditoría sin afirmar que Flyway ejecutó los archivos antiguos.
10. **¿Cómo sabemos que la suite es estable?** Configuración con workers=1, retries=0 y tres repeticiones por escenario. Se utilizan expectativas sobre estados/HTTP, sesiones nuevas y fixtures propios; no waitForTimeout para sincronizar. El informe registra resultados, duración y commits reales. La evidencia automatizada no sustituye la explicación oral.
''')
put('evidencias/README.md','''# Índice de evidencias

- u1-backend: puntero a código y pruebas actuales de backend; no se inventan capturas históricas de Unidad 1.
- u2-spa-segura: puntero a guardas, interceptores y pruebas actuales; no se presentan como evidencia histórica no disponible.
- s13-optimizacion: captura y resultados existentes recuperados de Semana 13, con procedencia indicada.
- s14-estabilizacion: mediciones JSON, migración/Oracle, capturas de navegador, trazabilidad de venta, ensayos y resultados de pruebas.

Los videos están en la carpeta Videos de Semana 14. El reporte HTML y sus adjuntos seguros están en la carpeta Pruebas. No compartir trazas de fallos sin revisión: pueden contener credenciales o tokens. El respaldo privado Data Pump queda fuera de los entregables publicables.
''')
put('evidencias/u1-backend/README.md','''# Backend
Evidencia actual verificable: controller, service/impl, repository, entity y src/test del backend. Pruebas ejecutadas registradas en resultados-pruebas.md. No hay capturas históricas de Unidad 1 con fecha verificable en esta entrega; no se reconstruyen como si fueran originales.
''')
put('evidencias/u2-spa-segura/README.md','''# SPA segura
Evidencia actual: core/guards, core/interceptors, security/SecurityConfig y E2E 01–05, 13, 14 y 17. No se adjuntan capturas históricas de Unidad 2 no disponibles. Las capturas S14 muestran el estado real integrado.
''')
put('evidencias/s13-optimizacion/README.md','''# Semana 13
Archivos copiados de evidence/s13 del trabajo previo: captura 06-productos.png y resultados.json. Conservan su procedencia; no se reclasifican como capturas de Semana 14.
''')
for name in ['06-productos.png','resultados.json']:
 p=r/'evidence/s13'/name
 if p.exists():shutil.copy2(p,d/'evidencias/s13-optimizacion'/name)
e=d/'evidencias/s14-estabilizacion';e.mkdir(parents=True,exist_ok=True)
for source,name in [(r/'evidence/s14/mediciones.json','mediciones.json'),(r/'evidence/s14/auditoria-oracle.txt','auditoria-oracle.txt'),(r/'evidence/s14/volumen-oracle.txt','volumen-oracle.txt'),(f/'evidence/s14/demo/ensayos.json','ensayos.json')]:
 if source.exists():shutil.copy2(source,e/name)
for source in (f/'evidence/s14/demo').glob('ensayo-1-*.png'):shutil.copy2(source,e/source.name)
# Trazabilidad segura y resumen de resultados; se excluyen cabeceras HTTP con tokens.
results=json.loads((f/'evidence/s14/playwright-results.json').read_text(encoding='utf-8-sig'))
attachments=[]
def walk(s):
 for spec in s.get('specs',[]):
  for t in spec.get('tests',[]):
   for rr in t.get('results',[]):attachments.extend(rr.get('attachments',[]))
 for child in s.get('suites',[]):walk(child)
for suite in results['suites']:walk(suite)
for a in attachments:
 if a.get('name') in ['trazabilidad-venta','cambio-precio']:
  if 'body' in a:
   import base64
   (e/(a['name']+'.json')).write_bytes(base64.b64decode(a['body']))
  elif a.get('path') and Path(a['path']).exists():shutil.copy2(a['path'],e/(a['name']+'.json'))
stats=results['stats'];put('evidencias/s14-estabilizacion/resumen-e2e.json',json.dumps(stats,indent=2))
tests_commit=f'Backend {commit(r)}; frontend {commit(f)}'
put('resultados-pruebas.md',f'''# Resultados de comprobación — 12/11/2026

Estado del código al recoger resultados: {tests_commit}. Las posteriores modificaciones documentales se identifican mediante el tag final v1.0-demo.

## Pruebas
- Backend: 55 pruebas, 0 fallos, 0 errores, 0 omitidas. Suite Maven real con conexión a Oracle local.
- Frontend: 18 pruebas unitarias, 16 archivos, todas correctas.
- E2E: 18 escenarios, tres repeticiones, {stats['expected']} correctas, {stats['unexpected']} inesperadas, {stats['flaky']} intermitentes, {stats['skipped']} omitidas. Duración total {stats['duration']/1000:.2f} segundos. Workers=1; retries=0.
- Navegador: Edge instalado, motor Chromium. URL frontend localhost:8081, backend localhost:8090. Oracle y Redis locales activos.
- HTML: carpeta Pruebas/playwright-report/index.html del entregable. Datos fuente seguros en evidencias/s14-estabilizacion.
- Sesión vencida: E2E 17 adelanta el reloj del navegador; comprueba retorno de ruta. El token manipulado se rechaza realmente en backend (401); no se presenta el reloj simulado como espera física de una hora.

## Matriz de integración
- I01 cliente nuevo disponible en ventas: E2E 10 y preparación de E2E 07.
- I02 productos activos, stock positivo y precio actual: E2E 18; selección de producto en E2E 07.
- I03 venta de dos unidades reduce stock y se refleja al navegar sin F5: E2E 07 y capturas/recorridos de demostración.
- I04 aumenta el reporte: E2E 07 compara respuesta anterior/posterior; el ensayo muestra el reporte como administrador.
- I05 venta registra usuario y requestId: E2E 07 correlaciona cabecera y evento; trazabilidad-venta.json y consulta Oracle/log.
- I06 cambio de precio registra valores y modificadoPor: E2E 16, cambio-precio.json.
- I07 expiración durante formulario dirige a login y recupera ruta: E2E 17, reloj controlado.
- I08 filtrar ventas desde página cinco vuelve a uno: E2E 11.

## Volumen y límites
Carga marcada: 5.000 productos y 20.000 ventas sintéticas ANULADAS. Los totales reales también incluyen datos previos y fixtures de pruebas. Mediciones de primera/última página, bytes, latencias y requestId: mediciones.json. Tamaño 500 y orden password responden 409. Son mediciones locales individuales sin prueba de carga concurrente; no constituyen un SLA.

## Alcance
La suite comprueba excepciones de negocio sin 500 y registra errores JS no controlados; no se detectaron en las ejecuciones aprobadas. Los recorridos grabados también comprueban ausencia de 500 y pageerror. El diagnóstico de dependencias y su corrección se registra en dependencias.md. No se simulan ensayos orales, capturas históricas ausentes ni entregas al aula virtual.
''')
# Colección importable; secretos siempre vacíos.
items=[]
def req(name,method,url,status,body=None,token='adminToken'):
 request={'method':method,'header':[{'key':'Content-Type','value':'application/json'}],'url':'{{baseUrl}}'+url}
 if token:request['auth']={'type':'bearer','bearer':[{'key':'token','value':'{{'+token+'}}','type':'string'}]}
 if body is not None:request['body']={'mode':'raw','raw':json.dumps(body,ensure_ascii=False)}
 items.append({'name':name,'request':request,'event':[{'listen':'test','script':{'type':'text/javascript','exec':[f"pm.test('HTTP {status}',()=>pm.response.to.have.status({status}));"]}}]})
for user in ['admin','vendedor']:
 req('Login '+user,'POST','/auth/login',200,{'username':user,'password':'{{'+user+'Password}}'},None)
 items[-1]['event'][0]['script']['exec'].append(f"if(pm.response.code===200)pm.collectionVariables.set('{user}Token',pm.response.json().token);")
req('Productos página 20 — 200','GET','/productos?pagina=0&tamanio=20',200)
req('Auditoría admin — 200','GET','/auditoria?pagina=0&tamanio=20',200)
req('Venta válida — 201','POST','/ventas',201,{'clienteId':'{{clienteId}}','detalles':[{'productoId':'{{productoId}}','cantidad':1}]})
items[-1]['event'][0]['script']['exec'].append("if(pm.response.code===201)pm.collectionVariables.set('ventaId',pm.response.json().id);")
req('Venta sin detalles — 400','POST','/ventas',400,{'clienteId':'{{clienteId}}','detalles':[]})
req('Productos sin sesión — 401','GET','/productos',401,token=None)
req('Auditoría vendedor — 403','GET','/auditoria',403,token='vendedorToken')
req('Producto inexistente — 404','GET','/productos/999999999999',404)
req('Stock insuficiente — 409','POST','/ventas',409,{'clienteId':'{{clienteId}}','detalles':[{'productoId':'{{productoId}}','cantidad':999999999}]})
req('Tamaño 500 — 409','GET','/productos?tamanio=500',409)
req('Orden fuera de lista — 409','GET','/productos?ordenarPor=password',409)
req('Anular venta de prueba — 200','PUT','/ventas/{{ventaId}}/anular',200)
collection={'info':{'name':'PharmaSoft Semana 14','description':'Solo entorno de desarrollo. Completar contraseñas y IDs de fixtures activos; no exportar tokens. La venta válida modifica stock; la última petición la anula.','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},'variable':[{'key':k,'value':v} for k,v in {'baseUrl':'http://localhost:8090/api/v1','adminPassword':'','vendedorPassword':'','adminToken':'','vendedorToken':'','clienteId':'','productoId':'','ventaId':''}.items()],'item':items}
put('postman/PharmaSoft-S14.postman_collection.json',json.dumps(collection,ensure_ascii=False,indent=2))
put('postman/README.md','''# Colección API
Importar PharmaSoft-S14.postman_collection.json en Postman. Definir passwords en valores locales/secretos, clienteId y productoId de prueba activos con stock. Ejecutar logins primero. La colección cubre 200/201, 400, 401, 403, 404 y 409. La venta válida debe anularse con la última petición. No exportar variables con tokens. La validación automática equivalente se registra en pruebas-api.json; no se afirma haber operado la interfaz de Postman.
''')
# Arquitectura estática exportable.
im=Image.new('RGB',(1800,920),'#f4f7fb');draw=ImageDraw.Draw(im)
font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',26);title=ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf',45)
draw.text((60,35),'PharmaSoft | Integración Semana 14',font=title,fill='#112e56')
boxes=[(60,160,520,420,'Angular SPA\nPáginas y filtros en URL\nGuards + interceptor JWT\nPaginador compartido'),(650,160,1190,420,'Spring Boot 4 / Java 21\nSeguridad por autoridad\nServicios transaccionales\nPaginación y DTO auditados'),(1320,160,1740,420,'Oracle\nDatos del negocio\nHistorial Flyway V5\nCampos de usuario'),(650,600,1190,820,'AFTER_COMMIT → REQUIRES_NEW\nCaptura usuario y requestId\nEventos de autenticación directos\nFallo → log, sin revertir venta'),(1320,600,1740,820,'auditoria_eventos\nSolo inserciones / lectura\nTrigger de inmutabilidad\nÍndices y filtros'),(60,600,520,820,'Redis\nCaché catálogo paginado\nTTL y expulsión al escribir\nFallback a Oracle')]
for x1,y1,x2,y2,text in boxes:
 draw.rounded_rectangle((x1,y1,x2,y2),20,fill='white',outline='#21547e',width=3);draw.multiline_text((x1+22,y1+30),text,font=font,fill='#173656',spacing=18)
for a,b in [((520,280),(650,280)),((1190,280),(1320,280)),((920,420),(920,600)),((1190,710),(1320,710)),((650,370),(400,600))]:
 draw.line((a,b),fill='#208b86',width=6);x,y=b;draw.ellipse((x-7,y-7,x+7,y+7),fill='#208b86')
im.save(d/'arquitectura.png')
print('Documentación generada; estadísticas E2E:',stats)

