-- ==================================================================
-- GYMNOVA - Dump de base de datos gymnova_db (v2, con IDENTITY).
-- Generado el Sun Aug 23 16:41:40 ECT 2026
-- Tablas: 35. Todas las PK autoincrementales quedan como
-- 'GENERATED ALWAYS AS IDENTITY' o con su secuencia respectiva.
-- Restaurar: psql -U postgres -d gymnova_db -f gymnova_db_entrega.sql
-- ==================================================================
SET client_encoding='UTF8';
SET standard_conforming_strings=on;
SET check_function_bodies=false;
SET client_min_messages=warning;

-- ----- DROP TABLE y DROP SEQUENCE -----
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS tipo_membresia CASCADE;
DROP TABLE IF EXISTS rutina_dia_entrenamiento CASCADE;
DROP TABLE IF EXISTS rutina CASCADE;
DROP TABLE IF EXISTS rol_permiso CASCADE;
DROP TABLE IF EXISTS rol CASCADE;
DROP TABLE IF EXISTS resultado_indicador CASCADE;
DROP TABLE IF EXISTS reserva CASCADE;
DROP TABLE IF EXISTS recomendacion CASCADE;
DROP TABLE IF EXISTS progreso_rutina CASCADE;
DROP TABLE IF EXISTS plan_nutricional CASCADE;
DROP TABLE IF EXISTS persona CASCADE;
DROP TABLE IF EXISTS permiso CASCADE;
DROP TABLE IF EXISTS pago CASCADE;
DROP TABLE IF EXISTS nutricionista CASCADE;
DROP TABLE IF EXISTS metodo_pago CASCADE;
DROP TABLE IF EXISTS membresia CASCADE;
DROP TABLE IF EXISTS medicion_corporal CASCADE;
DROP TABLE IF EXISTS indicador_salud CASCADE;
DROP TABLE IF EXISTS incluye_alimento CASCADE;
DROP TABLE IF EXISTS factura CASCADE;
DROP TABLE IF EXISTS evaluacion_fisica CASCADE;
DROP TABLE IF EXISTS entrenador CASCADE;
DROP TABLE IF EXISTS empleado CASCADE;
DROP TABLE IF EXISTS ejercicio CASCADE;
DROP TABLE IF EXISTS detalle_factura CASCADE;
DROP TABLE IF EXISTS contiene_ejercicio CASCADE;
DROP TABLE IF EXISTS congelacion CASCADE;
DROP TABLE IF EXISTS comprobante CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS clase_grupal CASCADE;
DROP TABLE IF EXISTS bitacora CASCADE;
DROP TABLE IF EXISTS asistencia CASCADE;
DROP TABLE IF EXISTS asignacion_rutina CASCADE;
DROP TABLE IF EXISTS alimento CASCADE;
DROP SEQUENCE IF EXISTS incluye_alimento_id_detalle_plan_seq CASCADE;
DROP SEQUENCE IF EXISTS plan_nutricional_id_plan_nutricional_seq CASCADE;

-- ----- CREATE SEQUENCE (para columnas con nextval) -----
CREATE SEQUENCE incluye_alimento_id_detalle_plan_seq AS BIGINT START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE plan_nutricional_id_plan_nutricional_seq AS BIGINT START WITH 1 INCREMENT BY 1;

