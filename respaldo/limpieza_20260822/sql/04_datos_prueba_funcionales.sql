-- GYMNOVA: datos relacionados para pruebas funcionales.
-- Es idempotente: puede ejecutarse nuevamente sin duplicar los registros.
BEGIN;

INSERT INTO objetivo_fitness (nombre_objetivo, descripcion, categoria, estado_objetivo)
VALUES
 ('Ganancia muscular', 'Aumento progresivo de masa y fuerza', 'COMPOSICION', TRUE),
 ('Perdida de grasa', 'Reduccion saludable del porcentaje de grasa', 'COMPOSICION', TRUE),
 ('Resistencia general', 'Mejora del acondicionamiento cardiovascular', 'RENDIMIENTO', TRUE)
ON CONFLICT (nombre_objetivo) DO NOTHING;

INSERT INTO tipo_membresia
 (nombre, descripcion, duracion_dias, precio_base, limite_accesos, acceso_ilimitado, estado_tipo)
SELECT v.* FROM (VALUES
 ('Premium'::varchar, 'Acceso completo, clases y seguimiento'::varchar, 30, 45.00::numeric, NULL::integer, TRUE, TRUE),
 ('Basica', 'Acceso general al gimnasio', 30, 25.00::numeric, 20, FALSE, TRUE)
) v(nombre,descripcion,duracion_dias,precio_base,limite_accesos,acceso_ilimitado,estado_tipo)
WHERE NOT EXISTS (SELECT 1 FROM tipo_membresia tm WHERE tm.nombre=v.nombre);

INSERT INTO tipo_membresia_beneficio (id_tipo_membresia, beneficio)
SELECT tm.id_tipo_membresia, b.beneficio
FROM tipo_membresia tm
CROSS JOIN (VALUES
 ('Acceso general al gimnasio'),
 ('Clases grupales'),
 ('Evaluacion fisica mensual'),
 ('Seguimiento de rutina')) AS b(beneficio)
WHERE tm.nombre = 'Premium'
ON CONFLICT DO NOTHING;

INSERT INTO membresia
 (numero_membresia, fecha_inicio, fecha_fin, costo_final,
  estado_membresia, observaciones, id_cliente, id_tipo_membresia)
SELECT 'MB' || RIGHT(c.codigo_cliente, 5), CURRENT_DATE - 8,
       CURRENT_DATE + 22, tm.precio_base, 'ACTIVA',
       'Membresia funcional para demostracion', c.id_persona, tm.id_tipo_membresia
FROM cliente c CROSS JOIN tipo_membresia tm
WHERE tm.nombre = 'Premium'
ON CONFLICT (numero_membresia) DO NOTHING;

INSERT INTO clase_grupal
 (nombre_clase, descripcion, nivel, duracion_base_minutos, intensidad, estado_clase)
VALUES
 ('Spinning', 'Sesion cardiovascular sobre bicicleta', 'INTERMEDIO', 60, 'ALTA', TRUE),
 ('Movilidad funcional', 'Movilidad y recuperacion activa', 'PRINCIPIANTE', 45, 'BAJA', TRUE)
ON CONFLICT (nombre_clase) DO NOTHING;

INSERT INTO horario
 (dia_semana, hora_inicio, hora_fin, duracion_programada,
  cupo_maximo, estado_horario, observaciones)
SELECT 'VIERNES', '07:00', '08:00', 60, 20, TRUE, 'Sala Cycling'
WHERE NOT EXISTS (SELECT 1 FROM horario
 WHERE dia_semana='VIERNES' AND hora_inicio='07:00');

INSERT INTO reserva
 (codigo_reserva, fecha_hora_reserva, estado_reserva,
  asistencia_confirmada, observaciones, id_cliente, id_horario)
SELECT 'RS' || RIGHT(c.codigo_cliente, 5),
       (CURRENT_DATE + ((5 - EXTRACT(ISODOW FROM CURRENT_DATE)::int + 7) % 7))
           + TIME '07:00',
       'ACTIVA', FALSE, 'Reserva semanal de demostracion',
       c.id_persona, h.id_horario
