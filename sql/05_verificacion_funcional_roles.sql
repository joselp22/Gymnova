-- GYMNOVA - verificacion de datos reales por rol (solo lectura)
\pset border 2
\x off

\echo '1. USUARIOS Y ROLES'
SELECT u.nombre_usuario, r.nombre_rol, u.id_persona,
       u.estado_usuario, u.bloqueado
FROM usuario u
JOIN rol r ON r.id_rol = u.id_rol
ORDER BY r.nombre_rol, u.nombre_usuario;

\echo '2. CLIENTES ASIGNADOS AL ENTRENADOR'
SELECT DISTINCT pe.nombres || ' ' || pe.apellidos AS entrenador,
       c.codigo_cliente,
       pc.nombres || ' ' || pc.apellidos AS cliente,
       r.nombre_rutina,
       ar.fecha_asignacion,
       ar.estado_asignacion
FROM entrenador e
JOIN persona pe ON pe.id_persona = e.id_persona
JOIN rutina r ON r.id_entrenador = e.id_persona
JOIN asignacion_rutina ar ON ar.id_rutina = r.id_rutina
JOIN cliente c ON c.id_persona = ar.id_cliente
JOIN persona pc ON pc.id_persona = c.id_persona
ORDER BY entrenador, cliente;

\echo '3. CLIENTES ASIGNADOS AL NUTRICIONISTA'
SELECT pntr.nombres || ' ' || pntr.apellidos AS nutricionista,
       c.codigo_cliente,
       pc.nombres || ' ' || pc.apellidos AS cliente,
       pn.codigo_plan,
       pn.nombre_plan,
       pn.estado_plan
FROM plan_nutricional pn
JOIN nutricionista n ON n.id_persona = pn.id_nutricionista
JOIN persona pntr ON pntr.id_persona = n.id_persona
JOIN cliente c ON c.id_persona = pn.id_cliente
JOIN persona pc ON pc.id_persona = c.id_persona
ORDER BY nutricionista, cliente;

\echo '4. OPERACION DE RECEPCION: MEMBRESIAS, RESERVAS Y PAGOS'
SELECT c.codigo_cliente,
       p.nombres || ' ' || p.apellidos AS cliente,
       m.numero_membresia,
       m.estado_membresia,
       COUNT(DISTINCT r.id_reserva) AS reservas,
       COUNT(DISTINCT a.id_asistencia) AS asistencias,
       COALESCE(SUM(DISTINCT pg.monto_pago), 0) AS pagos
FROM cliente c
JOIN persona p ON p.id_persona = c.id_persona
LEFT JOIN membresia m ON m.id_cliente = c.id_persona
LEFT JOIN reserva r ON r.id_cliente = c.id_persona
LEFT JOIN asistencia a ON a.id_cliente = c.id_persona
LEFT JOIN factura f ON f.id_cliente = c.id_persona
LEFT JOIN pago pg ON pg.id_factura = f.id_factura
GROUP BY c.codigo_cliente, p.nombres, p.apellidos,
         m.numero_membresia, m.estado_membresia
ORDER BY c.codigo_cliente;

\echo '5. PROGRESO REAL DEL CLIENTE'
SELECT c.codigo_cliente,
       p.nombres || ' ' || p.apellidos AS cliente,
       r.nombre_rutina,
       pr.fecha_registro,
       pr.sesiones_planificadas,
       pr.sesiones_completadas,
       pr.peso_corporal,
       pr.estado_progreso
FROM progreso_rutina pr
JOIN cliente c ON c.id_persona = pr.id_cliente
JOIN persona p ON p.id_persona = c.id_persona
JOIN rutina r ON r.id_rutina = pr.id_rutina
ORDER BY cliente, pr.fecha_registro DESC, r.nombre_rutina;

\echo '6. RESUMEN DE TABLAS FUNCIONALES'
SELECT 'clientes' AS entidad, COUNT(*) AS registros FROM cliente
UNION ALL SELECT 'membresias', COUNT(*) FROM membresia
UNION ALL SELECT 'reservas', COUNT(*) FROM reserva
UNION ALL SELECT 'asistencias', COUNT(*) FROM asistencia
UNION ALL SELECT 'rutinas', COUNT(*) FROM rutina
UNION ALL SELECT 'ejercicios', COUNT(*) FROM ejercicio
UNION ALL SELECT 'avances', COUNT(*) FROM progreso_rutina
UNION ALL SELECT 'evaluaciones', COUNT(*) FROM evaluacion_fisica
UNION ALL SELECT 'planes nutricionales', COUNT(*) FROM plan_nutricional
UNION ALL SELECT 'pagos', COUNT(*) FROM pago
UNION ALL SELECT 'productos', COUNT(*) FROM producto
UNION ALL SELECT 'equipos', COUNT(*) FROM equipo
ORDER BY entidad;
