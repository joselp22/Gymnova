# GYMNOVA - Asignación directa y múltiples rutinas por cliente

## Entrenador > Asignar rutina
- Se eliminaron Fecha inicio y Fecha fin de la interfaz.
- La asignación es directa Cliente <-> Rutina.
- Una nueva asignación no reemplaza las rutinas activas anteriores.
- Se agregó una tabla con: ID, Cliente, Rutina, Días, Estado.
- Internamente PostgreSQL conserva fecha_asignacion/fecha_inicio con CURRENT_DATE por compatibilidad del esquema; fecha_fin queda NULL.

## Cliente > Mi rutina
- Se agregó selector de Rutina.
- El cliente puede elegir cualquiera de sus rutinas ACTIVAS.
- La Fecha únicamente sirve para resolver el día semanal de la rutina elegida.
- Ejemplo: fecha lunes -> actividades LUNES de la rutina seleccionada.
- Marcar como hecho valida específicamente la asignación y la rutina seleccionadas.

## Base de datos
No requiere una nueva migración si ya fue aplicada la migración corregida anterior del flujo de rutinas.
No se eliminan las columnas fecha_inicio/fecha_fin porque forman parte del esquema existente; simplemente dejan de controlar la selección de rutina del cliente en este nuevo flujo.
