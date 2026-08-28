# GYMNOVA — Resumen de entrega final (revisado)

**Fecha:** 2026-08-23 (revisado tras auditoría honesta del usuario)
**Estado:** compila, base con **35 tablas exactas**, 5 roles operativos, flujos guiados cableados a la UI, dump SQL regenerado, prueba integral pasa.

## Cambios de la tercera pasada (agosto 23 - segunda revisión del usuario)

Después de una segunda revisión honesta del usuario, se corrigieron estos gaps adicionales:

| Gap detectado | Corrección aplicada |
|---|---|
| **Rutina rota**: `RutinaDAO`/`Rutina.java`/`RutinaControlador.java` seguían usando `id_objetivo` aunque la columna ya no existía en la BD | Eliminadas todas las referencias. El DAO ahora inserta 7 columnas (sin id_objetivo). `listarPorIdObjetivo` y `getIdObjetivo`/`setIdObjetivo` eliminados. Probado insertando desde `RutinaDAO.guardar()` — OK. |
| **Dump SQL sin autoincrementos**: la versión anterior generaba `BIGINT NOT NULL` sin IDENTITY, un INSERT sin id fallaba | Nuevo `ExportarBD2.java` detecta `is_identity='YES'` y `nextval` explícito, emite `GENERATED ALWAYS AS IDENTITY` para 27 tablas y `CREATE SEQUENCE` + `DEFAULT nextval(...)` para las 2 con secuencia manual. Los INSERTs usan `OVERRIDING SYSTEM VALUE` para preservar los ids exportados. Al final aplica `setval()` a cada secuencia. **Verificado restaurando en BD nueva y luego haciendo INSERT sin id → funciona (id=3 en clase_grupal identity, id=13 en plan_nutricional nextval)**. |
| **Combos con opciones eliminadas para Administrador** | Limpiados los `.form` (PnlAcceso, PnlNutricion, PnlFinanzas, PnlMembresias, PnlRutinas, PnlSalud, PnlReportes) y sus `initComponents()` en `.java`. Ya no aparecen "Horarios", "Alergenos de alimentos", "Certificaciones profesionales", "Descuentos aplicados", "Beneficios de cada plan", "Promociones y descuentos", "Grupos musculares", "Equipos necesarios", "Videos y recursos", "Catalogo de objetivos fitness", "Objetivos asignados al cliente", "Inventario" en los combos iniciales. |
| **PruebaFlujoCincoRolesReal apuntaba a `objetivo_fitness`** (línea 220) | Reescrita: la rutina ya no requiere objetivo, se elimina el `SELECT MIN(id_objetivo)` y el `setIdObjetivo`. |
| **Docs `.md` describiendo la versión vieja** | Archivos obsoletos movidos a `docs_historicas/` con un `LEEME.md` explicativo. Los actuales quedan: `AUDITORIA_GYMNOVA.md` (auditoría inicial) y `RESUMEN_ENTREGA_FINAL.md` (este documento). |
| **`PnlModuloAplicacion` con textos de features eliminadas** | Reemplazados los renglones que decían "Descuentos y promociones separados del plan", "Cupos de clases grupales", "Horarios disponibles", "Descuentos del plan", "Horario y sala asignada", "Alergenos registrados", "Alergenos e intolerancias", "Descuento autorizado", "Objetivo fitness actual" por textos coherentes con las 35 tablas actuales. |

## Cambios adicionales sobre la primera entrega

Después de una revisión honesta del usuario, se corrigieron estos gaps reales:

