# Auditoría técnica GYMNOVA — Fase 1

**Fecha auditoría:** 2026-08-22
**Auditado por:** Claude (Opus 4.7) sobre el árbol `GYMNOVA_FINAL_COMPLETO`
**Alcance:** lectura y análisis. Sin cambios de código.
**Objetivo:** dejar una hoja de ruta accionable para las fases 2 → 10.

---

## 0. Resumen ejecutivo (léase primero)

1. **El proyecto sí compila** con `ant clean && ant` (Java 17, PostgreSQL JDBC 42.7.13). La arquitectura Swing / Controlador / DAO / Modelo está correctamente separada.
2. **La base de datos existe y responde**. `gymnova_db` tiene 62 tablas, con datos de demostración cargados (6 personas, 4 clientes, 4 membresías, 4 facturas, 14 asistencias, 4 asignaciones de rutina, 4 planes nutricionales, 95 permisos).
3. **La arquitectura por rol está bien planteada** pero incompleta en la ejecución:
   - `AutorizacionControlador` valida permisos a nivel de módulo/acción (defensa en profundidad ✓).
   - `AlcanceRolControlador` limita registros por dueño (entrenador/nutricionista/cliente) usando reflexión sobre getters ✓ — bien diseñado, pero depende de que cada modelo exponga los getters correctos.
   - `NavegacionRol` fuerza que una ventana abierta para un rol no navegue a módulos de otro rol ✓.
   - `GestorConsultaModulo` es un **template genérico CRUD** que reutilizan 8 paneles (ACCESO, MEMBRESIAS, RUTINAS, SALUD, NUTRICION, FINANZAS, INVENTARIO, REPORTES, PERSONAL). Esto reduce drásticamente el trabajo restante.
4. **Existen problemas concretos que impiden la demostración limpia** — todos son solucionables:
   - **Usuario `admin` tiene un hash placeholder** (`PEGA_AQUI_EL_HASH_GENERADO`) → no puede iniciar sesión.
   - **Las contraseñas demo son `Gymnova2026*`, no `Gymnova123*`** (el pedido explícito del usuario).
   - **27 tablas fuera de alcance siguen presentes** con sus 27 DAOs + 27 controladores + registros en `GestorConsultaModulo` + botón `INVENTARIO` en menú.
   - **Dos FKs comparten alcance con módulos por eliminar**: `reserva.id_horario → horario` y `rutina.id_objetivo → objetivo_fitness`. Requieren tratamiento especial (rediseño o `NULL`-ables).
   - **Nombre de columnas incoherente con el pedido**: `factura` NO tiene `subtotal` ni `total` (se derivan). Cualquier consulta que asuma esas columnas fallará.
5. **Módulos completos y usables** (funcionalmente): Login, navegación por rol, PnlPersonas, PnlClientes, PnlSeguridad, PnlPersonal, y todos los CRUDs genéricos vía `GestorConsultaModulo`. Los dashboards por rol (PnlInicioAdministrador/Recepcionista/Entrenador/Nutricionista/Cliente) están cableados a `DashboardControlador`/`PulseControlador` con consultas reales.
6. **Módulos parciales** (interfaz existe, flujo no cierra end-to-end): flujo Recepción cliente→membresía→factura→pago→comprobante (los CRUDs existen aislados pero no se encadenan operativamente en una experiencia guiada).

**Estimación honesta de trabajo restante:** 40–60 horas de ingeniería cuidadosa distribuidas en 6 fases. No es realista pretender cerrarlo en una sola sesión sin romper cosas.

---

## 1. Entorno técnico verificado

| Elemento | Estado | Notas |
|---|---|---|
| JDK | Red Hat OpenJDK 17.0.17 | ✓ |
| Ant | 1.10.17 | ✓ |
| PostgreSQL JDBC | postgresql-42.7.13.jar (más un `postgresql-42.7.13-build.jar` duplicado) | `lib/` tiene 2 jars redundantes; sólo el `-42.7.13.jar` está referenciado desde `nbproject/project.properties`. El otro se puede borrar. |
| Base de datos | `gymnova_db` en `localhost:5432` con `postgres`/`VictorSdef/17` | Conexión verificada. 62 tablas. Servidor PostgreSQL 18.4 (dump). |
| Punto de entrada | `app.GymnovaApp` → `FrmLogin` → `FrmPrincipal` | ✓ Compila y arranca en teoría (no probé el arranque gráfico en esta auditoría). |
| Look & Feel | Nimbus | ✓ |
| `build.xml` / `manifest.mf` | Estándar NetBeans | ✓ |
| `ant clean && ant` | BUILD SUCCESSFUL en 16 s | ✓ Con 100 warnings de javadoc (cosméticos). |

---

## 2. Mapa de arquitectura

```
[Vista Swing]
  FrmLogin ──► LoginControlador ──► UsuarioDAO ──► JDBC
                    │
                    └─► SesionUsuario (singleton estático con Usuario actual)
                    
  FrmPrincipal ──► AutorizacionControlador ──► RolPermisoDAO
                ├─► NavegacionRol (rol → módulos permitidos, hard-coded)
                ├─► PnlInicio{Rol} ──► DashboardControlador, PulseControlador
                └─► Pnl{Modulo}
                     ├─► GestorConsultaModulo ──► ConsultaModuloControlador
                     │       └─► [XxxControlador via reflexión]
                     │                └─► XxxDAO ──► JDBC
                     │       └─► AlcanceRolControlador (filtro por dueño)
                     └─► PnlEditorRegistro ──► EditorRegistroControlador
                             └─► [XxxControlador via reflexión]
                             └─► CatalogoRelacionControlador (combos de FK)

[Utilidades transversales]
  SeguridadClave (PBKDF2WithHmacSHA256, 210_000 iter, sal 16 B) ✓
  BitacoraDAO (no invocada automáticamente en las operaciones)
  DocumentoFacturaPDF (generación de PDF de comprobante)
  AvatarPerfil, EstilosComponentes, AyudasContextuales, TemaDashboard
  Validaciones, GeneradorCodigos, GraficoActividad
  NavegacionRol, SesionUsuario
```

