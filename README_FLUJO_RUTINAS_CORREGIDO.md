# GYMNOVA - Flujo de rutinas corregido

## Flujo final

1. El entrenador crea actividades en **Mis actividades**.
2. En **Crear rutina** agrega cada actividad a un día concreto (LUNES..DOMINGO), con orden, series, repeticiones, peso, duración y descanso.
3. `rutina`, `rutina_dia_entrenamiento` y `contiene_ejercicio` se guardan en una sola transacción.
4. Al editar la rutina se conservan los `id_rutina_ejercicio` existentes; ya no se borra y recrea todo el detalle.
5. En **Asignar rutina**, el entrenador selecciona cliente, rutina, fecha inicio y fecha fin. La fecha fin sugerida usa la duración real inclusiva (`semanas * 7` días).
6. Una asignación futura queda **PROGRAMADA**. Una asignación de hoy queda **ACTIVA**.
7. Si la nueva planificación se superpone con otra, solo se recorta/cancela el tramo solapado; no se destruye el historial.
8. En **Cliente > Mi rutina**, la fecha mostrada se transforma al día de la semana y se consultan las filas de `contiene_ejercicio` de ese día.
9. Si hoy es descanso, al primer ingreso se salta automáticamente a la siguiente fecha con actividad (hasta 60 días).
10. Cada actividad se presenta como tarjeta con series/repeticiones y botón **Marcar como hecho**.
11. El progreso se registra en `progreso_rutina.detalle_ejercicios` usando el `id_rutina_ejercicio` estable.
12. Una rutina FINALIZADA continúa visible como historial, pero en modo solo lectura.

## Migración requerida

Ejecutar una vez:

`entrega/migracion_flujo_rutinas_completo.sql`

La migración:
- agrega `PROGRAMADA` a los estados de asignación;
- permite reutilizar una rutina en otro periodo para el mismo cliente;
- agrega/normaliza `detalle_ejercicios` y `fecha_hora_actualizacion` en `progreso_rutina`;
- asegura la FK entre día de rutina y detalle;
- crea índices para las consultas por periodo y progreso.

## Verificación

Usar `entrega/verificar_flujo_rutina_cliente.sql` reemplazando `:ID_CLIENTE`.