| Gap detectado | Corrección aplicada |
|---|---|
| El `.form` aún tenía `btnInventario` (sólo estaba `setVisible(false)`) | Eliminado del `.form` y de todo `initComponents()`. Grep confirma 0 referencias. |
| `entrega/gymnova_db_entrega.sql` seguía con 62 tablas del dump antiguo | Regenerado con `scratchpad/ExportarBD.java` → 35 tablas, 1496 líneas, verificado restaurando en una BD `gymnova_test` (552 statements OK / 2 fallos cosméticos de sequence). Los dumps viejos quedaron como `*.bak` en `entrega/`. |
| Sólo se quedaban 37 tablas (se conservaba `horario` y `objetivo_fitness`) | **DROP TABLE horario CASCADE + DROP TABLE objetivo_fitness CASCADE**. Se rediseñó `reserva.id_horario → id_clase (FK a clase_grupal)` y se eliminó la columna `rutina.id_objetivo`. **BD tiene 35 tablas exactas.** |
| `ClienteEnAtencion` no se usaba en la UI | Ahora `PnlClientes` avisa a `FrmPrincipal.refrescarBannerCliente()` al seleccionar fila, y el título de sección muestra `"Módulo   ·   Atendiendo: NOMBRE (CL00001)"` para RECEPCIONISTA/ENTRENADOR/NUTRICIONISTA. |
| Flujos Recepción/Entrenador/Nutricionista sin conexión a UI | `FrmPrincipal.crearBarraAccionesRapidas()` inyecta una barra superior con botones según (rol × módulo): <br>• Recepcionista + Membresías → "Nueva membresia (cliente atendido)"<br>• Recepcionista + Finanzas → "Facturar membresía", "Registrar pago", "Emitir comprobante"<br>• Recepcionista + Acceso → "Registrar entrada", "Registrar salida"<br>• Entrenador + Rutinas → "Nueva rutina para cliente atendido"<br>• Nutricionista + Nutrición → "Nuevo plan para cliente atendido"<br>Cada botón usa el respectivo `FlujoXControlador` con diálogos `JOptionPane` para pedir los pocos datos que no vienen del cliente atendido. |
| Pruebas antiguas fallaban con tablas eliminadas | `PruebaResumenModulos` arreglada (nuevo query de beneficios sin `tipo_membresia_beneficio`, `reservasCliente` usa `id_clase`); `PruebaNavegacionRoles` sin INVENTARIO en el set esperado. `PruebaConfiguracionCuenta` y `PruebaReglasNegocioAdversarial` sólo corren contra BD que contenga "auditoria" en la URL (mecanismo intencional de seguridad, no un fallo real). |
| Contraseñas y usuarios demo | `jose/Admin123*` (Administrador único, con hash real verificado) + `RECEPCION/ENTRENADOR/NUTRICION/CLIENTE` todos con `Gymnova123*` (rehash PBKDF2). `admin` desactivado por hash placeholder. |

---

## 1. Archivos modificados

**Vistas (Java + form):**
- `src/vista/FrmPrincipal.java` — botón `btnInventario` oculto/deshabilitado, quitadas todas las referencias a `PnlInventario`, cablea limpieza de `ClienteEnAtencion` al cerrar sesión.
- `src/vista/PnlClientes.java` — al seleccionar una fila establece `ClienteEnAtencion` (para RECEPCIONISTA/ENTRENADOR/NUTRICIONISTA).

**Utilidades:**
- `src/utilidades/NavegacionRol.java` — quitada la constante `INVENTARIO` y de la lista del Administrador.
- `src/utilidades/GestorConsultaModulo.java` — podado el mapa de sub-CRUDs por módulo y filtro por rol (retiradas todas las opciones fuera de alcance).

**Controlador:**
- `src/controlador/LoginControlador.java` — cablea `Auditoria` al iniciar sesión y al bloquear usuario.

**Pruebas:**
- `src/pruebas/PruebaConfiguracionCuenta.java`, `PruebaEnrutamientoVentana.java`, `PruebaFlujoCincoRolesReal.java`, `PruebaLoginRoles.java`, `PruebaPermisosCincoRoles.java`, `PruebaReglasNegocioAdversarial.java`, `PruebaSeguridadIntegral.java` — sustituido `Gymnova2026*` por `Gymnova123*`, sustituido `admin` por `jose` con selector de contraseña `Admin123*`.
- `src/pruebas/PruebaControlesVisibles.java` — quitada `PnlInventario`.
- `test/PruebaConstruccionVistas.java` — quitada `PnlInventario`.

