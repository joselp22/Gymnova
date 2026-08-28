# Logica funcional de GYMNOVA

## Recorrido principal para la demostracion

1. El administrador o recepcionista registra primero una `Persona`.
2. Esa persona se convierte en `Cliente`; `CL00001` se genera automaticamente.
3. Recepcion asigna uno de los tres planes: `Basica`, `Premium` o `Elite`.
4. Finanzas emite la factura, registra el pago y genera el comprobante/PDF.
5. Acceso permite reservas y asistencias solo con membresia vigente.
6. El entrenador crea una plantilla, agrega ejercicios y la asigna al cliente.
7. El entrenador registra evaluaciones, peso actual, medidas y progreso. Nunca cambia el peso inicial.
8. Nutricion asigna un plan, comidas y porciones, respetando los alergenos.
9. El cliente consulta su informacion, reserva, marca progreso y administra alias, avatar y contrasena.
10. Los reportes leen los mismos registros anteriores; no usan cifras independientes.

## Administrador

- Personas y clientes: altas, correcciones administrativas, activacion y consulta.
- Personal: crea empleados y sus perfiles profesionales.
- Seguridad: vincula persona, cuenta y uno de cinco roles; asigna permisos y consulta bitacora.
- Membresias: administra solo tres planes, sus beneficios, promociones, vigencias y congelaciones.
- Acceso: administra clases y horarios; consulta reservas y asistencias.
- Rutinas: administra catalogos y supervisa asignaciones/progreso.
- Salud y nutricion: supervisa catalogos e historiales; no reemplaza el trabajo profesional.
- Finanzas: factura, cobra, genera comprobantes y exporta documentos.
- Inventario: productos, proveedores, compras, equipos y mantenimientos.
- Reportes: genera informacion consolidada por periodo.
- Configuracion: cambia solo su alias, avatar y contrasena; el rol no se cambia aqui.

## Recepcionista

- Inicio: clientes nuevos del turno y resumen operativo.
- Personas/clientes: registra y actualiza datos administrativos.
- Membresias: asigna, renueva o congela usando los tres planes existentes; no crea planes.
- Acceso: registra reservas y asistencias; consulta clases y horarios.
- Finanzas: factura, cobra y genera comprobantes; no crea metodos de pago.
- No modifica rutinas, evaluaciones ni planes nutricionales.

## Entrenador

- Mis clientes: consulta exclusivamente clientes vinculados mediante rutinas asignadas.
- Rutinas: crea sus plantillas, agrega ejercicios, arma el calendario y asigna a sus clientes.
- Salud: registra evaluacion, peso actual, medidas, indicadores y recomendaciones.
- Progreso: registra series, repeticiones y cumplimiento; la barra se calcula de esos registros.
- No modifica cedula, codigo de cliente, peso inicial, membresia, pagos, rol ni plan nutricional.

## Nutricionista

- Mis clientes: consulta clientes vinculados mediante un plan nutricional propio.
- Nutricion: crea planes, comidas, porciones y revisiones para esos clientes.
- Salud: registra medidas e indicadores relevantes y recomendaciones nutricionales.
- Debe revisar alergenos antes de asociar alimentos.
- No modifica rutinas, membresias, pagos, identidad ni roles.

## Cliente

- Inicio/perfil: consulta sus propios datos y estado.
- Membresia: consulta plan, vigencia, beneficios y comprobantes; no modifica contratos.
- Reservas: crea o cancela sus reservas permitidas.
- Rutina: consulta el calendario real y registra solo su progreso.
- Salud/nutricion: consulta evaluaciones, evolucion y plan; no altera resultados profesionales.
- Configuracion: cambia alias, avatar y contrasena.

## Reglas que protegen la informacion

- Las llaves primarias y codigos automaticos son de solo lectura.
- Solo el administrador puede intentar una eliminacion definitiva.
- Si existe historial, PostgreSQL impide eliminar; se usa desactivacion.
- No se usa `ON DELETE CASCADE` en membresias, pagos, asistencias, evaluaciones o rutinas.
- Los profesionales solo consultan y modifican clientes dentro de su alcance.
- Una barra de progreso cambia por datos guardados, no por hacer clic sobre ella.
