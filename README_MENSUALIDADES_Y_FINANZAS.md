# Membresias, renovaciones y Finanzas

## Regla implementada
Toda alta o renovacion de membresia debe registrar un cobro. Ya no se permite extender fechas o cambiar de plan usando Modificar sin movimiento financiero.

## Flujo
1. Seleccionar cliente y plan en Membresias.
2. Pulsar **Cobrar / renovar membresia**.
3. Elegir metodo de pago y referencia cuando aplique.
4. La transaccion crea:
   - membresia nueva;
   - factura PAGADA;
   - detalle_factura con concepto MEMBRESIA;
   - pago CONFIRMADO;
   - comprobante GENERADO.
5. El Dashboard financiero refleja el pago al abrir/refrescar Finanzas.

## Renovacion
- Si no existe historial: crea una membresia inicial.
- Si la ultima membresia esta vencida/cancelada: la nueva puede iniciar hoy.
- Si la ultima membresia sigue vigente: la nueva empieza al dia siguiente de fecha_fin y queda PENDIENTE.
- VerificadorMembresias activa automaticamente las PENDIENTES cuando llega fecha_inicio.

## Dashboard
Ahora muestra:
- membresias activas;
- ingresos totales del mes;
- ingresos por membresias del mes + numero de cobros;
- ingresos del anio;
- facturas por cobrar;
- desglose por plan;
- ultimos cobros de membresias/renovaciones.

El Dashboard esta disponible en Finanzas tanto para Administrador como Recepcionista.

## Base de datos
No se agregaron tablas ni columnas. Se reutilizan las 35 tablas actuales.
No hace falta ejecutar una migracion para esta implementacion.
