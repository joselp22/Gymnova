# Guia de funcionamiento por rol

Todos los roles trabajan sobre la misma base `gymnova_db`. No son cinco
aplicaciones diferentes. Para ver datos recién creados, vuelva al modulo o
presione su accion de actualizar; el panel vuelve a consultar PostgreSQL.

## Administrador

Administra personas, clientes, personal, seguridad, catalogos, membresias,
acceso, rutinas, salud, nutricion, finanzas e inventario. Es el unico rol que
puede solicitar eliminacion definitiva. Si existe historial, PostgreSQL la
rechaza y el sistema propone desactivar.

## Recepcionista

Atiende el ingreso diario: crea personas/clientes, membresias, reservas,
asistencias, facturas, pagos y comprobantes. No administra seguridad,
inventario ni datos profesionales. Sus paneles muestran clientes nuevos,
reservas recientes, renovaciones y caja real.

## Entrenador

Ve solamente clientes asignados mediante una rutina propia. Puede crear y
modificar plantillas, ejercicios, calendario semanal, asignaciones,
evaluaciones, mediciones y progreso. No puede cambiar cedula, identidad,
membresia ni pagos del cliente.

## Nutricionista

Ve clientes que tengan un plan asignado por ese profesional. Puede administrar
planes, comidas, porciones, alimentos, alergenos, mediciones, indicadores y
recomendaciones. No puede cambiar identidad, membresia, rutina ni finanzas.

## Cliente

Consulta solo sus datos: perfil, membresia aunque este inactiva, reservas,
rutina por fecha real, progreso, nutricion, historial y comprobantes. Puede
crear/cancelar sus reservas, registrar su avance permitido y cambiar alias,
contraseña, correo, telefono y avatar. No puede cambiar cedula, nombre, rol,
estado ni datos de otra persona.

## Credenciales de demostracion

La contraseña común de la base entregada es `Gymnova2026*`.

- `admin` - Administrador
- `RECEPCION` - Recepcionista
- `ENTRENADOR` - Entrenador
- `NUTRICION` - Nutricionista
- `cliente` - Cliente

Estas credenciales son exclusivamente para la exposicion. Deben reemplazarse
antes de utilizar el sistema con informacion real.

