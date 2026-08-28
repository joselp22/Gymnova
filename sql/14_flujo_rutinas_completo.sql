BEGIN;

-- ================================================================
-- GYMNOVA - Correccion integral del flujo de rutinas
-- 1) Permite asignaciones futuras (PROGRAMADA).
-- 2) Permite reutilizar una misma rutina en periodos distintos.
-- 3) Habilita el registro por actividad dentro de progreso_rutina.
-- 4) Refuerza la relacion entre dias de rutina y su detalle.
-- ================================================================

-- Registro detallado de actividades realizadas sin crear una tabla adicional.
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

-- Una rutina puede volver a asignarse al mismo cliente en otro periodo.
ALTER TABLE asignacion_rutina
    DROP CONSTRAINT IF EXISTS uq_asignacion_rutina_cliente_rutina;
ALTER TABLE asignacion_rutina
    DROP CONSTRAINT IF EXISTS uq_asignacion_rutina_cliente_rutina_inicio;
ALTER TABLE asignacion_rutina
    ADD CONSTRAINT uq_asignacion_rutina_cliente_rutina_inicio
    UNIQUE (id_cliente, id_rutina, fecha_inicio);

-- Se agrega PROGRAMADA para rutinas que comienzan en una fecha futura.
ALTER TABLE asignacion_rutina
    DROP CONSTRAINT IF EXISTS ck_asignacion_rutina_estado;
ALTER TABLE asignacion_rutina
    ADD CONSTRAINT ck_asignacion_rutina_estado
    CHECK (estado_asignacion IN
        ('PROGRAMADA','ACTIVA','FINALIZADA','PAUSADA','CANCELADA'));

-- Normaliza estados existentes segun sus fechas.
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

-- Asegura que todo dia usado por contiene_ejercicio exista en la plantilla.
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

CREATE INDEX IF NOT EXISTS idx_asignacion_rutina_cliente_periodo
    ON asignacion_rutina (id_cliente, fecha_inicio, fecha_fin, estado_asignacion);
CREATE INDEX IF NOT EXISTS idx_contiene_ejercicio_rutina_dia
    ON contiene_ejercicio (id_rutina, dia_semana, orden);
CREATE INDEX IF NOT EXISTS idx_progreso_rutina_cliente_fecha
    ON progreso_rutina (id_cliente, fecha_registro, id_rutina);

COMMIT;

-- Verificacion rapida
SELECT estado_asignacion, COUNT(*)
FROM asignacion_rutina
GROUP BY estado_asignacion
ORDER BY estado_asignacion;

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'progreso_rutina'
  AND column_name IN ('detalle_ejercicios', 'fecha_hora_actualizacion')
ORDER BY column_name;
