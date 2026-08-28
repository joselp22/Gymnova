-- ============================================================
-- GYMNOVA - ACTIVIDADES PROPIAS DEL ENTRENADOR
-- Ejecutar UNA VEZ sobre la base existente.
--
-- No elimina ejercicios actuales:
-- los existentes quedan como actividades globales (id_entrenador NULL).
-- Las nuevas actividades creadas desde la vista del entrenador se guardan
-- con el id del entrenador autenticado.
-- ============================================================

ROLLBACK;
BEGIN;

ALTER TABLE public.ejercicio
    ADD COLUMN IF NOT EXISTS id_entrenador BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_ejercicio_entrenador'
          AND conrelid = 'public.ejercicio'::regclass
    ) THEN
        ALTER TABLE public.ejercicio
            ADD CONSTRAINT fk_ejercicio_entrenador
            FOREIGN KEY (id_entrenador)
            REFERENCES public.entrenador(id_persona)
            ON UPDATE RESTRICT
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_ejercicio_entrenador
    ON public.ejercicio(id_entrenador);

COMMIT;

-- Verificacion.
SELECT
    id_ejercicio,
    nombre_ejercicio,
    tipo_ejercicio,
    nivel_dificultad,
    id_entrenador
FROM public.ejercicio
ORDER BY id_ejercicio;
