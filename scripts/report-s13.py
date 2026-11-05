from pathlib import Path
import json,re,shutil,subprocess
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,Image,PageBreak
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from xml.sax.saxutils import escape
b=Path(__file__).resolve().parents[1]; f=b.parent/'pharma-frontend'; e=b/'evidence/s13'
out=Path('C:/Users/Asus/Desktop/Semana 13');out.mkdir(parents=True,exist_ok=True)
results=json.loads((e/'resultados.json').read_text(encoding='utf-8-sig'))
baseline=json.loads((e/'categorias-antes.json').read_text(encoding='utf-8-sig'))
sql=json.loads((e/'sql-despues.json').read_text(encoding='utf-8-sig'))
styles=getSampleStyleSheet();styles['BodyText'].fontSize=10;styles['BodyText'].leading=15
story=[]
def p(s,style='BodyText'):story.append(Paragraph(s,styles[style]));story.append(Spacer(1,9))
def image(file,maxh=510):
 im=Image(str(file));scale=min(495/im.imageWidth,maxh/im.imageHeight);im.drawWidth=im.imageWidth*scale;im.drawHeight=im.imageHeight*scale;story.append(im)
def page(c,d):
 c.setStrokeColor(colors.HexColor('#207d93'));c.line(40,806,555,806);c.setFont('Helvetica',8);c.drawString(40,25,'LP II | Semana 13 | Alef Altamirano | Evidencias locales, 05/11/2026');c.drawRightString(555,25,str(d.page))
p('PharmaSoft','Title');p('Optimización y preparación para producción','Heading1');p('Actividad práctica - Sesión 13','Heading2');p('Estudiante: Alef Altamirano<br/>Curso: Lenguaje de Programación II<br/>Docente: Benjamin David Reyna Barreto<br/>Fecha de ejecución: 5 de noviembre de 2026')
p('Resultado','Heading2');p('Se implementaron carga diferida del reporte, separación de entornos, presupuestos, Redis con TTL e invalidación, paginación real de productos, logs correlacionados, Actuator protegido y despliegue local con Nginx y Docker Compose. Las 14 comprobaciones automatizadas finalizaron correctamente. El backend pasó 48 pruebas y el frontend 16.')
p('Repositorios y revisión','Heading2');p('Rama en ambos proyectos: feature/optimizacion-produccion-altamirano.<br/>Backend: <link href="https://github.com/AlefAltamirano/PharmaSoft/pull/1">PR #1 hacia develop</link>.<br/>Frontend: <link href="https://github.com/AlefAltamirano/PharmaSoft_Front-end/pull/1">PR #1 hacia develop</link>.<br/>Las solicitudes se dejan abiertas, sin fusionarlas.')
p('Método y límites','Heading2');p('Se midió la base corregida antes de cambiar código. Las capturas de la interfaz proceden de Edge automatizado. Las capturas tituladas Resultado muestran salidas de comprobaciones HTTP, Redis y logs reales; no se presentan como capturas de DevTools. El entorno es local y los tiempos individuales varían según calentamiento de la JVM y la caché.')
story.append(PageBreak());p('Comparación de evidencias','Heading1')
r6=next(r['detail'] for r in results if r['id']==6)
data=[['Indicador','Antes','Después'],['Initial total (raw)','298,13 kB','309,97 kB'],['Transferencia estimada','82,88 kB','87,84 kB'],['Lazy chunk files','16','17 (incluye mas-vendidos)'],['SQL de categorías: dos GET','2 consultas','1 consulta; segundo GET: 0'],['Segundo GET categorías (dev)',str(baseline[1]['duracionMs'])+' ms',str(sql['mediciones'][1]['duracionMs'])+' ms'],['Caché HTTP /productos','Primera visita: '+str(r6['primeraVisitaBytes'])+' B','Recarga: '+str(r6['recargaBytes'])+' B'],['Recursos desde caché','Sin métrica previa','17 en sesión de medición']]
t=Table([[Paragraph(escape(str(x)),styles['BodyText']) for x in row] for row in data],colWidths=[200,137,158]);t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dceef2')),('GRID',(0,0),(-1,-1),.4,colors.lightgrey),('VALIGN',(0,0),(-1,-1),'TOP'),('BOTTOMPADDING',(0,0),(-1,-1),10)]));story.append(t);story.append(Spacer(1,16))
p('Interpretación','Heading2');p('El paquete inicial creció 11,84 kB al añadir soporte de @defer; no se afirma una reducción del paquete inicial. El reporte queda separado en un chunk de 1,74 kB y no se descarga para el vendedor. La política sin precarga limita descargas anticipadas.')
p('Los bytes HTTP comparan la primera entrada a /productos después del login y una recarga normal de la misma versión en Nginx, con caché habilitada. No son una medición histórica de la versión anterior. SQL: la línea base contiene dos SELECT de categorías; en desarrollo posterior hay una SQL impresa dos veces por show-sql y DEBUG, no dos consultas reales.')
p('Redis utiliza TTL de 600 segundos; los aciertos y fallos se verifican en /actuator/metrics/cache.gets. Se adjunta la respuesta real en metricas-cache.json. El tiempo del segundo GET fue '+str(sql['mediciones'][1]['duracionMs'])+' ms frente a '+str(baseline[1]['duracionMs'])+' ms; una muestra no constituye un benchmark estadístico.')
story.append(PageBreak());p('Implementación y despliegue','Heading1')
for title,text in [
('Frontend','Rutas con loadChildren/loadComponent. Reporte standalone dentro de @if REPORTES_VER y @defer on viewport, con placeholder/loading/error. Entorno prod con /api/v1, outputHashing all y budget inicial de aviso 360 kB (aproximadamente base +20%).'),
('Datos','Redis para categorías y productos. DTO y categoría anidada serializables. Clave paginada incluye página, tamaño, orden, dirección y filtro de categoría. Escrituras limpian caché; actualizar categoría limpia productos. Registrar o anular venta limpia productos. No se cachean usuarios, tokens ni auth/me.'),
('Observabilidad','RequestIdFilter asigna X-Request-Id, MDC y duración de acceso. Logs de producción con rotación de 10 MB y 14 períodos. Health público sin detalles; detalles para ADMIN. Métricas requieren USUARIOS_ADMINISTRAR. Exposición limitada a health, info y metrics.'),
('Infraestructura','Nginx en localhost:8081 sirve dist/pharma-frontend/browser, comprime, revalida index.html y conserva JS/CSS con hash un año. Proxy /api/ hacia backend:8080 en el host; try_files permite F5. Redis se publica solo en 127.0.0.1:6379. Backend JAR con prod y secretos por variables de entorno.'),
('Datos de prueba','Se crearon categorías temporales y se eliminaron. Se registraron ventas de prueba y se anularon para restituir stock; los registros anulados quedan como evidencia en el sistema. Se reinició Redis al terminar la prueba de caída.')]:p(title,'Heading2');p(text)
for r in results:
 story.append(PageBreak());p(f"Prueba {r['id']:02d} - {r['title']}",'Heading1');p('Resultado: '+('APROBADA' if r['ok'] else 'REVISAR'))
 img=e/(f"{r['id']:02d}-resultado.png")
 if r['id']==2:img=e/'02-vendedor.png'
 if r['id']==3:img=e/'03-admin.png'
 if r['id']==6:img=e/'06-productos.png'
 image(img,560)
 if r['id']==7:p('Complemento SQL: sql-despues.log registra la única ejecución real; la segunda petición se resuelve desde Redis.')
