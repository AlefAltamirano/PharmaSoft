from pathlib import Path
import json,statistics,re
from xml.sax.saxutils import escape
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,Image,PageBreak
from reportlab.lib.styles import getSampleStyleSheet,ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.graphics.shapes import Drawing,Rect,String,Line

b=Path(__file__).resolve().parents[1];f=b.parent/'pharma-frontend';e=b/'evidence/s13/autonoma'
d=json.loads((e/'resultados.json').read_text(encoding='utf-8-sig'))
top=json.loads((e/'A-top5.json').read_text());deploy=json.loads((e/'B-despliegue.json').read_text())
origins=json.loads((e/'B-origenes-DevTools.json').read_text())
styles=getSampleStyleSheet();styles.add(ParagraphStyle(name='Body',fontName='Helvetica',fontSize=10,leading=14,spaceAfter=9,textColor=colors.HexColor('#23394a')));styles.add(ParagraphStyle(name='SmallBody',parent=styles['Body'],fontSize=8,leading=11));styles['Heading1'].textColor=colors.HexColor('#16475c');styles['Heading2'].textColor=colors.HexColor('#168394');story=[]
def p(s,style='Body'):story.append(Paragraph(s,styles[style]))
def section(tag,title):
 if story:story.append(PageBreak())
 p(tag,'SmallBody');p(title,'Heading1')
def table(rows,widths):
 t=Table([[Paragraph(escape(str(c)),styles['SmallBody']) for c in r] for r in rows],colWidths=widths,repeatRows=1)
 t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dceef2')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.whitesmoke,colors.white]),('VALIGN',(0,0),(-1,-1),'TOP'),('TOPPADDING',(0,0),(-1,-1),1 if len(rows)>15 else 8),('BOTTOMPADDING',(0,0),(-1,-1),1 if len(rows)>15 else 8),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8)]));story.extend([t,Spacer(1,12)])
def image(name,h=300,w=495):
 im=Image(str(e/name));ratio=min(w/im.imageWidth,h/im.imageHeight);im.drawWidth=im.imageWidth*ratio;im.drawHeight=im.imageHeight*ratio;story.extend([im,Spacer(1,8)])
def pair(a,z,h=220):
 ims=[]
 for name in [a,z]:
  im=Image(str(e/name));s=min(240/im.imageWidth,h/im.imageHeight);im.drawWidth=im.imageWidth*s;im.drawHeight=im.imageHeight*s;ims.append(im)
 story.extend([Table([ims],colWidths=[247,248]),Spacer(1,10)])
def footer(c,doc):
 c.setStrokeColor(colors.HexColor('#168394'));c.line(48,802,547,802);c.setFont('Helvetica',8);c.setFillColor(colors.HexColor('#607786'));c.drawString(48,28,'LP II | Altamirano | Actividad autónoma S13 - Partes A a F');c.drawRightString(547,28,str(doc.page))

p('LENGUAJE DE PROGRAMACIÓN II','SmallBody');story.append(Spacer(1,35));p('Informe de optimización<br/>y preparación para producción','Title');story.append(Spacer(1,12));p('PharmaSoft - Actividad autónoma S13','Heading1')
table([['Dato','Detalle'],['Estudiante','Alef Altamirano'],['Docente','Benjamin David Reyna Barreto'],['Programa','EP Ingeniería de Sistemas - Ciclo IV'],['Fecha de elaboración','5 de noviembre de 2026'],['Plazo','11 de noviembre de 2026, 23:59'],['Base de trabajo','feature/optimizacion-produccion-altamirano en ambos repositorios']],[140,355])
p('Resumen ejecutivo','Heading2');p('Se ejecutaron las partes A a E de la guía autónoma correcta y se preparó la entrega versionada de la parte F. La mejora adicional difiere el layout y reduce Initial total de 309,97 a 303,54 kB. Cada caché obtuvo 19 aciertos y 1 fallo en 20 lecturas. La matriz C-01 a C-06 pasó después de corregir un defecto del controlador de categorías y el acceso CORS desde Nginx.')
p('El backend devuelve requestId en errores y la SPA muestra el código de seguimiento. La venta de prueba se registró desde la SPA y después se anuló para restituir existencias. La configuración final vuelve a TTL de diez minutos. Pasaron 50 pruebas del backend y 18 del frontend.')
p('Alcance y método','Heading2');p('Se emplearon builds de producción, Edge con Playwright, Chrome DevTools Protocol (CDP), curl -I, Redis CLI y Actuator. Las capturas de paneles de resultados muestran datos realmente obtenidos; se distinguen de las capturas de la interfaz. No se capturaron tokens ni contraseñas. Las cifras son del entorno local, no de una prueba de carga de los tres locales.')

