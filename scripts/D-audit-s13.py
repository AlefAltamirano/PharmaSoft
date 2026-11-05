from pathlib import Path
import json,re
p=Path('evidence/s13/autonoma/resultados.json')
d=json.loads(p.read_text(encoding='utf-8'))
logs=Path('logs/pharma-backend.log').read_text(encoding='utf-8')
without_ids=re.sub(r'\[[0-9a-f]{8}\]','[requestId]',logs)
d['Dauditoria']={
 'lineas':len(logs.splitlines()),
 'patronesPasswordBearerSecret':len(re.findall(r'password|Bearer |secret',logs,re.I)),
 'tokensJWT':len(re.findall(r'eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+',logs)),
 'dniFueraDeRequestId':len(re.findall(r'\b\d{8}\b',without_ids)),
 'candidatosNumericosRevisados':len(re.findall(r'\b\d{8}\b',logs)),
 'nota':'Las secuencias de ocho dígitos eran requestId numéricos entre corchetes. No se exportan DNI ni claves.'}
p.write_text(json.dumps(d,ensure_ascii=False,indent=2),encoding='utf-8')
print(d['Dauditoria'])
