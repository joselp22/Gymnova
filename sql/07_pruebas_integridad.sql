-- GYMNOVA - pruebas negativas de integridad (no conservan cambios).
\set ON_ERROR_STOP on
\pset border 2

\echo '1. NO EXISTEN BORRADOS CASCADE EN EL HISTORIAL'
SELECT COUNT(*) AS llaves_con_delete_cascade
FROM information_schema.referential_constraints
WHERE constraint_schema='public'
  AND delete_rule='CASCADE';

\echo '2. POSTGRESQL RECHAZA CEDULA DUPLICADA'
DO $$
DECLARE cedula_existente varchar;
BEGIN
  SELECT cedula INTO cedula_existente FROM persona LIMIT 1;
  BEGIN
    INSERT INTO persona(cedula,nombres,apellidos,fecha_nacimiento,sexo,
                        telefono,correo,estado)
    VALUES(cedula_existente,'Prueba','Duplicada','2000-01-01','Otro',
           '0990000000','duplicada@gymnova.test',TRUE);
    RAISE EXCEPTION 'FALLO: la cedula duplicada fue aceptada';
  EXCEPTION WHEN unique_violation THEN
    RAISE NOTICE 'OK: cedula duplicada rechazada por restriccion UNIQUE';
  END;
END $$;

\echo '3. POSTGRESQL PRESERVA EL HISTORIAL DEL CLIENTE'
DO $$
DECLARE cliente_historial bigint;
BEGIN
  SELECT c.id_persona INTO cliente_historial
  FROM cliente c
  WHERE EXISTS (SELECT 1 FROM membresia m WHERE m.id_cliente=c.id_persona)
  LIMIT 1;
  BEGIN
    DELETE FROM persona WHERE id_persona=cliente_historial;
    RAISE EXCEPTION 'FALLO: se elimino una persona con historial';
  EXCEPTION WHEN integrity_constraint_violation THEN
    RAISE NOTICE 'OK: historial protegido mediante llave foranea';
  END;
END $$;

\echo '4. CODIGOS FUERA DE LA RUBRICA DEFINIDA'
SELECT 'cliente' entidad, COUNT(*) invalidos FROM cliente
 WHERE codigo_cliente !~ '^CL[0-9]{5}$'
UNION ALL SELECT 'membresia', COUNT(*) FROM membresia
 WHERE numero_membresia !~ '^MB[0-9]{5}$'
UNION ALL SELECT 'reserva', COUNT(*) FROM reserva
 WHERE codigo_reserva !~ '^RS[0-9]{5}$'
UNION ALL SELECT 'pago', COUNT(*) FROM pago
 WHERE codigo_pago !~ '^PG[0-9]{5}$';

\echo '5. FACTURAS PAGADAS CON SALDO INCONSISTENTE'
WITH totales AS (
 SELECT f.id_factura, f.numero_factura,
        ROUND(SUM(d.cantidad*d.precio_unitario)
          - COALESCE(f.total_descuento,0)+COALESCE(f.impuesto,0),2) total,
        COALESCE((SELECT SUM(p.monto_pago) FROM pago p
          WHERE p.id_factura=f.id_factura AND p.estado_pago='CONFIRMADO'),0) pagado
 FROM factura f JOIN detalle_factura d ON d.id_factura=f.id_factura
 WHERE f.estado_factura='PAGADA'
 GROUP BY f.id_factura, f.numero_factura, f.total_descuento, f.impuesto)
SELECT COUNT(*) AS facturas_pagadas_con_saldo
FROM totales WHERE total<>pagado;
