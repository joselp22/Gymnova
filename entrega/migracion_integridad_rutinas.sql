-- ============================================================
-- GYMNOVA - AJUSTE DE INTEGRIDAD PARA ACTUALIZACION DE RUTINAS
-- Compatible con el backup backgym(1) revisado.
-- No elimina rutinas, asignaciones ni progreso.
-- ============================================================

ROLLBACK;
BEGIN;

-- 1) Normalizar cualquier dato legado de duración.
-- La regla correcta es: NULL (no aplica) o un entero > 0.
UPDATE contiene_ejercicio
SET duracion_minutos = NULL
WHERE duracion_minutos IS NOT NULL
  AND duracion_minutos <= 0;

-- 2) Garantizar que todo día utilizado en el detalle exista en
-- rutina_dia_entrenamiento antes de agregar la FK compuesta.
INSERT INTO rutina_dia_entrenamiento (id_rutina, dia_entrenamiento)
SELECT DISTINCT ce.id_rutina, ce.dia_semana
FROM contiene_ejercicio ce
LEFT JOIN rutina_dia_entrenamiento rd
       ON rd.id_rutina = ce.id_rutina
      AND rd.dia_entrenamiento = ce.dia_semana
WHERE rd.id_rutina IS NULL
ON CONFLICT DO NOTHING;

-- 3) Relacionar formalmente el día del detalle con los días de la rutina.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_contiene_ejercicio_dia_rutina'
          AND conrelid = 'contiene_ejercicio'::regclass
    ) THEN
        ALTER TABLE contiene_ejercicio
            ADD CONSTRAINT fk_contiene_ejercicio_dia_rutina
            FOREIGN KEY (id_rutina, dia_semana)
            REFERENCES rutina_dia_entrenamiento(id_rutina, dia_entrenamiento)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;
    END IF;
END $$;

COMMIT;

-- ============================================================
-- VERIFICACIONES
-- Todas deben devolver 0 filas.
-- ============================================================

-- Duraciones inválidas
SELECT *
FROM contiene_ejercicio
WHERE duracion_minutos IS NOT NULL
  AND duracion_minutos <= 0;

-- Órdenes duplicados dentro del mismo día de la misma rutina
SELECT id_rutina, dia_semana, orden, COUNT(*) AS cantidad
FROM contiene_ejercicio
GROUP BY id_rutina, dia_semana, orden
HAVING COUNT(*) > 1;

-- Días del detalle que no estén registrados en la rutina
SELECT ce.id_rutina, ce.dia_semana
FROM contiene_ejercicio ce
LEFT JOIN rutina_dia_entrenamiento rd
       ON rd.id_rutina = ce.id_rutina
      AND rd.dia_entrenamiento = ce.dia_semana
WHERE rd.id_rutina IS NULL;

-- Actividades huérfanas (debe ser 0 por la FK existente)
SELECT ce.id_rutina_ejercicio, ce.id_ejercicio
FROM contiene_ejercicio ce
LEFT JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio
WHERE e.id_ejercicio IS NULL;