story.append(PageBreak());p('Reproducción y entrega','Heading1')
p('1. Frontend: npm run build -- --stats-json; docker compose -f deploy/docker-compose.yml up -d.<br/>2. Backend: Java 21, ./mvnw.cmd package. Configurar SPRING_PROFILES_ACTIVE=prod, DB_URL, DB_USERNAME, DB_PASSWORD y JWT_SECRET. REDIS_HOST=localhost; ejecutar el JAR.<br/>3. Abrir http://localhost:8081. Actuator se consulta en http://localhost:8080/actuator/health.<br/>4. Pruebas: npm test -- --watch=false y ./mvnw.cmd test (requiere Oracle y Redis para contexto).<br/>5. La carpeta evidencias conserva resultados JSON, logs, builds y capturas. Los scripts de comprobación se adjuntan; verificar datos de prueba antes de repetirlos.')
p('Control de versiones','Heading2');p('Backend: 3e77b59 (caché), a4fac65 (logs), aba84db (monitoreo).<br/>Frontend: e4cf2b4 (reporte), 17184a8 (entornos), 5a6608b (despliegue).<br/>develop del backend se creó desde add563d con autorización del propietario; los cambios previos no se perdieron.')
p('Recomendaciones','Heading2');p('Antes de exponerlo públicamente: habilitar HTTPS, respaldos y restauración de Oracle, restringir puertos internos, gestionar secretos fuera de Git, verificar health y probar rollback. No reutilizar las credenciales de demostración. No se requiere hosting público para el despliegue local de esta práctica.')
p('Fuente','Heading2');p('<link href="https://drive.google.com/file/d/1gh6az79IVvs1nCOfNK2OT2qp6LkwHdzB/view">Guía práctica de la sesión 13, páginas 3-16</link>. Se implementa el reto práctico; la actividad autónoma es una revisión posterior adicional.')
pdf=out/'Altamirano_LP2_S13_Practica.pdf';SimpleDocTemplate(str(pdf),pagesize=A4,rightMargin=40,leftMargin=40,topMargin=50,bottomMargin=45).build(story,onFirstPage=page,onLaterPages=page)
dest=out/'evidencias';dest.mkdir(exist_ok=True)
for src in [e,f/'evidence/s13']:
 for item in src.iterdir():
  if item.is_file() and item.suffix in ['.json','.txt','.png','.log']:shutil.copy2(item,dest/item.name)
shutil.copytree(f/'deploy',out/'deploy',dirs_exist_ok=True)
shutil.copy2(b/'scripts/verify-s13.cjs',out/'verificar-s13.cjs')
writeup='''SEMANA 13 - ACTIVIDAD PRACTICA\n\nInforme: Altamirano_LP2_S13_Practica.pdf\nEvidencias: capturas reales de interfaz, paneles de resultados HTTP/Redis/logs y salidas originales.\nBackend: https://github.com/AlefAltamirano/PharmaSoft/pull/1\nFrontend: https://github.com/AlefAltamirano/PharmaSoft_Front-end/pull/1\nAmbas ramas: feature/optimizacion-produccion-altamirano\n48 pruebas backend, 16 frontend y 14 comprobaciones aprobadas.\nLos PR permanecen abiertos, sin merge.\nEl backend prod y los contenedores locales quedan disponibles.\nNo se incluyen secretos de produccion.\n'''
(out/'LEEME.txt').write_text(writeup,encoding='utf-8')
print(pdf)
