# GYMNOVA - estado de la entrega candidata

Fecha de verificacion: 13 de agosto de 2026.

## Evidencia automatica superada

- Compilacion limpia de 244 clases Java y construccion del JAR.
- Conexion JDBC real con `gymnova_db`.
- 58 controladores CRUD consultados: 58 correctos, 0 errores.
- 14 vistas principales, 5 dashboards y 19 experiencias por rol construidas.
- Alcance real comprobado para Cliente, Entrenador y Nutricionista.
- Cero llaves foraneas con `ON DELETE CASCADE`.
- Cedula duplicada rechazada y eliminacion con historial protegida.
- Cero codigos invalidos `CL`, `MB`, `RS` y `PG` con cinco digitos.
- Cuatro facturas pagadas con saldo cero.
- Factura A4 consultada desde PostgreSQL, generada y renderizada como imagen.
- Consecutivo de factura transaccional probado (`FAC-00005`) y dato limpiado.
- GYMNOVA Pulse calculado para clientes reales, siempre entre 0 y 100 y con
  explicacion de sus factores.

## Prueba manual obligatoria antes de exponer

Ninguna prueba automatica puede garantizar al 100 % el comportamiento de
impresoras, selectores de archivos, resoluciones de pantalla ni todas las
secuencias humanas. Se debe completar una sesion de aceptacion siguiendo
`PRUEBAS_FUNCIONALES.md`, registrar resultado y tomar capturas.

## Orden para preparar los documentos

1. Completar y firmar la prueba manual de aceptacion.
2. Congelar la base y crear respaldo con `pg_dump`.
3. Manual de usuario por rol con capturas reales.
4. Manual tecnico: arquitectura MVC, PostgreSQL, seguridad y despliegue.
5. Diccionario de datos y modelo relacional definitivo.
6. Informe del proyecto en Word/PDF.
7. Presentacion y guion de demostracion.
8. Paquete final con JAR, biblioteca JDBC, SQL, manuales y respaldo.
