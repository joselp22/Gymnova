# Corrección de días de rutina y vista del cliente

## Regla de funcionamiento
La rutina es una plantilla semanal. Cada fila de `contiene_ejercicio` guarda el `dia_semana` elegido al crear/editar la rutina. La asignación (`asignacion_rutina`) enlaza la rutina completa con el cliente y define el periodo de vigencia.

Cuando el cliente consulta una fecha concreta, el sistema:
1. Busca la asignación ACTIVA válida para esa fecha.
2. Calcula el día de semana de la fecha (LUNES, MARTES, etc.).
3. Consulta `contiene_ejercicio` por `id_rutina` + `dia_semana`.
4. Muestra cada actividad como tarjeta con series, repeticiones y botón "Marcar como hecho".
5. Aplica las marcas de `progreso_rutina` después de cargar las actividades.

## Mejoras incluidas
- Las actividades se muestran aunque la migración de progreso aún no se haya ejecutado.
- En Asignar rutina se muestran los días que realmente tienen actividades.
- Si la fecha de inicio cae en un día sin actividades se solicita confirmación.
- En Mi rutina se muestran los días programados y un botón "Próxima actividad".
- El selector de fecha determina automáticamente qué día de la plantilla se consulta.

## Importante
Para usar "Marcar como hecho" debe estar aplicada la migración `entrega/migracion_registro_actividad_rutina_cliente.sql` sobre la base existente.
