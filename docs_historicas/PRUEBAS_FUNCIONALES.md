# Pruebas funcionales de GYMNOVA

## Preparacion

1. Abrir PostgreSQL y comprobar que `gymnova_db` este disponible.
2. Ejecutar una sola vez `sql/04_datos_prueba_funcionales.sql` con `psql`.
   Es idempotente: puede repetirse sin duplicar los datos.
3. Ejecutar `sql/05_verificacion_funcional_roles.sql`. No modifica datos.
4. Ejecutar `sql/06_facturacion_e_innovacion.sql` para cuadrar los pagos
   demostrativos y `sql/07_pruebas_integridad.sql` para verificarlos.
5. En NetBeans usar **Clean and Build** y luego **Run Project**.

## Alcance esperado

- **Administrador:** consulta y administra todos los modulos. Solo este rol
  puede intentar la eliminacion definitiva; PostgreSQL la rechaza cuando hay
  historial relacionado.
- **Recepcionista:** personas, clientes, membresias, reservas, asistencias,
  pagos y comprobantes. No administra rutinas ni planes nutricionales.
- **Entrenador:** ve clientes relacionados con sus rutinas; crea/modifica sus
  rutinas, asignaciones, ejercicios, avances, evaluaciones, mediciones y
  recomendaciones. No puede modificar rutinas de otro entrenador.
- **Nutricionista:** ve clientes relacionados con sus planes; crea/modifica
  planes, alimentos, porciones, mediciones y recomendaciones. No puede
  modificar planes de otro nutricionista.
- **Cliente:** consulta exclusivamente su informacion y puede gestionar sus
  reservas y registrar su progreso. No puede cambiar identificadores ni datos
  de otro cliente.

## Casos que debe probar la profesora

1. Entrar con cada rol y confirmar que el menu cambia.
2. Abrir un submodulo y comprobar que los selectores muestran nombres, no IDs.
3. Entrenador: modificar una rutina propia y registrar avance/evaluacion.
4. Nutricionista: modificar un plan propio y sus alimentos.
5. Recepcionista: registrar reserva/asistencia/pago de un cliente.
6. Cliente: cambiar de dia en el calendario semanal y consultar ejercicios.
7. Reportes: elegir entidad, generar, previsualizar, exportar CSV e imprimir.
8. Configuracion: cambiar preferencia o contrasena de la sesion.
9. Administrador: intentar eliminar un cliente con historial. Debe ser
   rechazado por las llaves foraneas `NO ACTION`, preservando el historial.
10. Clientes: revisar la columna **GYMNOVA Pulse** y dejar el cursor sobre el
    indicador para leer las causas del riesgo calculado.
11. Finanzas: elegir Facturas, seleccionar una fila y hacer doble clic para
    vista previa; con clic derecho exportar o imprimir el PDF.

## Pruebas automaticas incluidas

- `pruebas.PruebaControladoresLectura`: consulta todos los controladores CRUD.
- `pruebas.PruebaAlcanceRoles`: muestra cuantos registros reales puede ver
  cliente, entrenador y nutricionista.
- `PruebaConstruccionVistas`: construye vistas y experiencias por rol.
- `PruebaPulse`: valida rango, nivel y explicacion del indicador preventivo.
- `PruebaFacturaPDF`: consulta PostgreSQL y genera un PDF A4 real.
- `PruebaNumeroFactura`: prueba consecutivo transaccional y limpia su dato.

## Calculo innovador GYMNOVA Pulse

Pulse suma factores explicables y limita el resultado a 100 puntos:

- Hasta 35 por inactividad de asistencia.
- Hasta 25 por membresia inactiva o proxima a vencer.
- Hasta 25 por baja adherencia a sesiones planificadas.
- 15 por no tener rutina activa.
- 10 por cancelaciones frecuentes.

Clasificacion: `ESTABLE 0-39`, `ATENCION 40-69`, `CRITICO 70-100`.