section('PARTE A · 1/2','Cinco módulos y mejora adicional')
rows=[['Módulo del paquete inicial','Aporte estimado*','Origen / dependencia']]
labels=['Core: renderizado interno','Router: navegación','Core: tareas pendientes','Common: utilidades base','Common: módulo común']
for (file,size),label in zip(top,labels):rows.append([file.replace('node_modules/@angular/','@angular/'),f'{size:,} B'.replace(',','.'),label])
table(rows,[270,85,140]);p('* bytesInOutput del metafile de esbuild: atribución por entrada, no tamaño comprimido ni cifras que deban sumarse como bytes de red. El nombre interno _debug_node no significa que este build de producción sea un build de desarrollo. No aparecen módulos de RxJS o código propio entre los cinco primeros.','SmallBody')
p('Análisis con esbuild','Heading2');p('Se cargó browser-stats.json en esbuild.github.io/analyze y se guardó A-analyzer.png. La visualización completa identifica 318 entradas y 39 salidas; incluye todo el build, mientras que la tabla anterior filtra únicamente archivos del paquete inicial.')
image('A-analyzer.png',170)
table([['Mejora aplicada','Antes','Después'],['Layout principal','Import estático de MainLayout','loadComponent con import dinámico'],['Initial total, raw','309,97 kB','303,54 kB'],['Transferencia estimada','87,84 kB','85,88 kB']],[225,135,135])
p('La reducción raw es 6,43 kB (2,07 %); la estimación transferida baja 1,96 kB (2,23 %). El login ya no necesita descargar el layout completo. La pantalla autenticada sigue necesitando ese layout al entrar; no se elimina funcionalidad ni se promete reducir todos los bytes de una sesión. Evidencias: A-build-antes.txt, A-build-despues.txt y A-tamano.png.','SmallBody')

section('PARTE A · 2/2','Navegación con y sin precarga')
p('Se comparó la misma funcionalidad con PreloadAllModules y sin precarga. Cada variante se sirvió con Nginx (8083 y 8081 respectivamente); tres contextos nuevos de navegador, rol administrador, caché HTTP desactivada, 1.500 ms de espera tras Inicio y luego clic en Ventas. Se registraron bytes de la ventana hasta ese clic y tiempo hasta mostrar el título Consulta de ventas.')
rows=[['Variante / ejecución','Bytes primera pantalla','Tiempo a Ventas']]
for a in d['A']:
 for i,r in enumerate(a['iteraciones'],1):rows.append([a['variante']+f' #{i}',str(r['bytesPrimeraPantalla']),str(r['ventasMs'])+' ms'])
table(rows,[215,140,140])
for a in d['A']:
 p(f"<b>{a['variante'].capitalize()}:</b> mediana de {statistics.median(r['bytesPrimeraPantalla'] for r in a['iteraciones']):,.0f} B y {statistics.median(r['ventasMs'] for r in a['iteraciones']):.0f} ms a Ventas.")
p('Decisión para la botica','Heading2');p('Se mantiene sin precarga: la mediana evita unos 54.653 B de descargas iniciales (192.176 frente a 137.523 B) en conexiones limitadas. Con precarga la mediana de navegación mejora de 349 a 87 ms. Para equipos que usan Ventas constantemente podría habilitarse precarga selectiva; la ventaja observada no justifica descargar módulos ajenos al rol en todos los puestos.')
p('La primera ejecución sin precarga tardó 1.727 ms y es sensible al calentamiento del servidor y navegador. Se muestran todas las muestras, no se oculta ese valor; tres ejecuciones locales no permiten inferir rendimiento en la red de los tres locales.','SmallBody')
pair('A-sin-precarga.png','A-con-precarga.png',190)