FROM cliente c
CROSS JOIN LATERAL (
 SELECT id_horario FROM horario
 WHERE dia_semana='VIERNES' AND hora_inicio='07:00' LIMIT 1
) h
ON CONFLICT (codigo_reserva) DO UPDATE
SET fecha_hora_reserva = EXCLUDED.fecha_hora_reserva,
    estado_reserva = 'ACTIVA';

INSERT INTO asistencia
 (fecha_asistencia, hora_entrada, hora_salida, tipo_acceso,
  metodo_registro, estado_acceso, observaciones, id_cliente)
SELECT d.fecha, TIME '07:05', TIME '08:20', 'MEMBRESIA',
       'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', c.id_persona
FROM cliente c
CROSS JOIN LATERAL (
 SELECT (CURRENT_DATE - n)::date AS fecha FROM generate_series(0, 6) AS n
 WHERE (n + c.id_persona::int) % 2 = 0
) d
WHERE NOT EXISTS (SELECT 1 FROM asistencia a
 WHERE a.id_cliente=c.id_persona AND a.fecha_asistencia=d.fecha);

INSERT INTO grupo_muscular (nombre_grupo, zona_corporal, descripcion, estado_grupo)
SELECT v.* FROM (VALUES
 ('Pectorales', 'TORSO', 'Musculatura del pecho', TRUE),
 ('Hombros', 'TORSO', 'Deltoides anterior, lateral y posterior', TRUE),
 ('Piernas', 'TREN_INFERIOR', 'Cuadriceps, femorales y gluteos', TRUE),
 ('Espalda', 'TORSO', 'Dorsales y zona media', TRUE)
) v(nombre_grupo,zona_corporal,descripcion,estado_grupo)
WHERE NOT EXISTS (SELECT 1 FROM grupo_muscular g WHERE g.nombre_grupo=v.nombre_grupo);

INSERT INTO ejercicio
 (nombre_ejercicio, descripcion, tipo_ejercicio, nivel_dificultad, instrucciones)
VALUES
 ('Press de banca', 'Ejercicio principal de pecho', 'FUERZA', 'INTERMEDIO', 'Controlar el descenso de la barra'),
 ('Aperturas con mancuernas', 'Trabajo complementario de pecho', 'FUERZA', 'PRINCIPIANTE', 'Mantener los codos semiflexionados'),
 ('Press militar', 'Ejercicio principal de hombros', 'FUERZA', 'INTERMEDIO', 'Mantener el abdomen activo'),
 ('Elevaciones laterales', 'Trabajo del deltoides lateral', 'FUERZA', 'PRINCIPIANTE', 'No elevar por encima del hombro'),
 ('Sentadilla goblet', 'Trabajo general de piernas', 'FUERZA', 'PRINCIPIANTE', 'Rodillas alineadas con los pies'),
 ('Peso muerto rumano', 'Cadena posterior', 'FUERZA', 'INTERMEDIO', 'Espalda neutra durante el recorrido'),
 ('Remo sentado', 'Trabajo de espalda', 'FUERZA', 'PRINCIPIANTE', 'Llevar los codos hacia atras'),
 ('Cardio moderado', 'Trabajo cardiovascular continuo', 'CARDIO', 'PRINCIPIANTE', 'Mantener una intensidad sostenible')
ON CONFLICT (nombre_ejercicio) DO NOTHING;

INSERT INTO rutina
 (nombre_rutina, descripcion, nivel, duracion_semanas,
  fecha_creacion, estado_rutina, id_entrenador, id_objetivo)
SELECT 'Plan semanal GYMNOVA', 'Pecho y hombros alternados con piernas y cardio',
       'INTERMEDIO', 8, CURRENT_DATE - 14, 'ACTIVA', e.id_persona, o.id_objetivo
FROM (SELECT id_persona FROM entrenador WHERE estado_entrenador LIMIT 1) e
CROSS JOIN (SELECT id_objetivo FROM objetivo_fitness
            WHERE nombre_objetivo='Ganancia muscular' LIMIT 1) o
WHERE NOT EXISTS (SELECT 1 FROM rutina WHERE nombre_rutina='Plan semanal GYMNOVA');

INSERT INTO contiene_ejercicio
 (dia_semana, orden, series, repeticiones, peso_sugerido,
  duracion_minutos, descanso_segundos, id_rutina, id_ejercicio)