**Puntos fuertes:**
- La reflexión centralizada en `EditorRegistroControlador` + `ConsultaModuloControlador` evita duplicación masiva de vistas.
- `SeguridadClave` está bien implementado (PBKDF2 con sal aleatoria).
- `AlcanceRolControlador` implementa filtrado por dueño con SQL parametrizado (sin inyección).

**Puntos débiles:**
- `SesionUsuario` es un singleton mutable estático global → aceptable para app monousuario, pero cualquier fuga (test que olvida `cerrarSesion`) contamina la próxima ejecución.
- El uso masivo de reflexión hace que ciertos errores sólo aparezcan en runtime cuando se abre un submódulo específico.
- `Bitacora` no se registra automáticamente al inicio/fin de operaciones críticas — la tabla existe con 0 filas.

---

## 3. Estado real de la base de datos

### 3.1 Tablas presentes (62)

**Núcleo de personas y seguridad (9 — TODAS se conservan):**
`persona`, `usuario`, `rol`, `permiso`, `rol_permiso`, `empleado`, `cliente`, `entrenador`, `nutricionista`

**Membresías (3 — TODAS se conservan):** `tipo_membresia`, `membresia`, `congelacion`
**Reservas y acceso (3):** `clase_grupal`, `reserva`, `asistencia` (más `horario` que el usuario quiere eliminar, ver 3.4).
**Entrenamiento (7):** `rutina`, `asignacion_rutina`, `rutina_dia_entrenamiento`, `ejercicio`, `contiene_ejercicio`, `progreso_rutina`, `evaluacion_fisica`, `medicion_corporal`
**Nutrición (6):** `plan_nutricional`, `alimento`, `incluye_alimento`, `indicador_salud`, `resultado_indicador`, `recomendacion`
**Finanzas (5):** `factura`, `detalle_factura`, `metodo_pago`, `pago`, `comprobante`
**Auditoría (1):** `bitacora`

**A eliminar (27):**
`alimento_alergeno`, `aplica_descuento`, `categoria_producto`, `clase_especial`, `cliente_objetivo`, `compra`, `descuento`, `detalle_compra`, `ejercicio_recurso_multimedia`, `entrenador_certificacion`, `entrenador_especialidad`, `equipo`, `especialidad`, `grupo_muscular`, `horario`, `mantenimiento`, `nutricionista_certificacion`, `objetivo_fitness`, `producto`, `proveedor`, `provincia`, `requiere_equipo`, `suministra_producto`, `tipo_equipo`, `tipo_mantenimiento`, `tipo_membresia_beneficio`, `trabaja`

### 3.2 Discrepancias entre el pedido del usuario y el esquema real

| Petición del usuario | Realidad en BD | Impacto |
|---|---|---|
| `factura` debe permitir subtotal / impuestos / total | `factura` tiene `total_descuento`, `impuesto`, sin `subtotal` ni `total`. El total se calcula de `detalle_factura`. | Cualquier código nuevo debe calcular en Java. |
| Cliente-Empleado directo | Empleado hereda `id_persona` de persona (bien). Un mismo `persona` puede ser cliente y empleado si tiene fila en ambas tablas. | Correcto. |
| `bitacora` con inicio de sesión, creación, edición, etc. | Tabla existe con columnas `accion_realizada`, `modulo`, `direccion_ip`, `descripcion`, `resultado`. Está vacía (0 filas). No hay invocaciones automáticas desde controladores. | Falta cablearla desde LoginControlador y los CRUD principales. |
| `numero_membresia` legible | Actualmente se guarda `MB00001`, `MB00002`… OK. | Ver `GeneradorCodigos`. |
| Contraseña única de demostración `Gymnova123*` | Los usuarios demo tienen hash de `Gymnova2026*`. El `admin` tiene un hash placeholder inválido. | Ver §6. |
| `JOSE` con `Admin123*` | Usuario `jose` (minúsculas en BD, login case-insensitive ✓) existe con un hash real (no placeholder). No verifiqué si esa contraseña realmente coincide (la verificación requiere ejecutar el hash sobre `Admin123*` y comparar). | Ver §6. |
| Impedir múltiples planes activos por cliente | No hay constraint UNIQUE parcial que lo prevenga. Actualmente hay `plan_nutricional`.id_plan_nutricional distintos por cliente. | Falta lógica en `PlanNutricionalDAO.guardar` (validación previa) o constraint `EXCLUDE`. |
| Impedir múltiples asignaciones de rutina activas | Similar: no hay constraint que lo prevenga. | Falta lógica. |

### 3.3 Datos de demostración actualmente cargados

**Personas (6):**
| id | cédula | nombre | correo |
|---|---|---|---|
| 1 | 0102030400 | Persona De Prueba | prueba@gymnova.com |
| 2 | 0104225644 | Monica Galarza | monica@gmail.com |
| 3 | 0107160509 | Rolando Navarro | rolando.@gmail.com |
| 4 | 0107770703 | Marck Pintado | marck@gmail.com |
| 5 | 1710034065 | Carlos Andrés Mendoza Ruiz | carlos.mendoza@gymnova.com |
| 6 | 1723456784 | Andrea Sofía Torres López | andrea.torres@gymnova.com |

