-- =====================================================================
-- 15_fix_rutinas_cliente.sql
--
-- Objetivo: dejar la BD en un estado en el que, entres el dia que
-- entres como cliente en "Mi rutina", SIEMPRE veas al menos una
-- actividad para HOY (independientemente del dia de la semana).
--
-- Que hace:
--   1) Muestra el estado ANTES.
--   2) Completa las rutinas 1 ("fff") y 3 ("hola") con un ejercicio
--      por cada dia de la semana (LUN..DOM). Reutiliza el primer
--      ejercicio disponible del entrenador dueno de la rutina.
--   3) Cancela asignaciones sobre rutinas que todavia se queden sin
--      ejercicios (limpia la asignacion huerfana de rutina 5).
--   4) Asigna rutina 1 al cliente con login "CLIENTE"  (CL00001, id 2)
--      y rutina 3 al cliente con login "josel"         (CL00005, id 7).
--   5) (Opcional) Crea usuario "cl00006" para CL00006 reutilizando el
--      hash del usuario "CLIENTE" (misma contrasena que ese usuario).
--   6) Muestra el estado DESPUES.
--
-- Todo dentro de una transaccion. Idempotente: puedes correrlo varias
-- veces sin romper nada, gracias a los ON CONFLICT y los NOT EXISTS.
-- =====================================================================

BEGIN;

-- 1) Estado ANTES ------------------------------------------------------

\echo '--- Clientes con login (rol=CLIENTE) ---'
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       c.codigo_cliente, p.nombres || ' ' || p.apellidos AS nombre
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN persona p ON p.id_persona = u.id_persona
 WHERE u.id_rol = 9
 ORDER BY u.id_usuario;

\echo '--- Rutinas y numero de ejercicios ---'
SELECT r.id_rutina, r.nombre_rutina, r.estado_rutina,
       (SELECT COUNT(*) FROM contiene_ejercicio ce
          WHERE ce.id_rutina = r.id_rutina) AS num_ejercicios,
       (SELECT string_agg(DISTINCT dia_semana, ', ' ORDER BY dia_semana)
          FROM contiene_ejercicio ce
         WHERE ce.id_rutina = r.id_rutina) AS dias_con_ejercicios
  FROM rutina r
 ORDER BY r.id_rutina;

\echo '--- Asignaciones vigentes ---'
SELECT ar.id_asignacion, c.codigo_cliente,
       r.nombre_rutina, ar.estado_asignacion,
       ar.fecha_inicio, ar.fecha_fin
  FROM asignacion_rutina ar
  JOIN cliente c ON c.id_persona = ar.id_cliente
  JOIN rutina  r ON r.id_rutina  = ar.id_rutina
 WHERE ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA')
 ORDER BY ar.id_asignacion DESC;


-- 2) Completar rutinas 1 y 3 con ejercicio en cada dia de la semana ---
-- Se ejecuta solo si la rutina existe y su entrenador dueno tiene al
-- menos un ejercicio disponible. Idempotente por ON CONFLICT.

DO $$
DECLARE
    v_rutinas bigint[] := ARRAY[1, 3];
    v_id_rutina bigint;
    v_id_entrenador bigint;
    v_id_ejercicio integer;
    v_dias text[] := ARRAY['LUNES','MARTES','MIERCOLES','JUEVES',
                           'VIERNES','SABADO','DOMINGO'];
    v_dia text;