section('PARTE B · 1/2','Encabezados y origen de la recarga')
table([['Recurso','Cache-Control de curl -I','Origen observado por CDP','¿Correcto?'],['index.html / documento SPA','no-cache','red, con revalidación permitida','Sí'],['main-RU4SMYLB.js','public, max-age=31536000, immutable','caché del navegador','Sí'],['chunk-By83XLOa.js (Ventas)','public, max-age=31536000, immutable','caché del navegador','Sí'],['styles-PHLWQL3M.css','public, max-age=31536000, immutable','caché del navegador','Sí'],['/api/v1/categorias','no-cache, no-store, max-age=0, must-revalidate','red','Sí']],[130,185,105,75])
p('Los encabezados se obtuvieron con curl -I. Los orígenes de reutilización se observaron en CDP, el protocolo de DevTools, al recargar Ventas y entrar a Categorías. El protocolo distingue caché de disco cuando informa fromDiskCache; cuando solo emite requestServedFromCache se informa caché del navegador, sin inventar una separación memoria/disco.')
p('En el ensayo el documento se obtuvo de la red; no se fuerza a declarar 304 porque no fue el estado capturado. Los archivos con hash se reutilizaron. El API se pidió a la red porque la información está ligada a permisos y cambia con el negocio. Guardar respuestas privadas en caché de navegador o proxy podría mostrar información antigua o de otra sesión.')
p('Spring Security aporta por defecto Cache-Control: no-cache, no-store, max-age=0, must-revalidate, además de Pragma: no-cache y Expires: 0. Redis es una caché interna del servidor y no cambia esta política del API. no-cache exige revalidar; no-store impide almacenar la respuesta.')
p('Evidencias: Bheaders-resultado.png, B-origenes-DevTools.json y B-politicas.png. Las cabeceras de autorización usadas para consultar el API no se incluyeron en archivos ni capturas.','SmallBody')

section('PARTE B · 2/2','Despliegue simulado sin detener Nginx')
p('Se cambió el título visible de Productos a “Productos - Catálogo actualizado” y se ejecutó ng build --stats-json con Nginx en marcha. El mismo contexto del navegador, con caché habilitada, se recargó después del build. No se borró la caché.')
table([['Archivo / pantalla','Antes','Después'],['Main del documento','main-VRB7M35K.js','main-RU4SMYLB.js'],['Hoja de estilo','styles-PHLWQL3M.css','styles-PHLWQL3M.css (sin cambio)'],['Título','Productos','Productos - Catálogo actualizado']],[140,175,180])
pair('B-productos-antes.png','B-productos-despues.png',265)
p('El nuevo index.html referencia main con otro hash; esa referencia conduce al nuevo grafo de chunks, incluido el de Productos. Los archivos cuyo contenido no cambió conservaron nombre, por ejemplo styles, y se reutilizaron. Los nombres nuevos se descargaron de la red. B-despliegue.json conserva los eventos y sus orígenes; el título nuevo aparece después de la recarga sin limpiar almacenamiento.')
p('Esta demostración usa el volumen local de Nginx y el build in situ, no un despliegue atómico público. En producción conviene publicar assets primero, cambiar index al final y conservar durante un tiempo chunks de la versión anterior para sesiones que continúan abiertas.')

section('PARTE C · 1/3','20 lecturas por caché: aciertos y tiempos')
rows=[['Caché','Hit antes/después','Miss antes/después','Delta hit / miss','Tasa']]
for c in d['Cseries']:rows.append([c['name'],f"{c['before']['hit']} / {c['after']['hit']}",f"{c['before']['miss']} / {c['after']['miss']}",f"{c['hit']} / {c['miss']}",f"{100*c['hit']/(c['hit']+c['miss']):.0f}%"])
table(rows,[105,110,110,100,70])
p('Se reinició el backend y se eliminaron exclusivamente las claves de caché de categorías y productos. Se ejecutaron 20 GET /categorias y 20 GET /productos?pagina=0. Se filtró cache.gets por name y result antes y después: usar diferencias evita confundir contadores anteriores con la ventana del experimento. La primera lectura de cada clave fue miss y las siguientes 19 fueron hit.')
rows=[['Serie de tiempo','Categorías','Productos']]
c1,c2=d['Cseries']
rows.extend([['Primer GET: fallo (una observación)',f"{c1['tiemposMs'][0]:.2f} ms",f"{c2['tiemposMs'][0]:.2f} ms"],['Media de siguientes 19 GET: aciertos',f"{c1['mediaAciertosMs']:.2f} ms",f"{c2['mediaAciertosMs']:.2f} ms"],['Lecturas del experimento','20','20']]);table(rows,[265,115,115])
p('La media de fallos en esta serie coincide con la única primera lectura: 19,98 ms en categorías y 78,38 ms en productos. Los aciertos promediaron 13,76 y 26,37 ms. Se midió tiempo HTTP completo desde el cliente; incluye seguridad, red local, deserialización y caché. La tasa del 95 % es consecuencia de repetir la misma clave, no una predicción del tráfico habitual.')
rows=[['GET','Categorías (ms)','Productos (ms)']]+[[i+1,f"{c1['tiemposMs'][i]:.2f}",f"{c2['tiemposMs'][i]:.2f}"] for i in range(20)]
table(rows,[55,220,220])