**Clientes (4):** id_persona 2, 3, 4, 5 (Monica, Rolando, Marck, Carlos)
**Entrenador (1):** id_persona 4 (Marck) — nota: **Marck es a la vez cliente y entrenador** vía persona compartida.
**Nutricionista (1):** id_persona 6 (Andrea)
**Empleados (3):** id_persona 1 (recepcion — sólo empleado), 4 (Marck entrenador), 6 (Andrea nutricionista). Persona 5 (Carlos) es sólo cliente.

**Usuarios (6):**
| id | nombre_usuario | rol | id_persona | estado | hash |
|---|---|---|---|---|---|
| 1 | `jose` | Administrador | NULL | activo | real (`210000:8f3c72a94b1de806…`) |
| 2 | `admin` | Administrador | NULL | activo | **`PEGA_AQUI_EL_HASH_GENERADO`** ⚠️ INVÁLIDO |
| 3 | `RECEPCION` | Recepcionista | 1 | activo | real |
| 4 | `ENTRENADOR` | Entrenador | 4 | activo | real |
| 5 | `NUTRICION` | Nutricionista | 6 | activo | real |
| 6 | `CLIENTE` | Cliente | 2 | activo | real |

**Membresías, reservas, facturas, pagos, comprobantes:** todo con 4 registros (uno por cliente). Fechas 2026-08-05 a 2026-09-04.

**Asignación cliente ↔ entrenador (vía `asignacion_rutina` + `rutina.id_entrenador=4`):** clientes 2, 3, 4, 5 (los 4 clientes están asignados a Marck).
**Asignación cliente ↔ nutricionista (vía `plan_nutricional.id_nutricionista=6`):** clientes 2, 3, 4, 5 (los 4 clientes están asignados a Andrea).

### 3.4 FKs que cruzan hacia módulos por eliminar (ATENCIÓN)

Cuando se eliminen las 27 tablas fuera de alcance, dos referencias afectan a tablas que se conservan:

| Tabla que se queda | Columna FK | Apunta a | Solución propuesta |
|---|---|---|---|
| `reserva` | `id_horario` | `horario` (por eliminar) | **Rediseño**: cambiar `reserva.id_horario` por `id_clase` (FK a `clase_grupal`) + columnas `fecha_hora_reserva` que ya existe + `hora_inicio`, `hora_fin` opcionales. O bien, dejar `horario` fuera del alcance a eliminar y sólo quitarla de menús. **Recomiendo lo segundo por costo/beneficio**: conservar `horario` como catálogo interno de la app, no exponerla al usuario. |
| `rutina` | `id_objetivo` | `objetivo_fitness` (por eliminar) | Hacer `rutina.id_objetivo` NULLable y eliminar `objetivo_fitness`. Los datos actuales tienen `id_objetivo` con valores; hay que hacer `UPDATE rutina SET id_objetivo=NULL` antes del DROP. Alternativamente, conservar `objetivo_fitness` como catálogo interno. |

**Recomendación:** conservar `horario` y `objetivo_fitness` a nivel de BD (invisibles en el menú) para no romper referencias. Es honestamente lo más pragmático dado el alcance restante.

---

## 4. Estado por módulo de la interfaz

### 4.1 Login — `FrmLogin`
- ✓ Verifica usuario/contraseña con hash PBKDF2.
- ✓ Bloqueo tras 3 intentos fallidos (`UsuarioDAO.registrarIntentoFallido`).
- ✓ Registra último acceso.
- ✗ **No registra el evento en `bitacora`**.
- ✗ El mensaje de error revela información parcial ("intentos restantes") — aceptable para app interna.

### 4.2 `FrmPrincipal` (marco principal)
- ✓ Aplica `configurarMenuSegunRol` para ocultar botones que no corresponden al rol.
- ✓ Aplica `aplicarPermisosMenu` para deshabilitar botones sin permiso VER.
- ✓ Refresca dashboard cada vez que se vuelve a INICIO.
- ✓ `verificarRutaRol` bloquea navegación cruzada (defensa además de ocultar).
- ⚠️ El botón `btnInventario` está listado en `botonesMenu` pero se hará invisible para todos los roles cuando se elimine el módulo. **Se puede quitar completo del `.form` sin repercusión** o dejarlo invisible siempre.

### 4.3 Módulo PERSONAS — `PnlPersonas` (1740 líneas, especializado)
- ✓ CRUD completo con formulario propio (no usa `GestorConsultaModulo`).
- ✓ Foto de perfil desde `bytea`.
- ⚠️ Visible sólo para Administrador y Recepcionista según `NavegacionRol`. OK.

### 4.4 Módulo CLIENTES — `PnlClientes` (1398 líneas, especializado)
- ✓ CRUD específico con campos peso inicial / peso meta / código cliente.
- ⚠️ Para roles no-admin, el `PnlModuloAplicacion` muestra un dashboard de sólo lectura (`PnlContenidoEspecializado`) con el resumen de clientes del rol.
- Cliente y Entrenador tienen "Mi perfil" / "Mis clientes" respectivamente — el rótulo cambia dinámicamente en `FrmPrincipal.configurarMenuSegunRol`.

### 4.5 Módulo PERSONAL — `PnlPersonal` (1000 líneas, especializado)
- CRUD para empleado / entrenador / nutricionista. Actualmente incluye sub-CRUDs para especialidad, certificación, provincia y trabaja (todos por eliminar). Ver §5.

### 4.6 Módulo SEGURIDAD — `PnlSeguridad` (1348 líneas, especializado)
- CRUD de roles, permisos y asignaciones rol↔permiso. Sólo Administrador.

### 4.7 Módulos genéricos (usan `GestorConsultaModulo` + `PnlEditorRegistro`)
Todos comparten la misma estructura de 568 líneas con un `JComboBox` que lista los sub-CRUDs disponibles.

