-- GYMNOVA - cierre de facturacion de datos demostrativos.
-- Solo corrige pagos semilla cuya factura dice PAGADA pero conserva saldo.
BEGIN;

UPDATE pago p
SET monto_pago = calculo.total,
    monto_recibido = GREATEST(COALESCE(p.monto_recibido, 0), calculo.total)
FROM (
    SELECT f.id_factura,
           ROUND(SUM(df.cantidad * df.precio_unitario)
                 - COALESCE(f.total_descuento, 0)
                 + COALESCE(f.impuesto, 0), 2) total
    FROM factura f
    JOIN detalle_factura df ON df.id_factura=f.id_factura
    WHERE f.numero_factura LIKE 'FAC-0000%'
      AND f.estado_factura='PAGADA'
    GROUP BY f.id_factura, f.total_descuento, f.impuesto
) calculo
WHERE p.id_factura=calculo.id_factura
  AND p.estado_pago='CONFIRMADO'
  AND p.monto_pago <> calculo.total;

COMMIT;

-- Verificacion para la exposicion: una factura PAGADA debe tener saldo cero.
SELECT f.numero_factura,
       ROUND(SUM(df.cantidad * df.precio_unitario)
             - COALESCE(f.total_descuento,0)
             + COALESCE(f.impuesto,0), 2) AS total_factura,
       COALESCE((SELECT SUM(p.monto_pago) FROM pago p
                 WHERE p.id_factura=f.id_factura
                   AND p.estado_pago='CONFIRMADO'), 0) AS total_pagado
FROM factura f
JOIN detalle_factura df ON df.id_factura=f.id_factura
GROUP BY f.id_factura, f.numero_factura, f.total_descuento, f.impuesto
ORDER BY f.numero_factura;
