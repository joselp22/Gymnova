-- =====================================================================
-- 17_reparar_perfiles_profesionales.sql
--
-- Repara usuarios cuya cuenta se creó con rol NUTRICIONISTA o
-- ENTRENADOR pero cuyo perfil profesional NO existe en las tablas
-- `empleado` y su subtipo (`nutricionista` / `entrenador`). Cuando eso
-- pasa, la app rebota con:
--   "insert or update on table X violates foreign key constraint
--    fk_..._nutricionista (o _entrenador)"
--
-- Este script:
--   1) Lista los usuarios con esa incoherencia.
--   2) Para cada uno, inserta la fila faltante en `empleado`
--      (contrato SERVICIOS_PROFESIONALES, turno 08:00-17:00) usando el
--      siguiente código EM##### disponible.
--   3) Inserta la fila faltante en el subtipo (`nutricionista` con
--      número de licencia sintético o `entrenador`).
--
-- Es idempotente: si el perfil ya existe, no lo duplica.
-- Se ejecuta en una transacción.
-- =====================================================================

BEGIN;

-- 1) Estado ANTES ------------------------------------------------------

\echo '--- Usuarios NUTRICIONISTA sin perfil profesional completo ---'
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       (SELECT nombre_rol FROM rol WHERE id_rol = u.id_rol) AS rol,
       (SELECT 1 FROM empleado e WHERE e.id_persona = u.id_persona) IS NOT NULL
           AS es_empleado,
       (SELECT 1 FROM nutricionista n WHERE n.id_persona = u.id_persona) IS NOT NULL
           AS es_nutricionista
  FROM usuario u
  JOIN rol r ON r.id_rol = u.id_rol
 WHERE r.nombre_rol ILIKE 'NUTRICIONISTA'
   AND u.id_persona IS NOT NULL
   AND (NOT EXISTS (SELECT 1 FROM empleado e WHERE e.id_persona = u.id_persona)
        OR NOT EXISTS (SELECT 1 FROM nutricionista n
                        WHERE n.id_persona = u.id_persona));

\echo '--- Usuarios ENTRENADOR sin perfil profesional completo ---'
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       (SELECT nombre_rol FROM rol WHERE id_rol = u.id_rol) AS rol,
       (SELECT 1 FROM empleado e WHERE e.id_persona = u.id_persona) IS NOT NULL
           AS es_empleado,
       (SELECT 1 FROM entrenador n WHERE n.id_persona = u.id_persona) IS NOT NULL
           AS es_entrenador
  FROM usuario u
  JOIN rol r ON r.id_rol = u.id_rol
 WHERE r.nombre_rol ILIKE 'ENTRENADOR'
   AND u.id_persona IS NOT NULL
   AND (NOT EXISTS (SELECT 1 FROM empleado e WHERE e.id_persona = u.id_persona)
        OR NOT EXISTS (SELECT 1 FROM entrenador n
                        WHERE n.id_persona = u.id_persona));


-- 2) Reparar perfiles -------------------------------------------------

DO $$
DECLARE
    v_id_persona bigint;
    v_rol text;
    v_codigo text;
    v_siguiente int;
BEGIN
    FOR v_id_persona, v_rol IN
        SELECT u.id_persona, UPPER(r.nombre_rol)
          FROM usuario u
          JOIN rol r ON r.id_rol = u.id_rol
         WHERE UPPER(r.nombre_rol) IN ('NUTRICIONISTA', 'ENTRENADOR')
           AND u.id_persona IS NOT NULL
    LOOP
        -- 2a) Empleado: crearlo si no existe.
        IF NOT EXISTS (SELECT 1 FROM empleado WHERE id_persona = v_id_persona) THEN
            SELECT COALESCE(MAX(
                        (SUBSTRING(codigo_empleado FROM 3))::int
                   ), 0) + 1
              INTO v_siguiente
              FROM empleado
             WHERE codigo_empleado ~ '^EM[0-9]{5}$';
            v_codigo := 'EM' || LPAD(v_siguiente::text, 5, '0');

            INSERT INTO empleado (
                id_persona, codigo_empleado, fecha_ingreso, tipo_contrato,
                salario, hora_inicio, hora_fin, estado_empleado)
            VALUES (
                v_id_persona, v_codigo, CURRENT_DATE,
                'SERVICIOS_PROFESIONALES', 0.00,
                TIME '08:00', TIME '17:00', true);
            RAISE NOTICE 'Empleado creado para persona % con código %.',
                v_id_persona, v_codigo;
        END IF;

        -- 2b) Subtipo profesional.
        IF v_rol = 'NUTRICIONISTA' THEN
            IF NOT EXISTS (SELECT 1 FROM nutricionista
                            WHERE id_persona = v_id_persona) THEN
                INSERT INTO nutricionista (
                    id_persona, numero_licencia, fecha_inicio_profesion,
                    estado_licencia)
                VALUES (
                    v_id_persona,
                    'LIC-' || LPAD(v_id_persona::text, 6, '0'),
                    CURRENT_DATE, 'ACTIVA');
                RAISE NOTICE 'Nutricionista creado para persona %.',
                    v_id_persona;
            END IF;
        ELSIF v_rol = 'ENTRENADOR' THEN
            IF NOT EXISTS (SELECT 1 FROM entrenador
                            WHERE id_persona = v_id_persona) THEN
                INSERT INTO entrenador (
                    id_persona, fecha_inicio_profesion,
                    nivel_entrenador, estado_entrenador)
                VALUES (
                    v_id_persona, CURRENT_DATE,
                    'BASICO', true);
                RAISE NOTICE 'Entrenador creado para persona %.',
                    v_id_persona;
            END IF;
        END IF;
    END LOOP;
END $$;


-- 3) Estado DESPUES ---------------------------------------------------

\echo '--- Usuarios NUTRICIONISTA ya con perfil completo ---'
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       e.codigo_empleado, n.numero_licencia, n.estado_licencia
  FROM usuario u
  JOIN rol r  ON r.id_rol = u.id_rol
  JOIN empleado e ON e.id_persona = u.id_persona
  JOIN nutricionista n ON n.id_persona = u.id_persona
 WHERE UPPER(r.nombre_rol) = 'NUTRICIONISTA'
 ORDER BY u.id_usuario;

\echo '--- Usuarios ENTRENADOR ya con perfil completo ---'
SELECT u.id_usuario, u.nombre_usuario, u.id_persona,
       e.codigo_empleado, en.nivel_entrenador, en.estado_entrenador
  FROM usuario u
  JOIN rol r  ON r.id_rol = u.id_rol
  JOIN empleado e ON e.id_persona = u.id_persona
  JOIN entrenador en ON en.id_persona = u.id_persona
 WHERE UPPER(r.nombre_rol) = 'ENTRENADOR'
 ORDER BY u.id_usuario;

COMMIT;
