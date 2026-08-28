# Nueva vista de Membresias con cobro integrado

## Cambio principal
La vista generica anterior `PnlMembresias` fue eliminada. La navegacion del modulo MEMBRESIAS abre ahora `PnlMembresiasCobro`.

## Flujo visible
1. Buscar/seleccionar cliente activo.
2. Ver la ultima membresia, plan, vigencia y estado.
3. Elegir plan de renovacion.
4. Ver inicio/fin de la nueva vigencia.
5. Elegir metodo de pago y referencia.
6. Ver subtotal, IVA y TOTAL A COBRAR en la misma pantalla.
7. Confirmar el cobro.
8. Se ejecuta una sola transaccion: membresia -> factura PAGADA -> detalle MEMBRESIA -> pago CONFIRMADO -> comprobante GENERADO.
9. El ingreso aparece automaticamente en Finanzas > Dashboard.

## Historial
La parte inferior muestra membresia, plan, fechas, estado, subtotal, factura, pago, metodo, fecha de cobro y total cobrado.

## Acciones conservadas
- Congelar una membresia activa.
- Reactivar una membresia congelada.
- Cancelar una membresia.
- Administrador: editar precio y descripcion del plan seleccionado.

## Base de datos
No se agregan ni modifican tablas. No requiere migracion SQL adicional.

## Verificacion
- `ant clean jar`: BUILD SUCCESSFUL.
- `ant test`: compila las fuentes de test y reporta `No tests executed` porque el proyecto no tiene suite JUnit configurada para ejecucion automatica.
- `ConexionPostgreSQL.java`: sin cambios respecto a la version base.