SELECT v.dia, v.orden, v.series, v.repeticiones, v.peso,
       v.duracion, v.descanso, r.id_rutina, e.id_ejercicio
FROM rutina r
JOIN (VALUES
 ('LUNES',1,'Press de banca',4,'8-10',40::numeric,NULL::int,90),
 ('LUNES',2,'Aperturas con mancuernas',3,'12-15',10,NULL,60),
 ('MARTES',1,'Press militar',4,'8-10',25,NULL,90),
 ('MARTES',2,'Elevaciones laterales',3,'12-15',8,NULL,60),
 ('MIERCOLES',1,'Sentadilla goblet',4,'10-12',22,NULL,90),
 ('MIERCOLES',2,'Peso muerto rumano',3,'10-12',35,NULL,90),
 ('JUEVES',1,'Press de banca',3,'10-12',35,NULL,75),
 ('JUEVES',2,'Aperturas con mancuernas',3,'15',8,NULL,60),
 ('VIERNES',1,'Press militar',3,'10-12',22,NULL,75),
 ('VIERNES',2,'Elevaciones laterales',4,'12',7,NULL,60),
 ('SABADO',1,'Cardio moderado',1,'Continuo',0,30,0)
) AS v(dia,orden,nombre,series,repeticiones,peso,duracion,descanso) ON TRUE
JOIN ejercicio e ON e.nombre_ejercicio=v.nombre
WHERE r.nombre_rutina='Plan semanal GYMNOVA'
ON CONFLICT (id_rutina,dia_semana,orden) DO UPDATE
SET id_ejercicio=EXCLUDED.id_ejercicio, series=EXCLUDED.series,
    repeticiones=EXCLUDED.repeticiones, peso_sugerido=EXCLUDED.peso_sugerido,
    duracion_minutos=EXCLUDED.duracion_minutos,
    descanso_segundos=EXCLUDED.descanso_segundos;

INSERT INTO rutina_dia_entrenamiento (id_rutina,dia_entrenamiento)
SELECT r.id_rutina, d.dia
FROM rutina r CROSS JOIN (VALUES ('LUNES'),('MARTES'),('MIERCOLES'),
 ('JUEVES'),('VIERNES'),('SABADO')) d(dia)
WHERE r.nombre_rutina='Plan semanal GYMNOVA'
ON CONFLICT DO NOTHING;

INSERT INTO asignacion_rutina
 (fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion,
  observaciones, id_cliente, id_rutina)
SELECT CURRENT_DATE - 14, CURRENT_DATE - 14, CURRENT_DATE + 42,
       'ACTIVA', 'Asignada por el entrenador', c.id_persona, r.id_rutina
FROM cliente c CROSS JOIN rutina r
WHERE r.nombre_rutina='Plan semanal GYMNOVA'
ON CONFLICT (id_cliente,id_rutina) DO UPDATE
SET estado_asignacion='ACTIVA', fecha_fin=CURRENT_DATE + 42;

INSERT INTO progreso_rutina
 (fecha_registro, sesiones_planificadas, sesiones_completadas,
  peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina)
SELECT (CURRENT_DATE - n)::date, 1,
       CASE WHEN (n+c.id_persona::int)%3=0 THEN 0 ELSE 1 END,
       c.peso_inicial - ((6-n)::numeric/10), 'MEDIO', 'REVISADO',
       c.id_persona, r.id_rutina
FROM cliente c CROSS JOIN rutina r CROSS JOIN generate_series(0,6) n
WHERE r.nombre_rutina='Plan semanal GYMNOVA'
AND NOT EXISTS (SELECT 1 FROM progreso_rutina p
 WHERE p.id_cliente=c.id_persona AND p.id_rutina=r.id_rutina
 AND p.fecha_registro=(CURRENT_DATE-n)::date);

INSERT INTO indicador_salud
 (nombre_indicador, descripcion, unidad_medida,
  valor_minimo_referencia, valor_maximo_referencia, categoria, estado_indicador)
VALUES
 ('IMC', 'Indice de masa corporal', 'kg/m2', 18.5, 24.9, 'COMPOSICION', TRUE),
 ('Porcentaje de grasa', 'Porcentaje estimado de grasa corporal', '%', 10, 25, 'COMPOSICION', TRUE)
