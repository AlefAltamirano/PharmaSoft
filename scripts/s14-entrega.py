from pathlib import Path
import shutil,json,hashlib,re,base64,zipfile,io
r=Path(__file__).resolve().parents[1];f=r.parent/'pharma-frontend';dest=Path('C:/Users/Asus/Desktop/Semana 14')
dest.mkdir(exist_ok=True)
for p in (r/'output/pdf').glob('*S14*.pdf'):shutil.copy2(p,dest/p.name);shutil.copy2(p,r/'docs'/p.name)
docs=dest/'Documentacion';docs.mkdir(exist_ok=True)
for p in (r/'docs').iterdir():
 if p.name in ['informe-s13.pdf','BASE_PREVIA_S13.md','SESION_13.md'] or p.suffix=='.pdf':continue
 if p.is_dir():shutil.copytree(p,docs/p.name,dirs_exist_ok=True)
 else:shutil.copy2(p,docs/p.name)
videos=dest/'Videos';videos.mkdir(exist_ok=True)
for p in (f/'evidence/s14/demo').glob('demostracion-ensayo-*.webm'):shutil.copy2(p,videos/p.name)
(videos/'README.md').write_text('''# Videos de respaldo
Abrir demostracion-ensayo-1.webm o demostracion-ensayo-2.webm con Edge, Chrome o VLC. Son recorridos reales automatizados con subtítulos, sin narración oral; no sustituyen el ensayo personal del estudiante. Los dos duran menos de diez minutos y sus tiempos exactos están en Documentacion/evidencias/s14-estabilizacion/ensayos.json.
''',encoding='utf-8')
report=f/'playwright-report';raw=(report/'index.html').read_text(encoding='utf-8')
secret=re.compile(r'(?:Admin|Vendedor|Almacen)2026\*|eyJ[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+')
def limpiar(text):return secret.sub('[REDACTADO]',text)
def limpiar_zip(match):
 result=io.BytesIO()
 with zipfile.ZipFile(io.BytesIO(base64.b64decode(match.group(1)))) as original,zipfile.ZipFile(result,'w',zipfile.ZIP_DEFLATED) as nuevo:
  for name in original.namelist():
   data=original.read(name)
   if name.endswith(('.json','.txt','.js','.html')):data=limpiar(data.decode('utf-8')).encode('utf-8')
   nuevo.writestr(name,data)
 return 'data:application/zip;base64,'+base64.b64encode(result.getvalue()).decode('ascii')
raw=re.sub(r'data:application/zip;base64,([A-Za-z0-9+/=]+)',limpiar_zip,raw)
raw=limpiar(raw)
# Revisar tanto texto directo como el ZIP embebido del reporte HTML.
texts=[raw]
for match in re.finditer(r'data:application/zip;base64,([A-Za-z0-9+/=]+)',raw):
 with zipfile.ZipFile(io.BytesIO(base64.b64decode(match.group(1)))) as z:
  for name in z.namelist():
   if name.endswith(('.json','.txt','.js','.html')):texts.append(z.read(name).decode('utf-8',errors='replace'))
for text in texts:
 if secret.search(text):raise RuntimeError('Se encontró un posible secreto en el reporte de pruebas; no se copia')
if list(report.rglob('*.zip')):raise RuntimeError('El reporte contiene trazas ZIP que requieren revisión antes de entregar')
pruebas=dest/'Pruebas';pruebas.mkdir(exist_ok=True);shutil.copytree(report,pruebas/'playwright-report',dirs_exist_ok=True)
(pruebas/'playwright-report/index.html').write_text(raw,encoding='utf-8')
for target in (pruebas/'playwright-report').rglob('*'):
 if target.suffix in ['.json','.txt','.js']:
  target.write_text(limpiar(target.read_text(encoding='utf-8')),encoding='utf-8')