section('PARTE C · 2/3','TTL e invalidación inmediata')
table([['Momento de la prueba','Redis CLI TTL'],['Arranque temporal con --spring.cache.redis.time-to-live=1m',str(d['CTTL']['ttlInicial'])+' s'],['Después de esperar la expiración',str(d['CTTL']['ttlTrasEspera'])+' (clave inexistente)'],['Nueva lectura que repuebla la caché',str(d['CTTL']['ttlTrasNuevaLectura'])+' s'],['Configuración final restaurada: 10m','600 s verificados']],[360,135])
p('El override de ejecución permitió probar un minuto sin dejar modificado el YAML de producción. Después se reinició con 10m, se eliminó la copia de categorías y se leyó nuevamente: el TTL volvió a 600. Evidencias: CTTL-resultado.png y C-TTL-restaurado.json.')
rows=[['ID','Operación','Esperado / obtenido','Resultado']]
descs=[('Crear categoría','La nueva categoría aparece; HTTP 201.'),('Renombrar categoría','La columna de productos muestra Autonoma renombrada 1452.'),('Editar precio','GET por ID y lista muestran 9,51.'),('Venta desde SPA','Stock baja de 150 a 149; venta 40.'),('Eliminar categoría sin productos','La categoría 1452 deja de aparecer.'),('Detener Redis','Productos HTTP 200, SPA utilizable y health DOWN.')]
for r,(op,result) in zip(d['Cmatrix'],descs):rows.append([r['id'],op,result,'Correcto' if r['ok'] else 'Falla'])
table(rows,[45,145,245,60])
p('Se calentó la caché antes de las escrituras. Se utilizó un producto existente, se cambiaron temporalmente categoría y precio y después se restauró el DTO original. La venta se anuló y el stock quedó restituido. La categoría temporal se eliminó una vez desvinculado el producto. Redis volvió a iniciarse al finalizar C-06.')
p('No se añadieron anotaciones de invalidación sin necesidad: las existentes limpian categorías/productos cuando corresponde, incluidas venta y anulación. El fallo C-02 inicial tenía otra causa, documentada en la siguiente página.')

section('PARTE C · 3/3','Defectos encontrados y correcciones')
table([['Defecto','Antes','Corrección / después'],['Edición de categorías','El controlador llamaba create y luego update. Se creaba otra categoría con el nombre nuevo y update devolvía 409 por duplicado. C-02 mostraba el nombre anterior.','Se eliminó la llamada create del PUT. Una prueba verifica update y nunca create. C-02 repitió la operación y mostró el nombre nuevo.'],['CORS en producción local','POST desde la SPA en 8081 era rechazado como Invalid CORS request, aunque las lecturas funcionaban.','Se configuraron orígenes explícitos, incluido 8081, y FRONTEND_ORIGINS configurable. La venta desde la SPA respondió 201.'],['Diagnóstico de errores','El cuerpo no tenía requestId y la SPA no mostraba identificador.','DTO, advice y handlers de seguridad lo incluyen; el helper de la SPA muestra el código.']],[100,200,195])
p('La primera hipótesis fue una comprobación de duplicados del repositorio; se descartó al localizar la llamada extra en CategoriaController. El cambio experimental de consulta no quedó en la versión final. Se eliminaron solo las categorías huérfanas creadas por nuestras pruebas. antes-correcciones.json y limpieza-defecto.json conservan los resultados y la limpieza.')
p('Las anotaciones @CacheEvict de actualización de categoría ya limpiaban ambas cachés. Por ello no se atribuye falsamente la falla a Redis. El TTL tampoco resuelve errores del controlador: la operación debía corregirse para que el dato de Oracle y su copia fueran correctos inmediatamente.')
p('Riesgo residual','Heading2');p('CacheConfig tolera errores de lectura y de invalidación para conservar disponibilidad. Una escritura confirmada en Oracle durante una caída de Redis puede dejar una copia anterior accesible tras la recuperación hasta su TTL. La matriz confirma los seis escenarios pedidos, pero no cubre todas las carreras entre transacciones e invalidación; se recomienda invalidar después del commit y probar escrituras durante una caída.')

