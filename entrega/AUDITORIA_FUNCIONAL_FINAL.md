# GYMNOVA - Auditoria funcional final

Fecha de verificacion: 18 de agosto de 2026.

## Resultado

El proyecto compila con JDK 17 y NetBeans 24. La validacion se ejecuto contra
`gymnova_auditoria`, restaurada desde el respaldo de `gymnova_db`. La base
principal no fue modificada durante las pruebas.

## Flujo compartido comprobado

1. Recepcionista crea la membresia, factura, pago y comprobante.
2. Administrador consulta inmediatamente esos registros.
3. Cliente consulta su membresia, pago y comprobante.
4. Entrenador crea rutina, asignacion y progreso para ese cliente.
5. Administrador y Cliente consultan el nuevo progreso.
6. Nutricionista crea un plan y sus comidas.
7. Administrador y Cliente consultan el nuevo plan nutricional.

Resultado automatizado:

`FLUJO_CINCO_ROLES_OK=RECEPCION>ADMIN>CLIENTE>ENTRENADOR>ADMIN>CLIENTE>NUTRICIONISTA>ADMIN>CLIENTE`

## Pruebas superadas

- 62 tablas y 67 llaves foraneas; cero `ON DELETE CASCADE`.
- Cedula, codigos y numeros de negocio protegidos contra modificacion.
- Codigos automaticos con prefijo y cinco digitos.
- Historial protegido por `NO ACTION` o `RESTRICT` con mensaje entendible.
- Cinco roles canonicos y rol desconocido denegado por defecto.
- Cinco usuarios de prueba autenticados con su rol correcto.
- Matriz allow/deny y navegacion de los cinco roles.
- 58 controladores consultados sin errores.
- 45 experiencias de rol construidas correctamente.
- 186 botones visibles con accion real o estado deshabilitado explicito.
- 13 rutas administrativas abren el panel correcto.
- Graficos y resumenes cargados desde PostgreSQL.
- Calendario real lunes-domingo para la rutina del Cliente.
- Configuracion de alias, clave, correo, telefono y avatar, con restauracion.
- Reportes filtrables, CSV e impresion.
- Factura PDF generada y validada.
- GYMNOVA Pulse consultado con datos reales.

## Correcciones principales

- Seguridad por nombres de roles y permisos, no por IDs visibles.
- `JPasswordField` para contraseña y confirmacion.
- Bitacora de solo lectura.
- Ayudas contextuales en campos, combos, botones y celdas de tablas.
- Planes comerciales limitados a Basica, Premium y Elite; promociones aparte.
- Recepcion muestra reservas y membresias operativas de todos los clientes.
- Entrenador y Nutricionista consultan el primer cliente realmente asignado,
  no su propio `idPersona` como si fueran clientes.
- Los paneles almacenados en memoria se reconstruyen al regresar al modulo,
  por lo que vuelven a consultar PostgreSQL.
- Alias y avatar actualizan inmediatamente la cabecera de la sesion.
- Los estados de desactivacion coinciden con los `CHECK` de PostgreSQL.
- Los errores `23503` y `23001` de integridad se traducen a mensajes claros.

## Datos de demostracion

No se ejecutó limpieza sobre `gymnova_db`. Antes de eliminar datos demo se
debe decidir cuales catalogos (ejercicios, alimentos, equipos, clases y
productos) son definitivos. La limpieza debe realizarse con un script
transaccional y un backup inmediatamente anterior.

