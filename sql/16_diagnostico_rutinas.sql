-- =====================================================================
-- 16_diagnostico_rutinas.sql
--
-- Imprime todo lo que necesita saber "Mi rutina" para no fallar:
--   * usuarios cliente y a que persona apuntan
--   * asignaciones vigentes por cliente
--   * dias configurados en rutina_dia_entrenamiento vs ejercicios
--     reales en contiene_ejercicio (deben coincidir)
--   * para HOY, cuantos ejercicios tiene cada rutina asignada
--
-- Solo lectura. Ejecutalo cuando el panel del cliente muestre "sin dias
-- configurados" o "no hay ejercicios para <dia>": la salida dice donde
-- esta el hueco.
-- =====================================================================

\echo '=== 1) Usuarios con rol CLIENTE ==='
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       c.codigo_cliente, p.nombres || ' ' || p.apellidos AS nombre,
       u.estado_usuario, u.bloqueado
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN persona p ON p.id_persona = u.id_persona
 WHERE u.id_rol = 9
 ORDER BY u.id_usuario;

\echo ''
\echo '=== 2) Rutinas y su detalle (dias vs ejercicios) ==='
SELECT r.id_rutina, r.nombre_rutina, r.nivel, r.estado_rutina,
       COALESCE((SELECT string_agg(DISTINCT dia_entrenamiento, ', '
                                   ORDER BY dia_entrenamiento)
                   FROM rutina_dia_entrenamiento
                  WHERE id_rutina = r.id_rutina), '(vacio)')
           AS dias_configurados,
       COALESCE((SELECT string_agg(DISTINCT dia_semana, ', '
                                   ORDER BY dia_semana)
                   FROM contiene_ejercicio
                  WHERE id_rutina = r.id_rutina), '(vacio)')
           AS dias_con_ejercicios,
       (SELECT COUNT(*) FROM contiene_ejercicio
         WHERE id_rutina = r.id_rutina) AS total_ejercicios
  FROM rutina r
 ORDER BY r.id_rutina;

\echo ''
\echo '=== 3) Asignaciones vigentes por cliente CON login ==='
SELECT u.nombre_usuario, c.codigo_cliente,
       ar.id_asignacion, ar.estado_asignacion,
       r.id_rutina, r.nombre_rutina,
       ar.fecha_inicio, ar.fecha_fin,
       (SELECT COUNT(*) FROM contiene_ejercicio
         WHERE id_rutina = r.id_rutina) AS num_ejercicios_rutina
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN asignacion_rutina ar ON ar.id_cliente = c.id_persona
  JOIN rutina r ON r.id_rutina = ar.id_rutina
 WHERE u.id_rol = 9
   AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA')
 ORDER BY u.nombre_usuario, ar.id_asignacion DESC;

\echo ''
\echo '=== 4) Que vera cada cliente HOY (' || CURRENT_DATE || ') ==='
WITH dia_hoy AS (
    SELECT CASE EXTRACT(DOW FROM CURRENT_DATE)::int
               WHEN 1 THEN 'LUNES'   WHEN 2 THEN 'MARTES'
               WHEN 3 THEN 'MIERCOLES' WHEN 4 THEN 'JUEVES'
               WHEN 5 THEN 'VIERNES' WHEN 6 THEN 'SABADO'
               WHEN 0 THEN 'DOMINGO'
           END AS dia
)
SELECT u.nombre_usuario, c.codigo_cliente,
       ar.id_asignacion, r.nombre_rutina, ar.estado_asignacion,
       (SELECT dia FROM dia_hoy) AS dia_hoy,
       (SELECT COUNT(*) FROM contiene_ejercicio ce, dia_hoy dh
         WHERE ce.id_rutina = r.id_rutina
           AND ce.dia_semana = dh.dia) AS ejercicios_hoy,
       (CASE
          WHEN ar.estado_asignacion <> 'ACTIVA'
              THEN 'Asignacion no ACTIVA -> solo lectura'
          WHEN CURRENT_DATE < ar.fecha_inicio
              THEN 'Aun no inicia la rutina'
          WHEN ar.fecha_fin IS NOT NULL AND CURRENT_DATE > ar.fecha_fin
              THEN 'Rutina ya termino'
          WHEN (SELECT COUNT(*) FROM contiene_ejercicio ce, dia_hoy dh
                 WHERE ce.id_rutina = r.id_rutina
                   AND ce.dia_semana = dh.dia) = 0
              THEN 'Sin ejercicios para hoy en esta rutina'
          ELSE 'OK: veras tarjetas hoy'
        END) AS estado_esperado_hoy
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN asignacion_rutina ar ON ar.id_cliente = c.id_persona
  JOIN rutina r ON r.id_rutina = ar.id_rutina
 WHERE u.id_rol = 9
   AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA')
 ORDER BY u.nombre_usuario, ar.id_asignacion DESC;

\echo ''
\echo '=== 5) Detalle de rutinas asignadas (ejercicio por dia) ==='
SELECT ar.id_asignacion, r.id_rutina, r.nombre_rutina,
       ce.dia_semana, ce.orden, e.nombre_ejercicio,
       ce.series, ce.repeticiones
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN asignacion_rutina ar ON ar.id_cliente = c.id_persona
  JOIN rutina r ON r.id_rutina = ar.id_rutina
  LEFT JOIN contiene_ejercicio ce ON ce.id_rutina = r.id_rutina
  LEFT JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio
 WHERE u.id_rol = 9
   AND ar.estado_asignacion = 'ACTIVA'
 ORDER BY u.nombre_usuario, ar.id_asignacion,
          CASE ce.dia_semana
              WHEN 'LUNES'     THEN 1 WHEN 'MARTES'    THEN 2
              WHEN 'MIERCOLES' THEN 3 WHEN 'JUEVES'    THEN 4
              WHEN 'VIERNES'   THEN 5 WHEN 'SABADO'    THEN 6
              WHEN 'DOMINGO'   THEN 7 ELSE 8 END,
          ce.orden;
