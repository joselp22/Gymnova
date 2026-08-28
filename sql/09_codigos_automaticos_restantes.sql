BEGIN;

CREATE OR REPLACE FUNCTION gymnova_codigo_automatico()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    campo text := TG_ARGV[0];
    prefijo text := TG_ARGV[1];
    codigo_actual text;
    siguiente integer;
BEGIN
    EXECUTE format('SELECT ($1).%I::text', campo)
       INTO codigo_actual USING NEW;
    IF codigo_actual IS NOT NULL
       AND btrim(codigo_actual) <> ''
       AND upper(codigo_actual) <> 'SE GENERA AUTOMATICAMENTE' THEN
        RETURN NEW;
    END IF;

    PERFORM pg_advisory_xact_lock(hashtext(TG_TABLE_NAME || '.' || campo));
    EXECUTE format(
        'SELECT COALESCE(MAX(substring(%I from ''([0-9]{5})$'')::integer),0)+1 '
        || 'FROM %I WHERE %I ~ $1', campo, TG_TABLE_NAME, campo)
       INTO siguiente USING ('^' || prefijo || '[0-9]{5}$');
    IF siguiente > 99999 THEN
        RAISE EXCEPTION 'Se agotaron los codigos para %', TG_TABLE_NAME;
    END IF;
    NEW := jsonb_populate_record(
        NEW, jsonb_build_object(campo, prefijo || lpad(siguiente::text, 5, '0'))
    );
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_codigo_membresia ON membresia;
CREATE TRIGGER trg_codigo_membresia
BEFORE INSERT ON membresia FOR EACH ROW
EXECUTE FUNCTION gymnova_codigo_automatico('numero_membresia', 'MB');

DROP TRIGGER IF EXISTS trg_codigo_compra ON compra;
CREATE TRIGGER trg_codigo_compra
BEFORE INSERT ON compra FOR EACH ROW
EXECUTE FUNCTION gymnova_codigo_automatico('numero_compra', 'CP');

DROP TRIGGER IF EXISTS trg_codigo_mantenimiento ON mantenimiento;
CREATE TRIGGER trg_codigo_mantenimiento
BEFORE INSERT ON mantenimiento FOR EACH ROW
EXECUTE FUNCTION gymnova_codigo_automatico('numero_mantenimiento', 'MT');

DROP TRIGGER IF EXISTS trg_codigo_comprobante ON comprobante;
CREATE TRIGGER trg_codigo_comprobante
BEFORE INSERT ON comprobante FOR EACH ROW
EXECUTE FUNCTION gymnova_codigo_automatico('numero_comprobante', 'CO');

COMMIT;