-- ----- CREATE TABLE -----
CREATE TABLE alimento (
  id_alimento INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_alimento VARCHAR(120) NOT NULL,
  categoria VARCHAR(80),
  descripcion VARCHAR(255),
  porcion_referencia_g NUMERIC(10,2),
  proteinas_g NUMERIC(10,2),
  carbohidratos_g NUMERIC(10,2),
  fibra_g NUMERIC(10,2)
);
CREATE TABLE asignacion_rutina (
  id_asignacion BIGINT GENERATED ALWAYS AS IDENTITY,
  fecha_asignacion DATE DEFAULT CURRENT_DATE NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE,
  estado_asignacion VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL,
  motivo_finalizacion TEXT,
  observaciones TEXT,
  id_cliente BIGINT NOT NULL,
  id_rutina BIGINT NOT NULL
);
CREATE TABLE asistencia (
  id_asistencia BIGINT GENERATED ALWAYS AS IDENTITY,
  fecha_asistencia DATE DEFAULT CURRENT_DATE NOT NULL,
  hora_entrada TIME NOT NULL,
  hora_salida TIME,
  tipo_acceso VARCHAR(50) NOT NULL,
  metodo_registro VARCHAR(50) NOT NULL,
  estado_acceso VARCHAR(30) DEFAULT 'REGISTRADO'::character varying NOT NULL,
  observaciones TEXT,
  id_cliente BIGINT NOT NULL
);
CREATE TABLE bitacora (
  id_bitacora BIGINT GENERATED ALWAYS AS IDENTITY,
  id_usuario BIGINT NOT NULL,
  fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  accion_realizada VARCHAR(150) NOT NULL,
  modulo VARCHAR(100) NOT NULL,
  direccion_ip VARCHAR(45),
  descripcion TEXT,
  resultado TEXT
);
CREATE TABLE clase_grupal (
  id_clase INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_clase VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  nivel VARCHAR(30),
  duracion_base_minutos INTEGER NOT NULL,
  intensidad VARCHAR(30),
  id_entrenador BIGINT NOT NULL,
  cupo_maximo INTEGER DEFAULT 30 NOT NULL,
  fecha_hora TIMESTAMP NOT NULL,
  estado_clase BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE cliente (
  id_persona BIGINT NOT NULL,
  codigo_cliente VARCHAR(20) NOT NULL,
  fecha_registro DATE DEFAULT CURRENT_DATE NOT NULL,
  peso_inicial NUMERIC(5,2) NOT NULL,
  peso_meta NUMERIC(5,2) NOT NULL,
  observaciones VARCHAR(500),
  estado_cliente BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE comprobante (
  id_comprobante BIGINT GENERATED ALWAYS AS IDENTITY,
  numero_comprobante VARCHAR(50) NOT NULL,
  tipo_comprobante VARCHAR(150) NOT NULL,
  fecha_emision DATE DEFAULT CURRENT_DATE NOT NULL,
  formato_archivo VARCHAR(150),
  ruta_archivo VARCHAR(500),
  correo_envio VARCHAR(150),
  fecha_envio DATE,
  estado_comprobante VARCHAR(30) DEFAULT 'GENERADO'::character varying NOT NULL,
  id_pago BIGINT NOT NULL
);
CREATE TABLE congelacion (
  id_congelacion BIGINT GENERATED ALWAYS AS IDENTITY,
  fecha_solicitud DATE DEFAULT CURRENT_DATE NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  motivo TEXT NOT NULL,
  observaciones TEXT,
  estado_congelacion VARCHAR(30) DEFAULT 'SOLICITADA'::character varying NOT NULL,
  id_membresia BIGINT NOT NULL
);
CREATE TABLE contiene_ejercicio (
  id_rutina_ejercicio BIGINT GENERATED ALWAYS AS IDENTITY,
  dia_semana VARCHAR(15) NOT NULL,
  orden INTEGER NOT NULL,
  series INTEGER NOT NULL,
  repeticiones VARCHAR(50) NOT NULL,
  peso_sugerido NUMERIC(6,2),
  duracion_minutos INTEGER,
  descanso_segundos INTEGER,
  id_rutina BIGINT NOT NULL,
  id_ejercicio INTEGER NOT NULL
);
CREATE TABLE detalle_factura (
  id_detalle_factura BIGINT GENERATED ALWAYS AS IDENTITY,
  tipo_concepto VARCHAR(150) NOT NULL,
  codigo_referencia VARCHAR(50),
  cantidad INTEGER NOT NULL,
  precio_unitario NUMERIC(10,2) NOT NULL,
  porcentaje_impuesto NUMERIC(5,2) DEFAULT 0 NOT NULL,
  id_factura BIGINT NOT NULL
);
CREATE TABLE ejercicio (
  id_ejercicio INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_ejercicio VARCHAR(120) NOT NULL,
  descripcion VARCHAR(500),
  tipo_ejercicio VARCHAR(50),
  nivel_dificultad VARCHAR(30),
  instrucciones TEXT,
  id_entrenador BIGINT
);
CREATE TABLE empleado (
  id_persona BIGINT NOT NULL,
  codigo_empleado VARCHAR(20) NOT NULL,
  fecha_ingreso DATE DEFAULT CURRENT_DATE NOT NULL,
  tipo_contrato VARCHAR(40) NOT NULL,
  salario NUMERIC(10,2) NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  estado_empleado BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE entrenador (
  id_persona BIGINT NOT NULL,
  fecha_inicio_profesion DATE,
  nivel_entrenador VARCHAR(80),
  estado_entrenador BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE evaluacion_fisica (
  id_evaluacion BIGINT GENERATED ALWAYS AS IDENTITY,
  codigo_evaluacion VARCHAR(50) NOT NULL,
  fecha_evaluacion DATE DEFAULT CURRENT_DATE NOT NULL,
  tipo_evaluacion VARCHAR(80) NOT NULL,
  motivo TEXT,
  condicion_general VARCHAR(150),
  nivel_riesgo VARCHAR(50),
  proxima_evaluacion DATE,
  estado_evaluacion VARCHAR(30) DEFAULT 'REGISTRADA'::character varying NOT NULL,
  id_cliente BIGINT NOT NULL,
  id_entrenador BIGINT NOT NULL
);
CREATE TABLE factura (
  id_factura BIGINT GENERATED ALWAYS AS IDENTITY,
  numero_factura VARCHAR(50) NOT NULL,
  fecha_emision DATE DEFAULT CURRENT_DATE NOT NULL,
  total_descuento NUMERIC(10,2) DEFAULT 0 NOT NULL,
  impuesto NUMERIC(10,2) DEFAULT 0 NOT NULL,
  estado_factura VARCHAR(30) DEFAULT 'EMITIDA'::character varying NOT NULL,
  id_cliente BIGINT NOT NULL
);
CREATE TABLE incluye_alimento (
  id_detalle_plan BIGINT DEFAULT nextval('incluye_alimento_id_detalle_plan_seq'::regclass) NOT NULL,
  dia_semana VARCHAR(15) NOT NULL,
  tipo_comida VARCHAR(50) NOT NULL,
  hora_consumo TIME,
  cantidad NUMERIC(10,2) NOT NULL,
  unidad_medida VARCHAR(50) NOT NULL,
  orden_comida INTEGER NOT NULL,
  indicaciones TEXT,
  estado_detalle VARCHAR(30) DEFAULT 'ACTIVO'::character varying NOT NULL,
  id_plan_nutricional BIGINT NOT NULL,
  id_alimento INTEGER NOT NULL
);
CREATE TABLE indicador_salud (
  id_indicador INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_indicador VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  unidad_medida VARCHAR(40),
  valor_minimo_referencia NUMERIC(10,2),
  valor_maximo_referencia NUMERIC(10,2),
  categoria VARCHAR(80),
  estado_indicador BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE medicion_corporal (
  id_medicion BIGINT GENERATED ALWAYS AS IDENTITY,
  peso_kg NUMERIC(5,2),
  altura_m NUMERIC(4,2),
  porcentaje_grasa NUMERIC(5,2),
  cintura_cm NUMERIC(6,2),
  pecho_cm NUMERIC(6,2),
  brazo_cm NUMERIC(6,2),
  muslo_cm NUMERIC(6,2),
  observaciones TEXT,
  id_evaluacion BIGINT NOT NULL
);
CREATE TABLE membresia (
  id_membresia BIGINT GENERATED ALWAYS AS IDENTITY,
  numero_membresia VARCHAR(50) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  costo_final NUMERIC(10,2) NOT NULL,
  estado_membresia VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL,
  observaciones TEXT,
  id_cliente BIGINT NOT NULL,
  id_tipo_membresia INTEGER NOT NULL
);
CREATE TABLE metodo_pago (
  id_metodo_pago INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_metodo VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255),
  requiere_referencia BOOLEAN DEFAULT false NOT NULL,
  permite_cuotas BOOLEAN DEFAULT false NOT NULL,
  porcentaje_comision NUMERIC(5,2) DEFAULT 0 NOT NULL,
  estado_metodo BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE nutricionista (
  id_persona BIGINT NOT NULL,
  numero_licencia VARCHAR(50) NOT NULL,
  fecha_inicio_profesion DATE,
  estado_licencia VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL
);
CREATE TABLE pago (
  id_pago BIGINT GENERATED ALWAYS AS IDENTITY,
  monto_recibido NUMERIC(10,2) NOT NULL,
  monto_pago NUMERIC(10,2) NOT NULL,
  codigo_pago VARCHAR(50) NOT NULL,
  fecha_hora_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  estado_pago VARCHAR(30) DEFAULT 'REGISTRADO'::character varying NOT NULL,
  referencia_transaccion VARCHAR(50),
  id_factura BIGINT NOT NULL,
  id_metodo_pago INTEGER NOT NULL
);
CREATE TABLE permiso (
  id_permiso INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_permiso VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  modulo VARCHAR(80) NOT NULL,
  accion VARCHAR(50) NOT NULL,
  estado_permiso BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE persona (
  id_persona BIGINT GENERATED ALWAYS AS IDENTITY,
  cedula VARCHAR(10) NOT NULL,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  fecha_nacimiento DATE NOT NULL,
  sexo VARCHAR(15) NOT NULL,
  telefono VARCHAR(10) NOT NULL,
  correo VARCHAR(120) NOT NULL,
  foto_perfil BYTEA,
  estado BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE plan_nutricional (
  id_plan_nutricional BIGINT DEFAULT nextval('plan_nutricional_id_plan_nutricional_seq'::regclass) NOT NULL,
  codigo_plan VARCHAR(50) NOT NULL,
  nombre_plan VARCHAR(150) NOT NULL,
  fecha_creacion DATE DEFAULT CURRENT_DATE NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE,
  calorias_objetivo INTEGER,
  proteinas_objetivo_g NUMERIC(10,2),
  carbohidratos_objetivo_g NUMERIC(10,2),
  restricciones_generales TEXT,
  estado_plan VARCHAR(30) DEFAULT 'ACTIVO'::character varying NOT NULL,
  id_cliente BIGINT NOT NULL,
  id_nutricionista BIGINT NOT NULL
);
CREATE TABLE progreso_rutina (
  id_progreso BIGINT GENERATED ALWAYS AS IDENTITY,
  fecha_registro DATE DEFAULT CURRENT_DATE NOT NULL,
  sesiones_planificadas INTEGER NOT NULL,
  sesiones_completadas INTEGER NOT NULL,
  peso_corporal NUMERIC(5,2),
  nivel_esfuerzo VARCHAR(30),
  estado_progreso VARCHAR(30) DEFAULT 'REGISTRADO'::character varying NOT NULL,
  id_cliente BIGINT NOT NULL,
  id_rutina BIGINT NOT NULL,
  detalle_ejercicios JSONB DEFAULT '{}'::jsonb NOT NULL,
  fecha_hora_actualizacion TIMESTAMP
);
CREATE TABLE recomendacion (
  id_recomendacion BIGINT GENERATED ALWAYS AS IDENTITY,
  fecha_recomendacion DATE DEFAULT CURRENT_DATE NOT NULL,
  tipo_recomendacion VARCHAR(80) NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  descripcion TEXT NOT NULL,
  prioridad VARCHAR(30) DEFAULT 'MEDIA'::character varying NOT NULL,
  fecha_inicio DATE,
  fecha_fin DATE,
  estado_recomendacion VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL,
  id_evaluacion BIGINT NOT NULL
);
CREATE TABLE reserva (
  id_reserva BIGINT GENERATED ALWAYS AS IDENTITY,
  codigo_reserva VARCHAR(50) NOT NULL,
  fecha_hora_reserva TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  estado_reserva VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL,
  fecha_hora_cancelacion TIMESTAMP,
  motivo_cancelacion TEXT,
  asistencia_confirmada BOOLEAN DEFAULT false NOT NULL,
  observaciones TEXT,
  id_cliente BIGINT NOT NULL,
  id_clase INTEGER NOT NULL
);
CREATE TABLE resultado_indicador (
  id_resultado BIGINT GENERATED ALWAYS AS IDENTITY,
  valor_obtenido NUMERIC(10,2) NOT NULL,
  clasificacion VARCHAR(100),
  fuera_de_rango BOOLEAN DEFAULT false NOT NULL,
  observaciones TEXT,
  fecha_registro DATE DEFAULT CURRENT_DATE NOT NULL,
  id_evaluacion BIGINT NOT NULL,
  id_indicador INTEGER NOT NULL
);
CREATE TABLE rol (
  id_rol INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre_rol VARCHAR(50) NOT NULL,
  descripcion VARCHAR(200),
  estado_rol BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE rol_permiso (
  id_rol INTEGER NOT NULL,
  id_permiso INTEGER NOT NULL,
  fecha_asignacion DATE DEFAULT CURRENT_DATE NOT NULL,
  estado_asignacion VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL
);
CREATE TABLE rutina (
  id_rutina BIGINT GENERATED ALWAYS AS IDENTITY,
  nombre_rutina VARCHAR(150) NOT NULL,
  descripcion TEXT,
  nivel VARCHAR(50) NOT NULL,
  duracion_semanas INTEGER NOT NULL,
  fecha_creacion DATE DEFAULT CURRENT_DATE NOT NULL,
  estado_rutina VARCHAR(30) DEFAULT 'ACTIVA'::character varying NOT NULL,
  id_entrenador BIGINT NOT NULL
);
CREATE TABLE rutina_dia_entrenamiento (
  id_rutina BIGINT NOT NULL,
  dia_entrenamiento VARCHAR(15) NOT NULL
);
CREATE TABLE tipo_membresia (
  id_tipo_membresia INTEGER GENERATED ALWAYS AS IDENTITY,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  duracion_dias INTEGER NOT NULL,
  precio_base NUMERIC(10,2) NOT NULL,
  limite_accesos INTEGER,
  acceso_ilimitado BOOLEAN DEFAULT false NOT NULL,
  estado_tipo BOOLEAN DEFAULT true NOT NULL
);
CREATE TABLE usuario (
  id_usuario BIGINT GENERATED ALWAYS AS IDENTITY,
  id_persona BIGINT,
  id_rol INTEGER NOT NULL,
  nombre_usuario VARCHAR(50) NOT NULL,
  clave_hash VARCHAR(200) NOT NULL,
  bloqueado BOOLEAN DEFAULT false NOT NULL,
  intentos_fallidos INTEGER DEFAULT 0 NOT NULL,
  estado_usuario BOOLEAN DEFAULT true NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  ultimo_acceso TIMESTAMP
);

-- ----- INSERT INTO (OVERRIDING SYSTEM VALUE en columnas IDENTITY) -----
-- rol (5 filas)
INSERT INTO rol (id_rol, nombre_rol, descripcion, estado_rol) OVERRIDING SYSTEM VALUE VALUES
(1, 'Administrador', 'Acceso completo al sistema GYMNOVA.', TRUE),
(2, 'Recepcionista', 'Gestiona personas, clientes, membresias, acceso y cobros.', TRUE),
(3, 'Entrenador', 'Gestiona clientes asignados, rutinas y evaluaciones.', TRUE),
(4, 'Nutricionista', 'Gestiona clientes asignados y planes nutricionales.', TRUE),
(9, 'Cliente', 'Consulta y actualiza exclusivamente su informacion personal.', TRUE);

-- permiso (90 filas)
INSERT INTO permiso (id_permiso, nombre_permiso, descripcion, modulo, accion, estado_permiso) OVERRIDING SYSTEM VALUE VALUES
(34, 'Personas - Ver', 'Permite ver registros del modulo personas.', 'PERSONAS', 'VER', TRUE),
(3, 'Personas - Crear', 'Permite crear registros del modulo personas.', 'PERSONAS', 'CREAR', TRUE),
(33, 'Personas - Modificar', 'Permite modificar registros del modulo personas.', 'PERSONAS', 'MODIFICAR', TRUE),
(16, 'Personas - Desactivar', 'Permite desactivar registros del modulo personas.', 'PERSONAS', 'DESACTIVAR', TRUE),
(20, 'Personas - Eliminar', 'Permite eliminar registros del modulo personas.', 'PERSONAS', 'ELIMINAR', TRUE),
(21, 'Clientes - Ver', 'Permite ver registros del modulo clientes.', 'CLIENTES', 'VER', TRUE),
(19, 'Clientes - Crear', 'Permite crear registros del modulo clientes.', 'CLIENTES', 'CREAR', TRUE),
(22, 'Clientes - Modificar', 'Permite modificar registros del modulo clientes.', 'CLIENTES', 'MODIFICAR', TRUE),
(1, 'Clientes - Desactivar', 'Permite desactivar registros del modulo clientes.', 'CLIENTES', 'DESACTIVAR', TRUE),
(2, 'Clientes - Eliminar', 'Permite eliminar registros del modulo clientes.', 'CLIENTES', 'ELIMINAR', TRUE),
(28, 'Empleados - Ver', 'Permite ver registros del modulo empleados.', 'EMPLEADOS', 'VER', TRUE),
(11, 'Empleados - Crear', 'Permite crear registros del modulo empleados.', 'EMPLEADOS', 'CREAR', TRUE),
(27, 'Empleados - Modificar', 'Permite modificar registros del modulo empleados.', 'EMPLEADOS', 'MODIFICAR', TRUE),
(8, 'Empleados - Desactivar', 'Permite desactivar registros del modulo empleados.', 'EMPLEADOS', 'DESACTIVAR', TRUE),
(7, 'Empleados - Eliminar', 'Permite eliminar registros del modulo empleados.', 'EMPLEADOS', 'ELIMINAR', TRUE),
(18, 'Entrenadores - Ver', 'Permite ver registros del modulo entrenadores.', 'ENTRENADORES', 'VER', TRUE),
(23, 'Entrenadores - Crear', 'Permite crear registros del modulo entrenadores.', 'ENTRENADORES', 'CREAR', TRUE),
(17, 'Entrenadores - Modificar', 'Permite modificar registros del modulo entrenadores.', 'ENTRENADORES', 'MODIFICAR', TRUE),
(32, 'Entrenadores - Desactivar', 'Permite desactivar registros del modulo entrenadores.', 'ENTRENADORES', 'DESACTIVAR', TRUE),
(35, 'Entrenadores - Eliminar', 'Permite eliminar registros del modulo entrenadores.', 'ENTRENADORES', 'ELIMINAR', TRUE),
(31, 'Nutricionistas - Ver', 'Permite ver registros del modulo nutricionistas.', 'NUTRICIONISTAS', 'VER', TRUE),
(5, 'Nutricionistas - Crear', 'Permite crear registros del modulo nutricionistas.', 'NUTRICIONISTAS', 'CREAR', TRUE),
(30, 'Nutricionistas - Modificar', 'Permite modificar registros del modulo nutricionistas.', 'NUTRICIONISTAS', 'MODIFICAR', TRUE),
(14, 'Nutricionistas - Desactivar', 'Permite desactivar registros del modulo nutricionistas.', 'NUTRICIONISTAS', 'DESACTIVAR', TRUE),
(13, 'Nutricionistas - Eliminar', 'Permite eliminar registros del modulo nutricionistas.', 'NUTRICIONISTAS', 'ELIMINAR', TRUE),
(61, 'Personal - Ver', 'Permite ver registros del modulo personal.', 'PERSONAL', 'VER', TRUE),
(62, 'Personal - Crear', 'Permite crear registros del modulo personal.', 'PERSONAL', 'CREAR', TRUE),
(63, 'Personal - Modificar', 'Permite modificar registros del modulo personal.', 'PERSONAL', 'MODIFICAR', TRUE),
(64, 'Personal - Desactivar', 'Permite desactivar registros del modulo personal.', 'PERSONAL', 'DESACTIVAR', TRUE),
(65, 'Personal - Eliminar', 'Permite eliminar registros del modulo personal.', 'PERSONAL', 'ELIMINAR', TRUE),
(66, 'Usuarios - Ver', 'Permite ver registros del modulo usuarios.', 'USUARIOS', 'VER', TRUE),
(67, 'Usuarios - Crear', 'Permite crear registros del modulo usuarios.', 'USUARIOS', 'CREAR', TRUE),
(68, 'Usuarios - Modificar', 'Permite modificar registros del modulo usuarios.', 'USUARIOS', 'MODIFICAR', TRUE),
(69, 'Usuarios - Desactivar', 'Permite desactivar registros del modulo usuarios.', 'USUARIOS', 'DESACTIVAR', TRUE),
(70, 'Usuarios - Eliminar', 'Permite eliminar registros del modulo usuarios.', 'USUARIOS', 'ELIMINAR', TRUE),
(26, 'Roles - Ver', 'Permite ver registros del modulo roles.', 'ROLES', 'VER', TRUE),
(12, 'Roles - Crear', 'Permite crear registros del modulo roles.', 'ROLES', 'CREAR', TRUE),
(29, 'Roles - Modificar', 'Permite modificar registros del modulo roles.', 'ROLES', 'MODIFICAR', TRUE),
(9, 'Roles - Desactivar', 'Permite desactivar registros del modulo roles.', 'ROLES', 'DESACTIVAR', TRUE),
(4, 'Roles - Eliminar', 'Permite eliminar registros del modulo roles.', 'ROLES', 'ELIMINAR', TRUE),
(25, 'Permisos - Ver', 'Permite ver registros del modulo permisos.', 'PERMISOS', 'VER', TRUE),
(15, 'Permisos - Crear', 'Permite crear registros del modulo permisos.', 'PERMISOS', 'CREAR', TRUE),
(24, 'Permisos - Modificar', 'Permite modificar registros del modulo permisos.', 'PERMISOS', 'MODIFICAR', TRUE),
(6, 'Permisos - Desactivar', 'Permite desactivar registros del modulo permisos.', 'PERMISOS', 'DESACTIVAR', TRUE),
(10, 'Permisos - Eliminar', 'Permite eliminar registros del modulo permisos.', 'PERMISOS', 'ELIMINAR', TRUE),
(81, 'Bitacora - Ver', 'Permite ver registros del modulo bitacora.', 'BITACORA', 'VER', TRUE),
(82, 'Bitacora - Crear', 'Permite crear registros del modulo bitacora.', 'BITACORA', 'CREAR', TRUE),
(83, 'Bitacora - Modificar', 'Permite modificar registros del modulo bitacora.', 'BITACORA', 'MODIFICAR', TRUE),
(84, 'Bitacora - Desactivar', 'Permite desactivar registros del modulo bitacora.', 'BITACORA', 'DESACTIVAR', TRUE),
(85, 'Bitacora - Eliminar', 'Permite eliminar registros del modulo bitacora.', 'BITACORA', 'ELIMINAR', TRUE),
(86, 'Membresias - Ver', 'Permite ver registros del modulo membresias.', 'MEMBRESIAS', 'VER', TRUE),
(87, 'Membresias - Crear', 'Permite crear registros del modulo membresias.', 'MEMBRESIAS', 'CREAR', TRUE),
(88, 'Membresias - Modificar', 'Permite modificar registros del modulo membresias.', 'MEMBRESIAS', 'MODIFICAR', TRUE),
(89, 'Membresias - Desactivar', 'Permite desactivar registros del modulo membresias.', 'MEMBRESIAS', 'DESACTIVAR', TRUE),
(90, 'Membresias - Eliminar', 'Permite eliminar registros del modulo membresias.', 'MEMBRESIAS', 'ELIMINAR', TRUE),
(91, 'Acceso - Ver', 'Permite ver registros del modulo acceso.', 'ACCESO', 'VER', TRUE),
(92, 'Acceso - Crear', 'Permite crear registros del modulo acceso.', 'ACCESO', 'CREAR', TRUE),
(93, 'Acceso - Modificar', 'Permite modificar registros del modulo acceso.', 'ACCESO', 'MODIFICAR', TRUE),
(94, 'Acceso - Desactivar', 'Permite desactivar registros del modulo acceso.', 'ACCESO', 'DESACTIVAR', TRUE),
(95, 'Acceso - Eliminar', 'Permite eliminar registros del modulo acceso.', 'ACCESO', 'ELIMINAR', TRUE),
(96, 'Rutinas - Ver', 'Permite ver registros del modulo rutinas.', 'RUTINAS', 'VER', TRUE),
(97, 'Rutinas - Crear', 'Permite crear registros del modulo rutinas.', 'RUTINAS', 'CREAR', TRUE),
(98, 'Rutinas - Modificar', 'Permite modificar registros del modulo rutinas.', 'RUTINAS', 'MODIFICAR', TRUE),
(99, 'Rutinas - Desactivar', 'Permite desactivar registros del modulo rutinas.', 'RUTINAS', 'DESACTIVAR', TRUE),
(100, 'Rutinas - Eliminar', 'Permite eliminar registros del modulo rutinas.', 'RUTINAS', 'ELIMINAR', TRUE),
(101, 'Salud - Ver', 'Permite ver registros del modulo salud.', 'SALUD', 'VER', TRUE),
(102, 'Salud - Crear', 'Permite crear registros del modulo salud.', 'SALUD', 'CREAR', TRUE),
(103, 'Salud - Modificar', 'Permite modificar registros del modulo salud.', 'SALUD', 'MODIFICAR', TRUE),
(104, 'Salud - Desactivar', 'Permite desactivar registros del modulo salud.', 'SALUD', 'DESACTIVAR', TRUE),
(105, 'Salud - Eliminar', 'Permite eliminar registros del modulo salud.', 'SALUD', 'ELIMINAR', TRUE),
(106, 'Nutricion - Ver', 'Permite ver registros del modulo nutricion.', 'NUTRICION', 'VER', TRUE),
(107, 'Nutricion - Crear', 'Permite crear registros del modulo nutricion.', 'NUTRICION', 'CREAR', TRUE),
(108, 'Nutricion - Modificar', 'Permite modificar registros del modulo nutricion.', 'NUTRICION', 'MODIFICAR', TRUE),
(109, 'Nutricion - Desactivar', 'Permite desactivar registros del modulo nutricion.', 'NUTRICION', 'DESACTIVAR', TRUE),
(110, 'Nutricion - Eliminar', 'Permite eliminar registros del modulo nutricion.', 'NUTRICION', 'ELIMINAR', TRUE),
(111, 'Finanzas - Ver', 'Permite ver registros del modulo finanzas.', 'FINANZAS', 'VER', TRUE),
(112, 'Finanzas - Crear', 'Permite crear registros del modulo finanzas.', 'FINANZAS', 'CREAR', TRUE),
(113, 'Finanzas - Modificar', 'Permite modificar registros del modulo finanzas.', 'FINANZAS', 'MODIFICAR', TRUE),
(114, 'Finanzas - Desactivar', 'Permite desactivar registros del modulo finanzas.', 'FINANZAS', 'DESACTIVAR', TRUE),
(115, 'Finanzas - Eliminar', 'Permite eliminar registros del modulo finanzas.', 'FINANZAS', 'ELIMINAR', TRUE),
(121, 'Reportes - Ver', 'Permite ver registros del modulo reportes.', 'REPORTES', 'VER', TRUE),
(122, 'Reportes - Crear', 'Permite crear registros del modulo reportes.', 'REPORTES', 'CREAR', TRUE),
(123, 'Reportes - Modificar', 'Permite modificar registros del modulo reportes.', 'REPORTES', 'MODIFICAR', TRUE),
(124, 'Reportes - Desactivar', 'Permite desactivar registros del modulo reportes.', 'REPORTES', 'DESACTIVAR', TRUE),
(125, 'Reportes - Eliminar', 'Permite eliminar registros del modulo reportes.', 'REPORTES', 'ELIMINAR', TRUE),
(126, 'Configuracion - Ver', 'Permite ver registros del modulo configuracion.', 'CONFIGURACION', 'VER', TRUE),
(127, 'Configuracion - Crear', 'Permite crear registros del modulo configuracion.', 'CONFIGURACION', 'CREAR', TRUE),
(128, 'Configuracion - Modificar', 'Permite modificar registros del modulo configuracion.', 'CONFIGURACION', 'MODIFICAR', TRUE),
(129, 'Configuracion - Desactivar', 'Permite desactivar registros del modulo configuracion.', 'CONFIGURACION', 'DESACTIVAR', TRUE),
(130, 'Configuracion - Eliminar', 'Permite eliminar registros del modulo configuracion.', 'CONFIGURACION', 'ELIMINAR', TRUE);

-- rol_permiso (151 filas)
INSERT INTO rol_permiso (id_rol, id_permiso, fecha_asignacion, estado_asignacion) VALUES
(1, 34, '2026-08-10', 'ACTIVA'),
(1, 3, '2026-08-10', 'ACTIVA'),
(1, 33, '2026-08-10', 'ACTIVA'),
(1, 16, '2026-08-10', 'ACTIVA'),
(1, 20, '2026-08-10', 'ACTIVA'),
(1, 21, '2026-08-10', 'ACTIVA'),
(1, 19, '2026-08-10', 'ACTIVA'),
(1, 22, '2026-08-10', 'ACTIVA'),
(1, 1, '2026-08-10', 'ACTIVA'),
(1, 2, '2026-08-10', 'ACTIVA'),
(1, 28, '2026-08-10', 'ACTIVA'),
(1, 11, '2026-08-10', 'ACTIVA'),
(1, 27, '2026-08-10', 'ACTIVA'),
(1, 8, '2026-08-10', 'ACTIVA'),
(1, 7, '2026-08-10', 'ACTIVA'),
(1, 18, '2026-08-10', 'ACTIVA'),
(1, 23, '2026-08-10', 'ACTIVA'),
(1, 17, '2026-08-10', 'ACTIVA'),
(1, 32, '2026-08-10', 'ACTIVA'),
(1, 35, '2026-08-10', 'ACTIVA'),
(1, 31, '2026-08-10', 'ACTIVA'),
(1, 5, '2026-08-10', 'ACTIVA'),
(1, 30, '2026-08-10', 'ACTIVA'),
(1, 14, '2026-08-10', 'ACTIVA'),
(1, 13, '2026-08-10', 'ACTIVA'),
(1, 61, '2026-08-10', 'ACTIVA'),
(1, 62, '2026-08-10', 'ACTIVA'),
(1, 63, '2026-08-10', 'ACTIVA'),
(1, 64, '2026-08-10', 'ACTIVA'),
(1, 65, '2026-08-10', 'ACTIVA'),
(1, 66, '2026-08-10', 'ACTIVA'),
(1, 67, '2026-08-10', 'ACTIVA'),
(1, 68, '2026-08-10', 'ACTIVA'),
(1, 69, '2026-08-10', 'ACTIVA'),
(1, 70, '2026-08-10', 'ACTIVA'),
(1, 26, '2026-08-10', 'ACTIVA'),
(1, 12, '2026-08-10', 'ACTIVA'),
(1, 29, '2026-08-10', 'ACTIVA'),
(1, 9, '2026-08-10', 'ACTIVA'),
(1, 4, '2026-08-10', 'ACTIVA'),
(1, 25, '2026-08-10', 'ACTIVA'),
(1, 15, '2026-08-10', 'ACTIVA'),
(1, 24, '2026-08-10', 'ACTIVA'),
(1, 6, '2026-08-10', 'ACTIVA'),
(1, 10, '2026-08-10', 'ACTIVA'),
(1, 81, '2026-08-10', 'ACTIVA'),
(1, 82, '2026-08-10', 'ACTIVA'),
(1, 83, '2026-08-10', 'ACTIVA'),
(1, 84, '2026-08-10', 'ACTIVA'),
(1, 85, '2026-08-10', 'ACTIVA'),
(1, 86, '2026-08-10', 'ACTIVA'),
(1, 87, '2026-08-10', 'ACTIVA'),
(1, 88, '2026-08-10', 'ACTIVA'),
(1, 89, '2026-08-10', 'ACTIVA'),
(1, 90, '2026-08-10', 'ACTIVA'),
(1, 91, '2026-08-10', 'ACTIVA'),
(1, 92, '2026-08-10', 'ACTIVA'),
(1, 93, '2026-08-10', 'ACTIVA'),
(1, 94, '2026-08-10', 'ACTIVA'),
(1, 95, '2026-08-10', 'ACTIVA'),
(1, 96, '2026-08-10', 'ACTIVA'),
(1, 97, '2026-08-10', 'ACTIVA'),
(1, 98, '2026-08-10', 'ACTIVA'),
(1, 99, '2026-08-10', 'ACTIVA'),
(1, 100, '2026-08-10', 'ACTIVA'),
(1, 101, '2026-08-10', 'ACTIVA'),
(1, 102, '2026-08-10', 'ACTIVA'),
(1, 103, '2026-08-10', 'ACTIVA'),
(1, 104, '2026-08-10', 'ACTIVA'),
(1, 105, '2026-08-10', 'ACTIVA'),
(1, 106, '2026-08-10', 'ACTIVA'),
(1, 107, '2026-08-10', 'ACTIVA'),
(1, 108, '2026-08-10', 'ACTIVA'),
(1, 109, '2026-08-10', 'ACTIVA'),
(1, 110, '2026-08-10', 'ACTIVA'),
(1, 111, '2026-08-10', 'ACTIVA'),
(1, 112, '2026-08-10', 'ACTIVA'),
(1, 113, '2026-08-10', 'ACTIVA'),
(1, 114, '2026-08-10', 'ACTIVA'),
(1, 115, '2026-08-10', 'ACTIVA'),
(1, 121, '2026-08-10', 'ACTIVA'),
(1, 122, '2026-08-10', 'ACTIVA'),
(1, 123, '2026-08-10', 'ACTIVA'),
(1, 124, '2026-08-10', 'ACTIVA'),
(1, 125, '2026-08-10', 'ACTIVA'),
(1, 126, '2026-08-10', 'ACTIVA'),
(1, 127, '2026-08-10', 'ACTIVA'),
(1, 128, '2026-08-10', 'ACTIVA'),
(1, 129, '2026-08-10', 'ACTIVA'),
(1, 130, '2026-08-10', 'ACTIVA'),
(2, 34, '2026-08-10', 'ACTIVA'),
(2, 3, '2026-08-10', 'ACTIVA'),
(2, 33, '2026-08-10', 'ACTIVA'),
(2, 16, '2026-08-10', 'ACTIVA'),
(2, 21, '2026-08-10', 'ACTIVA'),
(2, 19, '2026-08-10', 'ACTIVA'),
(2, 22, '2026-08-10', 'ACTIVA'),
(2, 1, '2026-08-10', 'ACTIVA'),
(2, 86, '2026-08-10', 'ACTIVA'),
(2, 87, '2026-08-10', 'ACTIVA'),
(2, 88, '2026-08-10', 'ACTIVA'),
(2, 89, '2026-08-10', 'ACTIVA'),
(2, 91, '2026-08-10', 'ACTIVA'),
(2, 92, '2026-08-10', 'ACTIVA'),
(2, 93, '2026-08-10', 'ACTIVA'),
(2, 94, '2026-08-10', 'ACTIVA'),
(2, 111, '2026-08-10', 'ACTIVA'),
(2, 112, '2026-08-10', 'ACTIVA'),
(2, 113, '2026-08-10', 'ACTIVA'),
(2, 114, '2026-08-10', 'ACTIVA'),
(2, 121, '2026-08-10', 'ACTIVA'),
(2, 126, '2026-08-10', 'ACTIVA'),
(2, 128, '2026-08-10', 'ACTIVA'),
(3, 21, '2026-08-10', 'ACTIVA'),
(3, 96, '2026-08-10', 'ACTIVA'),
(3, 97, '2026-08-10', 'ACTIVA'),
(3, 98, '2026-08-10', 'ACTIVA'),
(3, 99, '2026-08-10', 'ACTIVA'),
(3, 101, '2026-08-10', 'ACTIVA'),
(3, 102, '2026-08-10', 'ACTIVA'),
(3, 103, '2026-08-10', 'ACTIVA'),
(3, 104, '2026-08-10', 'ACTIVA'),
(3, 121, '2026-08-10', 'ACTIVA'),
(3, 126, '2026-08-10', 'ACTIVA'),
(3, 128, '2026-08-10', 'ACTIVA'),
(4, 21, '2026-08-10', 'ACTIVA'),
(4, 101, '2026-08-10', 'ACTIVA'),
(4, 102, '2026-08-10', 'ACTIVA'),
(4, 103, '2026-08-10', 'ACTIVA'),
(4, 104, '2026-08-10', 'ACTIVA'),
(4, 106, '2026-08-10', 'ACTIVA'),
(4, 107, '2026-08-10', 'ACTIVA'),
(4, 108, '2026-08-10', 'ACTIVA'),
(4, 109, '2026-08-10', 'ACTIVA'),
(4, 121, '2026-08-10', 'ACTIVA'),
(4, 126, '2026-08-10', 'ACTIVA'),
(4, 128, '2026-08-10', 'ACTIVA'),
(9, 21, '2026-08-10', 'ACTIVA'),
(9, 86, '2026-08-10', 'ACTIVA'),
(9, 91, '2026-08-10', 'ACTIVA'),
(9, 92, '2026-08-10', 'ACTIVA'),
(9, 93, '2026-08-10', 'ACTIVA'),
(9, 94, '2026-08-10', 'ACTIVA'),
(9, 96, '2026-08-10', 'ACTIVA'),
(9, 97, '2026-08-10', 'ACTIVA'),
(9, 98, '2026-08-10', 'ACTIVA'),
(9, 101, '2026-08-10', 'ACTIVA'),
(9, 106, '2026-08-10', 'ACTIVA'),
(9, 121, '2026-08-10', 'ACTIVA'),
(9, 126, '2026-08-10', 'ACTIVA'),
(9, 128, '2026-08-10', 'ACTIVA');

-- persona (6 filas)
INSERT INTO persona (id_persona, cedula, nombres, apellidos, fecha_nacimiento, sexo, telefono, correo, foto_perfil, estado) OVERRIDING SYSTEM VALUE VALUES
(1, '0102030400', 'Persona', 'De Prueba', '2000-05-15', 'Femenino', '0999999999', 'prueba@gymnova.com', NULL, TRUE),
(3, '0107160509', 'Rolando', 'Navarro', '2005-11-04', 'Masculino', '0987795406', 'rolando.@gmail.com', NULL, TRUE),
(2, '0104225644', 'Monica', 'Galarza', '2000-09-20', 'Femenino', '0984979788', 'monica@gmail.com', NULL, TRUE),
(4, '0107770703', 'Marck', 'Pintado', '2007-01-01', 'Masculino', '0980196522', 'marck@gmail.com', NULL, TRUE),
(5, '1710034065', 'Carlos Andrés', 'Mendoza Ruiz', '1998-05-20', 'Masculino', '0987654321', 'carlos.mendoza@gymnova.com', NULL, TRUE),
(6, '1723456784', 'Andrea Sofía', 'Torres López', '1995-09-14', 'Femenino', '0998765432', 'andrea.torres@gymnova.com', NULL, TRUE);

-- usuario (6 filas)
INSERT INTO usuario (id_usuario, id_persona, id_rol, nombre_usuario, clave_hash, bloqueado, intentos_fallidos, estado_usuario, fecha_creacion, ultimo_acceso) OVERRIDING SYSTEM VALUE VALUES
(2, NULL, 1, 'admin', 'PEGA_AQUI_EL_HASH_GENERADO', FALSE, 1, FALSE, '2026-08-05 17:56:25.937622', NULL),
(3, 1, 2, 'RECEPCION', '210000:ad1437156be6754f4b566279bf7b1dda:59ac2c0eae54cd2983e26987af10ca9b6c723250644cc5a7764af6084b344b77', FALSE, 0, TRUE, '2026-08-10 16:27:57.86114', '2026-08-23 14:18:36.582577'),
(4, 4, 3, 'ENTRENADOR', '210000:ad1437156be6754f4b566279bf7b1dda:59ac2c0eae54cd2983e26987af10ca9b6c723250644cc5a7764af6084b344b77', FALSE, 0, TRUE, '2026-08-10 16:27:57.86114', '2026-08-23 14:18:37.641814'),
(5, 6, 4, 'NUTRICION', '210000:ad1437156be6754f4b566279bf7b1dda:59ac2c0eae54cd2983e26987af10ca9b6c723250644cc5a7764af6084b344b77', FALSE, 0, TRUE, '2026-08-10 16:27:57.86114', '2026-08-23 14:18:38.392267'),
(6, 2, 9, 'cliente', '210000:ad1437156be6754f4b566279bf7b1dda:59ac2c0eae54cd2983e26987af10ca9b6c723250644cc5a7764af6084b344b77', FALSE, 0, TRUE, '2026-08-10 16:27:57.86114', '2026-08-23 14:18:38.912744'),
(1, NULL, 1, 'jose', '210000:8f3c72a94b1de806d519c8f3a26475be:9cf33ac17b64f1d63885a38007fb4acea05ce8a79da6630dd4b2b9f0c853ffe6', FALSE, 0, TRUE, '2026-07-31 16:33:37.690331', '2026-08-23 16:29:05.475552');

-- empleado (3 filas)
INSERT INTO empleado (id_persona, codigo_empleado, fecha_ingreso, tipo_contrato, salario, hora_inicio, hora_fin, estado_empleado) VALUES
(4, 'EM00001', '2026-08-06', 'Medio tiempo', 400.00, '08:00:00', '17:00:00', TRUE),
(6, 'EM00002', '2026-08-06', 'Tiempo completo', 650.00, '08:00:00', '17:00:00', TRUE),
(1, 'EM00003', '2026-08-10', 'TIEMPO_COMPLETO', 500.00, '08:00:00', '17:00:00', TRUE);

-- cliente (4 filas)
INSERT INTO cliente (id_persona, codigo_cliente, fecha_registro, peso_inicial, peso_meta, observaciones, estado_cliente) VALUES
(3, 'CL00002', '2026-08-04', 55.00, 70.00, NULL, TRUE),
(4, 'CL00003', '2026-08-06', 70.00, 65.00, 'ninguna', TRUE),
(2, 'CL00001', '2026-08-03', 90.00, 70.00, 'gordo', TRUE),
(5, 'CL00004', '2026-08-06', 82.50, 75.00, 'Objetivo de reducción de peso', TRUE);

-- entrenador (1 filas)
INSERT INTO entrenador (id_persona, fecha_inicio_profesion, nivel_entrenador, estado_entrenador) VALUES
(4, '2021-01-15', 'INTERMEDIO', TRUE);

-- nutricionista (1 filas)
INSERT INTO nutricionista (id_persona, numero_licencia, fecha_inicio_profesion, estado_licencia) VALUES
(6, 'NUT-00001', '2020-02-10', 'ACTIVA');

-- tipo_membresia (2 filas)
INSERT INTO tipo_membresia (id_tipo_membresia, nombre, descripcion, duracion_dias, precio_base, limite_accesos, acceso_ilimitado, estado_tipo) OVERRIDING SYSTEM VALUE VALUES
(1, 'Basica', 'Acceso general al gimnasio', 30, 25.00, 20, FALSE, TRUE),
(2, 'Premium', 'Acceso completo, clases y seguimiento', 30, 45.00, NULL, TRUE, TRUE);

-- membresia (8 filas)
INSERT INTO membresia (id_membresia, numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia) OVERRIDING SYSTEM VALUE VALUES
(1, 'MB00002', '2026-08-05', '2026-09-04', 45.00, 'ACTIVA', 'Membresia funcional para demostracion', 3, 2),
(2, 'MB00003', '2026-08-05', '2026-09-04', 45.00, 'ACTIVA', 'Membresia funcional para demostracion', 4, 2),
(3, 'MB00001', '2026-08-05', '2026-09-04', 45.00, 'ACTIVA', 'Membresia funcional para demostracion', 2, 2),
(4, 'MB00004', '2026-08-05', '2026-09-04', 45.00, 'ACTIVA', 'Membresia funcional para demostracion', 5, 2),
(9, 'MB00005', '2026-08-22', '2026-09-21', 25.00, 'ACTIVA', 'Prueba de flujo automatico', 2, 1),
(10, 'MB00006', '2026-08-22', '2026-09-21', 25.00, 'ACTIVA', 'Prueba de flujo automatico', 2, 1),
(11, 'MB00007', '2026-08-22', '2026-09-21', 25.00, 'ACTIVA', 'Integral 1787450799447', 2, 1),
(12, 'MB00008', '2026-08-23', '2026-09-22', 25.00, 'ACTIVA', 'Prueba final', 2, 1);

-- clase_grupal (2 filas)
INSERT INTO clase_grupal (id_clase, nombre_clase, descripcion, nivel, duracion_base_minutos, intensidad, id_entrenador, cupo_maximo, fecha_hora, estado_clase) OVERRIDING SYSTEM VALUE VALUES
(1, 'Spinning', 'Sesion cardiovascular sobre bicicleta', 'INTERMEDIO', 60, 'ALTA', 4, 20, '2026-08-30 18:00:00', TRUE),
(2, 'Movilidad funcional', 'Movilidad y recuperacion activa', 'PRINCIPIANTE', 45, 'BAJA', 4, 15, '2026-08-31 10:00:00', TRUE);

-- reserva (6 filas)
INSERT INTO reserva (id_reserva, codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase) OVERRIDING SYSTEM VALUE VALUES
(1, 'RS00002', '2026-08-14 07:00:00.0', 'ACTIVA', NULL, NULL, FALSE, 'Reserva semanal de demostracion', 3, 1),
(2, 'RS00003', '2026-08-14 07:00:00.0', 'ACTIVA', NULL, NULL, FALSE, 'Reserva semanal de demostracion', 4, 1),
(3, 'RS00001', '2026-08-14 07:00:00.0', 'ACTIVA', NULL, NULL, FALSE, 'Reserva semanal de demostracion', 2, 1),
(4, 'RS00004', '2026-08-14 07:00:00.0', 'ACTIVA', NULL, NULL, FALSE, 'Reserva semanal de demostracion', 5, 1),
(9, 'RS00005', '2026-08-22 22:54:42.654569', 'ACTIVA', NULL, NULL, FALSE, 'Reserva de prueba', 2, 1),
(10, 'RS00006', '2026-08-23 17:18:37.088967', 'ACTIVA', NULL, NULL, FALSE, 'Prueba', 2, 1);

-- asistencia (17 filas)
INSERT INTO asistencia (id_asistencia, fecha_asistencia, hora_entrada, hora_salida, tipo_acceso, metodo_registro, estado_acceso, observaciones, id_cliente) OVERRIDING SYSTEM VALUE VALUES
(1, '2026-08-13', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 4),
(2, '2026-08-13', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 2),
(3, '2026-08-12', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 3),
(4, '2026-08-12', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 5),
(5, '2026-08-11', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 4),
(6, '2026-08-11', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 2),
(7, '2026-08-10', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 3),
(8, '2026-08-10', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 5),
(9, '2026-08-09', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 4),
(10, '2026-08-09', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 2),
(11, '2026-08-08', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 3),
(12, '2026-08-08', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 5),
(13, '2026-08-07', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 4),
(14, '2026-08-07', '07:05:00', '08:20:00', 'MEMBRESIA', 'QR', 'COMPLETADO', 'Ingreso de prueba registrado en PostgreSQL', 2),
(16, '2026-08-22', '20:55:48', '20:55:48', 'MEMBRESIA', 'MANUAL', 'COMPLETADO', NULL, 2),
(17, '2026-08-22', '21:06:40', '21:06:40', 'MEMBRESIA', 'MANUAL', 'COMPLETADO', NULL, 2),
(18, '2026-08-23', '14:18:37', '14:18:37', 'MEMBRESIA', 'MANUAL', 'COMPLETADO', NULL, 2);

-- ejercicio (8 filas)
INSERT INTO ejercicio (id_ejercicio, nombre_ejercicio, descripcion, tipo_ejercicio, nivel_dificultad, instrucciones) OVERRIDING SYSTEM VALUE VALUES
(1, 'Press de banca', 'Ejercicio principal de pecho', 'FUERZA', 'INTERMEDIO', 'Controlar el descenso de la barra'),
(2, 'Aperturas con mancuernas', 'Trabajo complementario de pecho', 'FUERZA', 'PRINCIPIANTE', 'Mantener los codos semiflexionados'),
(3, 'Press militar', 'Ejercicio principal de hombros', 'FUERZA', 'INTERMEDIO', 'Mantener el abdomen activo'),
(4, 'Elevaciones laterales', 'Trabajo del deltoides lateral', 'FUERZA', 'PRINCIPIANTE', 'No elevar por encima del hombro'),
(5, 'Sentadilla goblet', 'Trabajo general de piernas', 'FUERZA', 'PRINCIPIANTE', 'Rodillas alineadas con los pies'),
(6, 'Peso muerto rumano', 'Cadena posterior', 'FUERZA', 'INTERMEDIO', 'Espalda neutra durante el recorrido'),
(7, 'Remo sentado', 'Trabajo de espalda', 'FUERZA', 'PRINCIPIANTE', 'Llevar los codos hacia atras'),
(8, 'Cardio moderado', 'Trabajo cardiovascular continuo', 'CARDIO', 'PRINCIPIANTE', 'Mantener una intensidad sostenible');

-- rutina (5 filas)
INSERT INTO rutina (id_rutina, nombre_rutina, descripcion, nivel, duracion_semanas, fecha_creacion, estado_rutina, id_entrenador) OVERRIDING SYSTEM VALUE VALUES
(1, 'Plan semanal GYMNOVA', 'Pecho y hombros alternados con piernas y cardio', 'INTERMEDIO', 8, '2026-07-30', 'ACTIVA', 4),
(3, 'Rutina Prueba Auto', 'Rutina generada por prueba', 'INTERMEDIO', 8, '2026-08-22', 'ACTIVA', 4),
(4, 'Rutina Integral', 'Prueba integral', 'INTERMEDIO', 8, '2026-08-22', 'ACTIVA', 4),
(5, 'Rutina Final', 'desc', 'INTERMEDIO', 6, '2026-08-23', 'ACTIVA', 4),
(6, 'Rutina DAO puro', 'prueba DAO', 'INTERMEDIO', 4, '2026-08-23', 'ACTIVA', 4);

-- rutina_dia_entrenamiento (11 filas)
INSERT INTO rutina_dia_entrenamiento (id_rutina, dia_entrenamiento) VALUES
(1, 'LUNES'),
(1, 'MARTES'),
(1, 'MIERCOLES'),
(1, 'JUEVES'),
(1, 'VIERNES'),
(1, 'SABADO'),
(3, 'LUNES'),
(3, 'MIERCOLES'),
(4, 'LUNES'),
(4, 'MIERCOLES'),
(5, 'LUNES');

-- contiene_ejercicio (17 filas)
INSERT INTO contiene_ejercicio (id_rutina_ejercicio, dia_semana, orden, series, repeticiones, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio) OVERRIDING SYSTEM VALUE VALUES
(1, 'JUEVES', 1, 3, '10-12', 35.00, NULL, 75, 1, 1),
(2, 'LUNES', 1, 4, '8-10', 40.00, NULL, 90, 1, 1),
(3, 'JUEVES', 2, 3, '15', 8.00, NULL, 60, 1, 2),
(4, 'LUNES', 2, 3, '12-15', 10.00, NULL, 60, 1, 2),
(5, 'VIERNES', 1, 3, '10-12', 22.00, NULL, 75, 1, 3),
(6, 'MARTES', 1, 4, '8-10', 25.00, NULL, 90, 1, 3),
(7, 'VIERNES', 2, 4, '12', 7.00, NULL, 60, 1, 4),
(8, 'MARTES', 2, 3, '12-15', 8.00, NULL, 60, 1, 4),
(9, 'MIERCOLES', 1, 4, '10-12', 22.00, NULL, 90, 1, 5),
(10, 'MIERCOLES', 2, 3, '10-12', 35.00, NULL, 90, 1, 6),
(11, 'SABADO', 1, 1, 'Continuo', 0.00, 30, 0, 1, 8),
(23, 'LUNES', 1, 4, '12', NULL, NULL, 60, 3, 1),
(24, 'LUNES', 2, 3, '10', NULL, NULL, 90, 3, 2),
(25, 'MIERCOLES', 1, 3, '8', NULL, NULL, 90, 3, 3),
(26, 'LUNES', 1, 4, '12', NULL, NULL, 60, 4, 1),
(27, 'MIERCOLES', 1, 3, '10', NULL, NULL, 90, 4, 2),
(28, 'LUNES', 1, 3, '10', NULL, NULL, 60, 5, 1);

-- asignacion_rutina (7 filas)
INSERT INTO asignacion_rutina (id_asignacion, fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina) OVERRIDING SYSTEM VALUE VALUES
(1, '2026-07-30', '2026-07-30', '2026-09-24', 'ACTIVA', NULL, 'Asignada por el entrenador', 3, 1),
(2, '2026-07-30', '2026-07-30', '2026-09-24', 'ACTIVA', NULL, 'Asignada por el entrenador', 4, 1),
(4, '2026-07-30', '2026-07-30', '2026-09-24', 'ACTIVA', NULL, 'Asignada por el entrenador', 5, 1),
(3, '2026-07-30', '2026-07-30', '2026-08-22', 'FINALIZADA', 'Reemplazada por nueva asignacion', 'Asignada por el entrenador', 2, 1),
(9, '2026-08-22', '2026-08-22', '2026-08-22', 'FINALIZADA', 'Reemplazada por nueva asignacion', 'Asignacion de prueba', 2, 3),
(10, '2026-08-22', '2026-08-22', '2026-08-23', 'FINALIZADA', 'Reemplazada por nueva asignacion', 'Integral', 2, 4),
(11, '2026-08-23', '2026-08-23', '2026-10-04', 'ACTIVA', NULL, 'final', 2, 5);

-- evaluacion_fisica (7 filas)
INSERT INTO evaluacion_fisica (id_evaluacion, codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_cliente, id_entrenador) OVERRIDING SYSTEM VALUE VALUES
(1, 'EV00002', '2026-08-10', 'SEGUIMIENTO', 'Control mensual', 'BUENA', 'BAJO', '2026-09-09', 'REVISADA', 3, 4),
(2, 'EV00003', '2026-08-10', 'SEGUIMIENTO', 'Control mensual', 'BUENA', 'BAJO', '2026-09-09', 'REVISADA', 4, 4),
(3, 'EV00001', '2026-08-10', 'SEGUIMIENTO', 'Control mensual', 'BUENA', 'BAJO', '2026-09-09', 'REVISADA', 2, 4),
(4, 'EV00004', '2026-08-10', 'SEGUIMIENTO', 'Control mensual', 'BUENA', 'BAJO', '2026-09-09', 'REVISADA', 5, 4),
(9, 'EV00005', '2026-08-22', 'INICIAL', 'Evaluacion inicial', 'Buena', 'BAJO', '2026-11-22', 'REGISTRADA', 2, 4),
(10, 'EV00006', '2026-08-22', 'INTEGRAL', 'Prueba', 'OK', 'BAJO', '2026-11-22', 'REGISTRADA', 2, 4),
(11, 'EV00007', '2026-08-23', 'FINAL', 'test', 'OK', 'BAJO', NULL, 'REGISTRADA', 2, 4);

-- medicion_corporal (7 filas)
INSERT INTO medicion_corporal (id_medicion, peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm, brazo_cm, muslo_cm, observaciones, id_evaluacion) OVERRIDING SYSTEM VALUE VALUES
(1, 69.20, 1.68, 20.50, 82.00, 96.00, 32.00, 55.00, 'Medicion mensual de prueba', 2),
(2, 81.70, 1.68, 20.50, 82.00, 96.00, 32.00, 55.00, 'Medicion mensual de prueba', 4),
(3, 54.20, 1.68, 20.50, 82.00, 96.00, 32.00, 55.00, 'Medicion mensual de prueba', 1),
(4, 89.20, 1.68, 20.50, 82.00, 96.00, 32.00, 55.00, 'Medicion mensual de prueba', 3),
(5, 70.50, 1.70, 20.50, 80.00, 95.00, 30.00, 55.00, 'Sin observaciones', 9),
(6, 70.00, 1.68, 22.00, 80.00, NULL, NULL, NULL, 'Evaluacion integral', 10),
(7, 68.50, 1.68, NULL, NULL, NULL, NULL, NULL, NULL, 11);

-- indicador_salud (2 filas)
INSERT INTO indicador_salud (id_indicador, nombre_indicador, descripcion, unidad_medida, valor_minimo_referencia, valor_maximo_referencia, categoria, estado_indicador) OVERRIDING SYSTEM VALUE VALUES
(1, 'IMC', 'Indice de masa corporal', 'kg/m2', 18.50, 24.90, 'COMPOSICION', TRUE),
(2, 'Porcentaje de grasa', 'Porcentaje estimado de grasa corporal', '%', 10.00, 25.00, 'COMPOSICION', TRUE);

-- resultado_indicador (6 filas)
INSERT INTO resultado_indicador (id_resultado, valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador) OVERRIDING SYSTEM VALUE VALUES
(1, 23.40, 'SALUDABLE', FALSE, 'Resultado dentro del rango', '2026-08-10', 1, 1),
(2, 23.40, 'SALUDABLE', FALSE, 'Resultado dentro del rango', '2026-08-10', 2, 1),
(3, 23.40, 'SALUDABLE', FALSE, 'Resultado dentro del rango', '2026-08-10', 3, 1),
(4, 23.40, 'SALUDABLE', FALSE, 'Resultado dentro del rango', '2026-08-10', 4, 1),
(9, 22.50, 'NORMAL', FALSE, 'IMC saludable', '2026-08-22', 9, 1),
(10, 22.50, 'NORMAL', FALSE, 'Integral', '2026-08-22', 10, 1);

-- recomendacion (6 filas)
INSERT INTO recomendacion (id_recomendacion, fecha_recomendacion, tipo_recomendacion, titulo, descripcion, prioridad, fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion) OVERRIDING SYSTEM VALUE VALUES
(1, '2026-08-10', 'ACTIVIDAD', 'Mantener hidratacion', 'Consumir agua antes, durante y despues del entrenamiento', 'MEDIA', '2026-08-10', '2026-09-09', 'ACTIVA', 1),
(2, '2026-08-10', 'ACTIVIDAD', 'Mantener hidratacion', 'Consumir agua antes, durante y despues del entrenamiento', 'MEDIA', '2026-08-10', '2026-09-09', 'ACTIVA', 2),
(3, '2026-08-10', 'ACTIVIDAD', 'Mantener hidratacion', 'Consumir agua antes, durante y despues del entrenamiento', 'MEDIA', '2026-08-10', '2026-09-09', 'ACTIVA', 3),
(4, '2026-08-10', 'ACTIVIDAD', 'Mantener hidratacion', 'Consumir agua antes, durante y despues del entrenamiento', 'MEDIA', '2026-08-10', '2026-09-09', 'ACTIVA', 4),
(5, '2026-08-22', 'ENTRENAMIENTO', 'Incrementar cardio', 'Realizar 30 min de cardio 3 veces por semana', 'MEDIA', '2026-08-22', '2026-09-22', 'ACTIVA', 9),
(6, '2026-08-22', 'ENTRENAMIENTO', 'Constancia', 'Mantener 4 sesiones/semana', 'MEDIA', NULL, NULL, 'ACTIVA', 10);

-- progreso_rutina (28 filas)
INSERT INTO progreso_rutina (id_progreso, fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina) OVERRIDING SYSTEM VALUE VALUES
(1, '2026-08-10', 1, 1, 89.70, 'MEDIO', 'REVISADO', 2, 1),
(2, '2026-08-07', 1, 1, 90.00, 'MEDIO', 'REVISADO', 2, 1),
(3, '2026-08-13', 1, 0, 54.40, 'MEDIO', 'REVISADO', 3, 1),
(4, '2026-08-12', 1, 1, 69.50, 'MEDIO', 'REVISADO', 4, 1),
(5, '2026-08-11', 1, 0, 69.60, 'MEDIO', 'REVISADO', 4, 1),
(6, '2026-08-08', 1, 1, 89.90, 'MEDIO', 'REVISADO', 2, 1),
(7, '2026-08-09', 1, 0, 89.80, 'MEDIO', 'REVISADO', 2, 1),
(8, '2026-08-08', 1, 1, 54.90, 'MEDIO', 'REVISADO', 3, 1),
(9, '2026-08-09', 1, 1, 54.80, 'MEDIO', 'REVISADO', 3, 1),
(10, '2026-08-10', 1, 0, 54.70, 'MEDIO', 'REVISADO', 3, 1),
(11, '2026-08-13', 1, 1, 89.40, 'MEDIO', 'REVISADO', 2, 1),
(12, '2026-08-07', 1, 0, 55.00, 'MEDIO', 'REVISADO', 3, 1),
(13, '2026-08-12', 1, 0, 82.00, 'MEDIO', 'REVISADO', 5, 1),
(14, '2026-08-11', 1, 1, 82.10, 'MEDIO', 'REVISADO', 5, 1),
(15, '2026-08-10', 1, 1, 82.20, 'MEDIO', 'REVISADO', 5, 1),
(16, '2026-08-07', 1, 1, 82.50, 'MEDIO', 'REVISADO', 5, 1),
(17, '2026-08-11', 1, 1, 54.60, 'MEDIO', 'REVISADO', 3, 1),
(18, '2026-08-12', 1, 1, 54.50, 'MEDIO', 'REVISADO', 3, 1),
(19, '2026-08-08', 1, 1, 82.40, 'MEDIO', 'REVISADO', 5, 1),
(20, '2026-08-09', 1, 0, 82.30, 'MEDIO', 'REVISADO', 5, 1),
(21, '2026-08-13', 1, 1, 69.40, 'MEDIO', 'REVISADO', 4, 1),
(22, '2026-08-07', 1, 1, 70.00, 'MEDIO', 'REVISADO', 4, 1),
(23, '2026-08-10', 1, 1, 69.70, 'MEDIO', 'REVISADO', 4, 1),
(24, '2026-08-13', 1, 1, 81.90, 'MEDIO', 'REVISADO', 5, 1),
(25, '2026-08-09', 1, 1, 69.80, 'MEDIO', 'REVISADO', 4, 1),
(26, '2026-08-08', 1, 0, 69.90, 'MEDIO', 'REVISADO', 4, 1),
(27, '2026-08-11', 1, 1, 89.60, 'MEDIO', 'REVISADO', 2, 1),
(28, '2026-08-12', 1, 0, 89.50, 'MEDIO', 'REVISADO', 2, 1);

-- alimento (4 filas)
INSERT INTO alimento (id_alimento, nombre_alimento, categoria, descripcion, porcion_referencia_g, proteinas_g, carbohidratos_g, fibra_g) OVERRIDING SYSTEM VALUE VALUES
(1, 'Avena', 'CEREAL', 'Fuente de carbohidratos y fibra', 60.00, 8.00, 36.00, 6.00),
(2, 'Pechuga de pollo', 'PROTEINA', 'Proteina magra', 150.00, 42.00, 0.00, 0.00),
(3, 'Arroz integral', 'CEREAL', 'Carbohidrato complejo', 120.00, 4.00, 34.00, 3.00),
(4, 'Yogur natural', 'LACTEO', 'Fuente de proteina y calcio', 180.00, 10.00, 14.00, 0.00);

-- plan_nutricional (8 filas)
INSERT INTO plan_nutricional (id_plan_nutricional, codigo_plan, nombre_plan, fecha_creacion, fecha_inicio, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, id_cliente, id_nutricionista) VALUES
(1, 'PN00002', 'Plan equilibrado CL00002', '2026-08-08', '2026-08-08', '2026-09-07', 2200, 150.00, 260.00, 'Sin restricciones registradas', 'ACTIVO', 3, 6),
(2, 'PN00003', 'Plan equilibrado CL00003', '2026-08-08', '2026-08-08', '2026-09-07', 2200, 150.00, 260.00, 'Sin restricciones registradas', 'ACTIVO', 4, 6),
(4, 'PN00004', 'Plan equilibrado CL00004', '2026-08-08', '2026-08-08', '2026-09-07', 2200, 150.00, 260.00, 'Sin restricciones registradas', 'ACTIVO', 5, 6),
(3, 'PN00001', 'Plan equilibrado CL00001', '2026-08-08', '2026-08-08', '2026-09-07', 2200, 150.00, 260.00, 'Sin restricciones registradas', 'FINALIZADO', 2, 6),
(9, 'PN00005', 'Plan Prueba', '2026-08-22', '2026-08-22', '2026-11-22', 2000, 120.50, 250.75, 'Sin gluten', 'FINALIZADO', 2, 6),
(10, 'PN00006', 'Plan Segundo', '2026-08-22', '2026-08-22', '2026-10-22', 1800, NULL, NULL, NULL, 'FINALIZADO', 2, 6),
(11, 'PN00007', 'Plan Integral', '2026-08-22', '2026-08-22', '2026-11-22', 2000, 120.50, 250.00, NULL, 'FINALIZADO', 2, 6),
(12, 'PN00008', 'Plan Final', '2026-08-23', '2026-08-23', '2026-10-23', 2000, 100.00, 200.00, NULL, 'ACTIVO', 2, 6);

-- incluye_alimento (21 filas)
INSERT INTO incluye_alimento (id_detalle_plan, dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento) VALUES
(1, 'LUNES', 'DESAYUNO', '07:30:00', 60.00, 'g', 1, 'Preparacion saludable', 'ACTIVO', 1, 1),
(2, 'LUNES', 'ALMUERZO', '13:00:00', 150.00, 'g', 2, 'Preparacion saludable', 'ACTIVO', 1, 2),
(3, 'LUNES', 'ALMUERZO', '13:00:00', 120.00, 'g', 3, 'Preparacion saludable', 'ACTIVO', 1, 3),
(4, 'LUNES', 'MERIENDA', '17:00:00', 180.00, 'g', 4, 'Preparacion saludable', 'ACTIVO', 1, 4),
(5, 'LUNES', 'DESAYUNO', '07:30:00', 60.00, 'g', 1, 'Preparacion saludable', 'ACTIVO', 2, 1),
(6, 'LUNES', 'ALMUERZO', '13:00:00', 150.00, 'g', 2, 'Preparacion saludable', 'ACTIVO', 2, 2),
(7, 'LUNES', 'ALMUERZO', '13:00:00', 120.00, 'g', 3, 'Preparacion saludable', 'ACTIVO', 2, 3),
(8, 'LUNES', 'MERIENDA', '17:00:00', 180.00, 'g', 4, 'Preparacion saludable', 'ACTIVO', 2, 4),
(9, 'LUNES', 'DESAYUNO', '07:30:00', 60.00, 'g', 1, 'Preparacion saludable', 'ACTIVO', 3, 1),
(10, 'LUNES', 'ALMUERZO', '13:00:00', 150.00, 'g', 2, 'Preparacion saludable', 'ACTIVO', 3, 2),
(11, 'LUNES', 'ALMUERZO', '13:00:00', 120.00, 'g', 3, 'Preparacion saludable', 'ACTIVO', 3, 3),
(12, 'LUNES', 'MERIENDA', '17:00:00', 180.00, 'g', 4, 'Preparacion saludable', 'ACTIVO', 3, 4),
(13, 'LUNES', 'DESAYUNO', '07:30:00', 60.00, 'g', 1, 'Preparacion saludable', 'ACTIVO', 4, 1),
(14, 'LUNES', 'ALMUERZO', '13:00:00', 150.00, 'g', 2, 'Preparacion saludable', 'ACTIVO', 4, 2),
(15, 'LUNES', 'ALMUERZO', '13:00:00', 120.00, 'g', 3, 'Preparacion saludable', 'ACTIVO', 4, 3),
(16, 'LUNES', 'MERIENDA', '17:00:00', 180.00, 'g', 4, 'Preparacion saludable', 'ACTIVO', 4, 4),
(33, 'LUNES', 'DESAYUNO', '07:30:00', 150.50, 'gramos', 1, 'Sin sal', 'ACTIVO', 9, 1),
(34, 'MARTES', 'ALMUERZO', '13:00:00', 200.25, 'gramos', 1, NULL, 'ACTIVO', 9, 1),
(35, 'LUNES', 'DESAYUNO', '07:30:00', 150.50, 'gramos', 1, NULL, 'ACTIVO', 11, 1),
(36, 'LUNES', 'ALMUERZO', '13:00:00', 200.25, 'gramos', 2, NULL, 'ACTIVO', 11, 1),
(37, 'LUNES', 'DESAYUNO', '08:00:00', 100.50, 'gramos', 1, NULL, 'ACTIVO', 12, 1);

-- metodo_pago (1 filas)
INSERT INTO metodo_pago (id_metodo_pago, nombre_metodo, descripcion, requiere_referencia, permite_cuotas, porcentaje_comision, estado_metodo) OVERRIDING SYSTEM VALUE VALUES
(1, 'Tarjeta', 'Pago con tarjeta debito o credito', TRUE, TRUE, 2.50, TRUE);

-- factura (8 filas)
INSERT INTO factura (id_factura, numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente) OVERRIDING SYSTEM VALUE VALUES
(1, 'FAC-00002', '2026-08-11', 0.00, 5.40, 'PAGADA', 3),
(2, 'FAC-00003', '2026-08-11', 0.00, 5.40, 'PAGADA', 4),
(3, 'FAC-00001', '2026-08-11', 0.00, 5.40, 'PAGADA', 2),
(4, 'FAC-00004', '2026-08-11', 0.00, 5.40, 'PAGADA', 5),
(11, 'FA00005', '2026-08-22', 0.00, 3.75, 'PAGADA', 2),
(12, 'FA00006', '2026-08-22', 0.00, 3.75, 'PAGADA', 2),
(13, 'FA00007', '2026-08-22', 0.00, 3.75, 'PAGADA', 2),
(14, 'FA00008', '2026-08-23', 0.00, 3.75, 'PAGADA', 2);

-- detalle_factura (8 filas)
INSERT INTO detalle_factura (id_detalle_factura, tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura) OVERRIDING SYSTEM VALUE VALUES
(1, 'MEMBRESIA', 'MB00002', 1, 45.00, 12.00, 1),
(2, 'MEMBRESIA', 'MB00003', 1, 45.00, 12.00, 2),
(3, 'MEMBRESIA', 'MB00001', 1, 45.00, 12.00, 3),
(4, 'MEMBRESIA', 'MB00004', 1, 45.00, 12.00, 4),
(5, 'MEMBRESIA', 'MB00005', 1, 25.00, 15.00, 11),
(6, 'MEMBRESIA', 'MB00006', 1, 25.00, 15.00, 12),
(7, 'MEMBRESIA', 'MB00007', 1, 25.00, 15.00, 13),
(8, 'MEMBRESIA', 'MB00008', 1, 25.00, 15.00, 14);

-- pago (8 filas)
INSERT INTO pago (id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago) OVERRIDING SYSTEM VALUE VALUES
(1, 50.40, 50.40, 'PG00002', '2026-08-12 08:14:25.445945', 'CONFIRMADO', 'TRX-CL00002', 1, 1),
(2, 50.40, 50.40, 'PG00003', '2026-08-12 08:14:25.445945', 'CONFIRMADO', 'TRX-CL00003', 2, 1),
(3, 50.40, 50.40, 'PG00001', '2026-08-12 08:14:25.445945', 'CONFIRMADO', 'TRX-CL00001', 3, 1),
(4, 50.40, 50.40, 'PG00004', '2026-08-12 08:14:25.445945', 'CONFIRMADO', 'TRX-CL00004', 4, 1),
(9, 28.75, 28.75, 'PG00005', '2026-08-22 20:54:42.491612', 'CONFIRMADO', 'REF-PRUEBA-001', 11, 1),
(10, 28.75, 28.75, 'PG00006', '2026-08-22 20:55:47.853509', 'CONFIRMADO', 'REF-PRUEBA-001', 12, 1),
(11, 28.75, 28.75, 'PG00007', '2026-08-22 21:06:39.915825', 'CONFIRMADO', 'INTEGRAL', 13, 1),
(12, 28.75, 28.75, 'PG00008', '2026-08-23 14:18:36.922809', 'CONFIRMADO', 'PRUEBA', 14, 1);

-- comprobante (7 filas)
INSERT INTO comprobante (id_comprobante, numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago) OVERRIDING SYSTEM VALUE VALUES
(1, 'COMP-00002', 'RECIBO', '2026-08-12', 'PDF', NULL, NULL, NULL, 'GENERADO', 1),
(2, 'COMP-00003', 'RECIBO', '2026-08-12', 'PDF', NULL, NULL, NULL, 'GENERADO', 2),
(3, 'COMP-00001', 'RECIBO', '2026-08-12', 'PDF', NULL, NULL, NULL, 'GENERADO', 3),
(4, 'COMP-00004', 'RECIBO', '2026-08-12', 'PDF', NULL, NULL, NULL, 'GENERADO', 4),
(10, 'CP00005', 'RECIBO', '2026-08-22', 'PDF', 'comprobantes/CP00005.pdf', NULL, NULL, 'GENERADO', 10),
(11, 'CP00006', 'RECIBO', '2026-08-22', 'PDF', 'comprobantes/CP00006.pdf', NULL, NULL, 'GENERADO', 11),
(12, 'CP00007', 'RECIBO', '2026-08-23', 'PDF', 'comprobantes/CP00007.pdf', NULL, NULL, 'GENERADO', 12);

-- bitacora (98 filas)
INSERT INTO bitacora (id_bitacora, id_usuario, fecha_hora, accion_realizada, modulo, direccion_ip, descripcion, resultado) OVERRIDING SYSTEM VALUE VALUES
(1, 3, '2026-08-22 20:54:42.02911', 'CREAR', 'MEMBRESIAS', '192.168.18.178', 'Membresia MB00005 creada para cliente 2 (tipo 1).', 'EXITOSO'),
(2, 3, '2026-08-22 20:54:42.231582', 'FACTURAR', 'FINANZAS', '192.168.18.178', 'Factura FA00005 creada para cliente 2 por membresia MB00005 (subtotal 25.00, IVA 3.75).', 'EXITOSO'),
(3, 3, '2026-08-22 20:54:42.499118', 'REGISTRAR_PAGO', 'FINANZAS', '192.168.18.178', 'Pago PG00005 por 28.75 sobre factura 11 (estado PAGADA).', 'EXITOSO'),
(4, 3, '2026-08-22 20:54:42.579115', 'EMITIR_COMPROBANTE', 'FINANZAS', '192.168.18.178', 'No se pudo emitir el comprobante: ERROR: el nuevo registro para la relación «comprobante» viola la restricción «check» «ck_comprobante_estado»
  Detail: La fila que falla contiene (9, CP00005, RECIBO, 2026-08-22, PDF, comprobantes/CP00005.pdf, null, null, EMITIDO, 9).', 'FALLIDO: ERROR: el nuevo registro para la relación «comprobante» viola la restricción «check» «ck_comprobante_estado»
  Detail: La fila que falla contiene (9, CP00005, RECIBO, 2026-08-22, PDF, comprobantes/CP00005.pdf, null, null, EMITIDO, 9).'),
(5, 3, '2026-08-22 20:54:42.69794', 'RESERVAR', 'ACCESO', '192.168.18.178', 'Reserva RS00005 creada para cliente 2 en horario 1', 'EXITOSO'),
(6, 3, '2026-08-22 20:54:42.759296', 'ENTRADA', 'ACCESO', '192.168.18.178', 'No se pudo registrar la entrada: ERROR: el nuevo registro para la relación «asistencia» viola la restricción «check» «ck_asistencia_estado_acceso»
  Detail: La fila que falla contiene (15, 2026-08-22, 20:54:42, null, MEMBRESIA, MANUAL, EN_CURSO, null, 2).', 'FALLIDO: ERROR: el nuevo registro para la relación «asistencia» viola la restricción «check» «ck_asistencia_estado_acceso»
  Detail: La fila que falla contiene (15, 2026-08-22, 20:54:42, null, MEMBRESIA, MANUAL, EN_CURSO, null, 2).'),
(7, 3, '2026-08-22 20:55:47.399873', 'CREAR', 'MEMBRESIAS', '192.168.18.178', 'Membresia MB00006 creada para cliente 2 (tipo 1).', 'EXITOSO'),
(8, 3, '2026-08-22 20:55:47.599387', 'FACTURAR', 'FINANZAS', '192.168.18.178', 'Factura FA00006 creada para cliente 2 por membresia MB00006 (subtotal 25.00, IVA 3.75).', 'EXITOSO'),
(9, 3, '2026-08-22 20:55:47.859503', 'REGISTRAR_PAGO', 'FINANZAS', '192.168.18.178', 'Pago PG00006 por 28.75 sobre factura 12 (estado PAGADA).', 'EXITOSO'),
(10, 3, '2026-08-22 20:55:47.931197', 'EMITIR_COMPROBANTE', 'FINANZAS', '192.168.18.178', 'Comprobante CP00005 emitido para pago 10.', 'EXITOSO'),
(11, 3, '2026-08-22 20:55:48.175821', 'ENTRADA', 'ACCESO', '192.168.18.178', 'Entrada registrada para cliente 2', 'EXITOSO'),
(12, 3, '2026-08-22 20:55:48.237321', 'SALIDA', 'ACCESO', '192.168.18.178', 'Salida registrada para cliente 2', 'EXITOSO'),
(13, 4, '2026-08-22 20:58:18.211539', 'CREAR', 'RUTINAS', '192.168.18.178', 'No se pudo crear la rutina: ERROR: el valor nulo en la columna «id_objetivo» de la relación «rutina» viola la restricción “not-null”
  Detail: La fila que falla contiene (2, Rutina Prueba Auto, Rutina generada por prueba, INTERMEDIO, 8, 2026-08-22, ACTIVA, 4, null).', 'FALLIDO: ERROR: el valor nulo en la columna «id_objetivo» de la relación «rutina» viola la restricción “not-null”
  Detail: La fila que falla contiene (2, Rutina Prueba Auto, Rutina generada por prueba, INTERMEDIO, 8, 2026-08-22, ACTIVA, 4, null).'),
(14, 4, '2026-08-22 20:58:59.982746', 'CREAR', 'RUTINAS', '192.168.18.178', 'Rutina ''Rutina Prueba Auto'' (id 3) creada por entrenador 4 con 3 ejercicios en 2 dias.', 'EXITOSO'),
(15, 4, '2026-08-22 20:59:00.18131', 'ASIGNAR', 'RUTINAS', '192.168.18.178', 'Asignacion 9 de rutina 3 al cliente 2.', 'EXITOSO'),
(16, 4, '2026-08-22 20:59:00.253535', 'PAUSAR', 'RUTINAS', '192.168.18.178', 'Asignacion pausada id=9', 'EXITOSO'),
(17, 4, '2026-08-22 20:59:00.324803', 'REACTIVAR', 'RUTINAS', '192.168.18.178', 'Asignacion reactivada id=9', 'EXITOSO'),
(18, 4, '2026-08-22 20:59:00.421661', 'EVALUACION', 'SALUD', '192.168.18.178', 'Evaluacion EV00005 (id 9) registrada por entrenador 4 para cliente 2.', 'EXITOSO'),
(19, 4, '2026-08-22 20:59:00.505409', 'RECOMENDACION', 'SALUD', '192.168.18.178', 'Recomendacion 5 (''Incrementar cardio'') registrada sobre evaluacion 9', 'EXITOSO'),
(20, 5, '2026-08-22 21:00:32.046113', 'CREAR_PLAN', 'NUTRICION', '192.168.18.178', 'Plan PN00005 (id 9) creado por nutricionista 6 para cliente 2.', 'EXITOSO'),
(21, 5, '2026-08-22 21:00:32.170616', 'AGREGAR_ALIMENTO', 'NUTRICION', '192.168.18.178', 'Alimento 1 agregado al plan 9 (150.50 gramos) el LUNES en DESAYUNO', 'EXITOSO'),
(22, 5, '2026-08-22 21:00:32.263812', 'AGREGAR_ALIMENTO', 'NUTRICION', '192.168.18.178', 'Alimento 1 agregado al plan 9 (200.25 gramos) el MARTES en ALMUERZO', 'EXITOSO'),
(23, 5, '2026-08-22 21:00:32.48575', 'RESULTADO_INDICADOR', 'SALUD', '192.168.18.178', 'Resultado 9 (valor 22.5) registrado sobre evaluacion 9', 'EXITOSO'),
(24, 5, '2026-08-22 21:00:32.561287', 'CREAR_PLAN', 'NUTRICION', '192.168.18.178', 'Plan PN00006 (id 10) creado por nutricionista 6 para cliente 2.', 'EXITOSO'),
(25, 1, '2026-08-22 21:02:47.491758', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(26, 1, '2026-08-22 21:03:19.010994', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(27, 1, '2026-08-22 21:03:19.19511', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(28, 3, '2026-08-22 21:03:19.366987', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(29, 4, '2026-08-22 21:03:19.625337', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(30, 5, '2026-08-22 21:03:19.780519', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(31, 6, '2026-08-22 21:03:19.94028', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de CLIENTE (rol Cliente)', 'EXITOSO'),
(32, 6, '2026-08-22 21:03:20.110259', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de CLIENTE (rol Cliente)', 'EXITOSO'),
(33, 1, '2026-08-22 21:05:48.329216', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(34, 3, '2026-08-22 21:05:48.601023', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(35, 4, '2026-08-22 21:05:48.732755', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(36, 5, '2026-08-22 21:05:48.89164', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(37, 6, '2026-08-22 21:05:49.031297', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de CLIENTE (rol Cliente)', 'EXITOSO'),
(38, 1, '2026-08-22 21:06:38.916483', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(39, 3, '2026-08-22 21:06:39.309278', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(40, 3, '2026-08-22 21:06:39.604092', 'CREAR', 'MEMBRESIAS', '192.168.18.178', 'Membresia MB00007 creada para cliente 2 (tipo 1).', 'EXITOSO'),
(41, 3, '2026-08-22 21:06:39.686436', 'FACTURAR', 'FINANZAS', '192.168.18.178', 'Factura FA00007 creada para cliente 2 por membresia MB00007 (subtotal 25.00, IVA 3.75).', 'EXITOSO'),
(42, 3, '2026-08-22 21:06:39.922001', 'REGISTRAR_PAGO', 'FINANZAS', '192.168.18.178', 'Pago PG00007 por 28.75 sobre factura 13 (estado PAGADA).', 'EXITOSO'),
(43, 3, '2026-08-22 21:06:40.101841', 'EMITIR_COMPROBANTE', 'FINANZAS', '192.168.18.178', 'Comprobante CP00006 emitido para pago 11.', 'EXITOSO'),
(44, 3, '2026-08-22 21:06:40.167078', 'ENTRADA', 'ACCESO', '192.168.18.178', 'Entrada registrada para cliente 2', 'EXITOSO'),
(45, 3, '2026-08-22 21:06:40.226688', 'SALIDA', 'ACCESO', '192.168.18.178', 'Salida registrada para cliente 2', 'EXITOSO'),
(46, 4, '2026-08-22 21:06:40.359591', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(47, 4, '2026-08-22 21:06:40.551251', 'CREAR', 'RUTINAS', '192.168.18.178', 'Rutina ''Rutina Integral'' (id 4) creada por entrenador 4 con 2 ejercicios en 2 dias.', 'EXITOSO'),
(48, 4, '2026-08-22 21:06:40.628438', 'ASIGNAR', 'RUTINAS', '192.168.18.178', 'Asignacion 10 de rutina 4 al cliente 2.', 'EXITOSO'),
(49, 4, '2026-08-22 21:06:40.702485', 'EVALUACION', 'SALUD', '192.168.18.178', 'Evaluacion EV00006 (id 10) registrada por entrenador 4 para cliente 2.', 'EXITOSO'),
(50, 4, '2026-08-22 21:06:40.762742', 'RECOMENDACION', 'SALUD', '192.168.18.178', 'Recomendacion 6 (''Constancia'') registrada sobre evaluacion 10', 'EXITOSO'),
(51, 5, '2026-08-22 21:06:41.013309', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(52, 5, '2026-08-22 21:06:41.079459', 'CREAR_PLAN', 'NUTRICION', '192.168.18.178', 'Plan PN00007 (id 11) creado por nutricionista 6 para cliente 2.', 'EXITOSO'),
(53, 5, '2026-08-22 21:06:41.262428', 'AGREGAR_ALIMENTO', 'NUTRICION', '192.168.18.178', 'Alimento 1 agregado al plan 11 (150.50 gramos) el LUNES en DESAYUNO', 'EXITOSO'),
(54, 5, '2026-08-22 21:06:41.337394', 'AGREGAR_ALIMENTO', 'NUTRICION', '192.168.18.178', 'Alimento 1 agregado al plan 11 (200.25 gramos) el LUNES en ALMUERZO', 'EXITOSO'),
(55, 5, '2026-08-22 21:06:41.407629', 'RESULTADO_INDICADOR', 'SALUD', '192.168.18.178', 'Resultado 10 (valor 22.5) registrado sobre evaluacion 10', 'EXITOSO'),
(56, 6, '2026-08-22 21:06:41.559204', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de CLIENTE (rol Cliente)', 'EXITOSO'),
(57, 1, '2026-08-22 21:15:59.252231', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(58, 1, '2026-08-23 13:56:55.848547', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(59, 3, '2026-08-23 13:56:56.026833', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(60, 4, '2026-08-23 13:56:56.180414', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(61, 5, '2026-08-23 13:56:56.348948', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(62, 6, '2026-08-23 13:56:56.508636', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de cliente (rol Cliente)', 'EXITOSO'),
(63, 1, '2026-08-23 13:56:57.288449', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(64, 3, '2026-08-23 13:56:57.565495', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(65, 4, '2026-08-23 13:56:58.091613', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(66, 5, '2026-08-23 13:56:58.664581', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(67, 6, '2026-08-23 13:56:59.325286', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de cliente (rol Cliente)', 'EXITOSO'),
(68, 1, '2026-08-23 13:57:13.668349', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(69, 1, '2026-08-23 14:07:45.61524', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(70, 3, '2026-08-23 14:07:45.897801', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(71, 4, '2026-08-23 14:07:46.157916', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(72, 5, '2026-08-23 14:07:46.438465', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(73, 6, '2026-08-23 14:07:46.726909', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de cliente (rol Cliente)', 'EXITOSO'),
(74, 1, '2026-08-23 14:07:47.84078', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(75, 3, '2026-08-23 14:07:48.379398', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(76, 4, '2026-08-23 14:07:49.215672', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(77, 5, '2026-08-23 14:07:50.386162', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(78, 6, '2026-08-23 14:07:51.294063', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de cliente (rol Cliente)', 'EXITOSO'),
(79, 1, '2026-08-23 14:08:06.803981', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(80, 1, '2026-08-23 14:08:45.527035', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(81, 1, '2026-08-23 14:18:36.127876', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO'),
(82, 3, '2026-08-23 14:18:36.588683', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de RECEPCION (rol Recepcionista)', 'EXITOSO'),
(83, 3, '2026-08-23 14:18:36.703928', 'CREAR', 'MEMBRESIAS', '192.168.18.178', 'Membresia MB00008 creada para cliente 2 (tipo 1).', 'EXITOSO'),
(84, 3, '2026-08-23 14:18:36.782804', 'FACTURAR', 'FINANZAS', '192.168.18.178', 'Factura FA00008 creada para cliente 2 por membresia MB00008 (subtotal 25.00, IVA 3.75).', 'EXITOSO'),
(85, 3, '2026-08-23 14:18:36.931302', 'REGISTRAR_PAGO', 'FINANZAS', '192.168.18.178', 'Pago PG00008 por 28.75 sobre factura 14 (estado PAGADA).', 'EXITOSO'),
(86, 3, '2026-08-23 14:18:37.010999', 'EMITIR_COMPROBANTE', 'FINANZAS', '192.168.18.178', 'Comprobante CP00007 emitido para pago 12.', 'EXITOSO'),
(87, 3, '2026-08-23 14:18:37.139241', 'RESERVAR', 'ACCESO', '192.168.18.178', 'Reserva RS00006 creada para cliente 2 en horario 1', 'EXITOSO'),
(88, 3, '2026-08-23 14:18:37.238858', 'ENTRADA', 'ACCESO', '192.168.18.178', 'Entrada registrada para cliente 2', 'EXITOSO'),
(89, 3, '2026-08-23 14:18:37.352016', 'SALIDA', 'ACCESO', '192.168.18.178', 'Salida registrada para cliente 2', 'EXITOSO'),
(90, 4, '2026-08-23 14:18:37.647395', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de ENTRENADOR (rol Entrenador)', 'EXITOSO'),
(91, 4, '2026-08-23 14:18:37.838506', 'CREAR', 'RUTINAS', '192.168.18.178', 'Rutina ''Rutina Final'' (id 5) creada por entrenador 4 con 1 ejercicios en 1 dias.', 'EXITOSO'),
(92, 4, '2026-08-23 14:18:38.135047', 'ASIGNAR', 'RUTINAS', '192.168.18.178', 'Asignacion 11 de rutina 5 al cliente 2.', 'EXITOSO'),
(93, 4, '2026-08-23 14:18:38.227018', 'EVALUACION', 'SALUD', '192.168.18.178', 'Evaluacion EV00007 (id 11) registrada por entrenador 4 para cliente 2.', 'EXITOSO'),
(94, 5, '2026-08-23 14:18:38.396451', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de NUTRICION (rol Nutricionista)', 'EXITOSO'),
(95, 5, '2026-08-23 14:18:38.484761', 'CREAR_PLAN', 'NUTRICION', '192.168.18.178', 'Plan PN00008 (id 12) creado por nutricionista 6 para cliente 2.', 'EXITOSO'),
(96, 5, '2026-08-23 14:18:38.571283', 'AGREGAR_ALIMENTO', 'NUTRICION', '192.168.18.178', 'Alimento 1 agregado al plan 12 (100.50 gramos) el LUNES en DESAYUNO', 'EXITOSO'),
(97, 6, '2026-08-23 14:18:38.91904', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de cliente (rol Cliente)', 'EXITOSO'),
(98, 1, '2026-08-23 16:29:05.488474', 'INICIO', 'SESION', '192.168.18.178', 'Inicio de sesion de jose (rol Administrador)', 'EXITOSO');


-- ----- CONSTRAINTS (PK / UNIQUE / CHECK / FK) -----
ALTER TABLE alimento ADD CONSTRAINT alimento_pkey PRIMARY KEY (id_alimento);
ALTER TABLE asignacion_rutina ADD CONSTRAINT pk_asignacion_rutina PRIMARY KEY (id_asignacion);
ALTER TABLE asistencia ADD CONSTRAINT pk_asistencia PRIMARY KEY (id_asistencia);
ALTER TABLE bitacora ADD CONSTRAINT pk_bitacora PRIMARY KEY (id_bitacora);
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_pkey PRIMARY KEY (id_clase);
ALTER TABLE clase_grupal ADD CONSTRAINT ck_clase_grupal_cupo CHECK (cupo_maximo > 0);
ALTER TABLE cliente ADD CONSTRAINT pk_cliente PRIMARY KEY (id_persona);
ALTER TABLE comprobante ADD CONSTRAINT pk_comprobante PRIMARY KEY (id_comprobante);
ALTER TABLE congelacion ADD CONSTRAINT pk_congelacion PRIMARY KEY (id_congelacion);
ALTER TABLE contiene_ejercicio ADD CONSTRAINT pk_contiene_ejercicio PRIMARY KEY (id_rutina_ejercicio);
ALTER TABLE detalle_factura ADD CONSTRAINT pk_detalle_factura PRIMARY KEY (id_detalle_factura);
ALTER TABLE ejercicio ADD CONSTRAINT ejercicio_pkey PRIMARY KEY (id_ejercicio);
ALTER TABLE empleado ADD CONSTRAINT pk_empleado PRIMARY KEY (id_persona);
ALTER TABLE entrenador ADD CONSTRAINT pk_entrenador PRIMARY KEY (id_persona);
ALTER TABLE evaluacion_fisica ADD CONSTRAINT pk_evaluacion_fisica PRIMARY KEY (id_evaluacion);
ALTER TABLE factura ADD CONSTRAINT pk_factura PRIMARY KEY (id_factura);
ALTER TABLE incluye_alimento ADD CONSTRAINT pk_incluye_alimento PRIMARY KEY (id_detalle_plan);
ALTER TABLE indicador_salud ADD CONSTRAINT indicador_salud_pkey PRIMARY KEY (id_indicador);
ALTER TABLE medicion_corporal ADD CONSTRAINT pk_medicion_corporal PRIMARY KEY (id_medicion);
ALTER TABLE membresia ADD CONSTRAINT pk_membresia PRIMARY KEY (id_membresia);
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_pkey PRIMARY KEY (id_metodo_pago);
ALTER TABLE nutricionista ADD CONSTRAINT pk_nutricionista PRIMARY KEY (id_persona);
ALTER TABLE pago ADD CONSTRAINT pk_pago PRIMARY KEY (id_pago);
ALTER TABLE permiso ADD CONSTRAINT permiso_pkey PRIMARY KEY (id_permiso);
ALTER TABLE persona ADD CONSTRAINT pk_persona PRIMARY KEY (id_persona);
ALTER TABLE plan_nutricional ADD CONSTRAINT pk_plan_nutricional PRIMARY KEY (id_plan_nutricional);
ALTER TABLE progreso_rutina ADD CONSTRAINT pk_progreso_rutina PRIMARY KEY (id_progreso);
ALTER TABLE recomendacion ADD CONSTRAINT pk_recomendacion PRIMARY KEY (id_recomendacion);
ALTER TABLE reserva ADD CONSTRAINT pk_reserva PRIMARY KEY (id_reserva);
ALTER TABLE resultado_indicador ADD CONSTRAINT pk_resultado_indicador PRIMARY KEY (id_resultado);
ALTER TABLE rol ADD CONSTRAINT pk_rol PRIMARY KEY (id_rol);
ALTER TABLE rol_permiso ADD CONSTRAINT pk_rol_permiso PRIMARY KEY (id_rol, id_permiso);
ALTER TABLE rutina ADD CONSTRAINT pk_rutina PRIMARY KEY (id_rutina);
ALTER TABLE rutina_dia_entrenamiento ADD CONSTRAINT pk_rutina_dia_entrenamiento PRIMARY KEY (id_rutina, dia_entrenamiento);
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_pkey PRIMARY KEY (id_tipo_membresia);
ALTER TABLE usuario ADD CONSTRAINT pk_usuario PRIMARY KEY (id_usuario);
ALTER TABLE alimento ADD CONSTRAINT uq_alimento_nombre_alimento UNIQUE (nombre_alimento);
ALTER TABLE asignacion_rutina ADD CONSTRAINT uq_asignacion_rutina_cliente_rutina_inicio UNIQUE (id_cliente, id_rutina, fecha_inicio);
ALTER TABLE clase_grupal ADD CONSTRAINT uq_clase_grupal_nombre_clase UNIQUE (nombre_clase);
ALTER TABLE cliente ADD CONSTRAINT uq_cliente_codigo UNIQUE (codigo_cliente);
ALTER TABLE comprobante ADD CONSTRAINT uq_comprobante_numero_comprobante UNIQUE (numero_comprobante);
ALTER TABLE contiene_ejercicio ADD CONSTRAINT uq_contiene_ejercicio_rutina_dia_orden UNIQUE (id_rutina, dia_semana, orden);
ALTER TABLE ejercicio ADD CONSTRAINT uq_ejercicio_nombre_ejercicio UNIQUE (nombre_ejercicio);
ALTER TABLE empleado ADD CONSTRAINT uq_empleado_codigo_empleado UNIQUE (codigo_empleado);
ALTER TABLE evaluacion_fisica ADD CONSTRAINT uq_evaluacion_fisica_codigo_evaluacion UNIQUE (codigo_evaluacion);
ALTER TABLE factura ADD CONSTRAINT uq_factura_numero_factura UNIQUE (numero_factura);
ALTER TABLE incluye_alimento ADD CONSTRAINT uq_incluye_alimento_plan_dia_tipo_orden UNIQUE (id_plan_nutricional, dia_semana, tipo_comida, orden_comida);
ALTER TABLE indicador_salud ADD CONSTRAINT uq_indicador_salud_nombre_indicador UNIQUE (nombre_indicador);
ALTER TABLE membresia ADD CONSTRAINT uq_membresia_numero_membresia UNIQUE (numero_membresia);
ALTER TABLE nutricionista ADD CONSTRAINT uq_nutricionista_numero_licencia UNIQUE (numero_licencia);
ALTER TABLE pago ADD CONSTRAINT uq_pago_codigo_pago UNIQUE (codigo_pago);
ALTER TABLE permiso ADD CONSTRAINT uq_permiso_nombre_permiso UNIQUE (nombre_permiso);
ALTER TABLE persona ADD CONSTRAINT uq_persona_cedula UNIQUE (cedula);
ALTER TABLE persona ADD CONSTRAINT uq_persona_correo UNIQUE (correo);
ALTER TABLE plan_nutricional ADD CONSTRAINT uq_plan_nutricional_codigo_plan UNIQUE (codigo_plan);
ALTER TABLE reserva ADD CONSTRAINT uq_reserva_codigo_reserva UNIQUE (codigo_reserva);
ALTER TABLE resultado_indicador ADD CONSTRAINT uq_resultado_indicador_evaluacion_indicador UNIQUE (id_evaluacion, id_indicador);
ALTER TABLE rol ADD CONSTRAINT uq_rol_nombre UNIQUE (nombre_rol);
ALTER TABLE usuario ADD CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario);
ALTER TABLE usuario ADD CONSTRAINT uq_usuario_persona UNIQUE (id_persona);
ALTER TABLE alimento ADD CONSTRAINT alimento_carbohidratos_g_check CHECK (((carbohidratos_g IS NULL) OR (carbohidratos_g >= (0)::numeric)));
ALTER TABLE alimento ADD CONSTRAINT alimento_fibra_g_check CHECK (((fibra_g IS NULL) OR (fibra_g >= (0)::numeric)));
ALTER TABLE alimento ADD CONSTRAINT alimento_porcion_referencia_g_check CHECK (((porcion_referencia_g IS NULL) OR (porcion_referencia_g > (0)::numeric)));
ALTER TABLE alimento ADD CONSTRAINT alimento_proteinas_g_check CHECK (((proteinas_g IS NULL) OR (proteinas_g >= (0)::numeric)));
ALTER TABLE alimento ADD CONSTRAINT ck_alimento_nombre_no_vacio CHECK ((btrim((nombre_alimento)::text) <> ''::text));
ALTER TABLE asignacion_rutina ADD CONSTRAINT ck_asignacion_rutina_estado CHECK (((estado_asignacion)::text = ANY (ARRAY[('PROGRAMADA'::character varying)::text, ('ACTIVA'::character varying)::text, ('FINALIZADA'::character varying)::text, ('PAUSADA'::character varying)::text, ('CANCELADA'::character varying)::text])));
ALTER TABLE asignacion_rutina ADD CONSTRAINT ck_asignacion_rutina_fecha_asignacion CHECK ((fecha_asignacion <= fecha_inicio));
ALTER TABLE asignacion_rutina ADD CONSTRAINT ck_asignacion_rutina_fechas CHECK (((fecha_fin IS NULL) OR (fecha_fin >= fecha_inicio)));
ALTER TABLE asignacion_rutina ADD CONSTRAINT ck_asignacion_rutina_motivo_finalizacion CHECK ((((estado_asignacion)::text <> 'FINALIZADA'::text) OR (motivo_finalizacion IS NOT NULL)));
ALTER TABLE asistencia ADD CONSTRAINT ck_asistencia_estado_acceso CHECK (((estado_acceso)::text = ANY (ARRAY[('REGISTRADO'::character varying)::text, ('COMPLETADO'::character varying)::text, ('ANULADO'::character varying)::text])));
ALTER TABLE asistencia ADD CONSTRAINT ck_asistencia_horas CHECK (((hora_salida IS NULL) OR (hora_salida >= hora_entrada)));
ALTER TABLE asistencia ADD CONSTRAINT ck_asistencia_metodo_registro CHECK (((metodo_registro)::text = ANY (ARRAY[('QR'::character varying)::text, ('CEDULA'::character varying)::text, ('MANUAL'::character varying)::text, ('BIOMETRICO'::character varying)::text])));
ALTER TABLE asistencia ADD CONSTRAINT ck_asistencia_tipo_acceso CHECK (((tipo_acceso)::text = ANY (ARRAY[('MEMBRESIA'::character varying)::text, ('RESERVA'::character varying)::text, ('INVITADO'::character varying)::text, ('MANUAL'::character varying)::text])));
ALTER TABLE bitacora ADD CONSTRAINT ck_bitacora_accion_realizada CHECK ((btrim((accion_realizada)::text) <> ''::text));
ALTER TABLE bitacora ADD CONSTRAINT ck_bitacora_direccion_ip CHECK (((direccion_ip IS NULL) OR ((length(btrim((direccion_ip)::text)) >= 3) AND (length(btrim((direccion_ip)::text)) <= 45))));
ALTER TABLE bitacora ADD CONSTRAINT ck_bitacora_modulo CHECK ((btrim((modulo)::text) <> ''::text));
ALTER TABLE clase_grupal ADD CONSTRAINT ck_clase_grupal_nombre_no_vacio CHECK ((btrim((nombre_clase)::text) <> ''::text));
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_duracion_base_minutos_check CHECK ((duracion_base_minutos > 0));
ALTER TABLE cliente ADD CONSTRAINT chk_cliente_codigo_formato CHECK (((codigo_cliente)::text ~ '^CL[0-9]{5}$'::text));
ALTER TABLE cliente ADD CONSTRAINT ck_cliente_codigo_cliente_no_vacio CHECK ((btrim((codigo_cliente)::text) <> ''::text));
ALTER TABLE cliente ADD CONSTRAINT ck_cliente_fecha_registro CHECK ((fecha_registro <= CURRENT_DATE));
ALTER TABLE cliente ADD CONSTRAINT ck_cliente_peso_inicial CHECK ((peso_inicial > (0)::numeric));
ALTER TABLE cliente ADD CONSTRAINT ck_cliente_peso_meta CHECK ((peso_meta > (0)::numeric));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_correo_envio CHECK (((correo_envio IS NULL) OR ((correo_envio)::text ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'::text)));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_estado CHECK (((estado_comprobante)::text = ANY (ARRAY[('GENERADO'::character varying)::text, ('ENVIADO'::character varying)::text, ('ANULADO'::character varying)::text, ('ERROR'::character varying)::text])));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_fecha_emision CHECK ((fecha_emision <= CURRENT_DATE));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_fecha_envio CHECK (((fecha_envio IS NULL) OR (fecha_envio >= fecha_emision)));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_formato_archivo CHECK (((formato_archivo IS NULL) OR (btrim((formato_archivo)::text) <> ''::text)));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_numero_comprobante CHECK ((btrim((numero_comprobante)::text) <> ''::text));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_ruta_archivo CHECK (((ruta_archivo IS NULL) OR (btrim((ruta_archivo)::text) <> ''::text)));
ALTER TABLE comprobante ADD CONSTRAINT ck_comprobante_tipo_comprobante CHECK ((btrim((tipo_comprobante)::text) <> ''::text));
ALTER TABLE congelacion ADD CONSTRAINT ck_congelacion_estado CHECK (((estado_congelacion)::text = ANY (ARRAY[('SOLICITADA'::character varying)::text, ('APROBADA'::character varying)::text, ('RECHAZADA'::character varying)::text, ('ACTIVA'::character varying)::text, ('FINALIZADA'::character varying)::text, ('CANCELADA'::character varying)::text])));
ALTER TABLE congelacion ADD CONSTRAINT ck_congelacion_fechas CHECK ((fecha_fin >= fecha_inicio));
ALTER TABLE congelacion ADD CONSTRAINT ck_congelacion_motivo CHECK ((btrim(motivo) <> ''::text));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_descanso CHECK (((descanso_segundos IS NULL) OR (descanso_segundos >= 0)));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_dia CHECK (((dia_semana)::text = ANY (ARRAY[('LUNES'::character varying)::text, ('MARTES'::character varying)::text, ('MIERCOLES'::character varying)::text, ('JUEVES'::character varying)::text, ('VIERNES'::character varying)::text, ('SABADO'::character varying)::text, ('DOMINGO'::character varying)::text])));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_duracion CHECK (((duracion_minutos IS NULL) OR (duracion_minutos > 0)));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_orden CHECK ((orden > 0));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_peso_sugerido CHECK (((peso_sugerido IS NULL) OR (peso_sugerido >= (0)::numeric)));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_repeticiones CHECK ((btrim((repeticiones)::text) <> ''::text));
ALTER TABLE contiene_ejercicio ADD CONSTRAINT ck_contiene_ejercicio_series CHECK ((series > 0));
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_cantidad CHECK ((cantidad > 0));
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_codigo_referencia CHECK (((codigo_referencia IS NULL) OR (btrim((codigo_referencia)::text) <> ''::text)));
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_porcentaje_impuesto CHECK (((porcentaje_impuesto >= (0)::numeric) AND (porcentaje_impuesto <= (100)::numeric)));
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_precio_unitario CHECK ((precio_unitario >= (0)::numeric));
ALTER TABLE detalle_factura ADD CONSTRAINT ck_detalle_factura_tipo_concepto CHECK ((btrim((tipo_concepto)::text) <> ''::text));
ALTER TABLE ejercicio ADD CONSTRAINT ck_ejercicio_nombre_no_vacio CHECK ((btrim((nombre_ejercicio)::text) <> ''::text));
ALTER TABLE empleado ADD CONSTRAINT chk_empleado_codigo_formato CHECK (((codigo_empleado)::text ~ '^EM[0-9]{5}$'::text));
ALTER TABLE empleado ADD CONSTRAINT ck_empleado_codigo CHECK ((btrim((codigo_empleado)::text) <> ''::text));
ALTER TABLE empleado ADD CONSTRAINT ck_empleado_fecha_ingreso CHECK ((fecha_ingreso <= CURRENT_DATE));
ALTER TABLE empleado ADD CONSTRAINT ck_empleado_salario CHECK ((salario >= (0)::numeric));
ALTER TABLE empleado ADD CONSTRAINT ck_empleado_tipo_contrato CHECK (((tipo_contrato)::text = ANY (ARRAY[('TIEMPO_COMPLETO'::character varying)::text, ('MEDIO_TIEMPO'::character varying)::text, ('TEMPORAL'::character varying)::text, ('SERVICIOS_PROFESIONALES'::character varying)::text, ('Tiempo completo'::character varying)::text, ('Medio tiempo'::character varying)::text, ('Temporal'::character varying)::text, ('Servicios profesionales'::character varying)::text])));
ALTER TABLE empleado ADD CONSTRAINT ck_empleado_turno CHECK ((hora_inicio < hora_fin));
ALTER TABLE entrenador ADD CONSTRAINT ck_entrenador_fecha_inicio_profesion CHECK (((fecha_inicio_profesion IS NULL) OR (fecha_inicio_profesion <= CURRENT_DATE)));
ALTER TABLE entrenador ADD CONSTRAINT ck_entrenador_nivel CHECK (((nivel_entrenador IS NULL) OR (btrim((nivel_entrenador)::text) <> ''::text)));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT chk_evaluacion_codigo_formato CHECK (((codigo_evaluacion)::text ~ '^EV[0-9]{5}$'::text));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_codigo CHECK ((btrim((codigo_evaluacion)::text) <> ''::text));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_estado CHECK (((estado_evaluacion)::text = ANY (ARRAY[('REGISTRADA'::character varying)::text, ('REVISADA'::character varying)::text, ('ANULADA'::character varying)::text])));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_fecha CHECK ((fecha_evaluacion <= CURRENT_DATE));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_nivel_riesgo CHECK (((nivel_riesgo IS NULL) OR ((nivel_riesgo)::text = ANY (ARRAY[('BAJO'::character varying)::text, ('MEDIO'::character varying)::text, ('ALTO'::character varying)::text]))));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_proxima CHECK (((proxima_evaluacion IS NULL) OR (proxima_evaluacion >= fecha_evaluacion)));
ALTER TABLE evaluacion_fisica ADD CONSTRAINT ck_evaluacion_fisica_tipo CHECK ((btrim((tipo_evaluacion)::text) <> ''::text));
ALTER TABLE factura ADD CONSTRAINT ck_factura_estado CHECK (((estado_factura)::text = ANY (ARRAY[('EMITIDA'::character varying)::text, ('PAGADA'::character varying)::text, ('ANULADA'::character varying)::text, ('PENDIENTE'::character varying)::text])));
ALTER TABLE factura ADD CONSTRAINT ck_factura_fecha_emision CHECK ((fecha_emision <= CURRENT_DATE));
ALTER TABLE factura ADD CONSTRAINT ck_factura_impuesto CHECK ((impuesto >= (0)::numeric));
ALTER TABLE factura ADD CONSTRAINT ck_factura_numero_factura CHECK ((btrim((numero_factura)::text) <> ''::text));
ALTER TABLE factura ADD CONSTRAINT ck_factura_total_descuento CHECK ((total_descuento >= (0)::numeric));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_cantidad CHECK ((cantidad > (0)::numeric));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_dia_semana CHECK (((dia_semana)::text = ANY (ARRAY[('LUNES'::character varying)::text, ('MARTES'::character varying)::text, ('MIERCOLES'::character varying)::text, ('JUEVES'::character varying)::text, ('VIERNES'::character varying)::text, ('SABADO'::character varying)::text, ('DOMINGO'::character varying)::text])));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_estado_detalle CHECK (((estado_detalle)::text = ANY (ARRAY[('ACTIVO'::character varying)::text, ('SUSPENDIDO'::character varying)::text, ('REEMPLAZADO'::character varying)::text, ('CANCELADO'::character varying)::text])));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_orden_comida CHECK ((orden_comida > 0));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_tipo_comida CHECK (((tipo_comida)::text = ANY (ARRAY[('DESAYUNO'::character varying)::text, ('MEDIA_MANANA'::character varying)::text, ('ALMUERZO'::character varying)::text, ('MERIENDA'::character varying)::text, ('CENA'::character varying)::text, ('SNACK'::character varying)::text])));
ALTER TABLE incluye_alimento ADD CONSTRAINT ck_incluye_alimento_unidad_medida CHECK ((btrim((unidad_medida)::text) <> ''::text));
ALTER TABLE indicador_salud ADD CONSTRAINT ck_indicador_salud_nombre_no_vacio CHECK ((btrim((nombre_indicador)::text) <> ''::text));
ALTER TABLE indicador_salud ADD CONSTRAINT indicador_salud_check CHECK (((valor_minimo_referencia IS NULL) OR (valor_maximo_referencia IS NULL) OR (valor_maximo_referencia >= valor_minimo_referencia)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_altura CHECK (((altura_m IS NULL) OR (altura_m > (0)::numeric)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_brazo CHECK (((brazo_cm IS NULL) OR (brazo_cm > (0)::numeric)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_cintura CHECK (((cintura_cm IS NULL) OR (cintura_cm > (0)::numeric)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_grasa CHECK (((porcentaje_grasa IS NULL) OR ((porcentaje_grasa >= (0)::numeric) AND (porcentaje_grasa <= (100)::numeric))));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_muslo CHECK (((muslo_cm IS NULL) OR (muslo_cm > (0)::numeric)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_pecho CHECK (((pecho_cm IS NULL) OR (pecho_cm > (0)::numeric)));
ALTER TABLE medicion_corporal ADD CONSTRAINT ck_medicion_corporal_peso CHECK (((peso_kg IS NULL) OR (peso_kg > (0)::numeric)));
ALTER TABLE membresia ADD CONSTRAINT ck_membresia_costo_final CHECK ((costo_final >= (0)::numeric));
ALTER TABLE membresia ADD CONSTRAINT ck_membresia_estado CHECK (((estado_membresia)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('VENCIDA'::character varying)::text, ('CONGELADA'::character varying)::text, ('CANCELADA'::character varying)::text, ('PENDIENTE'::character varying)::text])));
ALTER TABLE membresia ADD CONSTRAINT ck_membresia_fechas CHECK ((fecha_fin >= fecha_inicio));
ALTER TABLE membresia ADD CONSTRAINT ck_membresia_numero CHECK ((btrim((numero_membresia)::text) <> ''::text));
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_porcentaje_comision_check CHECK (((porcentaje_comision >= (0)::numeric) AND (porcentaje_comision <= (100)::numeric)));
ALTER TABLE nutricionista ADD CONSTRAINT ck_nutricionista_estado_licencia CHECK (((estado_licencia)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('SUSPENDIDA'::character varying)::text, ('VENCIDA'::character varying)::text, ('INACTIVA'::character varying)::text])));
ALTER TABLE nutricionista ADD CONSTRAINT ck_nutricionista_fecha_inicio_profesion CHECK (((fecha_inicio_profesion IS NULL) OR (fecha_inicio_profesion <= CURRENT_DATE)));
ALTER TABLE nutricionista ADD CONSTRAINT ck_nutricionista_numero_licencia CHECK ((btrim((numero_licencia)::text) <> ''::text));
ALTER TABLE pago ADD CONSTRAINT chk_pago_codigo_formato CHECK (((codigo_pago)::text ~ '^PG[0-9]{5}$'::text));
ALTER TABLE pago ADD CONSTRAINT ck_pago_codigo_pago CHECK ((btrim((codigo_pago)::text) <> ''::text));
ALTER TABLE pago ADD CONSTRAINT ck_pago_estado CHECK (((estado_pago)::text = ANY (ARRAY[('REGISTRADO'::character varying)::text, ('CONFIRMADO'::character varying)::text, ('ANULADO'::character varying)::text, ('RECHAZADO'::character varying)::text, ('DEVUELTO'::character varying)::text])));
ALTER TABLE pago ADD CONSTRAINT ck_pago_monto_pago CHECK ((monto_pago >= (0)::numeric));
ALTER TABLE pago ADD CONSTRAINT ck_pago_monto_recibido CHECK ((monto_recibido >= (0)::numeric));
ALTER TABLE pago ADD CONSTRAINT ck_pago_monto_recibido_monto_pago CHECK ((monto_recibido >= monto_pago));
ALTER TABLE pago ADD CONSTRAINT ck_pago_referencia_transaccion CHECK (((referencia_transaccion IS NULL) OR (btrim((referencia_transaccion)::text) <> ''::text)));
ALTER TABLE permiso ADD CONSTRAINT ck_permiso_nombre_permiso_no_vacio CHECK ((btrim((nombre_permiso)::text) <> ''::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_apellidos_no_vacio CHECK ((btrim((apellidos)::text) <> ''::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_cedula CHECK (((cedula)::text ~ '^[0-9]{10}$'::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_cedula_formato CHECK (((cedula)::text ~ '^[0-9]{10}$'::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_correo_formato CHECK (((correo)::text ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_nombres_no_vacio CHECK ((btrim((nombres)::text) <> ''::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_sexo CHECK (((sexo)::text = ANY (ARRAY[('Masculino'::character varying)::text, ('Femenino'::character varying)::text, ('Otro'::character varying)::text])));
ALTER TABLE persona ADD CONSTRAINT ck_persona_telefono CHECK (((telefono)::text ~ '^[0-9]{10}$'::text));
ALTER TABLE persona ADD CONSTRAINT ck_persona_telefono_formato CHECK (((telefono)::text ~ '^[0-9]{7,15}$'::text));
ALTER TABLE plan_nutricional ADD CONSTRAINT chk_plan_codigo_formato CHECK (((codigo_plan)::text ~ '^PN[0-9]{5}$'::text));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_calorias CHECK (((calorias_objetivo IS NULL) OR (calorias_objetivo > 0)));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_carbohidratos CHECK (((carbohidratos_objetivo_g IS NULL) OR (carbohidratos_objetivo_g >= (0)::numeric)));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_codigo_plan CHECK ((btrim((codigo_plan)::text) <> ''::text));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_estado CHECK (((estado_plan)::text = ANY (ARRAY[('ACTIVO'::character varying)::text, ('FINALIZADO'::character varying)::text, ('SUSPENDIDO'::character varying)::text, ('CANCELADO'::character varying)::text])));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_fecha_creacion CHECK ((fecha_creacion <= CURRENT_DATE));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_fechas CHECK (((fecha_fin IS NULL) OR (fecha_fin >= fecha_inicio)));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_nombre_plan CHECK ((btrim((nombre_plan)::text) <> ''::text));
ALTER TABLE plan_nutricional ADD CONSTRAINT ck_plan_nutricional_proteinas CHECK (((proteinas_objetivo_g IS NULL) OR (proteinas_objetivo_g >= (0)::numeric)));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_estado CHECK (((estado_progreso)::text = ANY (ARRAY[('REGISTRADO'::character varying)::text, ('REVISADO'::character varying)::text, ('ANULADO'::character varying)::text])));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_fecha CHECK ((fecha_registro <= CURRENT_DATE));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_detalle_json CHECK (jsonb_typeof(detalle_ejercicios) = 'object');
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_nivel_esfuerzo CHECK (((nivel_esfuerzo IS NULL) OR ((nivel_esfuerzo)::text = ANY (ARRAY[('BAJO'::character varying)::text, ('MEDIO'::character varying)::text, ('ALTO'::character varying)::text]))));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_peso_corporal CHECK (((peso_corporal IS NULL) OR (peso_corporal > (0)::numeric)));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_sesiones_completadas CHECK (((sesiones_completadas >= 0) AND (sesiones_completadas <= sesiones_planificadas)));
ALTER TABLE progreso_rutina ADD CONSTRAINT ck_progreso_rutina_sesiones_planificadas CHECK ((sesiones_planificadas >= 0));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_descripcion CHECK ((btrim(descripcion) <> ''::text));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_estado CHECK (((estado_recomendacion)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('CUMPLIDA'::character varying)::text, ('CANCELADA'::character varying)::text])));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_fechas CHECK (((fecha_fin IS NULL) OR (fecha_inicio IS NULL) OR (fecha_fin >= fecha_inicio)));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_prioridad CHECK (((prioridad)::text = ANY (ARRAY[('BAJA'::character varying)::text, ('MEDIA'::character varying)::text, ('ALTA'::character varying)::text])));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_tipo CHECK ((btrim((tipo_recomendacion)::text) <> ''::text));
ALTER TABLE recomendacion ADD CONSTRAINT ck_recomendacion_titulo CHECK ((btrim((titulo)::text) <> ''::text));
ALTER TABLE reserva ADD CONSTRAINT chk_reserva_codigo_formato CHECK (((codigo_reserva)::text ~ '^RS[0-9]{5}$'::text));
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_cancelacion_fecha CHECK (((fecha_hora_cancelacion IS NULL) OR (fecha_hora_cancelacion >= fecha_hora_reserva)));
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_cancelacion_motivo CHECK ((((estado_reserva)::text <> 'CANCELADA'::text) OR (motivo_cancelacion IS NOT NULL)));
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_codigo CHECK ((btrim((codigo_reserva)::text) <> ''::text));
ALTER TABLE reserva ADD CONSTRAINT ck_reserva_estado CHECK (((estado_reserva)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('CANCELADA'::character varying)::text, ('ASISTIDA'::character varying)::text, ('NO_ASISTIDA'::character varying)::text])));
ALTER TABLE resultado_indicador ADD CONSTRAINT ck_resultado_indicador_fecha CHECK ((fecha_registro <= CURRENT_DATE));
ALTER TABLE rol ADD CONSTRAINT ck_rol_nombre_rol_no_vacio CHECK ((btrim((nombre_rol)::text) <> ''::text));
ALTER TABLE rol_permiso ADD CONSTRAINT ck_rol_permiso_estado_asignacion CHECK (((estado_asignacion)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('INACTIVA'::character varying)::text])));
ALTER TABLE rutina ADD CONSTRAINT ck_rutina_duracion CHECK ((duracion_semanas > 0));
ALTER TABLE rutina ADD CONSTRAINT ck_rutina_estado CHECK (((estado_rutina)::text = ANY (ARRAY[('ACTIVA'::character varying)::text, ('INACTIVA'::character varying)::text, ('ARCHIVADA'::character varying)::text])));
ALTER TABLE rutina ADD CONSTRAINT ck_rutina_fecha_creacion CHECK ((fecha_creacion <= CURRENT_DATE));
ALTER TABLE rutina ADD CONSTRAINT ck_rutina_nivel CHECK (((nivel)::text = ANY (ARRAY[('PRINCIPIANTE'::character varying)::text, ('INTERMEDIO'::character varying)::text, ('AVANZADO'::character varying)::text])));
ALTER TABLE rutina ADD CONSTRAINT ck_rutina_nombre CHECK ((btrim((nombre_rutina)::text) <> ''::text));
ALTER TABLE rutina_dia_entrenamiento ADD CONSTRAINT ck_rutina_dia_entrenamiento_dia CHECK (((dia_entrenamiento)::text = ANY (ARRAY[('LUNES'::character varying)::text, ('MARTES'::character varying)::text, ('MIERCOLES'::character varying)::text, ('JUEVES'::character varying)::text, ('VIERNES'::character varying)::text, ('SABADO'::character varying)::text, ('DOMINGO'::character varying)::text])));
ALTER TABLE tipo_membresia ADD CONSTRAINT ck_tipo_membresia_duracion_dias CHECK ((duracion_dias > 0));
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_duracion_dias_check CHECK ((duracion_dias > 0));
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_limite_accesos_check CHECK (((limite_accesos IS NULL) OR (limite_accesos >= 0)));
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_precio_base_check CHECK ((precio_base >= (0)::numeric));
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_intentos CHECK ((intentos_fallidos >= 0));
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_nombre_usuario_no_vacio CHECK ((btrim((nombre_usuario)::text) <> ''::text));
ALTER TABLE asignacion_rutina ADD CONSTRAINT fk_asignacion_rutina_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE asignacion_rutina ADD CONSTRAINT fk_asignacion_rutina_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE asistencia ADD CONSTRAINT fk_asistencia_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE bitacora ADD CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE cliente ADD CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE comprobante ADD CONSTRAINT fk_comprobante_pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE congelacion ADD CONSTRAINT fk_congelacion_membresia FOREIGN KEY (id_membresia) REFERENCES membresia(id_membresia) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT fk_contiene_ejercicio_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT fk_contiene_ejercicio_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT fk_contiene_ejercicio_dia_rutina FOREIGN KEY (id_rutina, dia_semana) REFERENCES rutina_dia_entrenamiento(id_rutina, dia_entrenamiento) ON UPDATE CASCADE ON DELETE RESTRICT;
ALTER TABLE ejercicio ADD CONSTRAINT fk_ejercicio_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador(id_persona) ON UPDATE RESTRICT ON DELETE SET NULL;
ALTER TABLE detalle_factura ADD CONSTRAINT fk_detalle_factura_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE empleado ADD CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE entrenador ADD CONSTRAINT fk_entrenador_empleado FOREIGN KEY (id_persona) REFERENCES empleado(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE clase_grupal ADD CONSTRAINT fk_clase_grupal_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT fk_evaluacion_fisica_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT fk_evaluacion_fisica_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE factura ADD CONSTRAINT fk_factura_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE incluye_alimento ADD CONSTRAINT fk_incluye_alimento_alimento FOREIGN KEY (id_alimento) REFERENCES alimento(id_alimento) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE incluye_alimento ADD CONSTRAINT fk_incluye_alimento_plan_nutricional FOREIGN KEY (id_plan_nutricional) REFERENCES plan_nutricional(id_plan_nutricional) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE medicion_corporal ADD CONSTRAINT fk_medicion_corporal_evaluacion_fisica FOREIGN KEY (id_evaluacion) REFERENCES evaluacion_fisica(id_evaluacion) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE membresia ADD CONSTRAINT fk_membresia_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE membresia ADD CONSTRAINT fk_membresia_tipo_membresia FOREIGN KEY (id_tipo_membresia) REFERENCES tipo_membresia(id_tipo_membresia) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE nutricionista ADD CONSTRAINT fk_nutricionista_empleado FOREIGN KEY (id_persona) REFERENCES empleado(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE pago ADD CONSTRAINT fk_pago_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE pago ADD CONSTRAINT fk_pago_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id_metodo_pago) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE plan_nutricional ADD CONSTRAINT fk_plan_nutricional_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE plan_nutricional ADD CONSTRAINT fk_plan_nutricional_nutricionista FOREIGN KEY (id_nutricionista) REFERENCES nutricionista(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE progreso_rutina ADD CONSTRAINT fk_progreso_rutina_asignacion_rutina FOREIGN KEY (id_cliente, id_rutina) REFERENCES asignacion_rutina(id_cliente, id_rutina) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE recomendacion ADD CONSTRAINT fk_recomendacion_evaluacion_fisica FOREIGN KEY (id_evaluacion) REFERENCES evaluacion_fisica(id_evaluacion) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE reserva ADD CONSTRAINT fk_reserva_clase FOREIGN KEY (id_clase) REFERENCES clase_grupal(id_clase);
ALTER TABLE reserva ADD CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE resultado_indicador ADD CONSTRAINT fk_resultado_indicador_evaluacion_fisica FOREIGN KEY (id_evaluacion) REFERENCES evaluacion_fisica(id_evaluacion) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE resultado_indicador ADD CONSTRAINT fk_resultado_indicador_indicador_salud FOREIGN KEY (id_indicador) REFERENCES indicador_salud(id_indicador) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rutina ADD CONSTRAINT fk_rutina_entrenador FOREIGN KEY (id_entrenador) REFERENCES entrenador(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rutina_dia_entrenamiento ADD CONSTRAINT fk_rutina_dia_entrenamiento_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON UPDATE RESTRICT ON DELETE RESTRICT;
CREATE INDEX IF NOT EXISTS idx_clase_grupal_entrenador ON clase_grupal(id_entrenador);
CREATE INDEX IF NOT EXISTS idx_reserva_clase_estado ON reserva(id_clase, estado_reserva);
CREATE INDEX IF NOT EXISTS idx_reserva_cliente ON reserva(id_cliente);
CREATE INDEX IF NOT EXISTS idx_membresia_cliente ON membresia(id_cliente);
CREATE INDEX IF NOT EXISTS idx_factura_cliente ON factura(id_cliente);
CREATE INDEX IF NOT EXISTS idx_pago_factura ON pago(id_factura);
CREATE INDEX IF NOT EXISTS idx_rutina_entrenador ON rutina(id_entrenador);
CREATE INDEX IF NOT EXISTS idx_progreso_cliente_fecha_rutina ON progreso_rutina(id_cliente, fecha_registro, id_rutina);
CREATE INDEX IF NOT EXISTS idx_plan_cliente ON plan_nutricional(id_cliente);
CREATE INDEX IF NOT EXISTS idx_plan_nutricionista ON plan_nutricional(id_nutricionista);
CREATE UNIQUE INDEX IF NOT EXISTS ux_plan_activo_cliente
    ON plan_nutricional(id_cliente)
    WHERE estado_plan = 'ACTIVO';
ALTER TABLE alimento ADD CONSTRAINT alimento_id_alimento_not_null NOT NULL id_alimento;
ALTER TABLE alimento ADD CONSTRAINT alimento_nombre_alimento_not_null NOT NULL nombre_alimento;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_estado_asignacion_not_null NOT NULL estado_asignacion;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_fecha_asignacion_not_null NOT NULL fecha_asignacion;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_fecha_inicio_not_null NOT NULL fecha_inicio;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_id_asignacion_not_null NOT NULL id_asignacion;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE asignacion_rutina ADD CONSTRAINT asignacion_rutina_id_rutina_not_null NOT NULL id_rutina;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_estado_acceso_not_null NOT NULL estado_acceso;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_fecha_asistencia_not_null NOT NULL fecha_asistencia;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_hora_entrada_not_null NOT NULL hora_entrada;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_id_asistencia_not_null NOT NULL id_asistencia;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_metodo_registro_not_null NOT NULL metodo_registro;
ALTER TABLE asistencia ADD CONSTRAINT asistencia_tipo_acceso_not_null NOT NULL tipo_acceso;
ALTER TABLE bitacora ADD CONSTRAINT bitacora_accion_realizada_not_null NOT NULL accion_realizada;
ALTER TABLE bitacora ADD CONSTRAINT bitacora_fecha_hora_not_null NOT NULL fecha_hora;
ALTER TABLE bitacora ADD CONSTRAINT bitacora_id_bitacora_not_null NOT NULL id_bitacora;
ALTER TABLE bitacora ADD CONSTRAINT bitacora_id_usuario_not_null NOT NULL id_usuario;
ALTER TABLE bitacora ADD CONSTRAINT bitacora_modulo_not_null NOT NULL modulo;
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_duracion_base_minutos_not_null NOT NULL duracion_base_minutos;
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_estado_clase_not_null NOT NULL estado_clase;
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_id_clase_not_null NOT NULL id_clase;
ALTER TABLE clase_grupal ADD CONSTRAINT clase_grupal_nombre_clase_not_null NOT NULL nombre_clase;
ALTER TABLE cliente ADD CONSTRAINT cliente_codigo_cliente_not_null NOT NULL codigo_cliente;
ALTER TABLE cliente ADD CONSTRAINT cliente_estado_cliente_not_null NOT NULL estado_cliente;
ALTER TABLE cliente ADD CONSTRAINT cliente_fecha_registro_not_null NOT NULL fecha_registro;
ALTER TABLE cliente ADD CONSTRAINT cliente_id_persona_not_null NOT NULL id_persona;
ALTER TABLE cliente ADD CONSTRAINT cliente_peso_inicial_not_null NOT NULL peso_inicial;
ALTER TABLE cliente ADD CONSTRAINT cliente_peso_meta_not_null NOT NULL peso_meta;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_estado_comprobante_not_null NOT NULL estado_comprobante;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_fecha_emision_not_null NOT NULL fecha_emision;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_id_comprobante_not_null NOT NULL id_comprobante;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_id_pago_not_null NOT NULL id_pago;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_numero_comprobante_not_null NOT NULL numero_comprobante;
ALTER TABLE comprobante ADD CONSTRAINT comprobante_tipo_comprobante_not_null NOT NULL tipo_comprobante;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_estado_congelacion_not_null NOT NULL estado_congelacion;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_fecha_fin_not_null NOT NULL fecha_fin;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_fecha_inicio_not_null NOT NULL fecha_inicio;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_fecha_solicitud_not_null NOT NULL fecha_solicitud;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_id_congelacion_not_null NOT NULL id_congelacion;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_id_membresia_not_null NOT NULL id_membresia;
ALTER TABLE congelacion ADD CONSTRAINT congelacion_motivo_not_null NOT NULL motivo;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_dia_semana_not_null NOT NULL dia_semana;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_id_ejercicio_not_null NOT NULL id_ejercicio;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_id_rutina_ejercicio_not_null NOT NULL id_rutina_ejercicio;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_id_rutina_not_null NOT NULL id_rutina;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_orden_not_null NOT NULL orden;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_repeticiones_not_null NOT NULL repeticiones;
ALTER TABLE contiene_ejercicio ADD CONSTRAINT contiene_ejercicio_series_not_null NOT NULL series;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_cantidad_not_null NOT NULL cantidad;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_id_detalle_factura_not_null NOT NULL id_detalle_factura;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_id_factura_not_null NOT NULL id_factura;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_porcentaje_impuesto_not_null NOT NULL porcentaje_impuesto;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_precio_unitario_not_null NOT NULL precio_unitario;
ALTER TABLE detalle_factura ADD CONSTRAINT detalle_factura_tipo_concepto_not_null NOT NULL tipo_concepto;
ALTER TABLE ejercicio ADD CONSTRAINT ejercicio_id_ejercicio_not_null NOT NULL id_ejercicio;
ALTER TABLE ejercicio ADD CONSTRAINT ejercicio_nombre_ejercicio_not_null NOT NULL nombre_ejercicio;
ALTER TABLE empleado ADD CONSTRAINT empleado_codigo_empleado_not_null NOT NULL codigo_empleado;
ALTER TABLE empleado ADD CONSTRAINT empleado_estado_empleado_not_null NOT NULL estado_empleado;
ALTER TABLE empleado ADD CONSTRAINT empleado_fecha_ingreso_not_null NOT NULL fecha_ingreso;
ALTER TABLE empleado ADD CONSTRAINT empleado_hora_fin_not_null NOT NULL hora_fin;
ALTER TABLE empleado ADD CONSTRAINT empleado_hora_inicio_not_null NOT NULL hora_inicio;
ALTER TABLE empleado ADD CONSTRAINT empleado_id_persona_not_null NOT NULL id_persona;
ALTER TABLE empleado ADD CONSTRAINT empleado_salario_not_null NOT NULL salario;
ALTER TABLE empleado ADD CONSTRAINT empleado_tipo_contrato_not_null NOT NULL tipo_contrato;
ALTER TABLE entrenador ADD CONSTRAINT entrenador_estado_entrenador_not_null NOT NULL estado_entrenador;
ALTER TABLE entrenador ADD CONSTRAINT entrenador_id_persona_not_null NOT NULL id_persona;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_codigo_evaluacion_not_null NOT NULL codigo_evaluacion;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_estado_evaluacion_not_null NOT NULL estado_evaluacion;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_fecha_evaluacion_not_null NOT NULL fecha_evaluacion;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_id_entrenador_not_null NOT NULL id_entrenador;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_id_evaluacion_not_null NOT NULL id_evaluacion;
ALTER TABLE evaluacion_fisica ADD CONSTRAINT evaluacion_fisica_tipo_evaluacion_not_null NOT NULL tipo_evaluacion;
ALTER TABLE factura ADD CONSTRAINT factura_estado_factura_not_null NOT NULL estado_factura;
ALTER TABLE factura ADD CONSTRAINT factura_fecha_emision_not_null NOT NULL fecha_emision;
ALTER TABLE factura ADD CONSTRAINT factura_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE factura ADD CONSTRAINT factura_id_factura_not_null NOT NULL id_factura;
ALTER TABLE factura ADD CONSTRAINT factura_impuesto_not_null NOT NULL impuesto;
ALTER TABLE factura ADD CONSTRAINT factura_numero_factura_not_null NOT NULL numero_factura;
ALTER TABLE factura ADD CONSTRAINT factura_total_descuento_not_null NOT NULL total_descuento;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_cantidad_not_null NOT NULL cantidad;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_dia_semana_not_null NOT NULL dia_semana;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_estado_detalle_not_null NOT NULL estado_detalle;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_id_alimento_not_null NOT NULL id_alimento;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_id_detalle_plan_not_null NOT NULL id_detalle_plan;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_id_plan_nutricional_not_null NOT NULL id_plan_nutricional;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_orden_comida_not_null NOT NULL orden_comida;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_tipo_comida_not_null NOT NULL tipo_comida;
ALTER TABLE incluye_alimento ADD CONSTRAINT incluye_alimento_unidad_medida_not_null NOT NULL unidad_medida;
ALTER TABLE indicador_salud ADD CONSTRAINT indicador_salud_estado_indicador_not_null NOT NULL estado_indicador;
ALTER TABLE indicador_salud ADD CONSTRAINT indicador_salud_id_indicador_not_null NOT NULL id_indicador;
ALTER TABLE indicador_salud ADD CONSTRAINT indicador_salud_nombre_indicador_not_null NOT NULL nombre_indicador;
ALTER TABLE medicion_corporal ADD CONSTRAINT medicion_corporal_id_evaluacion_not_null NOT NULL id_evaluacion;
ALTER TABLE medicion_corporal ADD CONSTRAINT medicion_corporal_id_medicion_not_null NOT NULL id_medicion;
ALTER TABLE membresia ADD CONSTRAINT membresia_costo_final_not_null NOT NULL costo_final;
ALTER TABLE membresia ADD CONSTRAINT membresia_estado_membresia_not_null NOT NULL estado_membresia;
ALTER TABLE membresia ADD CONSTRAINT membresia_fecha_fin_not_null NOT NULL fecha_fin;
ALTER TABLE membresia ADD CONSTRAINT membresia_fecha_inicio_not_null NOT NULL fecha_inicio;
ALTER TABLE membresia ADD CONSTRAINT membresia_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE membresia ADD CONSTRAINT membresia_id_membresia_not_null NOT NULL id_membresia;
ALTER TABLE membresia ADD CONSTRAINT membresia_id_tipo_membresia_not_null NOT NULL id_tipo_membresia;
ALTER TABLE membresia ADD CONSTRAINT membresia_numero_membresia_not_null NOT NULL numero_membresia;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_estado_metodo_not_null NOT NULL estado_metodo;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_id_metodo_pago_not_null NOT NULL id_metodo_pago;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_nombre_metodo_not_null NOT NULL nombre_metodo;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_permite_cuotas_not_null NOT NULL permite_cuotas;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_porcentaje_comision_not_null NOT NULL porcentaje_comision;
ALTER TABLE metodo_pago ADD CONSTRAINT metodo_pago_requiere_referencia_not_null NOT NULL requiere_referencia;
ALTER TABLE nutricionista ADD CONSTRAINT nutricionista_estado_licencia_not_null NOT NULL estado_licencia;
ALTER TABLE nutricionista ADD CONSTRAINT nutricionista_id_persona_not_null NOT NULL id_persona;
ALTER TABLE nutricionista ADD CONSTRAINT nutricionista_numero_licencia_not_null NOT NULL numero_licencia;
ALTER TABLE pago ADD CONSTRAINT pago_codigo_pago_not_null NOT NULL codigo_pago;
ALTER TABLE pago ADD CONSTRAINT pago_estado_pago_not_null NOT NULL estado_pago;
ALTER TABLE pago ADD CONSTRAINT pago_fecha_hora_pago_not_null NOT NULL fecha_hora_pago;
ALTER TABLE pago ADD CONSTRAINT pago_id_factura_not_null NOT NULL id_factura;
ALTER TABLE pago ADD CONSTRAINT pago_id_metodo_pago_not_null NOT NULL id_metodo_pago;
ALTER TABLE pago ADD CONSTRAINT pago_id_pago_not_null NOT NULL id_pago;
ALTER TABLE pago ADD CONSTRAINT pago_monto_pago_not_null NOT NULL monto_pago;
ALTER TABLE pago ADD CONSTRAINT pago_monto_recibido_not_null NOT NULL monto_recibido;
ALTER TABLE permiso ADD CONSTRAINT permiso_accion_not_null NOT NULL accion;
ALTER TABLE permiso ADD CONSTRAINT permiso_estado_permiso_not_null NOT NULL estado_permiso;
ALTER TABLE permiso ADD CONSTRAINT permiso_id_permiso_not_null NOT NULL id_permiso;
ALTER TABLE permiso ADD CONSTRAINT permiso_modulo_not_null NOT NULL modulo;
ALTER TABLE permiso ADD CONSTRAINT permiso_nombre_permiso_not_null NOT NULL nombre_permiso;
ALTER TABLE persona ADD CONSTRAINT persona_apellidos_not_null NOT NULL apellidos;
ALTER TABLE persona ADD CONSTRAINT persona_cedula_not_null NOT NULL cedula;
ALTER TABLE persona ADD CONSTRAINT persona_correo_not_null NOT NULL correo;
ALTER TABLE persona ADD CONSTRAINT persona_estado_not_null NOT NULL estado;
ALTER TABLE persona ADD CONSTRAINT persona_fecha_nacimiento_not_null NOT NULL fecha_nacimiento;
ALTER TABLE persona ADD CONSTRAINT persona_id_persona_not_null NOT NULL id_persona;
ALTER TABLE persona ADD CONSTRAINT persona_nombres_not_null NOT NULL nombres;
ALTER TABLE persona ADD CONSTRAINT persona_sexo_not_null NOT NULL sexo;
ALTER TABLE persona ADD CONSTRAINT persona_telefono_not_null NOT NULL telefono;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_codigo_plan_not_null NOT NULL codigo_plan;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_estado_plan_not_null NOT NULL estado_plan;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_fecha_creacion_not_null NOT NULL fecha_creacion;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_fecha_inicio_not_null NOT NULL fecha_inicio;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_id_nutricionista_not_null NOT NULL id_nutricionista;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_id_plan_nutricional_not_null NOT NULL id_plan_nutricional;
ALTER TABLE plan_nutricional ADD CONSTRAINT plan_nutricional_nombre_plan_not_null NOT NULL nombre_plan;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_estado_progreso_not_null NOT NULL estado_progreso;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_fecha_registro_not_null NOT NULL fecha_registro;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_id_progreso_not_null NOT NULL id_progreso;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_id_rutina_not_null NOT NULL id_rutina;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_sesiones_completadas_not_null NOT NULL sesiones_completadas;
ALTER TABLE progreso_rutina ADD CONSTRAINT progreso_rutina_sesiones_planificadas_not_null NOT NULL sesiones_planificadas;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_descripcion_not_null NOT NULL descripcion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_estado_recomendacion_not_null NOT NULL estado_recomendacion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_fecha_recomendacion_not_null NOT NULL fecha_recomendacion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_id_evaluacion_not_null NOT NULL id_evaluacion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_id_recomendacion_not_null NOT NULL id_recomendacion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_prioridad_not_null NOT NULL prioridad;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_tipo_recomendacion_not_null NOT NULL tipo_recomendacion;
ALTER TABLE recomendacion ADD CONSTRAINT recomendacion_titulo_not_null NOT NULL titulo;
ALTER TABLE reserva ADD CONSTRAINT reserva_asistencia_confirmada_not_null NOT NULL asistencia_confirmada;
ALTER TABLE reserva ADD CONSTRAINT reserva_codigo_reserva_not_null NOT NULL codigo_reserva;
ALTER TABLE reserva ADD CONSTRAINT reserva_estado_reserva_not_null NOT NULL estado_reserva;
ALTER TABLE reserva ADD CONSTRAINT reserva_fecha_hora_reserva_not_null NOT NULL fecha_hora_reserva;
ALTER TABLE reserva ADD CONSTRAINT reserva_id_clase_not_null NOT NULL id_clase;
ALTER TABLE reserva ADD CONSTRAINT reserva_id_cliente_not_null NOT NULL id_cliente;
ALTER TABLE reserva ADD CONSTRAINT reserva_id_reserva_not_null NOT NULL id_reserva;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_fecha_registro_not_null NOT NULL fecha_registro;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_fuera_de_rango_not_null NOT NULL fuera_de_rango;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_id_evaluacion_not_null NOT NULL id_evaluacion;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_id_indicador_not_null NOT NULL id_indicador;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_id_resultado_not_null NOT NULL id_resultado;
ALTER TABLE resultado_indicador ADD CONSTRAINT resultado_indicador_valor_obtenido_not_null NOT NULL valor_obtenido;
ALTER TABLE rol ADD CONSTRAINT rol_estado_rol_not_null NOT NULL estado_rol;
ALTER TABLE rol ADD CONSTRAINT rol_id_rol_not_null NOT NULL id_rol;
ALTER TABLE rol ADD CONSTRAINT rol_nombre_rol_not_null NOT NULL nombre_rol;
ALTER TABLE rol_permiso ADD CONSTRAINT rol_permiso_estado_asignacion_not_null NOT NULL estado_asignacion;
ALTER TABLE rol_permiso ADD CONSTRAINT rol_permiso_fecha_asignacion_not_null NOT NULL fecha_asignacion;
ALTER TABLE rol_permiso ADD CONSTRAINT rol_permiso_id_permiso_not_null NOT NULL id_permiso;
ALTER TABLE rol_permiso ADD CONSTRAINT rol_permiso_id_rol_not_null NOT NULL id_rol;
ALTER TABLE rutina ADD CONSTRAINT rutina_duracion_semanas_not_null NOT NULL duracion_semanas;
ALTER TABLE rutina ADD CONSTRAINT rutina_estado_rutina_not_null NOT NULL estado_rutina;
ALTER TABLE rutina ADD CONSTRAINT rutina_fecha_creacion_not_null NOT NULL fecha_creacion;
ALTER TABLE rutina ADD CONSTRAINT rutina_id_entrenador_not_null NOT NULL id_entrenador;
ALTER TABLE rutina ADD CONSTRAINT rutina_id_rutina_not_null NOT NULL id_rutina;
ALTER TABLE rutina ADD CONSTRAINT rutina_nivel_not_null NOT NULL nivel;
ALTER TABLE rutina ADD CONSTRAINT rutina_nombre_rutina_not_null NOT NULL nombre_rutina;
ALTER TABLE rutina_dia_entrenamiento ADD CONSTRAINT rutina_dia_entrenamiento_dia_entrenamiento_not_null NOT NULL dia_entrenamiento;
ALTER TABLE rutina_dia_entrenamiento ADD CONSTRAINT rutina_dia_entrenamiento_id_rutina_not_null NOT NULL id_rutina;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_acceso_ilimitado_not_null NOT NULL acceso_ilimitado;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_duracion_dias_not_null NOT NULL duracion_dias;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_estado_tipo_not_null NOT NULL estado_tipo;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_id_tipo_membresia_not_null NOT NULL id_tipo_membresia;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_nombre_not_null NOT NULL nombre;
ALTER TABLE tipo_membresia ADD CONSTRAINT tipo_membresia_precio_base_not_null NOT NULL precio_base;
ALTER TABLE usuario ADD CONSTRAINT usuario_bloqueado_not_null NOT NULL bloqueado;
ALTER TABLE usuario ADD CONSTRAINT usuario_clave_hash_not_null NOT NULL clave_hash;
ALTER TABLE usuario ADD CONSTRAINT usuario_estado_usuario_not_null NOT NULL estado_usuario;
ALTER TABLE usuario ADD CONSTRAINT usuario_fecha_creacion_not_null NOT NULL fecha_creacion;
ALTER TABLE usuario ADD CONSTRAINT usuario_id_rol_not_null NOT NULL id_rol;
ALTER TABLE usuario ADD CONSTRAINT usuario_id_usuario_not_null NOT NULL id_usuario;
ALTER TABLE usuario ADD CONSTRAINT usuario_intentos_fallidos_not_null NOT NULL intentos_fallidos;
ALTER TABLE usuario ADD CONSTRAINT usuario_nombre_usuario_not_null NOT NULL nombre_usuario;

-- ----- Ajuste de secuencias/identity -----
SELECT setval(pg_get_serial_sequence('alimento', 'id_alimento'), GREATEST(COALESCE((SELECT MAX(id_alimento) FROM alimento), 1), 1));
SELECT setval(pg_get_serial_sequence('asignacion_rutina', 'id_asignacion'), GREATEST(COALESCE((SELECT MAX(id_asignacion) FROM asignacion_rutina), 1), 1));
SELECT setval(pg_get_serial_sequence('asistencia', 'id_asistencia'), GREATEST(COALESCE((SELECT MAX(id_asistencia) FROM asistencia), 1), 1));
SELECT setval(pg_get_serial_sequence('bitacora', 'id_bitacora'), GREATEST(COALESCE((SELECT MAX(id_bitacora) FROM bitacora), 1), 1));
SELECT setval(pg_get_serial_sequence('clase_grupal', 'id_clase'), GREATEST(COALESCE((SELECT MAX(id_clase) FROM clase_grupal), 1), 1));
SELECT setval(pg_get_serial_sequence('comprobante', 'id_comprobante'), GREATEST(COALESCE((SELECT MAX(id_comprobante) FROM comprobante), 1), 1));
SELECT setval(pg_get_serial_sequence('congelacion', 'id_congelacion'), GREATEST(COALESCE((SELECT MAX(id_congelacion) FROM congelacion), 1), 1));
SELECT setval(pg_get_serial_sequence('contiene_ejercicio', 'id_rutina_ejercicio'), GREATEST(COALESCE((SELECT MAX(id_rutina_ejercicio) FROM contiene_ejercicio), 1), 1));
SELECT setval(pg_get_serial_sequence('detalle_factura', 'id_detalle_factura'), GREATEST(COALESCE((SELECT MAX(id_detalle_factura) FROM detalle_factura), 1), 1));
SELECT setval(pg_get_serial_sequence('ejercicio', 'id_ejercicio'), GREATEST(COALESCE((SELECT MAX(id_ejercicio) FROM ejercicio), 1), 1));
SELECT setval(pg_get_serial_sequence('evaluacion_fisica', 'id_evaluacion'), GREATEST(COALESCE((SELECT MAX(id_evaluacion) FROM evaluacion_fisica), 1), 1));
SELECT setval(pg_get_serial_sequence('factura', 'id_factura'), GREATEST(COALESCE((SELECT MAX(id_factura) FROM factura), 1), 1));
SELECT setval('incluye_alimento_id_detalle_plan_seq', GREATEST(COALESCE((SELECT MAX(id_detalle_plan) FROM incluye_alimento), 1), 1));
SELECT setval(pg_get_serial_sequence('indicador_salud', 'id_indicador'), GREATEST(COALESCE((SELECT MAX(id_indicador) FROM indicador_salud), 1), 1));
SELECT setval(pg_get_serial_sequence('medicion_corporal', 'id_medicion'), GREATEST(COALESCE((SELECT MAX(id_medicion) FROM medicion_corporal), 1), 1));
SELECT setval(pg_get_serial_sequence('membresia', 'id_membresia'), GREATEST(COALESCE((SELECT MAX(id_membresia) FROM membresia), 1), 1));
SELECT setval(pg_get_serial_sequence('metodo_pago', 'id_metodo_pago'), GREATEST(COALESCE((SELECT MAX(id_metodo_pago) FROM metodo_pago), 1), 1));
SELECT setval(pg_get_serial_sequence('pago', 'id_pago'), GREATEST(COALESCE((SELECT MAX(id_pago) FROM pago), 1), 1));
SELECT setval(pg_get_serial_sequence('permiso', 'id_permiso'), GREATEST(COALESCE((SELECT MAX(id_permiso) FROM permiso), 1), 1));
SELECT setval(pg_get_serial_sequence('persona', 'id_persona'), GREATEST(COALESCE((SELECT MAX(id_persona) FROM persona), 1), 1));
SELECT setval('plan_nutricional_id_plan_nutricional_seq', GREATEST(COALESCE((SELECT MAX(id_plan_nutricional) FROM plan_nutricional), 1), 1));
SELECT setval(pg_get_serial_sequence('progreso_rutina', 'id_progreso'), GREATEST(COALESCE((SELECT MAX(id_progreso) FROM progreso_rutina), 1), 1));
SELECT setval(pg_get_serial_sequence('recomendacion', 'id_recomendacion'), GREATEST(COALESCE((SELECT MAX(id_recomendacion) FROM recomendacion), 1), 1));
SELECT setval(pg_get_serial_sequence('reserva', 'id_reserva'), GREATEST(COALESCE((SELECT MAX(id_reserva) FROM reserva), 1), 1));
SELECT setval(pg_get_serial_sequence('resultado_indicador', 'id_resultado'), GREATEST(COALESCE((SELECT MAX(id_resultado) FROM resultado_indicador), 1), 1));
SELECT setval(pg_get_serial_sequence('rol', 'id_rol'), GREATEST(COALESCE((SELECT MAX(id_rol) FROM rol), 1), 1));
SELECT setval(pg_get_serial_sequence('rutina', 'id_rutina'), GREATEST(COALESCE((SELECT MAX(id_rutina) FROM rutina), 1), 1));
SELECT setval(pg_get_serial_sequence('tipo_membresia', 'id_tipo_membresia'), GREATEST(COALESCE((SELECT MAX(id_tipo_membresia) FROM tipo_membresia), 1), 1));
SELECT setval(pg_get_serial_sequence('usuario', 'id_usuario'), GREATEST(COALESCE((SELECT MAX(id_usuario) FROM usuario), 1), 1));

-- Fin del dump.
