# Correccion de navegacion por roles

Esta entrega separa expresamente las pantallas administrativas de las
experiencias personales de los otros roles.

## Administrador

Los botones abren directamente sus vistas de gestion: personas, clientes,
personal, seguridad, membresias, acceso, rutinas, salud, nutricion, finanzas,
inventario, reportes y configuracion. No pasan por las portadas de cliente,
entrenador, nutricionista o recepcionista.

## Otros roles

- Recepcionista: inicio, personas, clientes, membresias, acceso, finanzas,
  reportes y configuracion.
- Entrenador: inicio, mis clientes, rutinas, salud, reportes y configuracion.
- Nutricionista: inicio, mis clientes, salud, nutricion, reportes y
  configuracion.
- Cliente: inicio, mi perfil, mi membresia, mis reservas, mi rutina, mi
  progreso, mi nutricion, mi historial y configuracion.

La clase `utilidades.NavegacionRol` es la unica matriz de navegacion. La
ventana conserva el rol con el que fue creada y rechaza cualquier intento de
abrir un modulo ajeno a ese rol.

## Pruebas incluidas

- `pruebas.PruebaNavegacionRoles`: comprueba la matriz exacta de los cinco
  roles y varios cruces prohibidos.
- `pruebas.PruebaEnrutamientoVentana`: inicia sesion como administrador y
  comprueba las 13 vistas centrales reales que abre `FrmPrincipal`.