for source,name in [(f/'evidence/s14/e2e-triple-final.txt','E2E-54-correctas.txt'),(f/'evidence/s14/final-unit-tests.txt','Frontend-18-correctas.txt'),(r/'evidence/s14/final-backend-tests.txt','Backend-55-correctas.txt'),(f/'evidence/s14/npm-audit-final.json','npm-audit-final.json')]:shutil.copy2(source,pruebas/name)
readme='''# Entrega Semana 14 — Alef Altamirano

- Altamirano_LP2_S14_Practica.pdf: informe práctico con evidencias.
- Altamirano_LP2_S14_Autonoma.pdf: informe autónomo, documentación y defensa.
- Documentacion/README.md: índice técnico y enlaces de repositorios/PR.
- Pruebas/playwright-report/index.html: reporte navegable de 54 ejecuciones E2E correctas.
- Videos/: dos recorridos reales automatizados de respaldo.

Aplicación local de Semana 14: http://localhost:8081 (backend 8090). Los procesos deben seguir activos. Instrucciones de reinicio: Documentacion/ejecucion-s14.md.

Pendiente personal: practicar dos veces el guion con narración, comprobar el checklist antes de exponer y subir los entregables al aula virtual. Los videos automatizados no se presentan como ensayos orales humanos.

Respaldo_privado contiene la copia Data Pump de Oracle: conservarla de forma privada. Se excluye del ZIP publicable, de los repositorios y de los informes. No compartirla como evidencia académica.
'''
(dest/'README.md').write_text(readme,encoding='utf-8')
(dest/'ABRIR-ENTREGA.html').write_text('''<!doctype html><html lang="es"><meta charset="utf-8"><title>PharmaSoft · Semana 14</title><style>body{font:18px system-ui;max-width:900px;margin:60px auto;padding:24px;color:#18385c}li{margin:18px 0}a{color:#075985}p{line-height:1.6}</style><h1>PharmaSoft · Semana 14</h1><p>Alef Altamirano · Práctica y actividad autónoma</p><ul><li><a href="Altamirano_LP2_S14_Practica.pdf">Informe de práctica</a></li><li><a href="Altamirano_LP2_S14_Autonoma.pdf">Informe autónomo</a></li><li><a href="Pruebas/playwright-report/index.html">Reporte de 54 pruebas E2E correctas</a></li><li><a href="Videos/demostracion-ensayo-1.webm">Video de respaldo 1</a> · <a href="Videos/demostracion-ensayo-2.webm">Video de respaldo 2</a></li><li><a href="Documentacion/README.md">Documentación técnica</a></li><li><a href="https://github.com/AlefAltamirano/PharmaSoft/pull/2">PR backend</a> · <a href="https://github.com/AlefAltamirano/PharmaSoft_Front-end/pull/2">PR frontend</a></li><li><a href="http://localhost:8081">Abrir aplicación local</a></li></ul><p>Los videos son recorridos automatizados sin narración. Practica el guion oral y realiza la subida al aula. El respaldo privado queda fuera del paquete publicable.</p></html>''',encoding='utf-8')
selected=[dest/'Altamirano_LP2_S14_Practica.pdf',dest/'Altamirano_LP2_S14_Autonoma.pdf',dest/'README.md',dest/'ABRIR-ENTREGA.html',docs,pruebas,videos]
files=[]
for p in selected:files.extend(p.rglob('*') if p.is_dir() else [p])
files=[p for p in files if p.is_file()]
manifest={str(p.relative_to(dest)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
(dest/'SHA256-entrega.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False),encoding='utf-8');files.append(dest/'SHA256-entrega.json')
with zipfile.ZipFile(dest/'Altamirano_LP2_S14_Entregables.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in files:z.write(p,str(p.relative_to(dest)))
with zipfile.ZipFile(dest/'Altamirano_LP2_S14_Entregables.zip') as z:
 assert not any('Respaldo_privado' in n or n.endswith('.dmp') for n in z.namelist())
print('Entrega copiada:',dest,'archivos publicables:',len(files),'ZIP bytes:',(dest/'Altamirano_LP2_S14_Entregables.zip').stat().st_size)
