-- ============================================================================
-- GYMNOVA - Migracion corregida del flujo integral de rutinas
-- Ejecutar UNA SOLA VEZ sobre la base actual.
-- Es segura para volver a ejecutar: usa IF EXISTS / IF NOT EXISTS cuando aplica.
-- ============================================================================

-- Si el intento anterior dejo la sesion dentro de una transaccion abortada,
-- este ROLLBACK la limpia. Si no hay transaccion activa, PostgreSQL solo mostrara
-- una advertencia y continuara.
ROLLBACK;

BEGIN;

-- --------------------------------------------------------------------------
-- 1. Registro detallado del progreso por actividad
-- --------------------------------------------------------------------------
ALTER TABLE progreso_rutina
    ADD COLUMN IF NOT EXISTS detalle_ejercicios JSONB,
    ADD COLUMN IF NOT EXISTS fecha_hora_actualizacion TIMESTAMP;

UPDATE progreso_rutina
SET detalle_ejercicios = '{}'::jsonb
WHERE detalle_ejercicios IS NULL;

ALTER TABLE progreso_rutina
    ALTER COLUMN detalle_ejercicios SET DEFAULT '{}'::jsonb,
    ALTER COLUMN detalle_ejercicios SET NOT NULL;

ALTER TABLE progreso_rutina
    DROP CONSTRAINT IF EXISTS ck_progreso_rutina_detalle_json;

ALTER TABLE progreso_rutina
    ADD CONSTRAINT ck_progreso_rutina_detalle_json
    CHECK (jsonb_typeof(detalle_ejercicios) = 'object');

-- --------------------------------------------------------------------------
-- 2. Corregir la dependencia que impedia reutilizar una rutina
--
-- La BD original relacionaba progreso_rutina -> asignacion_rutina mediante
-- (id_cliente, id_rutina). Esa FK depende del UNIQUE del mismo par y por eso
-- PostgreSQL no permite quitar uq_asignacion_rutina_cliente_rutina directamente.
--
-- El progreso ya guarda id_cliente, id_rutina y fecha_registro, y el sistema
-- resuelve la asignacion correspondiente por periodo. Conservamos integridad
-- referencial haciendo que cliente y rutina deban existir, sin obligar a que el
-- par cliente/rutina sea unico para siempre.
-- --------------------------------------------------------------------------
ALTER TABLE progreso_rutina
    DROP CONSTRAINT IF EXISTS fk_progreso_rutina_asignacion_rutina;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'progreso_rutina'::regclass
          AND conname = 'fk_progreso_rutina_cliente'
    ) THEN
        ALTER TABLE progreso_rutina
            ADD CONSTRAINT fk_progreso_rutina_cliente
            FOREIGN KEY (id_cliente)
            REFERENCES cliente(id_persona)
            ON UPDATE RESTRICT ON DELETE RESTRICT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'progreso_rutina'::regclass
          AND conname = 'fk_progreso_rutina_rutina'
    ) THEN
        ALTER TABLE progreso_rutina
            ADD CONSTRAINT fk_progreso_rutina_rutina
            FOREIGN KEY (id_rutina)
            REFERENCES rutina(id_rutina)
            ON UPDATE RESTRICT ON DELETE RESTRICT;
    END IF;
END $$;

-- Ahora ya se puede eliminar el UNIQUE antiguo sin CASCADE.
ALTER TABLE asignacion_rutina
    DROP CONSTRAINT IF EXISTS uq_asignacion_rutina_cliente_rutina;

-- Evita duplicar exactamente la misma asignacion en la misma fecha de inicio,
-- pero permite volver a usar la misma rutina con el mismo cliente en otro periodo.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'asignacion_rutina'::regclass
          AND conname = 'uq_asignacion_rutina_cliente_rutina_inicio'
    ) THEN
        ALTER TABLE asignacion_rutina
            ADD CONSTRAINT uq_asignacion_rutina_cliente_rutina_inicio
            UNIQUE (id_cliente, id_rutina, fecha_inicio);
    END IF;
