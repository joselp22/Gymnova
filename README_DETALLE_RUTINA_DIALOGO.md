# Detalle de rutina en ventana compacta

Cambios aplicados:

- Administrador: la pantalla de Rutinas ya no muestra el detalle fijo en la parte inferior.
- Administrador: cada fila mantiene el botón `Ver detalle`, que ahora abre `DlgDetalleRutina`.
- Entrenador: la tabla `Mis rutinas guardadas` incorpora la columna `Detalle` con botón `Ver detalle`.
- Entrenador: se retiró la tabla fija `Detalle de la rutina seleccionada`.
- `DlgDetalleRutina` es una vista modal reutilizada por Administrador y Entrenador.
- La ventana muestra número/nombre de rutina, entrenador, nivel, semanas, estado y ejercicios.
- No se realizaron cambios en PostgreSQL ni en `ConexionPostgreSQL.java`.

Validación realizada:

```text
ant clean jar
Compiling 192 source files
BUILD SUCCESSFUL
```