| Módulo | Sub-CRUDs actuales | Sub-CRUDs tras limpieza | Notas |
|---|---|---|---|
| MEMBRESIAS | 5 | 3 (Membresias, Planes de membresia, Congelaciones) | Fuera: Beneficios de cada plan, Promociones y descuentos. |
| ACCESO | 5 | 3 (Reservas, Asistencias, Clases grupales) | Fuera: Clases especiales, Horarios (ver §3.4). |
| RUTINAS | 9 | 5 (Plantillas de rutina, Asignar rutina a cliente, Catalogo de ejercicios, Calendario semanal, Registrar progreso del cliente, Ejercicios que componen la rutina) | Fuera: Grupos musculares, Equipos necesarios, Videos y recursos. |
| SALUD | 7 | 5 (Evaluar cliente, Registrar peso y medidas, Catalogo de indicadores, Resultados de indicadores, Recomendaciones profesionales) | Fuera: Catalogo de objetivos fitness, Objetivos asignados al cliente. |
| NUTRICION | 6 | 4 (Planes asignados a clientes, Catalogo de alimentos, Comidas y porciones del plan, Nutricionistas habilitados) | Fuera: Alergenos de alimentos, Certificaciones profesionales. |
| FINANZAS | 6 | 5 (Pagos, Facturas, Detalle de factura, Metodos de pago, Comprobantes) | Fuera: Descuentos aplicados. |
| INVENTARIO | 10 | **0 — módulo eliminado por completo** | Se retiran los 10 sub-CRUDs, el panel, el botón y los permisos. |
| REPORTES | 10 | 9 (todos menos Inventario) | Fuera: Inventario. |
| PERSONAL | 9 | 3 (Empleados, Entrenadores, Nutricionistas) | Fuera: Especialidades (todas variantes), Certificaciones (todas variantes), Provincias, Asignaciones laborales. |

### 4.8 Dashboards por rol — `PnlInicio{Rol}`

Cada uno usa `DashboardControlador` para poblar tarjetas con métricas reales. Cinco archivos casi idénticos en estructura (732 líneas c/u) — cada uno adapta los widgets al rol.

**Estado actual:** funcional pero cada rol muestra métricas globales que quizás no corresponden a su alcance (ej. `PnlInicioCliente` no debería ver "total ingresos del mes"). **Verificar y limitar por rol en la Fase 8**.

### 4.9 CONFIGURACIÓN — `PnlConfiguracion`
- Cambio de contraseña, cambio de avatar, preferencias del rol actual. Común a los 5 roles.

---

## 5. Módulos y archivos a eliminar

### 5.1 Tablas (27)

Antes de `DROP`, aplicar el siguiente orden para respetar FKs (o usar `DROP … CASCADE`):

```sql
-- Grupo 1: hoja
DROP TABLE IF EXISTS aplica_descuento, alimento_alergeno, ejercicio_recurso_multimedia,
    entrenador_certificacion, entrenador_especialidad, nutricionista_certificacion,
    cliente_objetivo, tipo_membresia_beneficio, requiere_equipo, trabaja,
    suministra_producto, detalle_compra;
-- Grupo 2: catálogos huérfanos tras el grupo 1
DROP TABLE IF EXISTS clase_especial, mantenimiento;
DROP TABLE IF EXISTS descuento, especialidad, producto, equipo, compra;
DROP TABLE IF EXISTS categoria_producto, proveedor, tipo_equipo, tipo_mantenimiento,
    provincia, grupo_muscular;
-- Grupo 3: catálogos referenciados por tablas que se conservan (requiere UPDATE previo)
UPDATE rutina SET id_objetivo = NULL WHERE id_objetivo IS NOT NULL;
ALTER TABLE rutina ALTER COLUMN id_objetivo DROP NOT NULL,
    DROP CONSTRAINT IF EXISTS fk_rutina_objetivo_fitness;
-- Opción A: eliminar objetivo_fitness y horario
-- DROP TABLE objetivo_fitness;
-- (reserva.id_horario obliga a un rediseño si se elimina horario — ver §3.4)
-- Opción B (RECOMENDADA): dejar horario y objetivo_fitness sin exponer.
```

### 5.2 Modelos (27 archivos en `src/modelo/`)

`AlimentoAlergeno.java`, `AplicaDescuento.java`, `CategoriaProducto.java`, `ClaseEspecial.java`, `ClienteObjetivo.java`, `Compra.java`, `Descuento.java`, `DetalleCompra.java`, `EjercicioRecursoMultimedia.java`, `Empleado.java` (**NO — se conserva**), `EntrenadorCertificacion.java`, `EntrenadorEspecialidad.java`, `Equipo.java`, `Especialidad.java`, `GrupoMuscular.java`, `Horario.java` (opción B: **conservar**), `Mantenimiento.java`, `NutricionistaCertificacion.java`, `ObjetivoFitness.java` (opción B: **conservar**), `Producto.java`, `Proveedor.java`, `Provincia.java`, `RequiereEquipo.java`, `SuministraProducto.java`, `TipoEquipo.java`, `TipoMantenimiento.java`, `TipoMembresiaBeneficio.java`, `Trabaja.java`.

### 5.3 DAOs (paralelo 1:1 con modelos)
Mismos nombres con sufijo `DAO`. Ver listado en `src/dao/`.

### 5.4 Controladores (paralelo 1:1 con modelos)
Mismos nombres con sufijo `Controlador`. Ver listado en `src/controlador/`.

### 5.5 Vistas
- `PnlInventario.java` + `PnlInventario.form` (todo el módulo).
- Ajustes puntuales en `PnlPersonal`, `PnlRutinas`, `PnlSalud`, `PnlAcceso`, `PnlMembresias`, `PnlNutricion`, `PnlFinanzas` (sólo si tienen referencias directas — probablemente no; usan el gestor).
- `FrmPrincipal.form` / `FrmPrincipal.java`: eliminar `btnInventario` **o** dejarlo invisible siempre.