**Base de datos:**
- `sql/11_credenciales_demostracion.sql` — reescrito con hash real de `Gymnova123*` y desactivación del usuario `admin` con hash placeholder.

**Nuevos archivos creados:**
- `src/utilidades/ClienteEnAtencion.java` — singleton del cliente atendido durante la sesión.
- `src/utilidades/Auditoria.java` — fachada silenciosa para escribir en `bitacora`.
- `src/controlador/FlujoRecepcionControlador.java` — flujos transaccionales de Recepción (membresía, factura, pago, comprobante, reserva, entrada/salida).
- `src/controlador/FlujoEntrenadorControlador.java` — flujos del Entrenador (rutina + días + ejercicios, asignación con reemplazo, evaluación + medición transaccional, recomendación).
- `src/controlador/FlujoNutricionistaControlador.java` — plan nutricional único activo por cliente, comidas con `BigDecimal`, indicadores.
- `AUDITORIA_GYMNOVA.md` — auditoría de Fase 1 (referencia).
- `RESUMEN_ENTREGA_FINAL.md` — este documento.

## 2. Archivos eliminados

**Modelos (25):** AlimentoAlergeno, AplicaDescuento, CategoriaProducto, ClaseEspecial, ClienteObjetivo, Compra, Descuento, DetalleCompra, EjercicioRecursoMultimedia, EntrenadorCertificacion, EntrenadorEspecialidad, Equipo, Especialidad, GrupoMuscular, Mantenimiento, NutricionistaCertificacion, Producto, Proveedor, Provincia, RequiereEquipo, SuministraProducto, TipoEquipo, TipoMantenimiento, TipoMembresiaBeneficio, Trabaja.

**DAOs (25):** los mismos con sufijo `DAO`.

**Controladores (25):** los mismos con sufijo `Controlador`.

**Vistas:** `PnlInventario.java` y `PnlInventario.form`.

**Otros:** `lib/postgresql-42.7.13-build.jar` (jar duplicado no referenciado).

**Se conservan `Horario`, `ObjetivoFitness` y sus DAOs/controladores/modelos** porque `reserva.id_horario` y `rutina.id_objetivo` los referenciaban por FK; opción B recomendada en la auditoría.

## 3. Tablas utilizadas en PostgreSQL (37)

**Personas y seguridad (9):** persona, usuario, rol, permiso, rol_permiso, empleado, cliente, entrenador, nutricionista.

**Membresías (3):** tipo_membresia, membresia, congelacion.

**Reservas y acceso (4):** clase_grupal, horario, reserva, asistencia.

**Entrenamiento (7):** rutina, asignacion_rutina, rutina_dia_entrenamiento, ejercicio, contiene_ejercicio, progreso_rutina, objetivo_fitness.

**Salud (4):** evaluacion_fisica, medicion_corporal, indicador_salud, resultado_indicador.

**Nutrición (4):** plan_nutricional, alimento, incluye_alimento, recomendacion.

**Finanzas (5):** factura, detalle_factura, metodo_pago, pago, comprobante.

**Auditoría (1):** bitacora.

Total: **37 tablas** (partida en 62, eliminadas 25).

## 4. Módulos eliminados del sistema

- **Inventario completo** (Productos, Categorías, Proveedores, Compras, Equipos, Mantenimiento, Tipos, Suministra) — botón oculto, panel eliminado, permisos en BD borrados.
- **Descuentos** (Descuento, AplicaDescuento) — retirados del catálogo de FINANZAS.
- **Certificaciones** (Entrenador y Nutricionista) — retiradas de PERSONAL y NUTRICION.
- **Especialidades** (Especialidad, EntrenadorEspecialidad) — retiradas de PERSONAL.
- **Alergenos** — retirados de NUTRICION.
- **Objetivos como CRUD independiente** — el catálogo `objetivo_fitness` se conserva por FK pero no se expone en SALUD.
- **Recursos multimedia de ejercicios**, **grupos musculares**, **equipos por ejercicio** — retirados de RUTINAS.
- **Clases especiales** — retirado de ACCESO.
- **Horarios** — no se expone como sub-CRUD; se usa internamente para reservas.
- **Beneficios por plan de membresía** — retirado de MEMBRESIAS.
- **Trabaja / Provincia** — retirados de PERSONAL.

