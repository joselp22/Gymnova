-- ============================================================================
-- GYMNOVA - Verificacion del flujo de rutina de un cliente
--
-- PASO 1: ejecuta la consulta de clientes y busca el ID que quieras revisar.
-- PASO 2: cambia SOLO el numero 4 de la linea INSERT por ese id_persona.
-- Este script NO usa :ID_CLIENTE porque PostgreSQL no reconoce ese marcador
-- cuando se ejecuta como SQL normal en pgAdmin/DataGrip/DBeaver.
-- ============================================================================

-- 0. Lista de clientes para encontrar el id_persona correcto.
SELECT c.id_persona AS id_cliente,
       c.codigo_cliente,
       p.nombres,
       p.apellidos,
       c.estado_cliente
FROM cliente c
JOIN persona p ON p.id_persona = c.id_persona
ORDER BY p.apellidos, p.nombres;

-- --------------------------------------------------------------------------
-- PARAMETRO: CAMBIA SOLAMENTE ESTE 4 POR EL id_cliente QUE QUIERAS REVISAR.
-- Ejemplo: VALUES (12);
-- --------------------------------------------------------------------------
DROP TABLE IF EXISTS tmp_gymnova_cliente_verificar;
CREATE TEMP TABLE tmp_gymnova_cliente_verificar (
    id_cliente BIGINT NOT NULL
);
INSERT INTO tmp_gymnova_cliente_verificar (id_cliente) VALUES (4);

-- Verifica que el cliente exista.
SELECT c.id_persona AS id_cliente,
       c.codigo_cliente,
       p.nombres,
       p.apellidos
FROM cliente c
JOIN persona p ON p.id_persona = c.id_persona
WHERE c.id_persona = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar);

-- 1. Asignaciones del cliente
SELECT ar.id_asignacion,
       ar.estado_asignacion,
       ar.fecha_asignacion,
       ar.fecha_inicio,
       ar.fecha_fin,
       r.id_rutina,
       r.nombre_rutina,
       r.id_entrenador
FROM asignacion_rutina ar
JOIN rutina r ON r.id_rutina = ar.id_rutina
WHERE ar.id_cliente = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar)
ORDER BY ar.fecha_inicio DESC, ar.id_asignacion DESC;

-- 2. Dias y actividades realmente guardadas para cada rutina del cliente
SELECT ar.id_asignacion,
       ar.estado_asignacion,
       ar.fecha_inicio,
       ar.fecha_fin,
       r.id_rutina,
       r.nombre_rutina,
       ce.id_rutina_ejercicio,
       ce.dia_semana,
       ce.orden,
       e.nombre_ejercicio,
       ce.series,
       ce.repeticiones,
       ce.peso_sugerido,
       ce.duracion_minutos,
       ce.descanso_segundos
FROM asignacion_rutina ar
JOIN rutina r ON r.id_rutina = ar.id_rutina
JOIN contiene_ejercicio ce ON ce.id_rutina = r.id_rutina
JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio
WHERE ar.id_cliente = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar)
ORDER BY ar.fecha_inicio DESC,
         CASE ce.dia_semana
             WHEN 'LUNES' THEN 1
             WHEN 'MARTES' THEN 2
             WHEN 'MIERCOLES' THEN 3
             WHEN 'JUEVES' THEN 4
             WHEN 'VIERNES' THEN 5
             WHEN 'SABADO' THEN 6
             WHEN 'DOMINGO' THEN 7
             ELSE 8
         END,
         ce.orden;

-- 3. Progreso registrado por fecha
SELECT pr.id_progreso,
       pr.fecha_registro,
       pr.id_cliente,
       pr.id_rutina,
       r.nombre_rutina,
       pr.sesiones_planificadas,
       pr.sesiones_completadas,
       pr.detalle_ejercicios,
       pr.fecha_hora_actualizacion
FROM progreso_rutina pr
JOIN rutina r ON r.id_rutina = pr.id_rutina
WHERE pr.id_cliente = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar)
ORDER BY pr.fecha_registro DESC, pr.id_progreso DESC;

-- 4. Vista rapida: que rutina deberia resolver el sistema para HOY.
SELECT ar.id_asignacion,
       ar.estado_asignacion,
       ar.fecha_inicio,
       ar.fecha_fin,
       r.id_rutina,
       r.nombre_rutina
FROM asignacion_rutina ar
JOIN rutina r ON r.id_rutina = ar.id_rutina
WHERE ar.id_cliente = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar)
  AND CURRENT_DATE >= ar.fecha_inicio
  AND (ar.fecha_fin IS NULL OR CURRENT_DATE <= ar.fecha_fin)
  AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','FINALIZADA')
ORDER BY ar.fecha_inicio DESC, ar.id_asignacion DESC;

-- 5. Actividades que deberia ver HOY segun el dia real.
WITH dia_hoy AS (
    SELECT CASE EXTRACT(ISODOW FROM CURRENT_DATE)::INT
        WHEN 1 THEN 'LUNES'
        WHEN 2 THEN 'MARTES'
        WHEN 3 THEN 'MIERCOLES'
        WHEN 4 THEN 'JUEVES'
        WHEN 5 THEN 'VIERNES'
        WHEN 6 THEN 'SABADO'
        WHEN 7 THEN 'DOMINGO'
    END AS dia_semana
), asignacion_hoy AS (
    SELECT ar.id_asignacion, ar.id_rutina
    FROM asignacion_rutina ar
    WHERE ar.id_cliente = (SELECT id_cliente FROM tmp_gymnova_cliente_verificar)
      AND CURRENT_DATE >= ar.fecha_inicio
      AND (ar.fecha_fin IS NULL OR CURRENT_DATE <= ar.fecha_fin)
      AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','FINALIZADA')
    ORDER BY ar.fecha_inicio DESC, ar.id_asignacion DESC
    LIMIT 1
)
SELECT ah.id_asignacion,
       ce.id_rutina_ejercicio,
       ce.dia_semana,
       ce.orden,
       e.nombre_ejercicio,
       ce.series,
       ce.repeticiones,
       ce.peso_sugerido,
       ce.duracion_minutos,
       ce.descanso_segundos
FROM asignacion_hoy ah
JOIN contiene_ejercicio ce ON ce.id_rutina = ah.id_rutina
JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio
JOIN dia_hoy dh ON dh.dia_semana = ce.dia_semana
ORDER BY ce.orden;