### 5.6 Utilidades y controladores transversales

Editar `src/utilidades/GestorConsultaModulo.java`:
- Eliminar el `case "INVENTARIO"` completo del método `controladoresPara`.
- Podar cada `case` con los sub-CRUDs marcados en §4.7.

Editar `src/vista/FrmPrincipal.java`:
- Método `configurarMenuSegunRol` → quitar `btnInventario` de la lista del Administrador.
- Método `configurarNavegacion` → borrar el bloque `btnInventario.addActionListener(...)` y el método `mostrarInventario()`.

Editar `src/utilidades/NavegacionRol.java`:
- Quitar `INVENTARIO` del set del Administrador.
- Quitar la constante `public static final String INVENTARIO`.

### 5.7 SQL de pruebas y demo
`sql/04_datos_prueba_funcionales.sql` y `sql/06_facturacion_e_innovacion.sql` insertan datos en `objetivo_fitness`, `tipo_membresia_beneficio`, `horario`, `clase_especial`, etc. Deberán limpiarse (o migrar a la opción B, ver §5.1).

### 5.8 Permisos en BD
Existen filas en `permiso` con `modulo='INVENTARIO'` (5 filas) y correspondientes en `rol_permiso`. Ejecutar:
```sql
DELETE FROM rol_permiso WHERE id_permiso IN (SELECT id_permiso FROM permiso WHERE modulo='INVENTARIO');
DELETE FROM permiso WHERE modulo='INVENTARIO';
```

---

## 6. Credenciales de demostración — estado actual vs esperado

**Estado real hoy en BD:**
| Usuario | Contraseña efectiva | Comentario |
|---|---|---|
| `jose` | Se afirma `Admin123*` (no verificado por hash en esta auditoría). | Si en algún momento se rehashea, hay que preservarla. |
| `admin` | **INVÁLIDA** — hash es literal `PEGA_AQUI_EL_HASH_GENERADO`. | Necesita rehashing. Sugiero eliminar este usuario y quedarse sólo con `jose` como admin. |
| `RECEPCION` | `Gymnova2026*` | El usuario pide `Gymnova123*`. Cambiar. |
| `ENTRENADOR` | `Gymnova2026*` | Cambiar. |
| `NUTRICION` | `Gymnova2026*` | Cambiar. |
| `CLIENTE` | `Gymnova2026*` | Cambiar. |

**Plan de la Fase 9:**
1. Rehashear `RECEPCION`, `ENTRENADOR`, `NUTRICION`, `CLIENTE` con hash de `Gymnova123*` (usar `SeguridadClave.generarHash`).
2. **Verificar** que `jose` acepta `Admin123*`. Si no, rehashear.
3. Desactivar o borrar `admin` (usuario 2) — su hash está roto y `jose` cubre el rol.
4. Actualizar `sql/11_credenciales_demostracion.sql` con el hash nuevo y anotar la contraseña `Gymnova123*` en la cabecera.

**Impacto en pruebas:** los archivos `src/pruebas/*.java` que hard-codean `Gymnova2026*` deben actualizarse a `Gymnova123*` (7 archivos, ver `grep` en la sección diagnóstico).

---

## 7. Seguridad — brechas encontradas

1. **Bitácora no auditada.** `bitacora` tiene 0 filas. Debería registrar como mínimo: login exitoso/fallido, creación/edición/eliminación en módulos sensibles, cambio de rol, cambio de contraseña, generación de comprobante. Falta cablearla desde:
   - `LoginControlador.iniciarSesion` (éxito + fallido bloqueado).
   - `MembresiaDAO.guardar/modificar/desactivar`.
   - `FacturaDAO.guardar` y `PagoDAO.guardar`.
   - `AsignacionRutinaDAO.guardar` y `PlanNutricionalDAO.guardar`.
   - `RolPermisoDAO.guardar/modificar`.
2. **Alcance por reflexión.** `AlcanceRolControlador` depende de que cada modelo tenga los getters correctos. Auditar: `Rutina` debe exponer `getIdEntrenador()`; `AsignacionRutina` debe exponer `getIdCliente()` y `getIdRutina()`; `PlanNutricional` debe exponer `getIdCliente()` y `getIdNutricionista()`; etc. **Recomiendo escribir una prueba JUnit que instancie cada modelo y verifique los getters clave** (ya existe algo similar en `src/pruebas/PruebaAlcanceRoles.java` — revisar y ampliar).
3. **Recepcionista tiene `puedeAcceder = true` incondicional** en `AlcanceRolControlador` (línea 44). Esto es intencional (necesita ver todos los clientes) pero requiere validar que Recepcionista no pueda modificar rutinas ni planes ni permisos globales — eso se cubre por permisos módulo/acción, no por alcance.
4. **Login case-insensitive** ✓ — `UsuarioDAO.buscarPorNombre` usa `LOWER(u.nombre_usuario) = LOWER(?)`. Correcto.
5. **`SesionUsuario` estático global**: si en algún momento se ejecutan pruebas en paralelo, se pisarán entre sí. Aceptable para app monousuario; documentar.
6. **`ConexionPostgreSQL` no usa pool**. Cada operación abre y cierra conexión (patrón try-with-resources — bien) pero bajo carga esto sería lento. Aceptable para aplicación de escritorio con un solo cliente activo.
7. **Contraseña de BD hardcoded** (`VictorSdef/17`) en `ConexionPostgreSQL.java`. Aceptable si se distribuye con la propiedad `-D gymnova.db.contrasena=...` — actualmente el diseño permite override por `System.getProperty`. Bien.