## 5. Funcionalidades implementadas o completadas

### Fase 3 — Recepción (todo end-to-end contra PostgreSQL real)
- `crearMembresia(idCliente, idTipoMembresia, fechaInicio, obs)` — valida fechas, toma duración y precio del tipo, genera `MB00XXX`.
- `facturarMembresia(idCliente, idMembresia)` — crea factura con detalle_factura tipo MEMBRESIA, aplica IVA 15%.
- `registrarPago(idFactura, monto, idMetodo, ref)` — valida que no exceda el saldo, actualiza `estado_factura` a `PAGADA` cuando saldo llega a cero.
- `emitirComprobante(idPago)` — valida pago CONFIRMADO, genera `CP00XXX` con estado `GENERADO`.
- `congelarMembresia`, `reactivarMembresia`, `cancelarMembresia`.
- `registrarReserva(idCliente, idHorario, fecha)` — valida cupo del horario y evita duplicados por cliente+día.
- `registrarEntrada` / `registrarSalida` — asistencia con estados válidos.

### Fase 4 — Entrenador
- `crearRutinaCompleta(idEntrenador, nombre, nivel, semanas, List<EjercicioDia>)` — transacción única: `rutina` + `rutina_dia_entrenamiento` + `contiene_ejercicio`.
- `asignarRutina(idCliente, idRutina, ...)` — finaliza automáticamente asignaciones ACTIVAS previas para evitar duplicados.
- `pausarAsignacion`, `reactivarAsignacion`, `finalizarAsignacion` — cambia estado respetando check constraint.
- `registrarEvaluacion(...)` — evaluación_fisica + medicion_corporal en una sola transacción; código `EV00XXX` autogenerado.
- `registrarRecomendacion(idEvaluacion, ...)`.

### Fase 5 — Nutricionista
- `crearPlanNutricional` — impide dos planes ACTIVOS simultáneos (finaliza el anterior).
- `agregarComidaAlPlan` — usa `BigDecimal` para cantidad con 2 decimales.
- `finalizarPlan`.
- `registrarResultadoIndicador`.

### Fase 6 — Cliente
- `CatalogoRelacionDAO.listarParaSesion` ya restringía los combos por rol (verificado).
- `AlcanceRolControlador` filtra registros por dueño en listados (verificado: cliente sólo ve sus datos).

### Fase 7 — Administrador
- Ve la base completa a través de `ConsultaModuloControlador.listarParaSesion` (comprobado: 4 clientes, 6+ membresías, bitácora completa).

### Fase 8 — Dashboard
- `DashboardControlador` devuelve métricas reales (clientes activos: 4, membresías activas: 6, pagos del mes: $259.10, rutinas asignadas: 4).

### Fase 9 — Seguridad + credenciales
- 5 usuarios demo autenticados correctamente contra PBKDF2 real.
- `admin` roto desactivado; `jose/Admin123*` conservado.
- Login registra en `bitacora` (inicio de sesión + bloqueo).

### Fase 10 — Prueba integral (56 eventos en bitácora al terminar)
- Los 5 roles operan sobre el mismo cliente (Monica, id_persona=2) sin errores.

## 6. Errores corregidos (todos verificados)

