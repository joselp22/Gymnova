# GYMNOVA - entrega Java revisada

Proyecto compatible con NetBeans 24, JDK 17, Java Swing, Ant y PostgreSQL.

## Actualizacion funcional V6

- Alcance real por sesion: entrenador y nutricionista solo consultan y editan
  datos vinculados; cliente solo consulta su informacion; recepcion conserva
  la operacion administrativa autorizada.
- Los selectores de llaves foraneas muestran descripciones legibles y se
  filtran por rol. Ya no es necesario escribir identificadores manualmente.
- Las relaciones vuelven a validarse antes de guardar, modificar o desactivar.
- Los botones informativos de cada experiencia abren su gestion real.
- Reportes generan datos reales y habilitan vista previa, CSV e impresion.
- La cedula queda protegida durante toda modificacion.
- Se incluyeron pruebas para 58 controladores, alcances y catalogos por rol.

## Cobertura

- Login y sesion compartida con nombre de usuario y rol.
- Un `FrmPrincipal` que selecciona cinco formularios de inicio independientes:
  `PnlInicioAdministrador`, `PnlInicioRecepcionista`,
  `PnlInicioEntrenador`, `PnlInicioNutricionista` y `PnlInicioCliente`.
- Navegacion no administrativa orientada a tareas: el cliente consulta rutina,
  ejercicios, progreso, membresia, reservas y nutricion; el entrenador trabaja
  con clientes, planificacion y evaluaciones; el nutricionista con planes,
  alertas y seguimiento; recepcion con accesos, agenda, membresias y caja.
- Nueve composiciones visuales especializadas: calendario y ejercicios para
  Rutinas; mediciones para Salud; comidas y macronutrientes para Nutricion;
  agenda para Acceso; plan y beneficios para Membresias; fichas para Clientes;
  caja para Finanzas; graficas para Reportes; perfil y seguridad para
  Configuracion. No reutilizan la misma distribucion.
- Tres banners fotograficos propios en la paleta GYMNOVA para entrenamiento,
  nutricion y recepcion.
- Los CRUD no desaparecen: el Administrador entra directamente a la gestion y
  los otros roles abren el detalle desde la portada operativa cuando tienen el
  permiso correspondiente.
- Identidad visual y contenido operativo diferente para cada rol: paneles
  administrativos oscuros, paneles profesionales claros y portal del cliente.
- Dashboard con indicadores y actividad consultados desde PostgreSQL segun el
  rol autenticado.
- CRUD de Personas, Clientes y Empleados.
- Gestion de Usuarios, Roles, Permisos, Rol-Permiso y Bitacora.
- Gestion de Personal complementaria: entrenadores, nutricionistas,
  especialidades, certificaciones, provincias y asignaciones laborales.
- Submodulos de Membresias, Acceso, Rutinas, Salud, Nutricion, Finanzas e
  Inventario enlazados con sus controladores y DAO existentes.
- Reportes con consulta, exportacion CSV e impresion.
- Configuracion de cuenta y cambio de clave.
- Eliminacion definitiva visible solo para Administrador y protegida por las
  relaciones de PostgreSQL; los demas roles utilizan desactivacion logica.
- Cedula, identificadores y codigos automaticos no se modifican.

## Formularios Design

Las 23 vistas Swing conservan su archivo `.form` valido. El formulario
reutilizable `PnlEditorRegistro.form` contiene los campos visuales para las
altas y modificaciones de catalogos y movimientos. La logica permanece en
controladores y utilidades, sin JDBC dentro de las vistas.

## Verificacion realizada

- `Clean and Build`: exitoso.
- JAR generado: `dist/GYMNOVA_FINAL.jar`.
- 23 pares `.java`/`.form` y 23 XML validos.
- 62 modelos, 63 DAO y 67 controladores.
- Prueba de construccion: 14 paneles principales.
- Prueba de dashboard: 5 formularios de rol construidos y renderizados.
- Prueba de experiencias: 19 portadas operativas construidas y 15 capturas de
  revision, incluyendo todos los tipos de composicion especializada.

La base de datos no fue modificada durante esta fase. Los roles, permisos,
usuarios de prueba y datos maestros se completan en la siguiente fase SQL.

## Tema fotografico azul V4

- Los modulos operativos muestran una fotografia de gimnasio como fondo total.
- Se aplica una capa azul marino semitransparente para conservar la lectura.
- Tarjetas, bordes, indicadores, botones y estados usan azul electrico, cian,
  azul marino y blanco; se retiraron los acentos verdes del tema anterior.
- El fondo cambia segun el contexto: entrenamiento, nutricion o recepcion.
- Se conservaron sin cambios los modelos, DAO, controladores, permisos, sesion,
  navegacion y reglas de negocio.
