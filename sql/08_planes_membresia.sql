-- GYMNOVA: catalogo comercial simple de tres planes.
BEGIN;

UPDATE tipo_membresia SET descripcion='Acceso a sala general en horario regular',
 duracion_dias=30, precio_base=25, limite_accesos=12,
 acceso_ilimitado=FALSE, estado_tipo=TRUE WHERE nombre='Basica';
UPDATE tipo_membresia SET descripcion='Acceso ilimitado y clases grupales',
 duracion_dias=30, precio_base=45, limite_accesos=NULL,
 acceso_ilimitado=TRUE, estado_tipo=TRUE WHERE nombre='Premium';
UPDATE tipo_membresia SET descripcion='Acceso ilimitado, clases, evaluacion y seguimiento',
 duracion_dias=30, precio_base=65, limite_accesos=NULL,
 acceso_ilimitado=TRUE, estado_tipo=TRUE WHERE nombre='Elite';

INSERT INTO tipo_membresia(nombre,descripcion,duracion_dias,precio_base,
 limite_accesos,acceso_ilimitado,estado_tipo)
SELECT 'Basica','Acceso a sala general en horario regular',30,25,12,FALSE,TRUE
WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre='Basica');
INSERT INTO tipo_membresia(nombre,descripcion,duracion_dias,precio_base,
 limite_accesos,acceso_ilimitado,estado_tipo)
SELECT 'Premium','Acceso ilimitado y clases grupales',30,45,NULL,TRUE,TRUE
WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre='Premium');
INSERT INTO tipo_membresia(nombre,descripcion,duracion_dias,precio_base,
 limite_accesos,acceso_ilimitado,estado_tipo)
SELECT 'Elite','Acceso ilimitado, clases, evaluacion y seguimiento',30,65,NULL,TRUE,TRUE
WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia WHERE nombre='Elite');

-- No se borran tipos usados historicamente; solamente se ocultan del catalogo.
UPDATE tipo_membresia
SET estado_tipo=FALSE
WHERE nombre NOT IN ('Basica','Premium','Elite');

COMMIT;

SELECT nombre, descripcion, precio_base, duracion_dias, estado_tipo
FROM tipo_membresia WHERE estado_tipo=TRUE ORDER BY precio_base;
