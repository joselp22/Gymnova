# Corrección de actualización de rutinas

Base revisada: backup PostgreSQL `backgym(1)`.

## Estructura confirmada

- `rutina`: cabecera de la rutina.
- `rutina_dia_entrenamiento`: días incluidos en la rutina.
- `contiene_ejercicio`: detalle de ejercicios.
- `ejercicio`: catálogo/actividades del entrenador.
- `asignacion_rutina`: asignación de la rutina al cliente.

## Restricciones relevantes de PostgreSQL

`contiene_ejercicio` exige:

- día válido (LUNES ... DOMINGO)
- orden > 0
- series > 0
- repeticiones no vacías
- peso >= 0 y tipo NUMERIC(6,2)
- duración NULL o > 0
- descanso NULL o >= 0
- orden único por `(id_rutina, dia_semana, orden)`
- `id_ejercicio` existente

## Ajustes realizados

1. `FlujoEntrenadorControlador` valida las reglas anteriores antes de abrir la transacción de actualización.
2. Duración 0 se normaliza a NULL para compatibilidad con datos/formularios anteriores.
3. Se valida peso máximo 9999.99 y máximo 2 decimales.
4. Se valida longitud de repeticiones (50) y nombre de rutina (150).
5. Los errores batch recorren `SQLException.getNextException()` para mostrar la causa real.
6. La actualización sigue siendo transaccional: si falla cualquier detalle se hace ROLLBACK y se conserva la rutina anterior.
7. Se agregó `entrega/migracion_integridad_rutinas.sql` para reforzar la relación entre los días de la rutina y los días usados por su detalle.

## Base de datos

Ejecutar `entrega/migracion_integridad_rutinas.sql` sobre la base existente. El script no elimina rutinas ni asignaciones.