section('PARTE D · 1/2','Trazabilidad de venta y errores')
p(f"La venta {d['Dventa']['ventaId']} se registró desde la SPA. El X-Request-Id de su respuesta fue <b>{d['Dventa']['requestId']}</b>. Al filtrar logs por ese identificador se obtuvieron dos líneas:")
for line in d['Dventa']['lineas']:p(escape(line.strip()),'SmallBody')
p('La primera confirma el hecho del negocio: venta guardada e ítems. La segunda registra método, ruta, estado 201 y duración total HTTP de 148 ms. El filtro usa MDC y elimina el identificador en finally para evitar mezclar peticiones. No se registraron el cuerpo de venta ni datos personales del cliente.')
p('requestId en las respuestas de error','Heading2');p('ErrorResponseDTO incorpora requestId. GlobalExceptionHandler toma MDC.get("requestId") y los handlers de autenticación/autorización hacen lo mismo. La SPA lee error.error.requestId, valida su formato y añade “Código de seguimiento: ...”. El login también utiliza el helper común.')
er=d['Derror'];table([['Error provocado','Evidencia'],['Operación','Crear dos categorías con el mismo nombre'],['Estado HTTP',str(er['status'])],['X-Request-Id',er['header']],['requestId del cuerpo',er['body']['requestId']],['Mensaje de la SPA','Nombre duplicado y código de seguimiento visible']],[180,315])
image('D-error-SPA.png',220)
p('El soporte puede pedir solo el código al usuario y buscar la petición correspondiente, sin solicitar contraseña ni token. Se añadieron pruebas del contrato cabecera/cuerpo y de la limpieza del MDC, además de pruebas del helper de mensajes. La categoría de prueba se eliminó.')

section('PARTE D · 2/2','Salud, métricas y auditoría de secretos')
rows=[['Redis / acceso','HTTP','Estado','Detalles']]
for k,label in [('encendidoPublico','Encendido / sin token'),('encendidoAdmin','Encendido / admin'),('apagadoPublico','Apagado / sin token'),('apagadoAdmin','Apagado / admin')]:
 v=d['Dhealth'][k];rows.append([label,v['status'],v['body']['status'],'Componentes db, redis, etc.' if v['body'].get('components') else 'Sin componentes'])
table(rows,[190,55,65,185]);p('El estado global fue DOWN con Redis apagado y devolvió 503, aunque productos continuó consultando Oracle. Esto expresa una dependencia degradada, no prueba que toda operación de negocio esté caída. El detalle de salud requiere administrador; no debe exponerse a todos. Se conservaron las cuatro respuestas completas sin credenciales.')
m={v['statistic']:v['value'] for v in d['Dmetrics']['body']['measurements']}
table([['http.server.requests filtrado por uri:/api/v1/ventas','Valor'],['COUNT',m['COUNT']],['TOTAL_TIME',str(m['TOTAL_TIME'])+' s'],['MAX',str(m['MAX'])+' s'],['Interpretación','Una petición POST exitosa (201) en esa instancia; media = TOTAL_TIME / COUNT, aproximadamente 147,94 ms.']],[340,155])
p('Las métricas corresponden a esa instancia y ventana tras reinicio, no al historial completo de ventas. Actuator no conserva por sí solo series de tiempo ni genera alertas. El backend mantiene métricas restringidas por USUARIOS_ADMINISTRAR.')
a=d['Dauditoria'];table([['Auditoría del archivo de producción','Resultado'],['Líneas revisadas',a['lineas']],['password, Bearer o secret',a['patronesPasswordBearerSecret']],['Patrones de token JWT completo',a['tokensJWT']],['DNI de ocho dígitos fuera del campo requestId',a['dniFueraDeRequestId']],['Falsos positivos revisados','Cinco secuencias numéricas eran requestId entre corchetes, no DNI.']],[340,155]);p('No se encontraron secretos en la muestra. La búsqueda de JWT y cadenas sensibles ayuda a detectar filtraciones, pero no constituye una prueba universal de ausencia. El secreto temporal se generó en memoria, no se incluyó en capturas ni anexos. En el comprobante adjunto se ocultó el DNI.','SmallBody')

