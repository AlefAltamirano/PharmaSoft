const {chromium}=require('C:/Users/Asus/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const fs=require('fs');
(async()=>{
 const o='evidence/s13/autonoma/';const d=JSON.parse(fs.readFileSync(o+'resultados.json','utf8').replace(/^\uFEFF/,''));
 const items=[['A-tamano.png','Mejora adicional medida',{antes:'309,97 kB raw / 87,84 kB estimados',despues:'303,54 kB raw / 85,88 kB estimados',cambio:'MainLayout estático -> loadComponent',reduccionRaw:'6,43 kB (2,07%)'}],['B-politicas.png','Cache-Control y recarga',{recursos:d.Bheaders,origenes:'Detalle en B-origenes-DevTools.json'}],['Dauditoria-resultado.png','Auditoría de secretos',d.Dauditoria]];
 for(const r of d.Cmatrix)items.push([r.id+(r.id==='C-04'?'-stock':'-resultado')+'.png','Matriz '+r.id,r]);
 const b=await chromium.launch({channel:'msedge',headless:true});const p=await b.newPage({viewport:{width:1280,height:850}});
 for(const [file,title,v] of items){await p.setContent(`<meta charset="utf-8"><body style="font:20px Arial;padding:30px;color:#16364c"><h1>${title}</h1><p>Resultado real de la actividad autónoma S13</p><pre style="white-space:pre-wrap;font:17px Consolas;background:#edf5f7;padding:20px">${JSON.stringify(v,null,2).replaceAll('&','&amp;').replaceAll('<','&lt;')}</pre></body>`);await p.screenshot({path:o+file,fullPage:true});}
 await b.close();
})().catch(e=>{console.error(e);process.exitCode=1;});