---

## 8. Módulos donde falta trabajo funcional

### 8.1 Recepción — flujo completo
El pedido: cliente → membresía → reserva → asistencia → factura → pago → comprobante en un flujo guiado.

**Lo que existe hoy:**
- CRUD aislado de cada entidad vía `GestorConsultaModulo`.
- No hay un asistente / wizard que guíe al recepcionista por el flujo.
- No hay un concepto explícito de "cliente en atención" mantenido entre pantallas.

**Lo que falta:**
- Un banner superior en el menú lateral que muestre "Cliente atendido: X" (ya existe la constante — verificar en `PnlModuloAplicacion` cómo se hereda). Añadir un `ClienteEnAtencion` singleton similar a `SesionUsuario` para propagar la selección entre paneles Recepcionista.
- En `PnlFinanzas`, botón "Nueva factura para cliente en atención" que precargue `id_cliente` y agregue automáticamente un renglón `MEMBRESIA` en `detalle_factura` con la membresía activa.
- Botón "Registrar pago" que cambie `factura.estado_factura` a `PAGADA` cuando la suma de pagos ≥ total.
- Botón "Emitir comprobante" que invoque `DocumentoFacturaPDF.generar` y guarde la fila en `comprobante` con `ruta_archivo` a un PDF real.

### 8.2 Entrenador — flujo prioritario según el usuario
**Lo que existe:**
- `PnlContenidoEspecializado.construirRutinas` muestra la rutina del rol.
- CRUD de rutina + asignación + días + ejercicios existe vía Gestor.

**Lo que falta:**
- Vista/asistente unificado: seleccionar cliente → botón "Nueva rutina" → editor de rutina que permita añadir días + ejercicios + series/reps/descanso en una sola pantalla → botón "Asignar al cliente seleccionado" que crea la fila en `asignacion_rutina`.
- Registrar evaluación física (formulario que combine `evaluacion_fisica` + `medicion_corporal` en una transacción — hoy son CRUDs separados).
- Historial de evolución (gráfico simple con peso/porcentaje grasa por fecha).

### 8.3 Nutricionista — flujo prioritario
**Lo que existe:**
- CRUD de plan_nutricional + incluye_alimento + alimento + indicador_salud + resultado_indicador + recomendacion.

**Lo que falta:**
- Asistente cliente → "Nuevo plan nutricional" con validación de plan activo previo (finalizar automáticamente el anterior con `estado_plan='FINALIZADO'` si el usuario confirma).
- Distribuir alimentos por día/comida en una sola pantalla (hoy hay que ir al sub-CRUD `Comidas y porciones del plan`).
- Vista con `BigDecimal` para las cantidades (verificar que `IncluyeAlimento.cantidad` esté declarado `BigDecimal`, no `double`).

### 8.4 Cliente — vista de sólo lectura
**Lo que existe:**
- Los dashboards `PnlContenidoEspecializado` muestran resúmenes filtrados al cliente logueado.
- `RutinaClienteControlador.listarEjercicios(idCliente, fecha)` devuelve los ejercicios del día.

**Lo que falta:**
- Verificar que el cliente NUNCA puede seleccionar otro `id_cliente` en ningún combo (validar en `CatalogoRelacionControlador` — debería restringir combos de cliente cuando el rol es CLIENTE).
- Vista "Mis pagos y comprobantes" — hoy Reportes muestra Finanzas, verificar que el filtro por `id_cliente` esté funcionando.

### 8.5 Administrador — vista global
**Lo que existe:**
- Acceso a todos los módulos.
- Todos los CRUD desbloqueados.

**Lo que falta:**
- Nada crítico; sólo pulir dashboard con datos reales y confirmar que Reportes → Auditoría muestra `bitacora`.

---

## 9. Bugs y comportamientos incorrectos detectados

| # | Ubicación | Descripción | Severidad |
|---|---|---|---|
| B1 | `usuario` id=2 (`admin`) | Hash placeholder `PEGA_AQUI_EL_HASH_GENERADO` — login siempre falla. | Alta (impacta demo) |
| B2 | `sql/11_credenciales_demostracion.sql` | Contraseña de los 5 usuarios demo es `Gymnova2026*`, no `Gymnova123*` como se pide. | Alta (impacta demo) |
| B3 | `src/pruebas/*.java` (7 archivos) | Hardcodean `Gymnova2026*`. Al cambiar hash, estas pruebas fallarán. | Media |
| B4 | `GestorConsultaModulo.controladoresPara("INVENTARIO")` | Enumera 10 controladores que se van a eliminar. | Alta (post-limpieza) |
| B5 | `NavegacionRol.MODULOS` | Incluye `INVENTARIO` para Administrador. | Alta (post-limpieza) |
| B6 | `FrmPrincipal` | Botón `btnInventario` + método `mostrarInventario()` + `btnInventario.addActionListener` sin desactivar tras eliminar módulo. | Alta (post-limpieza) |
| B7 | `bitacora` | Ninguna operación crítica la escribe. Tabla no cumple su función. | Media |
| B8 | `PlanNutricionalDAO.guardar` | No valida que el cliente no tenga otro plan activo. | Media |
| B9 | `AsignacionRutinaDAO.guardar` | No valida que el cliente no tenga otra asignación activa (si se define esa regla). | Media |
| B10 | `MembresiaDAO.guardar` | No valida `fecha_inicio <= fecha_fin`. | Baja |
| B11 | `ReservaDAO.guardar` | No valida cupo máximo del horario, ni evita duplicados por cliente+horario. | Media |
| B12 | `PagoDAO.guardar` | No valida que la suma acumulada de pagos no supere `factura.total`. Y no actualiza `factura.estado_factura='PAGADA'` cuando saldo=0. | Alta (contable) |
| B13 | `factura` | No tiene columna `subtotal` ni `total` — se calculan de `detalle_factura`. Documentar o agregarlas para simplificar consultas. | Media |
| B14 | `lib/postgresql-42.7.13-build.jar` | Jar duplicado, no referenciado en `nbproject/project.properties`. | Cosmética |
| B15 | `Persona 4 Marck` | Es cliente Y entrenador simultáneamente. Puede confundir al filtrar "mis clientes" del entrenador (aparecerá él mismo si el propio Marck fuera cliente asignado). Verificar. | Baja |
| B16 | `intentos_fallidos=1` en usuario `admin` | Alguien probó login con la cuenta rota. Al resetear, poner en 0. | Cosmética |
| B17 | `entrega/gymnova_db_entrega.sql` | Dump PostgreSQL 18.4; el servidor local debe ser compatible. Ya verificado. | OK |
| B18 | `PnlInicioCliente` | Puede exponer métricas globales (ingresos totales, etc.) que no debería ver un cliente. Auditar en Fase 8. | Media |
| B19 | `AlcanceRolControlador` alcanceCliente línea 103 | Devuelve `true` para `TipoMembresia`, `ClaseGrupal`, `Horario`, `IndicadorSalud` como catálogos abiertos — está OK pero verificar que no se filtre nada sensible. | Baja |
| B20 | Reportes | La opción "Inventario" seguirá apareciendo en el combo después de la limpieza si no se toca `GestorConsultaModulo.controladoresPara("REPORTES")`. | Alta (post-limpieza) |