ON CONFLICT (nombre_indicador) DO NOTHING;

INSERT INTO evaluacion_fisica
 (codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo,
  condicion_general, nivel_riesgo, proxima_evaluacion,
  estado_evaluacion, id_cliente, id_entrenador)
SELECT 'EV' || RIGHT(c.codigo_cliente,5), CURRENT_DATE - 3,
       'SEGUIMIENTO', 'Control mensual', 'BUENA', 'BAJO',
       CURRENT_DATE + 27, 'REVISADA', c.id_persona, e.id_persona
FROM cliente c CROSS JOIN (SELECT id_persona FROM entrenador LIMIT 1) e
ON CONFLICT (codigo_evaluacion) DO NOTHING;

INSERT INTO medicion_corporal
 (peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm,
  brazo_cm, muslo_cm, observaciones, id_evaluacion)
SELECT c.peso_inicial-0.8, 1.68, 20.5, 82, 96, 32, 55,
       'Medicion mensual de prueba', ev.id_evaluacion
FROM evaluacion_fisica ev JOIN cliente c ON c.id_persona=ev.id_cliente
WHERE NOT EXISTS (SELECT 1 FROM medicion_corporal m
                  WHERE m.id_evaluacion=ev.id_evaluacion);

INSERT INTO resultado_indicador
 (valor_obtenido, clasificacion, fuera_de_rango, observaciones,
  fecha_registro, id_evaluacion, id_indicador)
SELECT 23.4, 'SALUDABLE', FALSE, 'Resultado dentro del rango',
       ev.fecha_evaluacion, ev.id_evaluacion, i.id_indicador
FROM evaluacion_fisica ev CROSS JOIN indicador_salud i
WHERE i.nombre_indicador='IMC'
ON CONFLICT (id_evaluacion,id_indicador) DO NOTHING;

INSERT INTO recomendacion
 (fecha_recomendacion,tipo_recomendacion,titulo,descripcion,prioridad,
  fecha_inicio,fecha_fin,estado_recomendacion,id_evaluacion)
SELECT CURRENT_DATE-3,'ACTIVIDAD','Mantener hidratacion',
       'Consumir agua antes, durante y despues del entrenamiento','MEDIA',
       CURRENT_DATE-3,CURRENT_DATE+27,'ACTIVA',ev.id_evaluacion
FROM evaluacion_fisica ev
WHERE NOT EXISTS (SELECT 1 FROM recomendacion r
                  WHERE r.id_evaluacion=ev.id_evaluacion
                  AND r.titulo='Mantener hidratacion');

INSERT INTO alimento
 (nombre_alimento,categoria,descripcion,porcion_referencia_g,
  proteinas_g,carbohidratos_g,fibra_g)
VALUES
 ('Avena','CEREAL','Fuente de carbohidratos y fibra',60,8,36,6),
 ('Pechuga de pollo','PROTEINA','Proteina magra',150,42,0,0),
 ('Arroz integral','CEREAL','Carbohidrato complejo',120,4,34,3),
 ('Yogur natural','LACTEO','Fuente de proteina y calcio',180,10,14,0)
ON CONFLICT (nombre_alimento) DO NOTHING;

INSERT INTO plan_nutricional
 (codigo_plan,nombre_plan,fecha_creacion,fecha_inicio,fecha_fin,
  calorias_objetivo,proteinas_objetivo_g,carbohidratos_objetivo_g,
  restricciones_generales,estado_plan,id_cliente,id_nutricionista)
SELECT 'PN' || RIGHT(c.codigo_cliente,5),'Plan equilibrado '||c.codigo_cliente,
       CURRENT_DATE-5,CURRENT_DATE-5,CURRENT_DATE+25,2200,150,260,
       'Sin restricciones registradas','ACTIVO',c.id_persona,n.id_persona
FROM cliente c CROSS JOIN (SELECT id_persona FROM nutricionista LIMIT 1) n
ON CONFLICT (codigo_plan) DO UPDATE SET estado_plan='ACTIVO';

INSERT INTO incluye_alimento
 (dia_semana,tipo_comida,hora_consumo,cantidad,unidad_medida,
  orden_comida,indicaciones,estado_detalle,id_plan_nutricional,id_alimento)