END $$;

-- --------------------------------------------------------------------------
-- 3. Estado PROGRAMADA para asignaciones que comienzan en el futuro
-- --------------------------------------------------------------------------
ALTER TABLE asignacion_rutina
    DROP CONSTRAINT IF EXISTS ck_asignacion_rutina_estado;

ALTER TABLE asignacion_rutina
    ADD CONSTRAINT ck_asignacion_rutina_estado
    CHECK (estado_asignacion IN
        ('PROGRAMADA','ACTIVA','FINALIZADA','PAUSADA','CANCELADA'));

-- Normaliza estados existentes segun fechas.
UPDATE asignacion_rutina
SET estado_asignacion = 'FINALIZADA',
    motivo_finalizacion = COALESCE(motivo_finalizacion, 'Finalizada por vencimiento')
WHERE estado_asignacion IN ('ACTIVA','PAUSADA')
  AND fecha_fin IS NOT NULL
  AND fecha_fin < CURRENT_DATE;

UPDATE asignacion_rutina
SET estado_asignacion = 'PROGRAMADA'
WHERE estado_asignacion = 'ACTIVA'
  AND fecha_inicio > CURRENT_DATE;

-- --------------------------------------------------------------------------
-- 4. Integridad entre los dias de la rutina y el detalle de ejercicios
-- --------------------------------------------------------------------------
INSERT INTO rutina_dia_entrenamiento (id_rutina, dia_entrenamiento)
SELECT DISTINCT ce.id_rutina, ce.dia_semana
FROM contiene_ejercicio ce
LEFT JOIN rutina_dia_entrenamiento rd
  ON rd.id_rutina = ce.id_rutina
 AND rd.dia_entrenamiento = ce.dia_semana
WHERE rd.id_rutina IS NULL
ON CONFLICT DO NOTHING;

ALTER TABLE contiene_ejercicio
    DROP CONSTRAINT IF EXISTS fk_contiene_ejercicio_dia_rutina;

ALTER TABLE contiene_ejercicio
    ADD CONSTRAINT fk_contiene_ejercicio_dia_rutina
    FOREIGN KEY (id_rutina, dia_semana)
    REFERENCES rutina_dia_entrenamiento(id_rutina, dia_entrenamiento)
    ON UPDATE CASCADE ON DELETE RESTRICT;

-- --------------------------------------------------------------------------
-- 5. Indices de apoyo
-- --------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_asignacion_rutina_cliente_periodo
    ON asignacion_rutina (id_cliente, fecha_inicio, fecha_fin, estado_asignacion);

CREATE INDEX IF NOT EXISTS idx_contiene_ejercicio_rutina_dia
    ON contiene_ejercicio (id_rutina, dia_semana, orden);

CREATE INDEX IF NOT EXISTS idx_progreso_rutina_cliente_fecha
    ON progreso_rutina (id_cliente, fecha_registro, id_rutina);

COMMIT;

-- --------------------------------------------------------------------------
-- 6. Verificacion rapida (solo lectura)
-- --------------------------------------------------------------------------
SELECT estado_asignacion, COUNT(*)
FROM asignacion_rutina
GROUP BY estado_asignacion
ORDER BY estado_asignacion;

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'progreso_rutina'
  AND column_name IN ('detalle_ejercicios', 'fecha_hora_actualizacion')
ORDER BY column_name;

SELECT conrelid::regclass AS tabla,
       conname AS restriccion,
       pg_get_constraintdef(oid) AS definicion
FROM pg_constraint
WHERE conname IN (
    'fk_progreso_rutina_cliente',
    'fk_progreso_rutina_rutina',
    'uq_asignacion_rutina_cliente_rutina_inicio',
    'ck_asignacion_rutina_estado',
    'fk_contiene_ejercicio_dia_rutina'
)
ORDER BY tabla::text, restriccion;
