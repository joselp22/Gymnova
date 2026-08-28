BEGIN;

-- ============================================================
-- 1. CLASES GRUPALES: entrenador, cupo y fecha/hora
-- ============================================================
ALTER TABLE clase_grupal
    ADD COLUMN IF NOT EXISTS id_entrenador BIGINT,
    ADD COLUMN IF NOT EXISTS cupo_maximo INTEGER DEFAULT 30,
    ADD COLUMN IF NOT EXISTS fecha_hora TIMESTAMP;

-- Para registros existentes se usa temporalmente el primer entrenador activo.
-- Revise luego estas asignaciones desde la aplicacion si desea otro entrenador.
UPDATE clase_grupal cg
SET id_entrenador = (
    SELECT e.id_persona
    FROM entrenador e
    WHERE e.estado_entrenador = TRUE
    ORDER BY e.id_persona
    LIMIT 1
)
WHERE cg.id_entrenador IS NULL;

-- Horarios temporales para clases antiguas que no tenian fecha/hora.
UPDATE clase_grupal
SET fecha_hora = CURRENT_TIMESTAMP + INTERVAL '7 days'
        + (id_clase || ' hours')::interval
WHERE fecha_hora IS NULL;

UPDATE clase_grupal
SET cupo_maximo = 30
WHERE cupo_maximo IS NULL OR cupo_maximo <= 0;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM clase_grupal WHERE id_entrenador IS NULL) THEN
        RAISE EXCEPTION 'No existe un entrenador activo para completar clase_grupal.id_entrenador';
    END IF;
END $$;

ALTER TABLE clase_grupal
    ALTER COLUMN id_entrenador SET NOT NULL,
    ALTER COLUMN cupo_maximo SET NOT NULL,
    ALTER COLUMN fecha_hora SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_clase_grupal_entrenador'
    ) THEN
        ALTER TABLE clase_grupal
            ADD CONSTRAINT fk_clase_grupal_entrenador
            FOREIGN KEY (id_entrenador)
            REFERENCES entrenador(id_persona)
            ON UPDATE RESTRICT
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_clase_grupal_cupo'
    ) THEN
        ALTER TABLE clase_grupal
            ADD CONSTRAINT ck_clase_grupal_cupo
            CHECK (cupo_maximo > 0);
    END IF;
END $$;

-- ============================================================
-- 2. UN SOLO PLAN NUTRICIONAL ACTIVO POR CLIENTE
-- ============================================================
-- Si la BD antigua tuviera duplicados ACTIVO, conserva el mas reciente.
WITH duplicados AS (
    SELECT id_plan_nutricional,
           ROW_NUMBER() OVER (
               PARTITION BY id_cliente
               ORDER BY fecha_creacion DESC, id_plan_nutricional DESC
           ) AS rn
    FROM plan_nutricional
    WHERE estado_plan = 'ACTIVO'
)
UPDATE plan_nutricional pn
SET estado_plan = 'FINALIZADO',
    fecha_fin = COALESCE(fecha_fin, CURRENT_DATE)
FROM duplicados d
WHERE pn.id_plan_nutricional = d.id_plan_nutricional
  AND d.rn > 1;

CREATE UNIQUE INDEX IF NOT EXISTS ux_plan_activo_cliente
    ON plan_nutricional(id_cliente)
    WHERE estado_plan = 'ACTIVO';

-- ============================================================
-- 3. INDICES DE APOYO PARA FKs Y FLUJOS MAS USADOS
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_clase_grupal_entrenador
    ON clase_grupal(id_entrenador);
CREATE INDEX IF NOT EXISTS idx_reserva_clase_estado
    ON reserva(id_clase, estado_reserva);
CREATE INDEX IF NOT EXISTS idx_reserva_cliente
    ON reserva(id_cliente);
CREATE INDEX IF NOT EXISTS idx_membresia_cliente
    ON membresia(id_cliente);
CREATE INDEX IF NOT EXISTS idx_factura_cliente
    ON factura(id_cliente);
CREATE INDEX IF NOT EXISTS idx_pago_factura
    ON pago(id_factura);
CREATE INDEX IF NOT EXISTS idx_rutina_entrenador
    ON rutina(id_entrenador);
CREATE INDEX IF NOT EXISTS idx_plan_cliente
    ON plan_nutricional(id_cliente);
CREATE INDEX IF NOT EXISTS idx_plan_nutricionista
    ON plan_nutricional(id_nutricionista);
CREATE INDEX IF NOT EXISTS idx_bitacora_usuario
    ON bitacora(id_usuario);

COMMIT;