SELECT d.dia,d.comida,d.hora,d.cantidad,d.unidad,d.orden,
       'Preparacion saludable','ACTIVO',p.id_plan_nutricional,a.id_alimento
FROM plan_nutricional p
JOIN (VALUES
 ('LUNES','DESAYUNO','07:30'::time,60::numeric,'g',1,'Avena'),
 ('LUNES','ALMUERZO','13:00'::time,150::numeric,'g',2,'Pechuga de pollo'),
 ('LUNES','ALMUERZO','13:00'::time,120::numeric,'g',3,'Arroz integral'),
 ('LUNES','MERIENDA','17:00'::time,180::numeric,'g',4,'Yogur natural'))
 d(dia,comida,hora,cantidad,unidad,orden,nombre) ON TRUE
JOIN alimento a ON a.nombre_alimento=d.nombre
ON CONFLICT (id_plan_nutricional,dia_semana,tipo_comida,orden_comida) DO NOTHING;

INSERT INTO metodo_pago
 (nombre_metodo,descripcion,requiere_referencia,permite_cuotas,
  porcentaje_comision,estado_metodo)
SELECT 'Tarjeta','Pago con tarjeta debito o credito',TRUE,TRUE,2.5,TRUE
WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE nombre_metodo='Tarjeta');

INSERT INTO factura
 (numero_factura,fecha_emision,total_descuento,impuesto,
  estado_factura,id_cliente)
SELECT 'FAC-'||RIGHT(c.codigo_cliente,5),CURRENT_DATE-2,0,5.40,
       'PAGADA',c.id_persona
FROM cliente c ON CONFLICT (numero_factura) DO NOTHING;

INSERT INTO detalle_factura
 (tipo_concepto,codigo_referencia,cantidad,precio_unitario,
  porcentaje_impuesto,id_factura)
SELECT 'MEMBRESIA',m.numero_membresia,1,m.costo_final,12,f.id_factura
FROM factura f JOIN cliente c ON c.id_persona=f.id_cliente
JOIN membresia m ON m.id_cliente=c.id_persona
WHERE NOT EXISTS (SELECT 1 FROM detalle_factura d WHERE d.id_factura=f.id_factura);

INSERT INTO pago
 (monto_recibido,monto_pago,codigo_pago,fecha_hora_pago,
  estado_pago,referencia_transaccion,id_factura,id_metodo_pago)
SELECT 50.40,50.40,'PG'||RIGHT(c.codigo_cliente,5),CURRENT_TIMESTAMP-INTERVAL '1 day',
       'CONFIRMADO','TRX-'||c.codigo_cliente,f.id_factura,mp.id_metodo_pago
FROM factura f JOIN cliente c ON c.id_persona=f.id_cliente
CROSS JOIN (SELECT id_metodo_pago FROM metodo_pago
            WHERE nombre_metodo='Tarjeta' LIMIT 1) mp
ON CONFLICT (codigo_pago) DO NOTHING;

INSERT INTO comprobante
 (numero_comprobante,tipo_comprobante,fecha_emision,formato_archivo,
  estado_comprobante,id_pago)
SELECT 'COMP-'||RIGHT(c.codigo_cliente,5),'RECIBO',CURRENT_DATE-1,'PDF',
       'GENERADO',p.id_pago
FROM pago p JOIN factura f ON f.id_factura=p.id_factura
JOIN cliente c ON c.id_persona=f.id_cliente
ON CONFLICT (numero_comprobante) DO NOTHING;

INSERT INTO categoria_producto
 (nombre_categoria,descripcion,requiere_caducidad,estado_categoria)
VALUES ('Suplementos','Productos de apoyo nutricional',TRUE,TRUE),
       ('Accesorios','Accesorios deportivos',FALSE,TRUE)
ON CONFLICT (nombre_categoria) DO NOTHING;

INSERT INTO producto
 (codigo_producto,codigo_barras,nombre_producto,marca,unidad_medida,
  precio_compra,precio_venta,margen_ganancia,stock_actual,stock_minimo,
  estado_producto,id_categoria_producto)
SELECT 'PR00001','786000000001','Proteina 1 kg','GYMNOVA','UNIDAD',28,42,50,12,4,TRUE,
       id_categoria_producto FROM categoria_producto WHERE nombre_categoria='Suplementos'