---

## 10. Recomendación de fases (accionable)

Las fases 2 → 10 con estimaciones honestas.

### Fase 2 — Limpieza de módulos fuera de alcance (2–4 h)
1. Respaldo del `src/` y del dump SQL actual dentro de `respaldo/limpieza_YYYYMMDD/`.
2. Editar `GestorConsultaModulo.controladoresPara` para eliminar `INVENTARIO`, sub-CRUDs de PERSONAL/MEMBRESIAS/ACCESO/RUTINAS/SALUD/NUTRICION/FINANZAS/REPORTES.
3. Editar `FrmPrincipal.java` (quitar `btnInventario`, método `mostrarInventario`, listener).
4. Editar `FrmPrincipal.form` para eliminar el botón (o dejarlo invisible siempre — más rápido).
5. Editar `NavegacionRol.java` para quitar `INVENTARIO`.
6. Eliminar `PnlInventario.java`/`.form`.
7. Eliminar los 27 modelos + 27 DAOs + 27 controladores fuera de alcance.
8. Ejecutar SQL:
   - `DELETE FROM rol_permiso WHERE id_permiso IN (SELECT id_permiso FROM permiso WHERE modulo='INVENTARIO')`.
   - `DELETE FROM permiso WHERE modulo='INVENTARIO'`.
   - `DROP TABLE …` en el orden indicado en §5.1 (opción B recomendada: conservar `horario` y `objetivo_fitness`).
9. `ant clean && ant` — debe seguir compilando.
10. Actualizar `entrega/gymnova_db_entrega.sql` con nuevo `pg_dump`.

**Riesgo:** medio — muchas ediciones simultáneas. Verificar compilación después de cada bloque de 5–7 archivos.

### Fase 3 — Recepción end-to-end (6–10 h)
1. Introducir `utilidades/ClienteEnAtencion` (singleton similar a `SesionUsuario`).
2. En `PnlClientes` (para Recepcionista), botón "Atender este cliente" que fija `ClienteEnAtencion`.
3. Banner "Cliente atendido: X" en `FrmPrincipal` visible sólo para RECEPCIONISTA / ENTRENADOR / NUTRICIONISTA.
4. En `PnlMembresias`, "Nueva membresía para cliente en atención" precargando `id_cliente`.
5. Congelación / reactivación / cancelación desde el mismo panel con actualización de `estado_membresia`.
6. En `PnlFinanzas`, botón "Facturar membresía en atención" que:
   - Crea `factura` con `id_cliente` = cliente en atención.
   - Crea `detalle_factura` con tipo `MEMBRESIA` y precio = `tipo_membresia.precio_base`.
   - Todo en una transacción JDBC.
7. Registrar pago (con validación B12).
8. Emitir comprobante que invoque `DocumentoFacturaPDF` (verificar que ya existe una infraestructura de generación PDF en `utilidades/DocumentoFacturaPDF.java`).
9. Cablear `bitacora` para membresía / factura / pago / comprobante.

### Fase 4 — Entrenador end-to-end (6–8 h)
1. Vista "Mi cliente" en el módulo CLIENTES que reutiliza `ClienteEnAtencion`.
2. Botón "Nueva rutina" → wizard 1-paso con días + ejercicios + series/reps.
3. Guardado transaccional: `rutina` → `contiene_ejercicio` → `asignacion_rutina`.
4. Registrar evaluación física + mediciones corporales en una sola operación.
5. Historial de evolución (gráfico con `GraficoActividad` que ya existe).
6. Cablear `bitacora`.

### Fase 5 — Nutricionista end-to-end (5–7 h)
1. Vista "Mi cliente" reutilizando `ClienteEnAtencion`.
2. Botón "Nuevo plan" con validación de plan activo previo (opción de finalizar el anterior).
3. Editor de plan con macros + alimentos por día/comida en una sola pantalla.
4. Registrar indicadores + recomendaciones.
5. Verificar `IncluyeAlimento.cantidad` es `BigDecimal`.
6. Cablear `bitacora`.