section('PARTE E · 1/2','Arquitectura y variables de producción')
p('La sección “Despliegue en producción” se añadió al README.md de ambos repositorios con diagrama Mermaid, puertos, variables, pasos, rollback, estado de prácticas y tres recomendaciones priorizadas.')
diagram=Drawing(495,125)
boxes=[(0,65,95,'Navegador','HTTPS 443'),(125,65,105,'Nginx','80 / local 8081'),(265,65,110,'Backend','8080 privado'),(395,90,100,'Oracle','1521 / local 1522'),(395,15,100,'Redis','6379 privado')]
for x,y,w,title,port in boxes:
 diagram.add(Rect(x,y,w,40,fillColor=colors.HexColor('#e4f2f5'),strokeColor=colors.HexColor('#258899')));diagram.add(String(x+5,y+24,title,fontSize=10));diagram.add(String(x+5,y+9,port,fontSize=8))
for x1,y1,x2,y2 in [(95,85,125,85),(230,85,265,85),(375,85,395,110),(375,85,395,35)]:diagram.add(Line(x1,y1,x2,y2,strokeColor=colors.HexColor('#258899')))
story.append(diagram)
p('HTTPS 443 es la propuesta para un servidor público; la prueba local continúa en HTTP. SPA y API comparten origen mediante /api. Oracle y Redis no deben publicarse directamente a los clientes de los tres locales.')
table([['Variable','Propósito, sin valores reales'],['SPRING_PROFILES_ACTIVE','Seleccionar prod'],['DB_URL','Conexión JDBC de Oracle'],['DB_USERNAME / DB_PASSWORD','Credenciales del esquema'],['JWT_SECRET','Clave aleatoria Base64 de al menos 256 bits'],['JWT_EXPIRACION_MINUTOS','Vigencia de tokens'],['REDIS_HOST / REDIS_PORT','Servidor de caché compartido'],['SERVER_PORT','Puerto del backend'],['FRONTEND_ORIGINS','Orígenes explícitos permitidos, separados por coma']],[200,295])
p('Construcción y despliegue','Heading2');p('Registrar versiones exactas y respaldar Oracle; compilar backend con Java 21 y frontend con ng build --stats-json; levantar Compose; definir secretos por entorno, iniciar el JAR con prod y comprobar salud. Publicar assets antes de index.html y ejecutar pruebas de login, ventas, stock, permisos y caché. Los archivos de configuración no contienen valores reales de producción.')

section('PARTE E · 2/2','Buenas prácticas y plan para tres locales')
table([['Práctica','Estado','Acción pendiente'],['Secretos de producción fuera de Git','Parcial','Almacén de secretos real; credenciales de demostración solo en dev.'],['prod: validate + Flyway','Cumple','Respaldo y permisos antes de migrar.'],['Swagger y SQL apagados','Cumple','Verificar por despliegue.'],['CORS explícito / mismo origen','Cumple','Definir dominios reales en FRONTEND_ORIGINS.'],['HTTPS público','No cumple','Configurar certificado y terminación TLS.'],['Build con budgets','Cumple','Mantener aviso 360 kB y error 1 MB.'],['Hash + caché e index revalidable','Cumple','Conservar chunks previos; política para imágenes/fuentes.'],['Health backend, Redis y Nginx','Parcial','Automatizar comprobaciones de backend y web.'],['Rotación de logs y requestId','Cumple','Acceso restringido y recolección central.'],['Release etiquetada y rollback','Parcial','Crear release y ensayar retorno; ya hay procedimiento.']],[175,65,255])
p('Rollback','Heading2');p('Guardar el JAR y build previo, commits y versión del esquema. Si una versión falla, detener su instancia y restaurar binarios/assets anteriores; reiniciar y repetir pruebas de humo. Conservar chunks mientras haya sesiones abiertas. Una migración destructiva exige su propio procedimiento: volver al JAR no revierte datos ni garantiza compatibilidad.')
p('Tres recomendaciones priorizadas','Heading2');p('<b>1. Alta:</b> dominio HTTPS único, identidades por empleado y API/Oracle/Redis en red privada o VPN.<br/><b>2. Alta:</b> backend cercano a Oracle, respaldos cifrados y restauración ensayada; plan ante caída de enlace de cada local. Evitar tres bases independientes sin sincronización definida.<br/><b>3. Media:</b> Redis lógico compartido entre instancias con TTL e invalidación; vigilar capacidad y alta disponibilidad para que no sea un punto único de fallo. Alertas por health, errores y latencia; Redis no sustituye Oracle.')

