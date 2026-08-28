# Corrección de creación/edición de rutinas

## Error corregido
PostgreSQL rechazaba la actualización con `ck_contiene_ejercicio_duracion` porque `duracion_minutos` admite únicamente `NULL` o valores mayores que cero.

La causa raíz estaba en `ContieneEjercicioDAO`: `ResultSet.getInt("duracion_minutos")` convertía un `NULL` de PostgreSQL en `0`. Al volver a editar y guardar la rutina, ese `0` se intentaba insertar y violaba el CHECK.

## Cambios
- `ContieneEjercicioDAO` conserva `NULL` usando `getObject` y escribe correctamente campos opcionales.
- Duración `0` en formularios se interpreta como "sin duración" y se persiste como `NULL`.
- `FlujoEntrenadorControlador` normaliza la duración antes de crear/actualizar una rutina completa.
- Mensajes de error de restricciones de detalle más claros.
- `DlgRutinaEntrenador` inicia más grande y se adapta al área útil de la pantalla.
- La tabla del detalle tiene más espacio vertical/horizontal.
- `DlgEditarDetalleRutina` aplica la misma regla de duración para mantener compatibilidad.
- `ConexionPostgreSQL.java` no fue modificada.

## Validación
Ejecutado: `ant clean jar`
Resultado: `BUILD SUCCESSFUL` con 194 archivos fuente compilados.

No requiere migración SQL nueva para esta corrección.