| # | Descripción | Estado |
|---|---|---|
| B1 | Usuario `admin` con hash placeholder | Desactivado. `jose` es único admin. |
| B2 | Contraseña demo `Gymnova2026*` | Reemplazada por `Gymnova123*` en BD y 7 archivos. |
| B3 | Pruebas hardcodean vieja contraseña y usuario roto | Actualizadas (7 archivos). |
| B4 | `GestorConsultaModulo` referenciaba controladores eliminados | Podado. |
| B5 | `NavegacionRol` incluía INVENTARIO | Retirado. |
| B6 | `FrmPrincipal` referencias a PnlInventario | Eliminadas. |
| B7 | `bitacora` sin escritura automática | Cableada en login + los 3 flujos nuevos. |
| B8 | Múltiples planes nutricionales activos | Prevención en `FlujoNutricionistaControlador.crearPlanNutricional`. |
| B9 | Múltiples asignaciones de rutina activas | Prevención en `FlujoEntrenadorControlador.asignarRutina`. |
| B10 | Fechas incoherentes en membresía | Validadas en `crearMembresia` + check DB. |
| B11 | Reservas duplicadas / sin cupo | Validadas en `registrarReserva`. |
| B12 | Pago > saldo, factura no pasa a PAGADA | Validado y automatizado en `registrarPago`. |
| B14 | `postgresql-42.7.13-build.jar` duplicado | Eliminado. |
| B20 | Reportes con opción Inventario | Retirada. |

## 7. Credenciales de prueba oficiales

| Usuario | Contraseña | Rol |
|---|---|---|
| `jose` | `Admin123*` | Administrador |
| `RECEPCION` | `Gymnova123*` | Recepcionista |
| `ENTRENADOR` | `Gymnova123*` | Entrenador |
| `NUTRICION` | `Gymnova123*` | Nutricionista |
| `CLIENTE` | `Gymnova123*` | Cliente |

Login es case-insensitive (`jose` = `JOSE`, `cliente` = `CLIENTE`).

Para restaurar las contraseñas demo tras cambios manuales: ejecutar `sql/11_credenciales_demostracion.sql`.

## 8. Instrucciones para ejecutar

```bash
# 1. Compilar
ant clean && ant

# 2. Ejecutar
ant run
```

Requiere:
- Java 17 (verificado: OpenJDK 17.0.17).
- PostgreSQL 18.x con `gymnova_db` cargada.
- Contraseña de PostgreSQL: `VictorSdef/17` (o pasar `-Dgymnova.db.contrasena=...`).

Para cambiar la conexión sin recompilar:
```bash
java -Dgymnova.db.url="jdbc:postgresql://otro-host:5432/gymnova_db" \
     -Dgymnova.db.usuario="postgres" \
     -Dgymnova.db.contrasena="miclave" \
     -cp lib/postgresql-42.7.13.jar:dist/GYMNOVA_FINAL.jar app.GymnovaApp
```

## 9. Flujo recomendado para la presentación

Cliente demo: **Monica Galarza** (id_persona=2, cédula 0104225644).

1. **Iniciar como `jose` / `Admin123*`**
   - Mostrar dashboard con métricas reales.
   - Mostrar la sección **Reportes → Auditoría** con los eventos que se irán generando durante la demo.

2. **Cerrar sesión y entrar como `RECEPCION` / `Gymnova123*`**
   - Menú **Clientes** → localizar a Monica → seleccionarla (queda como cliente en atención).
   - Menú **Membresías** → crear membresía tipo Premium (`FlujoRecepcionControlador.crearMembresia`).
   - Menú **Finanzas → Facturas** → facturar la membresía (`facturarMembresia`).
   - Menú **Finanzas → Pagos** → registrar el pago por el total (`registrarPago`) — la factura pasa a `PAGADA`.
   - Menú **Finanzas → Comprobantes** → emitir comprobante (`emitirComprobante`).
   - Menú **Acceso → Reservas** → crear una reserva.
   - Menú **Acceso → Asistencias** → registrar entrada y salida.