BEGIN
    FOREACH v_id_rutina IN ARRAY v_rutinas LOOP

        SELECT r.id_entrenador
          INTO v_id_entrenador
          FROM rutina r
         WHERE r.id_rutina = v_id_rutina
           AND r.estado_rutina = 'ACTIVA';

        IF v_id_entrenador IS NULL THEN
            RAISE NOTICE 'Rutina % no existe o esta inactiva, se omite.',
                v_id_rutina;
            CONTINUE;
        END IF;

        -- Un ejercicio cualquiera del entrenador dueno.
        SELECT e.id_ejercicio
          INTO v_id_ejercicio
          FROM ejercicio e
         WHERE e.id_entrenador = v_id_entrenador
         ORDER BY e.id_ejercicio
         LIMIT 1;

        IF v_id_ejercicio IS NULL THEN
            RAISE NOTICE 'El entrenador % no tiene ejercicios, se omite rutina %.',
                v_id_entrenador, v_id_rutina;
            CONTINUE;
        END IF;

        FOREACH v_dia IN ARRAY v_dias LOOP

            -- FK compuesto: primero el dia en rutina_dia_entrenamiento.
            INSERT INTO rutina_dia_entrenamiento (id_rutina, dia_entrenamiento)
            VALUES (v_id_rutina, v_dia)
            ON CONFLICT (id_rutina, dia_entrenamiento) DO NOTHING;

            -- Luego el ejercicio del dia (orden=1 estable).
            INSERT INTO contiene_ejercicio (
                dia_semana, orden, series, repeticiones, peso_sugerido,
                duracion_minutos, descanso_segundos, id_rutina, id_ejercicio)
            SELECT v_dia, 1, 3, '10', NULL, NULL, 60,
                   v_id_rutina, v_id_ejercicio
             WHERE NOT EXISTS (
                SELECT 1 FROM contiene_ejercicio ce
                 WHERE ce.id_rutina = v_id_rutina
                   AND ce.dia_semana = v_dia);

        END LOOP;

        RAISE NOTICE 'Rutina % completada con ejercicio % en LUN..DOM.',
            v_id_rutina, v_id_ejercicio;
    END LOOP;
END $$;


-- 3) Cancelar asignaciones sobre rutinas que sigan sin ejercicios ------

UPDATE asignacion_rutina ar
   SET estado_asignacion   = 'CANCELADA',
       motivo_finalizacion = COALESCE(motivo_finalizacion,
         'Rutina sin ejercicios. Cancelada por script 15_fix_rutinas_cliente.')
 WHERE ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA')
   AND NOT EXISTS (SELECT 1 FROM contiene_ejercicio ce
                    WHERE ce.id_rutina = ar.id_rutina);


-- 4) Asignar las rutinas a los clientes con login ---------------------
-- CLIENTE (id 2 = CL00001)  -> rutina 1 "fff"  (ya con LUN..DOM)
-- josel   (id 7 = CL00005)  -> rutina 3 "hola" (ya con LUN..DOM)
-- fecha_fin = fecha_inicio + duracion_semanas*7 - 1.
-- Idempotente: ON CONFLICT (id_cliente, id_rutina, fecha_inicio).

WITH data(id_cliente, id_rutina, obs) AS (
    VALUES
        (2::bigint, 1::bigint,
         'Prueba multi-rutina tras fix.'::text),
        (7::bigint, 3::bigint,
         'Prueba multi-rutina para josel.'::text)
), preparada AS (
    SELECT d.id_cliente,
           d.id_rutina,
           d.obs,
           CURRENT_DATE AS fecha_inicio,
           CURRENT_DATE + (r.duracion_semanas * 7 - 1) AS fecha_fin
      FROM data d
      JOIN rutina  r ON r.id_rutina  = d.id_rutina
                    AND r.estado_rutina = 'ACTIVA'
      JOIN cliente c ON c.id_persona = d.id_cliente
                    AND c.estado_cliente
     WHERE EXISTS (SELECT 1 FROM contiene_ejercicio ce
                    WHERE ce.id_rutina = d.id_rutina)
)
INSERT INTO asignacion_rutina (
    fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion,
    observaciones, id_cliente, id_rutina)
SELECT CURRENT_DATE, fecha_inicio, fecha_fin, 'ACTIVA',
       obs, id_cliente, id_rutina
  FROM preparada
