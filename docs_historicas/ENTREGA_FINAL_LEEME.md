# GYMNOVA - entrega funcional

## Ejecucion

1. Iniciar PostgreSQL y comprobar que existe `gymnova_db`.
2. Abrir esta carpeta como proyecto en NetBeans 24 con JDK 17.
3. Ejecutar **Clean and Build**.
4. Ejecutar `app.GymnovaApp`.

La conexion local configurada es `localhost:5432`, base `gymnova_db`, usuario
`postgres`. Si cambia la clave local, modificar unicamente
`src/conexion/ConexionPostgreSQL.java`.

## Cuentas de demostracion

Todas usan la clave `Gymnova2026*`:

| Usuario | Rol | Alcance |
|---|---|---|
| `admin` | Administrador | Gestion completa |
| `RECEPCION` | Recepcionista | Personas nuevas, membresias, acceso y cobros |
| `ENTRENADOR` | Entrenador | Sus clientes, rutinas, progreso y evaluaciones |
| `NUTRICION` | Nutricionista | Sus clientes, planes, comidas y seguimiento |
| `cliente` | Cliente | Su perfil, membresia, reservas, rutina y progreso |

La cuenta personal `jose` no fue modificada. El script reproducible es
`sql/11_credenciales_demostracion.sql`.

## Logica principal

- Los codigos de negocio son automaticos y no editables: `CL`, `MB`, `RS`,
  `PG`, `PN`, `EV`, `CP`, `MT`, `CO` y otros prefijos definidos en los DAO.
- Las llaves primarias, cedula y codigos permanecen protegidos al modificar.
- Solo el administrador puede intentar eliminar definitivamente. PostgreSQL
  impide borrar una persona o cliente con historial.
- Recepcion no ve seguimientos deportivos: registra personas/clientes,
  membresias, reservas, asistencias, facturas, pagos y comprobantes.
- El entrenador modifica exclusivamente rutinas, evaluaciones, mediciones y
  progreso de clientes que tiene asignados.
- El nutricionista modifica exclusivamente planes, alimentos, porciones y
  recomendaciones de clientes vinculados a sus planes.
- El cliente solo crea o modifica reservas y sus registros de progreso; su
  configuracion permite alias, avatar y contrasena.
- Los planes comerciales activos son exactamente Basica, Premium y Elite.
- Las tarjetas, barras y graficas muestran consultas reales. Sin registros se
  presenta `Sin datos`; no se fabrican resultados.

## Flujo recomendado para la exposicion

1. Administrador: crear persona, convertirla en cliente y asignar usuario/rol.
2. Recepcionista: contratar una membresia, emitir factura, registrar pago y
   generar comprobante.
3. Entrenador: crear/asignar rutina, registrar evaluacion, medidas y progreso.
4. Nutricionista: crear plan, agregar comidas y porciones.
5. Cliente: revisar vigencia, reserva, calendario semanal, progreso y plan.
6. Administrador: abrir reportes, exportar CSV/imprimir y exportar factura PDF.

## Pruebas incluidas

- `pruebas.PruebaControladoresLectura`: lectura de los 58 controladores CRUD.
- `pruebas.PruebaCatalogosRoles`: listas relacionadas por rol.
- `pruebas.PruebaAlcanceRoles`: aislamiento de clientes por profesional.
- `pruebas.PruebaResumenModulos`: datos reales de todas las tarjetas.
- `pruebas.PruebaVistasRoles`: construye 45 vistas rol/modulo.
- `pruebas.PruebaLoginRoles`: autentica las cinco cuentas demo.
- `pruebas.PruebaFacturaPDFIntegrada`: genera y valida un PDF real.
- `pruebas.PruebaPulseIntegrada`: valida el indicador innovador Pulse.
- `sql/07_pruebas_integridad.sql`: historial, cedula, cascadas y saldos.
- `sql/10_prueba_codigos_automaticos.sql`: prueba con `ROLLBACK` los codigos.

La explicacion detallada de responsabilidades se encuentra en
`GUIA_LOGICA_GYMNOVA.md`.