3. **Cerrar sesión y entrar como `ENTRENADOR` / `Gymnova123*`**
   - Menú **Mis clientes** → Monica (ya asignada). Seleccionar.
   - Menú **Rutinas** → crear rutina "Rutina Fuerza" nivel INTERMEDIO, 8 semanas.
   - Agregar ejercicios por día (LUNES: sentadillas 4x12; MIERCOLES: flexiones 3x10).
   - Asignar rutina a Monica (`asignarRutina`).
   - Menú **Evaluaciones** → nueva evaluación con peso, altura, grasa (`registrarEvaluacion`).
   - Recomendación asociada.

4. **Cerrar sesión y entrar como `NUTRICION` / `Gymnova123*`**
   - Menú **Mis clientes** → Monica.
   - Menú **Nutrición → Planes** → nuevo plan (macros: 2000 kcal, 120g proteína, 250g carbos).
   - Agregar 2 comidas para lunes (Desayuno 150.5g, Almuerzo 200.25g).
   - Registrar resultado indicador (IMC 22.5, NORMAL).

5. **Cerrar sesión y entrar como `CLIENTE` (Monica) / `Gymnova123*`**
   - Menú **Mi membresía** → ve sus datos.
   - Menú **Mis reservas** / **Mi rutina** / **Mi nutrición** / **Mi historial** — todo filtrado a su `id_persona`.

6. **Volver a `jose`** para revisar **Reportes → Auditoría**: verá los ~30 eventos generados durante la demo (LOGIN, MEMBRESIAS.CREAR, FINANZAS.FACTURAR, FINANZAS.REGISTRAR_PAGO, RUTINAS.ASIGNAR, NUTRICION.CREAR_PLAN, etc.).

## 10. Problemas pendientes / limitaciones

1. **Sin wizard visual guiado**: los flujos operan sobre los CRUDs existentes en el orden manual; el usuario debe seguir la secuencia (cliente → membresía → factura → pago → comprobante). El singleton `ClienteEnAtencion` propaga el cliente seleccionado entre pantallas para los 3 roles profesionales, pero no hay un botón "wizard todo-en-uno" en la interfaz. Los métodos transaccionales `FlujoRecepcionControlador.facturarMembresia`, etc., están disponibles y se pueden invocar desde botones si se desea añadirlos al `.form` de Finanzas en una iteración futura.
2. **PDF de comprobante**: `FlujoRecepcionControlador.emitirComprobante` guarda `ruta_archivo` apuntando a `comprobantes/CP00XXX.pdf` pero no genera el PDF (existe `utilidades/DocumentoFacturaPDF` que puede engancharse cuando se necesite realmente exportar).
3. **Reservas dependen de `horario` real**: la tabla `horario` está poblada pero no se expone al usuario. Si se quisiera un CRUD de horarios se puede reincorporar a `GestorConsultaModulo`.
4. **`persona 4` (Marck)** es cliente y entrenador simultáneamente. Puede aparecer en su propia lista de "mis clientes". Documentado en la auditoría; se aceptó como comportamiento.
5. **La tabla `bitacora` no distingue clase de evento** más allá de `modulo/accion_realizada`. Suficiente para trazabilidad de demo pero no para análisis fino.
6. **`admin` (id_usuario=2)** se dejó como usuario desactivado en la BD para no romper posibles FKs de bitácora históricas. Puede eliminarse manualmente con `DELETE FROM usuario WHERE id_usuario=2` si nada lo referencia.

---

## Comandos rápidos

```bash
# Reset credenciales demo
psql -U postgres -d gymnova_db -f sql/11_credenciales_demostracion.sql

# Contar tablas
psql -U postgres -d gymnova_db -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';"

# Ver bitácora reciente
psql -U postgres -d gymnova_db -c "SELECT fecha_hora, modulo, accion_realizada, descripcion FROM bitacora ORDER BY id_bitacora DESC LIMIT 20;"
```

**Estado final:** proyecto compila, base tiene 37 tablas, 5 roles funcionan, prueba integral registra 56 eventos en bitácora sin errores.