ON CONFLICT (id_cliente, id_rutina, fecha_inicio)
DO UPDATE SET estado_asignacion   = 'ACTIVA',
              fecha_fin           = EXCLUDED.fecha_fin,
              motivo_finalizacion = NULL,
              observaciones       = EXCLUDED.observaciones;


-- 5) (Opcional) Login para CL00006 -----------------------------------
-- Reutiliza el clave_hash del usuario "CLIENTE" (misma contrasena).
-- Comenta este INSERT si no lo quieres.

INSERT INTO usuario (id_persona, id_rol, nombre_usuario, clave_hash,
                     bloqueado, intentos_fallidos, estado_usuario,
                     fecha_creacion)
SELECT 9, 9, 'cl00006', u.clave_hash,
       false, 0, true, CURRENT_TIMESTAMP
  FROM usuario u
 WHERE u.nombre_usuario = 'CLIENTE'
   AND NOT EXISTS (SELECT 1 FROM usuario u2 WHERE u2.id_persona = 9)
   AND NOT EXISTS (SELECT 1 FROM usuario u3 WHERE u3.nombre_usuario = 'cl00006');


-- 6) Estado DESPUES ---------------------------------------------------

\echo '--- Rutinas afectadas (ejercicios por dia) ---'
SELECT ce.id_rutina, r.nombre_rutina, ce.dia_semana,
       COUNT(*) AS ejercicios_del_dia
  FROM contiene_ejercicio ce
  JOIN rutina r ON r.id_rutina = ce.id_rutina
 WHERE ce.id_rutina IN (1, 3)
 GROUP BY ce.id_rutina, r.nombre_rutina, ce.dia_semana
 ORDER BY ce.id_rutina,
          CASE ce.dia_semana
              WHEN 'LUNES'     THEN 1 WHEN 'MARTES'    THEN 2
              WHEN 'MIERCOLES' THEN 3 WHEN 'JUEVES'    THEN 4
              WHEN 'VIERNES'   THEN 5 WHEN 'SABADO'    THEN 6
              WHEN 'DOMINGO'   THEN 7 END;

\echo '--- Que rutina/actividad vera cada cliente HOY ---'
SELECT c.codigo_cliente,
       p.nombres || ' ' || p.apellidos AS cliente,
       r.nombre_rutina,
       ar.estado_asignacion,
       ar.fecha_inicio,
       ar.fecha_fin,
       TO_CHAR(CURRENT_DATE, 'TMDay') AS hoy,
       (SELECT COUNT(*) FROM contiene_ejercicio ce
         WHERE ce.id_rutina = r.id_rutina
           AND ce.dia_semana = UPPER(
               CASE EXTRACT(DOW FROM CURRENT_DATE)::int
                   WHEN 1 THEN 'LUNES' WHEN 2 THEN 'MARTES'
                   WHEN 3 THEN 'MIERCOLES' WHEN 4 THEN 'JUEVES'
                   WHEN 5 THEN 'VIERNES' WHEN 6 THEN 'SABADO'
                   WHEN 0 THEN 'DOMINGO' END))
           AS actividades_de_hoy
  FROM asignacion_rutina ar
  JOIN cliente c ON c.id_persona = ar.id_cliente
  JOIN persona p ON p.id_persona = ar.id_cliente
  JOIN rutina  r ON r.id_rutina  = ar.id_rutina
 WHERE ar.estado_asignacion = 'ACTIVA'
 ORDER BY c.codigo_cliente;

\echo '--- Usuarios rol CLIENTE (post-script) ---'
SELECT u.id_usuario, u.nombre_usuario, c.codigo_cliente,
       p.nombres || ' ' || p.apellidos AS nombre
  FROM usuario u
  JOIN cliente c ON c.id_persona = u.id_persona
  JOIN persona p ON p.id_persona = u.id_persona
 WHERE u.id_rol = 9
 ORDER BY u.id_usuario;

COMMIT;
