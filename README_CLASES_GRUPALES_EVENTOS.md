# Corrección: clases grupales como eventos para clientes

Cambios incluidos:

- Eliminado el botón superior `Ver detalle` de `Entrenador > Planificador > Mis rutinas`.
  El detalle se mantiene únicamente en la columna `Ver detalle` de cada rutina.
- En `Acceso > Clases grupales`, el primer campo se presenta como `Tipo de clase grupal`.
  Se puede escribir libremente Yoga, Bailoterapia, Spinning, etc.
- La clase conserva entrenador responsable, fecha/hora, duración y cupo máximo.
- En la experiencia del rol Cliente, `Mis reservas` muestra las clases grupales futuras como eventos.
- Cada evento presenta cupos ocupados, cupos disponibles y estado DISPONIBLE/LLENA/RESERVADA.
- El cliente puede reservar su propio cupo y cancelar su propia reserva.
- El último cupo está protegido por la transacción existente de `FlujoRecepcionControlador`.
- Se corrigió la agenda del cliente para mostrar la fecha/hora real de la clase, no la fecha de creación de la reserva.
- El rol Cliente ya no abre el formulario administrativo `PnlAcceso` desde el botón general.

No requiere una migración nueva si la base actual ya tiene en `clase_grupal`:
`id_entrenador`, `cupo_maximo` y `fecha_hora`, y `reserva.id_clase`.

Compilación validada con `ant clean jar`: BUILD SUCCESSFUL.
