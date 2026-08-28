# Correcciones aplicadas a GYMNOVA

Esta copia incluye correcciones funcionales basadas en la hoja `correxiones proyecto.docx`.

## Cambios principales

1. `clase_grupal` ahora maneja entrenador, cupo maximo y fecha/hora.
2. Reservas toman el cupo desde PostgreSQL, bloquean duplicados y registran cancelacion con fecha/motivo.
3. Se agrego flujo real de renovacion de membresia y acciones de congelar/reactivar/cancelar en la interfaz de recepcion.
4. `IncluyeAlimento.cantidad` paso de `Integer` a `BigDecimal`.
5. `IndicadorSalud` ahora maneja valor minimo y maximo; `fuera_de_rango` se calcula automaticamente.
6. Facturas `PAGADA` y pagos `CONFIRMADO` quedan bloqueados para modificar/desactivar.
7. Entrenadores y nutricionistas pueden ver clientes sin asignacion para poder realizar la primera asignacion; luego quedan asociados por rutina/plan.
8. Credenciales de PostgreSQL ya no incluyen una contrasena fija en el codigo.
9. Se agregaron indices para relaciones y un indice unico parcial para un solo plan nutricional ACTIVO por cliente.
10. IVA puede configurarse con `-Dgymnova.iva=15.00`.

## Antes de ejecutar contra una BD existente

Ejecute:

```sql
entrega/migracion_correcciones_2026_08_25.sql
```

Si crea la BD desde cero, use `entrega/gymnova_db_entrega.sql`, que ya contiene los campos nuevos.

## Configurar PostgreSQL

En Windows PowerShell:

```powershell
$env:GYMNOVA_DB_URL="jdbc:postgresql://localhost:5432/gymnova_db"
$env:GYMNOVA_DB_USUARIO="postgres"
$env:GYMNOVA_DB_CONTRASENA="TU_CLAVE"
```

Tambien puede ejecutar Java con propiedades `-Dgymnova.db.*`.

## Validacion realizada

Se ejecuto `ant clean jar` despues de los cambios y finalizo con `BUILD SUCCESSFUL`.
