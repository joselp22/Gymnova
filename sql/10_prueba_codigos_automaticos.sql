\pset border 2
\echo 'PRUEBA TRANSACCIONAL: CODIGOS AUTOMATICOS (NO CONSERVA DATOS)'
BEGIN;

INSERT INTO membresia(numero_membresia,fecha_inicio,fecha_fin,costo_final,estado_membresia,id_cliente,id_tipo_membresia)
SELECT NULL,CURRENT_DATE,CURRENT_DATE+30,precio_base,'PENDIENTE',
       (SELECT id_persona FROM cliente ORDER BY id_persona LIMIT 1),id_tipo_membresia
FROM tipo_membresia WHERE estado_tipo ORDER BY id_tipo_membresia LIMIT 1
RETURNING numero_membresia AS codigo_generado;

INSERT INTO compra(numero_compra,fecha_compra,tipo_pago,estado_compra,id_proveedor,id_empleado)
VALUES(NULL,CURRENT_DATE,'EFECTIVO','REGISTRADA',
       (SELECT id_proveedor FROM proveedor ORDER BY id_proveedor LIMIT 1),
       (SELECT id_persona FROM empleado ORDER BY id_persona LIMIT 1))
RETURNING numero_compra AS codigo_generado;

INSERT INTO mantenimiento(numero_mantenimiento,fecha_solicitud,estado_mantenimiento,id_equipo,id_tipo_mantenimiento,id_empleado)
VALUES(NULL,CURRENT_DATE,'SOLICITADO',
       (SELECT id_equipo FROM equipo ORDER BY id_equipo LIMIT 1),
       (SELECT id_tipo_mantenimiento FROM tipo_mantenimiento ORDER BY id_tipo_mantenimiento LIMIT 1),
       (SELECT id_persona FROM empleado ORDER BY id_persona LIMIT 1))
RETURNING numero_mantenimiento AS codigo_generado;

INSERT INTO comprobante(numero_comprobante,tipo_comprobante,fecha_emision,estado_comprobante,id_pago)
VALUES(NULL,'PRUEBA',CURRENT_DATE,'GENERADO',
       (SELECT id_pago FROM pago ORDER BY id_pago LIMIT 1))
RETURNING numero_comprobante AS codigo_generado;

ROLLBACK;
\echo 'OK: la transaccion se revirtio; no se agregaron registros de prueba.'