ON CONFLICT (codigo_producto) DO NOTHING;

INSERT INTO proveedor
 (ruc,razon_social,nombre_comercial,correo,direccion_proveedor,
  nombre_contacto,fecha_registro)
VALUES ('1799999999001','Deportes Andinos S.A.','Deportes Andinos',
        'ventas@deportesandinos.ec','Quito, Ecuador','Sofia Torres',CURRENT_DATE-30)
ON CONFLICT (ruc) DO NOTHING;

INSERT INTO suministra_producto
 (id_proveedor,id_producto,codigo_producto_proveedor,costo_referencia,
  tiempo_entrega_dias,cantidad_minima_pedido,fecha_actualizacion)
SELECT pr.id_proveedor,p.id_producto,'DA-PROT-01',28,3,2,CURRENT_DATE
FROM proveedor pr CROSS JOIN producto p
WHERE pr.ruc='1799999999001' AND p.codigo_producto='PR00001'
ON CONFLICT DO NOTHING;

INSERT INTO tipo_equipo
 (nombre_tipo,requiere_electricidad,periodicidad_base_dias)
SELECT 'Cardiovascular',TRUE,30
WHERE NOT EXISTS (SELECT 1 FROM tipo_equipo WHERE nombre_tipo='Cardiovascular');

INSERT INTO equipo
 (codigo_interno,nombre_equipo,marca,modelo,fecha_adquisicion,
  costo_adquisicion,vida_util_anios,ubicacion,estado_equipo,id_tipo_equipo)
SELECT 'EQ00001','Caminadora electrica','FitPro','Run X2',CURRENT_DATE-180,
       1800,8,'Zona cardio',TRUE,id_tipo_equipo
FROM tipo_equipo WHERE nombre_tipo='Cardiovascular'
ON CONFLICT (codigo_interno) DO NOTHING;

INSERT INTO tipo_mantenimiento
 (nombre_tipo,requiere_repuestos,requiere_detener_equipo,
  periodicidad_recomendada_dias)
SELECT 'Preventivo',FALSE,TRUE,30
WHERE NOT EXISTS (SELECT 1 FROM tipo_mantenimiento WHERE nombre_tipo='Preventivo');

INSERT INTO mantenimiento
 (numero_mantenimiento,fecha_solicitud,fecha_programada,
  costo_repuestos,costo_mano_obra,descripcion_falla,
  estado_mantenimiento,observaciones,id_equipo,id_tipo_mantenimiento,id_empleado)
SELECT 'MT00001',CURRENT_DATE,CURRENT_DATE+3,0,0,'Revision mensual programada',
       'PROGRAMADO','Verificar banda y lubricacion',e.id_equipo,
       tm.id_tipo_mantenimiento,em.id_persona
FROM equipo e CROSS JOIN tipo_mantenimiento tm
CROSS JOIN (SELECT id_persona FROM empleado LIMIT 1) em
WHERE e.codigo_interno='EQ00001' AND tm.nombre_tipo='Preventivo'
ON CONFLICT (numero_mantenimiento) DO NOTHING;

COMMIT;

-- Resumen de comprobacion para la profesora.
SELECT 'membresias' AS entidad, COUNT(*) AS registros FROM membresia
UNION ALL SELECT 'reservas',COUNT(*) FROM reserva
UNION ALL SELECT 'asistencias',COUNT(*) FROM asistencia
UNION ALL SELECT 'rutinas',COUNT(*) FROM rutina
UNION ALL SELECT 'ejercicios de rutina',COUNT(*) FROM contiene_ejercicio
UNION ALL SELECT 'progresos',COUNT(*) FROM progreso_rutina
UNION ALL SELECT 'evaluaciones',COUNT(*) FROM evaluacion_fisica
UNION ALL SELECT 'planes nutricionales',COUNT(*) FROM plan_nutricional
UNION ALL SELECT 'pagos',COUNT(*) FROM pago
UNION ALL SELECT 'productos',COUNT(*) FROM producto
UNION ALL SELECT 'proveedores',COUNT(*) FROM proveedor
UNION ALL SELECT 'equipos',COUNT(*) FROM equipo
ORDER BY entidad;
