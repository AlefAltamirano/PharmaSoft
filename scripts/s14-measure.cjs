const fs=require('node:fs');
const api=process.env.S14_API_URL||'http://localhost:8090/api/v1';
(async()=>{
 if(!process.env.S14_ADMIN_PASSWORD)throw new Error('Falta S14_ADMIN_PASSWORD');
 let r=await fetch(api+'/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'admin',password:process.env.S14_ADMIN_PASSWORD})});if(!r.ok)throw new Error('Login '+r.status);
 const {token}=await r.json(),headers={Authorization:'Bearer '+token};const medidas=[];
 async function medir(nombre,path){let t=performance.now();const r=await fetch(api+path,{headers});const raw=await r.text();let body=JSON.parse(raw);medidas.push({nombre,status:r.status,ms:+(performance.now()-t).toFixed(2),bytes:Buffer.byteLength(raw),filas:body.contenido?.length,totalElementos:body.totalElementos,totalPaginas:body.totalPaginas,requestId:r.headers.get('x-request-id')});return body;}
 const p=await medir('Productos primera página','/productos?pagina=0&tamanio=20');await medir('Productos última página',`/productos?pagina=${p.totalPaginas-1}&tamanio=20`);await medir('Productos primera página repetida','/productos?pagina=0&tamanio=20');const v=await medir('Ventas primera página','/ventas/buscar?pagina=0&tamanio=20');await medir('Ventas última página',`/ventas/buscar?pagina=${v.totalPaginas-1}&tamanio=20`);await medir('Tamaño rechazado','/productos?tamanio=500');await medir('Orden rechazado','/productos?ordenarPor=password');
 fs.writeFileSync('evidence/s14/mediciones.json',JSON.stringify({fecha:new Date().toISOString(),entorno:'Oracle/Redis locales; datos sintéticos de desarrollo; sin concurrencia',medidas},null,2));console.log(JSON.stringify(medidas,null,2));
})();
