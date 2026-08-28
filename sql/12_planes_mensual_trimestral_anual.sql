-- GYMNOVA: catalogo comercial fijo con planes Mensual, Trimestral y Anual.
--
-- Estado esperado antes de correr esto:
--   - tabla tipo_membresia existe (schema publico)
--   - id_tipo_membresia es GENERATED ALWAYS AS IDENTITY (Postgres asigna el id)
--   - la tabla puede estar vacia o tener planes previos (Basica/Premium/etc.)
--
-- Idempotente: se puede ejecutar varias veces sin duplicar filas ni ids.
-- Los planes historicos que no sean Mensual/Trimestral/Anual quedan como
-- estado_tipo=FALSE (ocultos), nunca se borran para no romper la FK
-- fk_membresia_tipo_membresia (ON DELETE RESTRICT).

BEGIN;

-- 1) Actualiza valores si los tres planes estandar ya existen.
UPDATE tipo_membresia
   SET descripcion = 'Membresia mensual con acceso completo',
       duracion_dias = 30,
       precio_base = 25,
       limite_accesos = NULL,
       acceso_ilimitado = TRUE,
       estado_tipo = TRUE
 WHERE nombre = 'Mensual';

UPDATE tipo_membresia
   SET descripcion = 'Membresia trimestral con acceso completo',
       duracion_dias = 90,
       precio_base = 70,
       limite_accesos = NULL,
       acceso_ilimitado = TRUE,
       estado_tipo = TRUE
 WHERE nombre = 'Trimestral';

UPDATE tipo_membresia
   SET descripcion = 'Membresia anual con acceso completo',
       duracion_dias = 365,
       precio_base = 250,
       limite_accesos = NULL,
       acceso_ilimitado = TRUE,
       estado_tipo = TRUE
 WHERE nombre = 'Anual';

-- 2) Crea los que faltan. No se especifica id (IDENTITY lo asigna solo).
INSERT INTO tipo_membresia (nombre, descripcion, duracion_dias, precio_base,
                            limite_accesos, acceso_ilimitado, estado_tipo)
SELECT 'Mensual', 'Membresia mensual con acceso completo',
       30, 25, NULL, TRUE, TRUE
 WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre = 'Mensual');

INSERT INTO tipo_membresia (nombre, descripcion, duracion_dias, precio_base,
                            limite_accesos, acceso_ilimitado, estado_tipo)
SELECT 'Trimestral', 'Membresia trimestral con acceso completo',
       90, 70, NULL, TRUE, TRUE
 WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre = 'Trimestral');

INSERT INTO tipo_membresia (nombre, descripcion, duracion_dias, precio_base,
                            limite_accesos, acceso_ilimitado, estado_tipo)
SELECT 'Anual', 'Membresia anual con acceso completo',
       365, 250, NULL, TRUE, TRUE
 WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre = 'Anual');

-- 3) Desactiva cualquier otro plan (Basica, Premium, Elite, etc.). No se
--    borran para respetar la FK ON DELETE RESTRICT desde membresia.
UPDATE tipo_membresia
   SET estado_tipo = FALSE
 WHERE nombre NOT IN ('Mensual', 'Trimestral', 'Anual');

COMMIT;

-- Verificacion: deben quedar exactamente tres planes activos, ordenados
-- por duracion (30, 90, 365).
SELECT id_tipo_membresia, nombre, precio_base, duracion_dias, estado_tipo
  FROM tipo_membresia
 WHERE estado_tipo = TRUE
 ORDER BY duracion_dias;