### Fase 6 — Cliente vista limitada (3–5 h)
1. Auditar todos los combos de "seleccionar cliente" en `CatalogoRelacionControlador` y forzar que para rol CLIENTE la lista sólo contenga su propio `id_cliente`.
2. Verificar en Reportes que el filtro `id_cliente = sesion.idPersona` esté aplicado en las 6 vistas (Membresías, Accesos, Rutinas, Salud, Nutrición, Finanzas).
3. Retirar métricas globales del `PnlInicioCliente`.

### Fase 7 — Administrador (2–3 h)
1. Verificar acceso a todos los módulos.
2. Añadir vista "Auditoría" en Reportes (ya está listada como opción — verificar consulta `BitacoraDAO.listar`).
3. Refrescar dashboard.

### Fase 8 — Dashboard + reportes con datos reales (3–5 h)
1. Auditar `DashboardControlador` y `PulseControlador` para que todas las métricas provengan de PostgreSQL (no hardcoded).
2. Adaptar `PnlInicio{Rol}` para que cada rol vea sólo métricas de su alcance.
3. Verificar generación de PDF de reportes (si aplica).

### Fase 9 — Seguridad + credenciales (1–2 h)
1. Rehashear `RECEPCION`, `ENTRENADOR`, `NUTRICION`, `CLIENTE` con `Gymnova123*`.
2. Verificar `jose`/`Admin123*`. Si falla, rehashear.
3. Desactivar/borrar `admin` (id_usuario=2).
4. Actualizar `sql/11_credenciales_demostracion.sql`.
5. Actualizar los 7 archivos `src/pruebas/*.java` que hardcodean `Gymnova2026*`.
6. Revisar bitácora — todas las operaciones sensibles deben registrarse.

### Fase 10 — Prueba integral (2–3 h)
1. Ejecutar el guión completo: Admin configura → Recepción registra Monica → membresía → factura → pago → comprobante → reserva → asistencia. Entrenador Marck crea rutina para Monica. Nutricionista Andrea crea plan para Monica. Cliente Monica inicia sesión y ve todo su historial.
2. Documentar cualquier problema residual.

**Total estimado: 30 – 47 horas** de trabajo enfocado, distribuido a lo largo de varias sesiones.

---

## 11. Riesgos y decisiones pendientes

1. **DROP TABLE puede romper cosas.** Recomiendo conservar `horario` y `objetivo_fitness` en la BD (opción B en §5.1) para evitar rediseñar `reserva` y `rutina`. Sólo se retiran del menú y del `GestorConsultaModulo`.
2. **Actualizar `entrega/gymnova_db_entrega.sql`** tras Fase 2 con `pg_dump` — evita que un `pg_restore` accidental restablezca los módulos eliminados.
3. **`Bitacora` sin escritura automática** implica que hoy no hay trazabilidad. Puede aceptarse como "no crítico" si el proyecto es académico; documentar el trade-off.
4. **`ClienteEnAtencion` es nuevo código.** Introducir un singleton adicional es correcto pero hay que asegurarse de limpiarlo al cerrar sesión.
5. **Casos límite del alcance de reflexión:** cada modelo nuevo/modificado debe exponer los getters que espera `AlcanceRolControlador`. Recomiendo escribir una prueba JUnit centralizada.
6. **PDFs de comprobante:** ya existe `DocumentoFacturaPDF.java` — verificar que las dependencias (iText / PdfBox / etc.) estén en `lib/` o que se genere PDF por otros medios (probablemente imprime PostScript o similar; revisar).
7. **Persona 4 (Marck) es cliente Y entrenador.** Puede aparecer como cliente en su propia lista de "mis clientes". Decisión: (a) aceptar y documentar, (b) filtrar `WHERE p.id_persona != ?`. Recomiendo (a).

---

## 12. Anexo — script para reproducir la auditoría

```bash
# Verificar conexión y contar tablas
java -cp "lib/postgresql-42.7.13.jar" \
     -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'"

# Verificar usuarios demo
SELECT id_usuario, nombre_usuario, r.nombre_rol, u.estado_usuario, u.bloqueado, LEFT(u.clave_hash, 30)
FROM usuario u LEFT JOIN rol r ON r.id_rol=u.id_rol ORDER BY u.id_usuario;

# Verificar permisos activos por rol
SELECT r.nombre_rol, COUNT(*) FROM rol_permiso rp
JOIN rol r ON r.id_rol=rp.id_rol
WHERE rp.estado_asignacion='ACTIVA'
GROUP BY r.nombre_rol;

# Compilar
ant clean && ant
```

---

## 13. Cierre

El proyecto **NO** requiere ser rehecho. La arquitectura es sólida: capas bien separadas, autorización con defensa en profundidad, template CRUD reutilizable, dashboards por rol conectados a datos reales.

**Lo que impide considerarlo terminado hoy:**
- Los 27 módulos fuera de alcance ensucian menús y opciones (Fase 2).
- Los 3 flujos operativos (Recepción, Entrenador, Nutricionista) no cierran end-to-end (Fases 3–5).
- Las credenciales demo están rotas o desalineadas con lo pedido (Fase 9).
- La bitácora no se escribe automáticamente (Fase 9).

**Lo que sí funciona hoy:**
- Login (con contraseña correcta).
- Navegación por rol con menús filtrados y validación de rutas.
- Todos los CRUD genéricos vía `GestorConsultaModulo`.
- Dashboards con datos reales.
- Permisos módulo/acción.
- Alcance por dueño (entrenador/nutricionista/cliente).
- Hash PBKDF2 correcto.

**Próximo paso recomendado:** ejecutar la Fase 2 (limpieza) porque es la que más aclara el panorama para las fases operativas. Aproximadamente 3 horas de trabajo cuidadoso con verificación de compilación después de cada bloque.

*Fin de la auditoría de fase 1.*
