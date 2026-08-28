# Nuevo flujo de Clases Grupales

## Administrador
El botón **Clases grupales** abre una vista exclusiva para:
- Crear clase (Yoga, Bailoterapia, Spinning, etc.).
- Asignar entrenador responsable.
- Configurar fecha/hora, cupo máximo, duración, nivel, intensidad y descripción.
- Actualizar y desactivar clases.
- Consultar inscritos y cupos disponibles en tiempo real.

## Entrenador
El menú incorpora **Mis clases**. El entrenador ve únicamente las clases que el Administrador le asignó y, al seleccionar una clase, ve los participantes con reserva ACTIVA.

## Cliente
El botón **Clases grupales** muestra eventos futuros activos. Cada fila permite:
- **Unirme** si existe cupo.
- **Salir de la clase** si ya está inscrito.
- Ver entrenador, fecha/hora, duración, cupo ocupado y cupos disponibles.

## Recepción
`PnlAcceso` se conserva únicamente para el flujo operativo de recepción (reservas/asistencias). Administrador, Entrenador y Cliente ya no utilizan ese formulario genérico.

## Base de datos
No requiere una nueva migración si la base actual ya contiene:
- `clase_grupal.id_entrenador`
- `clase_grupal.cupo_maximo`
- `clase_grupal.fecha_hora`
- `reserva.id_clase`

## Compilación
`ant clean jar` -> BUILD SUCCESSFUL (201 clases Java).