section('PARTE F · ENTREGA','Versionamiento, trazabilidad y referencias')
p('Se trabajó en feature/optimizacion-produccion-altamirano en ambos repositorios. Se separaron commits de tamaño, errores, correcciones y documentación, con la convención perf/feat/fix/docs. El informe versionado se incorpora como docs/informe-s13.pdf en backend; para el aula virtual se entrega Altamirano_LP2_S13_Autonoma.pdf.')
table([['Proyecto','Cambios identificables'],['Backend','58c57f5: corregir PUT de categorías y CORS. ffb76ff: requestId en errores y logs de venta. Commit adicional de documentación, scripts e informe.'],['Frontend','6cef0ca: layout diferido. 93706cb: código de seguimiento en errores. Commit adicional de README y despliegue simulado.']],[100,395])
p('Los PR hacia develop continúan abiertos: <link href="https://github.com/AlefAltamirano/PharmaSoft/pull/1" color="#168394">backend #1</link> y <link href="https://github.com/AlefAltamirano/PharmaSoft_Front-end/pull/1" color="#168394">frontend #1</link>. No se fusionaron. Los archivos de pruebas y evidencias evitan tokens y contraseñas. La subida al aula virtual debe realizarla el estudiante.')
p('Validación final','Heading2');p('50 pruebas backend y 18 frontend aprobadas; builds de producción sin errores de budgets. Se verificaron los seis casos de invalidación, cuatro estados de salud, métricas de ventas, propagación de requestId, TTL de 1 minuto y restauración a 10 minutos. Las cifras locales no son una garantía de latencia en producción.')
p('Referencias','Heading2')
for text,url in [('Guía autónoma oficial S13 (documento rector, partes A-F)','https://drive.google.com/file/d/1WOnrjXOwORhWy2vxQvQ9GRNe2PDmh-8F/view'),('Angular: Deferred loading with @defer','https://angular.dev/guide/templates/defer'),('esbuild: Bundle Size Analyzer','https://esbuild.github.io/analyze/'),('MDN: Cache-Control','https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control'),('Spring: Caching','https://docs.spring.io/spring-boot/reference/io/caching.html'),('Spring: Metrics','https://docs.spring.io/spring-boot/reference/actuator/metrics.html')]:p(f'<link href="{url}" color="#168394">{text}</link>','SmallBody')
p('Fuentes de resultados: salidas A/B de ng build, stats JSON, curl, eventos CDP, Redis CLI, respuestas Actuator y resultados de las pruebas. Se conservan en Anexos_Autonoma junto con las capturas por parte.','SmallBody')

for title,names in [('A · Evidencia de tamaño y análisis',['A-tamano.png','A-analyzer.png']),('B · Caché y despliegue',['B-politicas.png','B-productos-despues.png']),('C · Vigencia y stock',['CTTL-resultado.png','C-04-stock.png']),('D · Venta y seguimiento',['D-venta-SPA.png','D-error-SPA.png'])]:
 section('ANEXOS VISUALES',title)
 for name in names:p(name,'SmallBody');image(name,280)

pdf=e/'Altamirano_LP2_S13_Autonoma.pdf';SimpleDocTemplate(str(pdf),pagesize=A4,leftMargin=50,rightMargin=50,topMargin=52,bottomMargin=45,title='Actividad autónoma S13 - Informe completo A-F',author='Alef Altamirano').build(story,onFirstPage=footer,onLaterPages=footer)
print(pdf)
