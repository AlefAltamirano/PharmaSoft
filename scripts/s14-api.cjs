const fs=require('node:fs');const base=process.env.S14_API_URL||'http://localhost:8090/api/v1';
(async()=>{
 const results=[];async function call(name,path,method='GET',data,token,expected=200){const headers={'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{})};const r=await fetch(base+path,{method,headers,...(data?{body:JSON.stringify(data)}:{})});const raw=await r.text();const body=raw?JSON.parse(raw):null;results.push({caso:name,status:r.status,esperado:expected,requestId:r.headers.get('x-request-id'),correcto:r.status===expected});if(r.status!==expected)throw new Error(name+': '+r.status);return body;}
 const admin=(await call('Login admin','/auth/login','POST',{username:'admin',password:process.env.S14_ADMIN_PASSWORD})).token;
 const vendedor=(await call('Login vendedor','/auth/login','POST',{username:'vendedor',password:process.env.S14_VENDEDOR_PASSWORD})).token;
 const producto=(await call('Producto activo de prueba','/productos/buscar?texto=S14-CARGA','GET',null,admin))[0];
 const cliente=(await call('Cliente activo','/clientes/buscar','GET',null,admin))[0];
 await call('Página 20 — 200','/productos?tamanio=20','GET',null,admin);await call('Auditoría admin — 200','/auditoria','GET',null,admin);
 const venta=await call('Venta válida — 201','/ventas','POST',{clienteId:cliente.id,detalles:[{productoId:producto.id,cantidad:1}]},vendedor,201);
 await call('Venta sin detalles — 400','/ventas','POST',{clienteId:cliente.id,detalles:[]},vendedor,400);
 await call('Sin autenticación — 401','/productos','GET',null,null,401);await call('Auditoría vendedor — 403','/auditoria','GET',null,vendedor,403);
 await call('Producto inexistente — 404','/productos/999999999999','GET',null,admin,404);
 await call('Stock insuficiente — 409','/ventas','POST',{clienteId:cliente.id,detalles:[{productoId:producto.id,cantidad:999999999}]},vendedor,409);
 await call('Tamaño inválido — 409','/productos?tamanio=500','GET',null,admin,409);await call('Orden inválido — 409','/productos?ordenarPor=password','GET',null,admin,409);
 await call('Anulación de prueba — 200',`/ventas/${venta.id}/anular`,'PUT',null,vendedor);
 const stock=await call('Stock restituido','/productos/'+producto.id,'GET',null,admin);if(stock.stock!==producto.stock)throw new Error('Stock no restituido');
 fs.writeFileSync('docs/evidencias/s14-estabilizacion/pruebas-api.json',JSON.stringify({fecha:new Date().toISOString(),modalidad:'HTTP automatizado con Node; colección Postman importable equivalente',results,stockRestituido:true},null,2));console.log(`${results.length} peticiones correctas; stock restituido`);
})().catch(e=>{console.error(e);process.exit(1)});
