# Corrección: integridad de Entrenador + Rutinas de Administración

## Cambios

1. `LoginControlador` valida que una cuenta con rol **Entrenador** tenga un perfil activo en `entrenador` antes de iniciar sesión.
2. `UsuarioControlador` impide crear/modificar una cuenta con rol Entrenador si la persona no fue registrada previamente en **Personal > Entrenadores**.
3. `EjercicioControlador` vuelve a validar el perfil profesional antes de crear/modificar actividades y muestra un mensaje específico si falta el perfil.
4. El Administrador ya no usa el CRUD general de Rutinas. Se agregó `PnlRutinasAdministrador`, de solo lectura.
5. La tabla administrativa muestra:
   - N° Rutina
   - Rutina
   - Entrenador
   - Nivel
   - Semanas
   - Estado
   - Cantidad de ejercicios
   - Botón `Ver detalle`
6. `Ver detalle` carga los ejercicios de la rutina en la parte inferior de la **misma pantalla**, sin abrir una ventana de edición.
7. El Planificador del Entrenador sigue siendo el único lugar donde se crean actividades, se construyen rutinas y se asignan a clientes.
8. Se retiró la ruta que permitía al Cliente abrir la antigua gestión administrativa de Rutinas.
9. `ConexionPostgreSQL.java` no fue modificada.

## Importante para cuentas existentes

Si ya existe una cuenta (por ejemplo `victor`) con rol Entrenador pero su persona no está registrada en la tabla `entrenador`, el nuevo código impedirá iniciar sesión hasta corregir los datos.

Flujo recomendado:

`Administrador > Personal > Entrenadores` → registrar/activar el perfil profesional de esa misma persona → luego `Seguridad > Cuentas de usuario` → mantener/asignar el rol Entrenador.
