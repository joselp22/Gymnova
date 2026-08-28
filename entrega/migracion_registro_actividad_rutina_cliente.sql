-- GYMNOVA - Registro diario de actividades realizadas por el cliente
-- Reutiliza progreso_rutina para mantener el esquema en 35 tablas.
-- Cada fila diaria mantiene el resumen sesiones_planificadas/completadas y,
-- adicionalmente, un JSONB con los id_rutina_ejercicio ya completados.

ROLLBACK;
BEGIN;

ALTER TABLE progreso_rutina
    ADD COLUMN IF NOT EXISTS detalle_ejercicios JSONB,
    ADD COLUMN IF NOT EXISTS fecha_hora_actualizacion TIMESTAMP;

UPDATE progreso_rutina
SET detalle_ejercicios = '{}'::jsonb
WHERE detalle_ejercicios IS NULL;

ALTER TABLE progreso_rutina
    ALTER COLUMN detalle_ejercicios SET DEFAULT '{}'::jsonb,
    ALTER COLUMN detalle_ejercicios SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_progreso_rutina_detalle_json'
          AND conrelid = 'progreso_rutina'::regclass
    ) THEN
        ALTER TABLE progreso_rutina
            ADD CONSTRAINT ck_progreso_rutina_detalle_json
            CHECK (jsonb_typeof(detalle_ejercicios) = 'object');
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_progreso_cliente_fecha_rutina
    ON progreso_rutina(id_cliente, fecha_registro, id_rutina);

COMMIT;

-- Verificacion
SELECT
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'progreso_rutina'
  AND column_name IN ('detalle_ejercicios', 'fecha_hora_actualizacion')
ORDER BY column_name;
