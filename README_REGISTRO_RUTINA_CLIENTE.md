# Registro diario de rutina del cliente

## Flujo implementado

Cliente -> Mi rutina -> se toma la fecha actual -> se obtiene LUNES/MARTES/... -> se muestran solo los ejercicios planificados para ese dia.

Cada fila muestra:

- orden
- ejercicio
- series
- repeticiones
- peso sugerido
- duracion
- descanso
- dia
- estado PENDIENTE/REALIZADA
- boton Marcar realizada / Deshacer

## Persistencia

Se mantiene el esquema de 35 tablas. No se crea `ejercicio_completado`.

La tabla `progreso_rutina` recibe dos columnas nuevas:

- `detalle_ejercicios JSONB`: objeto cuyas claves son `id_rutina_ejercicio` realizados y cuyo valor es la fecha/hora de marcacion.
- `fecha_hora_actualizacion TIMESTAMP`.

Ejemplo:

```json
{
  "31": "2026-08-27T14:30:02",
  "32": "2026-08-27T14:38:11"
}
```

`sesiones_planificadas` se sincroniza con la cantidad de ejercicios del dia y `sesiones_completadas` con la cantidad marcada.

## Migracion

Ejecutar una sola vez sobre `gymnova_db`:

`entrega/migracion_registro_actividad_rutina_cliente.sql`

No elimina tablas ni datos.

## Archivos principales

- `src/vista/PnlRutinaCliente.java`
- `src/controlador/RutinaProgresoControlador.java`
- `src/dao/ProgresoEjercicioDAO.java`
- `entrega/migracion_registro_actividad_rutina_cliente.sql`

`ConexionPostgreSQL.java` no fue modificada.
