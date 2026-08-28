# Calendario y seguimiento por fecha - GYMNOVA

## Cambio funcional

Se incorporo `utilidades.CalendarioSelector`, un selector visual reutilizable de
fecha y fecha/hora. Los campos compatibles dejan de exigir escribir fechas a
mano y abren un calendario al hacer clic.

Formatos que siguen usando internamente las capas existentes:

- Fecha: `yyyy-MM-dd`
- Fecha y hora: `yyyy-MM-dd HH:mm`

## Rutinas

La rutina sigue siendo una plantilla semanal (LUNES, MARTES, etc.), pero su
asignacion a un cliente ahora se realiza con fechas reales seleccionadas en
calendario:

- Fecha de inicio.
- Fecha de fin.

Al seleccionar una rutina, la fecha de fin se sugiere a partir de la duracion en
semanas de la plantilla.

## Vista del cliente

`Cliente -> Mi rutina` incluye un selector de fecha con calendario y botones
anterior / hoy / siguiente.

Para cada fecha se valida:

1. Que exista una asignacion ACTIVA cuyo periodo incluya la fecha seleccionada.
2. El dia real de semana de esa fecha.
3. Los ejercicios de la plantilla que corresponden a ese dia.
4. El progreso registrado en `progreso_rutina` para esa fecha exacta.

El cliente puede consultar fechas futuras, pero no marcarlas como realizadas.
Las fechas de hoy y anteriores dentro de la asignacion pueden registrar o
corregir actividades.

## Formularios con selector visual

El selector se integro en los campos de fecha mas usados:

- Personas: fecha de nacimiento.
- Clientes: fecha de registro.
- Personal: fecha de ingreso.
- Membresias y congelaciones.
- Reportes: desde / hasta.
- Entrenador: fechas de asignacion de rutina.
- Administrador: fecha y hora de clases grupales.
- Formularios directos de Acceso, Rutinas, Salud, Nutricion y Finanzas cuando
  el campo activo representa fecha o fecha/hora.
- Congelacion rapida de membresia desde `FrmPrincipal`.

## Base de datos

Esta implementacion no agrega tablas ni columnas. Utiliza las fechas existentes
en `asignacion_rutina` y el registro por fecha de `progreso_rutina`.

Si la base aun no tiene `detalle_ejercicios` y `fecha_hora_actualizacion` en
`progreso_rutina`, ejecutar primero la migracion ya incluida:

`entrega/migracion_registro_actividad_rutina_cliente.sql`
