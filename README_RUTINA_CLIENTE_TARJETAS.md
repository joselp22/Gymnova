# Rutina del cliente por fecha - tarjetas

## Flujo
1. El entrenador crea una rutina y agrega actividades indicando el dia semanal, series y repeticiones.
2. La rutina se guarda en `rutina`, `rutina_dia_entrenamiento` y `contiene_ejercicio`.
3. El entrenador asigna la rutina al cliente indicando `fecha_inicio` y `fecha_fin`.
4. En Cliente > Mi rutina, la fecha seleccionada se transforma al dia de semana correspondiente.
5. Solo se consultan las filas de `contiene_ejercicio` cuyo `dia_semana` coincide con esa fecha.
6. Cada actividad aparece como una tarjeta con nombre, series, repeticiones y boton de marcado.
7. El estado realizado se guarda en `progreso_rutina` para esa fecha concreta.

## Comportamiento de la vista
- Fecha jueves -> solo actividades configuradas para JUEVES.
- Fecha viernes -> solo actividades configuradas para VIERNES.
- Sin actividades para el dia -> se muestra Dia de descanso.
- Fecha futura -> las actividades aparecen como PROGRAMADAS y no se pueden marcar.
- Fecha actual o pasada valida -> boton `Marcar como hecho`.
- Actividad registrada -> `✓ HECHO`, con opcion de corregir/desmarcar.

## Base de datos
Esta correccion no agrega tablas ni columnas. Requiere la migracion de seguimiento de actividad ya entregada anteriormente para `progreso_rutina`.
