package controlador;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import utilidades.Auditoria;

/**
 * Cierre transaccional del flujo de Recepcion:
 *   cliente -> membresia -> factura -> pago -> comprobante.
 *
 * Todas las operaciones criticas se ejecutan en una unica transaccion JDBC
 * para no dejar la base en un estado inconsistente. Ademas escriben en la
 * bitacora al concluir.
 */
public class FlujoRecepcionControlador {

    private static final BigDecimal IVA = new BigDecimal(
            System.getProperty("gymnova.iva", "15.00"));

    private String mensaje = "";

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Alta sin cobro deshabilitada. Toda membresia debe tener su movimiento
     * financiero asociado (factura + pago + comprobante).
     */
    @Deprecated
    public Long crearMembresia(Long idCliente, Long idTipoMembresia,
            LocalDate fechaInicio, String observaciones) {
        mensaje = "La creacion de membresias requiere registrar el cobro. "
                + "Use crearMembresiaConPago().";
        return null;
    }

    /**
     * Asigna una membresia y la cobra en una sola transaccion.
     * Si el cliente ya tiene una membresia vigente/pending, este metodo
     * rechaza el alta para evitar dos vigencias superpuestas; la UI debe usar
     * renovarMembresiaConPago().
     */
    public Long crearMembresiaConPago(Long idCliente, Long idTipoMembresia,
            LocalDate fechaInicio, String observaciones,
            Integer idMetodoPago, String referenciaTransaccion) {

        mensaje = "";
        if (idCliente == null || idTipoMembresia == null
                || fechaInicio == null || idMetodoPago == null) {
            mensaje = "Faltan datos del cobro (cliente, plan, fecha o metodo).";
            return null;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                // Serializa ventas/renovaciones del mismo cliente.
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM cliente WHERE id_persona = ? "
                        + "AND estado_cliente = TRUE FOR UPDATE")) {
                    s.setLong(1, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            mensaje = "El cliente no existe o esta inactivo.";
                            c.rollback();
                            return null;
                        }
                    }
                }

                try (PreparedStatement s = c.prepareStatement(
                        "SELECT numero_membresia, fecha_fin FROM membresia "
                        + "WHERE id_cliente = ? "
                        + "AND estado_membresia IN ('ACTIVA','CONGELADA','PENDIENTE') "
                        + "AND fecha_fin >= CURRENT_DATE "
                        + "ORDER BY fecha_fin DESC, id_membresia DESC LIMIT 1")) {
                    s.setLong(1, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            mensaje = "El cliente ya tiene una membresia vigente o renovada ("
                                    + r.getString("numero_membresia") + ") hasta "
                                    + r.getDate("fecha_fin")
                                    + ". Use la renovacion para conservar el historial y cobrar correctamente.";
                            c.rollback();
                            return null;
                        }
                    }
                }

                PlanCobro plan = cargarPlanCobro(c, idTipoMembresia);
                if (plan == null) {
                    c.rollback();
                    return null;
                }

                LocalDate hoy = LocalDate.now();
                LocalDate inicio = fechaInicio.isBefore(hoy) ? hoy : fechaInicio;
                LocalDate fin = calcularFechaFin(inicio, plan.duracionDias);
                String estado = inicio.isAfter(hoy) ? "PENDIENTE" : "ACTIVA";

                String numeroMembresia = siguienteNumero(c, "membresia",
                        "numero_membresia", "MB", 5);
                Long idMembresia = insertarMembresia(c, numeroMembresia,
                        inicio, fin, plan.precio, estado, observaciones,
                        idCliente, idTipoMembresia);

                CobroMembresia cobro = registrarCobroMembresia(c, idCliente,
                        numeroMembresia, plan.precio, idMetodoPago,
                        referenciaTransaccion);

                c.commit();
                mensaje = "Membresia " + numeroMembresia + " cobrada y "
                        + ("ACTIVA".equals(estado) ? "activada" : "programada")
                        + ". Vigencia: " + inicio + " a " + fin
                        + ". Factura " + cobro.numeroFactura
                        + ", pago " + cobro.codigoPago
                        + " por $" + cobro.total.toPlainString() + ".";
                Auditoria.exito("MEMBRESIAS", "CREAR_CON_PAGO", mensaje);
                return idMembresia;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo asignar y cobrar la membresia: "
                    + ex.getMessage();
            Auditoria.fallo("MEMBRESIAS", "CREAR_CON_PAGO",
                    mensaje, ex.getMessage());
            return null;
        }
    }

    /**
     * Renueva y cobra una membresia conservando todo el historial.
     * Si la membresia actual sigue vigente, la nueva comienza al dia siguiente
     * de su fecha_fin y queda PENDIENTE hasta ese dia. El cobro, la factura y
     * el comprobante se registran en el momento de la renovacion, por lo que
     * Finanzas refleja el ingreso inmediatamente.
     */
    public Long renovarMembresiaConPago(Long idMembresiaActual,
            Long idTipoMembresiaNuevo, LocalDate fechaInicioSolicitada,
            String observaciones, Integer idMetodoPago,
            String referenciaTransaccion) {

        mensaje = "";
        if (idMembresiaActual == null || idTipoMembresiaNuevo == null
                || idMetodoPago == null) {
            mensaje = "Faltan datos para renovar y cobrar la membresia.";
            return null;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                Long idCliente = null;
                LocalDate fechaFinActual = null;
                String estadoActual = null;

                try (PreparedStatement s = c.prepareStatement(
                        "SELECT id_cliente, fecha_fin, estado_membresia "
                        + "FROM membresia WHERE id_membresia = ? FOR UPDATE")) {
                    s.setLong(1, idMembresiaActual);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            idCliente = r.getLong("id_cliente");
                            fechaFinActual = r.getDate("fecha_fin").toLocalDate();
                            estadoActual = r.getString("estado_membresia");
                        }
                    }
                }
                if (idCliente == null) {
                    mensaje = "La membresia que intenta renovar no existe.";
                    c.rollback();
                    return null;
                }

                // Bloquea el cliente para evitar dos renovaciones simultaneas.
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM cliente WHERE id_persona = ? FOR UPDATE")) {
                    s.setLong(1, idCliente);
                    s.executeQuery().close();
                }

                PlanCobro plan = cargarPlanCobro(c, idTipoMembresiaNuevo);
                if (plan == null) {
                    c.rollback();
                    return null;
                }

                LocalDate hoy = LocalDate.now();
                boolean vigente = fechaFinActual != null
                        && !fechaFinActual.isBefore(hoy)
                        && ("ACTIVA".equals(estadoActual)
                            || "CONGELADA".equals(estadoActual)
                            || "PENDIENTE".equals(estadoActual));

                LocalDate inicioNueva;
                if (vigente) {
                    inicioNueva = fechaFinActual.plusDays(1);
                } else {
                    LocalDate solicitada = fechaInicioSolicitada == null
                            ? hoy : fechaInicioSolicitada;
                    inicioNueva = solicitada.isBefore(hoy) ? hoy : solicitada;
                }

                LocalDate fechaFinNueva = calcularFechaFin(
                        inicioNueva, plan.duracionDias);
                String estadoNueva = inicioNueva.isAfter(hoy)
                        ? "PENDIENTE" : "ACTIVA";

                if ("ACTIVA".equals(estadoNueva)) {
                    try (PreparedStatement s = c.prepareStatement(
                            "UPDATE membresia SET estado_membresia = 'VENCIDA' "
                            + "WHERE id_membresia = ? "
                            + "AND estado_membresia IN ('ACTIVA','CONGELADA','PENDIENTE')")) {
                        s.setLong(1, idMembresiaActual);
                        s.executeUpdate();
                    }
                }

                String numero = siguienteNumero(c, "membresia",
                        "numero_membresia", "MB", 5);
                Long idNueva = insertarMembresia(c, numero, inicioNueva,
                        fechaFinNueva, plan.precio, estadoNueva, observaciones,
                        idCliente, idTipoMembresiaNuevo);

                CobroMembresia cobro = registrarCobroMembresia(c, idCliente,
                        numero, plan.precio, idMetodoPago,
                        referenciaTransaccion);

                c.commit();
                mensaje = "Renovacion cobrada correctamente. Nueva membresia "
                        + numero + " (" + estadoNueva + ") del "
                        + inicioNueva + " al " + fechaFinNueva
                        + ". Factura " + cobro.numeroFactura
                        + ", pago " + cobro.codigoPago
                        + " por $" + cobro.total.toPlainString() + ".";
                Auditoria.exito("MEMBRESIAS", "RENOVAR_CON_PAGO", mensaje);
                return idNueva;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo renovar y cobrar la membresia: "
                    + ex.getMessage();
            Auditoria.fallo("MEMBRESIAS", "RENOVAR_CON_PAGO",
                    mensaje, ex.getMessage());
            return null;
        }
    }

    /**
     * Renovacion sin cobro deshabilitada para no extender vigencias sin un
     * movimiento financiero. Use renovarMembresiaConPago().
     */
    @Deprecated
    public Long renovarMembresia(Long idMembresiaActual,
            Long idTipoMembresiaNuevo, LocalDate fechaInicio,
            String observaciones) {
        mensaje = "La renovacion requiere registrar el cobro. "
                + "Use el modulo Membresias -> Cobrar / renovar membresia.";
        return null;
    }

    /**
     * Facturar una membresia: crea la factura con un renglon de detalle
     * (concepto MEMBRESIA) por el precio del tipo de membresia.
     * Devuelve id_factura o null.
     */
    public Long facturarMembresia(Long idCliente, Long idMembresia) {
        mensaje = "";
        if (idCliente == null || idMembresia == null) {
            mensaje = "Cliente o membresia no seleccionada.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                String numeroMembresia = null;
                BigDecimal costo = null;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT numero_membresia, costo_final FROM membresia "
                        + "WHERE id_membresia = ? AND id_cliente = ?")) {
                    s.setLong(1, idMembresia);
                    s.setLong(2, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            numeroMembresia = r.getString(1);
                            costo = r.getBigDecimal(2);
                        }
                    }
                }
                if (costo == null) {
                    mensaje = "La membresia no pertenece al cliente.";
                    c.rollback();
                    return null;
                }
                String numeroFactura = siguienteNumero(c, "factura",
                        "numero_factura", "FA", 5);
                BigDecimal impuesto = costo.multiply(IVA)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                Long idFactura;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO factura (numero_factura, fecha_emision, "
                        + "total_descuento, impuesto, estado_factura, id_cliente) "
                        + "VALUES (?, ?, 0, ?, 'PENDIENTE', ?) "
                        + "RETURNING id_factura")) {
                    s.setString(1, numeroFactura);
                    s.setDate(2, Date.valueOf(LocalDate.now()));
                    s.setBigDecimal(3, impuesto);
                    s.setLong(4, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idFactura = r.getLong(1);
                    }
                }
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO detalle_factura (tipo_concepto, "
                        + "codigo_referencia, cantidad, precio_unitario, "
                        + "porcentaje_impuesto, id_factura) "
                        + "VALUES ('MEMBRESIA', ?, 1, ?, ?, ?)")) {
                    s.setString(1, numeroMembresia);
                    s.setBigDecimal(2, costo);
                    s.setBigDecimal(3, IVA);
                    s.setLong(4, idFactura);
                    s.executeUpdate();
                }
                c.commit();
                Auditoria.exito("FINANZAS", "FACTURAR",
                        "Factura " + numeroFactura + " creada para cliente "
                        + idCliente + " por membresia " + numeroMembresia
                        + " (subtotal " + costo + ", IVA " + impuesto + ").");
                return idFactura;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo facturar la membresia: " + ex.getMessage();
            Auditoria.fallo("FINANZAS", "FACTURAR", mensaje, ex.getMessage());
            return null;
        }
    }

    /**
     * Registra un pago sobre una factura y actualiza el estado si queda saldada.
     * Valida que el pago no supere el saldo pendiente.
     * Devuelve id_pago o null.
     */
    public Long registrarPago(Long idFactura, BigDecimal monto,
            Integer idMetodoPago, String referencia) {
        mensaje = "";
        if (idFactura == null || monto == null || idMetodoPago == null) {
            mensaje = "Datos incompletos para el pago.";
            return null;
        }
        if (monto.signum() <= 0) {
            mensaje = "El monto del pago debe ser mayor que cero.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                BigDecimal totalFactura = totalFactura(c, idFactura);
                if (totalFactura == null) {
                    mensaje = "La factura no existe.";
                    c.rollback();
                    return null;
                }
                BigDecimal pagado = totalPagado(c, idFactura);
                BigDecimal saldo = totalFactura.subtract(pagado);
                if (monto.compareTo(saldo) > 0) {
                    mensaje = "El pago supera el saldo pendiente ("
                            + saldo + ").";
                    c.rollback();
                    return null;
                }
                String codigoPago = siguienteNumero(c, "pago",
                        "codigo_pago", "PG", 5);
                Long idPago;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO pago (monto_recibido, monto_pago, "
                        + "codigo_pago, fecha_hora_pago, estado_pago, "
                        + "referencia_transaccion, id_factura, id_metodo_pago) "
                        + "VALUES (?, ?, ?, ?, 'CONFIRMADO', ?, ?, ?) "
                        + "RETURNING id_pago")) {
                    s.setBigDecimal(1, monto);
                    s.setBigDecimal(2, monto);
                    s.setString(3, codigoPago);
                    s.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    s.setString(5, referencia);
                    s.setLong(6, idFactura);
                    s.setInt(7, idMetodoPago);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idPago = r.getLong(1);
                    }
                }
                BigDecimal nuevoPagado = pagado.add(monto);
                String nuevoEstado = nuevoPagado.compareTo(totalFactura) >= 0
                        ? "PAGADA" : "PENDIENTE";
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE factura SET estado_factura = ? "
                        + "WHERE id_factura = ?")) {
                    s.setString(1, nuevoEstado);
                    s.setLong(2, idFactura);
                    s.executeUpdate();
                }
                c.commit();
                Auditoria.exito("FINANZAS", "REGISTRAR_PAGO",
                        "Pago " + codigoPago + " por " + monto
                        + " sobre factura " + idFactura
                        + " (estado " + nuevoEstado + ").");
                return idPago;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar el pago: " + ex.getMessage();
            Auditoria.fallo("FINANZAS", "REGISTRAR_PAGO", mensaje,
                    ex.getMessage());
            return null;
        }
    }

    /**
     * Emite un comprobante para un pago. La ruta apunta al PDF que se
     * generara con DocumentoFacturaPDF (opcional, no obligatorio aqui).
     */
    public Long emitirComprobante(Long idPago) {
        mensaje = "";
        if (idPago == null) {
            mensaje = "Pago no seleccionado.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM pago WHERE id_pago = ? "
                        + "AND estado_pago = 'CONFIRMADO'")) {
                    s.setLong(1, idPago);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            mensaje = "El pago no existe o no esta confirmado.";
                            c.rollback();
                            return null;
                        }
                    }
                }
                String numero = siguienteNumero(c, "comprobante",
                        "numero_comprobante", "CP", 5);
                Long idComprobante;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO comprobante (numero_comprobante, "
                        + "tipo_comprobante, fecha_emision, formato_archivo, "
                        + "ruta_archivo, estado_comprobante, id_pago) "
                        + "VALUES (?, 'RECIBO', ?, 'PDF', ?, 'GENERADO', ?) "
                        + "RETURNING id_comprobante")) {
                    s.setString(1, numero);
                    s.setDate(2, Date.valueOf(LocalDate.now()));
                    s.setString(3, "comprobantes/" + numero + ".pdf");
                    s.setLong(4, idPago);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idComprobante = r.getLong(1);
                    }
                }
                c.commit();
                Auditoria.exito("FINANZAS", "EMITIR_COMPROBANTE",
                        "Comprobante " + numero + " emitido para pago "
                        + idPago + ".");
                return idComprobante;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo emitir el comprobante: " + ex.getMessage();
            Auditoria.fallo("FINANZAS", "EMITIR_COMPROBANTE", mensaje,
                    ex.getMessage());
            return null;
        }
    }

    /**
     * Congela una membresia activa por un rango de fechas.
     */
    public boolean congelarMembresia(Long idMembresia, LocalDate fechaInicio,
            LocalDate fechaFin, String motivo) {
        mensaje = "";
        if (idMembresia == null || fechaInicio == null || fechaFin == null) {
            mensaje = "Datos incompletos.";
            return false;
        }
        if (fechaInicio.isAfter(fechaFin)) {
            mensaje = "La fecha de inicio debe ser anterior o igual a la fin.";
            return false;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO congelacion (fecha_solicitud, "
                        + "fecha_inicio, fecha_fin, motivo, estado_congelacion, "
                        + "id_membresia) VALUES (?, ?, ?, ?, 'ACTIVA', ?)")) {
                    s.setDate(1, Date.valueOf(LocalDate.now()));
                    s.setDate(2, Date.valueOf(fechaInicio));
                    s.setDate(3, Date.valueOf(fechaFin));
                    s.setString(4, motivo);
                    s.setLong(5, idMembresia);
                    s.executeUpdate();
                }
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE membresia SET estado_membresia = 'CONGELADA' "
                        + "WHERE id_membresia = ?")) {
                    s.setLong(1, idMembresia);
                    s.executeUpdate();
                }
                c.commit();
                Auditoria.exito("MEMBRESIAS", "CONGELAR",
                        "Membresia " + idMembresia + " congelada "
                        + fechaInicio + " a " + fechaFin + ".");
                return true;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo congelar: " + ex.getMessage();
            Auditoria.fallo("MEMBRESIAS", "CONGELAR", mensaje, ex.getMessage());
            return false;
        }
    }

    /**
     * Reactiva una membresia congelada.
     */
    public boolean reactivarMembresia(Long idMembresia) {
        mensaje = "";
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE congelacion SET estado_congelacion = 'FINALIZADA' "
                        + "WHERE id_membresia = ? AND estado_congelacion = 'ACTIVA'")) {
                    s.setLong(1, idMembresia);
                    s.executeUpdate();
                }
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE membresia SET estado_membresia = 'ACTIVA' "
                        + "WHERE id_membresia = ?")) {
                    s.setLong(1, idMembresia);
                    s.executeUpdate();
                }
                c.commit();
                Auditoria.exito("MEMBRESIAS", "REACTIVAR",
                        "Membresia " + idMembresia + " reactivada.");
                return true;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo reactivar: " + ex.getMessage();
            Auditoria.fallo("MEMBRESIAS", "REACTIVAR", mensaje, ex.getMessage());
            return false;
        }
    }

    /**
     * Cancela una membresia (estado CANCELADA). No la borra fisicamente.
     */
    public boolean cancelarMembresia(Long idMembresia, String motivo) {
        mensaje = "";
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "UPDATE membresia SET estado_membresia = 'CANCELADA', "
                + "observaciones = COALESCE(observaciones, '') || ' | Cancelada: ' || ? "
                + "WHERE id_membresia = ?")) {
            s.setString(1, motivo == null ? "sin motivo" : motivo);
            s.setLong(2, idMembresia);
            int n = s.executeUpdate();
            Auditoria.exito("MEMBRESIAS", "CANCELAR",
                    "Membresia " + idMembresia + " cancelada. Motivo: "
                    + motivo);
            return n > 0;
        } catch (SQLException ex) {
            mensaje = "No se pudo cancelar: " + ex.getMessage();
            Auditoria.fallo("MEMBRESIAS", "CANCELAR", mensaje, ex.getMessage());
            return false;
        }
    }

    /**
     * Registra una reserva para una clase grupal.
     * El cupo y horario salen de clase_grupal; fecha_hora_reserva representa
     * el momento en que el cliente realizo la reserva.
     */
    public Long registrarReserva(Long idCliente, Integer idClase,
            LocalDateTime fechaHoraIgnorada, String observaciones) {

        mensaje = "";

        if (idCliente == null || idClase == null) {
            mensaje = "Cliente o clase no seleccionada.";
            return null;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);

            try {
                Integer cupo = null;
                LocalDateTime fechaClase = null;

                try (PreparedStatement s = c.prepareStatement(
                        "SELECT cupo_maximo, fecha_hora "
                        + "FROM clase_grupal "
                        + "WHERE id_clase = ? AND estado_clase = TRUE "
                        + "FOR UPDATE")) {
                    s.setInt(1, idClase);

                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            mensaje = "La clase no existe o esta inactiva.";
                            c.rollback();
                            return null;
                        }

                        cupo = (Integer) r.getObject("cupo_maximo");
                        Timestamp fecha = r.getTimestamp("fecha_hora");
                        fechaClase = fecha == null
                                ? null : fecha.toLocalDateTime();
                    }
                }

                if (cupo == null || cupo <= 0) {
                    mensaje = "La clase no tiene un cupo valido configurado.";
                    c.rollback();
                    return null;
                }

                if (fechaClase == null) {
                    mensaje = "La clase no tiene fecha y hora configuradas.";
                    c.rollback();
                    return null;
                }

                if (fechaClase.isBefore(LocalDateTime.now())) {
                    mensaje = "No se puede reservar una clase que ya inicio.";
                    c.rollback();
                    return null;
                }

                int reservadas;

                try (PreparedStatement s = c.prepareStatement(
                        "SELECT COUNT(*) FROM reserva "
                        + "WHERE id_clase = ? "
                        + "AND estado_reserva = 'ACTIVA'")) {
                    s.setInt(1, idClase);

                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        reservadas = r.getInt(1);
                    }
                }

                if (reservadas >= cupo) {
                    mensaje = "No hay cupo disponible para esta clase.";
                    c.rollback();
                    return null;
                }

                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM reserva "
                        + "WHERE id_cliente = ? AND id_clase = ? "
                        + "AND estado_reserva = 'ACTIVA'")) {
                    s.setLong(1, idCliente);
                    s.setInt(2, idClase);

                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            mensaje = "El cliente ya tiene una reserva activa para esta clase.";
                            c.rollback();
                            return null;
                        }
                    }
                }

                String codigo = siguienteNumero(c, "reserva",
                        "codigo_reserva", "RS", 5);

                Long idReserva;

                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO reserva (codigo_reserva, "
                        + "fecha_hora_reserva, estado_reserva, "
                        + "asistencia_confirmada, observaciones, id_cliente, "
                        + "id_clase) VALUES (?, CURRENT_TIMESTAMP, 'ACTIVA', "
                        + "FALSE, ?, ?, ?) RETURNING id_reserva")) {
                    s.setString(1, codigo);
                    s.setString(2, observaciones);
                    s.setLong(3, idCliente);
                    s.setInt(4, idClase);

                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idReserva = r.getLong(1);
                    }
                }

                c.commit();

                Auditoria.exito("ACCESO", "RESERVAR",
                        "Reserva " + codigo + " creada para cliente "
                        + idCliente + " en clase " + idClase
                        + " programada para " + fechaClase + ".");

                return idReserva;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }

        } catch (SQLException ex) {
            mensaje = "No se pudo reservar: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "RESERVAR",
                    mensaje, ex.getMessage());
            return null;
        }
    }

    /** Cancela una reserva activa y conserva el historial. */
    public boolean cancelarReserva(Long idReserva, String motivo) {

        mensaje = "";

        if (idReserva == null) {
            mensaje = "Seleccione una reserva.";
            return false;
        }

        if (motivo == null || motivo.isBlank()) {
            mensaje = "Ingrese el motivo de cancelacion.";
            return false;
        }

        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(
                     "UPDATE reserva "
                     + "SET estado_reserva = 'CANCELADA', "
                     + "fecha_hora_cancelacion = CURRENT_TIMESTAMP, "
                     + "motivo_cancelacion = ? "
                     + "WHERE id_reserva = ? "
                     + "AND estado_reserva = 'ACTIVA'")) {

            s.setString(1, motivo.trim());
            s.setLong(2, idReserva);

            boolean ok = s.executeUpdate() > 0;

            if (!ok) {
                mensaje = "La reserva no existe o ya no esta activa.";
                return false;
            }

            Auditoria.exito("ACCESO", "CANCELAR_RESERVA",
                    "Reserva " + idReserva + " cancelada.");
            return true;

        } catch (SQLException ex) {
            mensaje = "No se pudo cancelar la reserva: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "CANCELAR_RESERVA",
                    mensaje, ex.getMessage());
            return false;
        }
    }

    /** Registra un ingreso (asistencia con hora_entrada). */
    public Long registrarEntrada(Long idCliente, String metodoRegistro) {
        mensaje = "";
        if (idCliente == null) {
            mensaje = "Cliente no seleccionado.";
            return null;
        }
        LocalDateTime ahora = LocalDateTime.now();
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "INSERT INTO asistencia (fecha_asistencia, hora_entrada, "
                + "tipo_acceso, metodo_registro, estado_acceso, id_cliente) "
                + "VALUES (?, ?, 'MEMBRESIA', ?, 'REGISTRADO', ?) "
                + "RETURNING id_asistencia")) {
            s.setDate(1, Date.valueOf(ahora.toLocalDate()));
            s.setTime(2, java.sql.Time.valueOf(ahora.toLocalTime()));
            s.setString(3, metodoRegistro == null ? "MANUAL" : metodoRegistro);
            s.setLong(4, idCliente);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                Long id = r.getLong(1);
                Auditoria.exito("ACCESO", "ENTRADA",
                        "Entrada registrada para cliente " + idCliente);
                return id;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la entrada: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "ENTRADA", mensaje, ex.getMessage());
            return null;
        }
    }

    /** Registra la salida cerrando la ultima asistencia EN_CURSO del cliente. */
    public boolean registrarSalida(Long idCliente) {
        mensaje = "";
        LocalDateTime ahora = LocalDateTime.now();
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "UPDATE asistencia SET hora_salida = ?, "
                + "estado_acceso = 'COMPLETADO' "
                + "WHERE id_asistencia = (SELECT id_asistencia FROM asistencia "
                + "WHERE id_cliente = ? AND estado_acceso = 'REGISTRADO' "
                + "AND hora_salida IS NULL "
                + "ORDER BY fecha_asistencia DESC, hora_entrada DESC LIMIT 1)")) {
            s.setTime(1, java.sql.Time.valueOf(ahora.toLocalTime()));
            s.setLong(2, idCliente);
            int n = s.executeUpdate();
            if (n == 0) {
                mensaje = "El cliente no tiene una entrada en curso.";
                return false;
            }
            Auditoria.exito("ACCESO", "SALIDA",
                    "Salida registrada para cliente " + idCliente);
            return true;
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la salida: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "SALIDA", mensaje, ex.getMessage());
            return false;
        }
    }

    private static final class PlanCobro {
        final int duracionDias;
        final BigDecimal precio;
        PlanCobro(int duracionDias, BigDecimal precio) {
            this.duracionDias = duracionDias;
            this.precio = precio;
        }
    }

    private static final class CobroMembresia {
        final String numeroFactura;
        final String codigoPago;
        final String numeroComprobante;
        final BigDecimal total;
        CobroMembresia(String numeroFactura, String codigoPago,
                String numeroComprobante, BigDecimal total) {
            this.numeroFactura = numeroFactura;
            this.codigoPago = codigoPago;
            this.numeroComprobante = numeroComprobante;
            this.total = total;
        }
    }

    private PlanCobro cargarPlanCobro(Connection c, Long idTipo)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT duracion_dias, precio_base FROM tipo_membresia "
                + "WHERE id_tipo_membresia = ? AND estado_tipo = TRUE")) {
            s.setLong(1, idTipo);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    return new PlanCobro(r.getInt("duracion_dias"),
                            r.getBigDecimal("precio_base"));
                }
            }
        }
        mensaje = "El plan de membresia no existe o esta inactivo.";
        return null;
    }

    private LocalDate calcularFechaFin(LocalDate inicio, int duracionDias) {
        // duracion_dias es cantidad de dias de vigencia incluyendo el inicio.
        return inicio.plusDays(Math.max(0L, (long) duracionDias - 1L));
    }

    private Long insertarMembresia(Connection c, String numero,
            LocalDate inicio, LocalDate fin, BigDecimal precio, String estado,
            String observaciones, Long idCliente, Long idTipo)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO membresia (numero_membresia, fecha_inicio, "
                + "fecha_fin, costo_final, estado_membresia, observaciones, "
                + "id_cliente, id_tipo_membresia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_membresia")) {
            s.setString(1, numero);
            s.setDate(2, Date.valueOf(inicio));
            s.setDate(3, Date.valueOf(fin));
            s.setBigDecimal(4, precio);
            s.setString(5, estado);
            s.setString(6, observaciones);
            s.setLong(7, idCliente);
            s.setLong(8, idTipo);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return r.getLong(1);
            }
        }
    }

    private CobroMembresia registrarCobroMembresia(Connection c,
            Long idCliente, String numeroMembresia, BigDecimal precio,
            Integer idMetodoPago, String referenciaTransaccion)
            throws SQLException {

        // Verifica que el metodo de pago siga activo y la referencia cumpla.
        boolean requiereReferencia = false;
        try (PreparedStatement s = c.prepareStatement(
                "SELECT requiere_referencia FROM metodo_pago "
                + "WHERE id_metodo_pago = ? AND estado_metodo = TRUE")) {
            s.setInt(1, idMetodoPago);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    throw new SQLException("El metodo de pago no existe o esta inactivo.");
                }
                requiereReferencia = r.getBoolean(1);
            }
        }
        if (requiereReferencia && (referenciaTransaccion == null
                || referenciaTransaccion.isBlank())) {
            throw new SQLException("El metodo de pago requiere referencia de transaccion.");
        }

        String numeroFactura = siguienteNumero(c, "factura",
                "numero_factura", "FA", 5);
        BigDecimal impuesto = precio.multiply(IVA)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        Long idFactura;
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO factura (numero_factura, fecha_emision, "
                + "total_descuento, impuesto, estado_factura, id_cliente) "
                + "VALUES (?, ?, 0, ?, 'PAGADA', ?) RETURNING id_factura")) {
            s.setString(1, numeroFactura);
            s.setDate(2, Date.valueOf(LocalDate.now()));
            s.setBigDecimal(3, impuesto);
            s.setLong(4, idCliente);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                idFactura = r.getLong(1);
            }
        }

        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO detalle_factura (tipo_concepto, codigo_referencia, "
                + "cantidad, precio_unitario, porcentaje_impuesto, id_factura) "
                + "VALUES ('MEMBRESIA', ?, 1, ?, ?, ?)")) {
            s.setString(1, numeroMembresia);
            s.setBigDecimal(2, precio);
            s.setBigDecimal(3, IVA);
            s.setLong(4, idFactura);
            s.executeUpdate();
        }

        BigDecimal montoTotal = precio.add(impuesto)
                .setScale(2, RoundingMode.HALF_UP);
        String codigoPago = siguienteNumero(c, "pago", "codigo_pago", "PG", 5);
        Long idPago;
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO pago (monto_recibido, monto_pago, codigo_pago, "
                + "fecha_hora_pago, estado_pago, referencia_transaccion, "
                + "id_factura, id_metodo_pago) "
                + "VALUES (?, ?, ?, ?, 'CONFIRMADO', ?, ?, ?) RETURNING id_pago")) {
            s.setBigDecimal(1, montoTotal);
            s.setBigDecimal(2, montoTotal);
            s.setString(3, codigoPago);
            s.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            s.setString(5, referenciaTransaccion);
            s.setLong(6, idFactura);
            s.setInt(7, idMetodoPago);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                idPago = r.getLong(1);
            }
        }

        String numeroComprobante = siguienteNumero(c, "comprobante",
                "numero_comprobante", "CP", 5);
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO comprobante (numero_comprobante, tipo_comprobante, "
                + "fecha_emision, estado_comprobante, id_pago) "
                + "VALUES (?, 'RECIBO_MEMBRESIA', ?, 'GENERADO', ?)")) {
            s.setString(1, numeroComprobante);
            s.setDate(2, Date.valueOf(LocalDate.now()));
            s.setLong(3, idPago);
            s.executeUpdate();
        }

        return new CobroMembresia(numeroFactura, codigoPago,
                numeroComprobante, montoTotal);
    }

    // ================== helpers ==================

    private String siguienteNumero(Connection c, String tabla, String columna,
            String prefijo, int digitos) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(NULLIF(regexp_replace("
                + columna + ", '\\D', '', 'g'), '') AS BIGINT)), 0) + 1 "
                + "FROM " + tabla;
        try (PreparedStatement s = c.prepareStatement(sql);
                ResultSet r = s.executeQuery()) {
            r.next();
            long siguiente = r.getLong(1);
            return prefijo + String.format("%0" + digitos + "d", siguiente);
        }
    }

    private BigDecimal totalFactura(Connection c, Long idFactura)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(SUM(cantidad * precio_unitario "
                + "* (1 + porcentaje_impuesto / 100.0)), 0) "
                + "- COALESCE((SELECT total_descuento FROM factura "
                + "WHERE id_factura = ?), 0) "
                + "FROM detalle_factura WHERE id_factura = ?")) {
            s.setLong(1, idFactura);
            s.setLong(2, idFactura);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                BigDecimal total = r.getBigDecimal(1);
                return total == null ? BigDecimal.ZERO
                        : total.setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    private BigDecimal totalPagado(Connection c, Long idFactura)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                + "WHERE id_factura = ? AND estado_pago = 'CONFIRMADO'")) {
            s.setLong(1, idFactura);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                BigDecimal total = r.getBigDecimal(1);
                return total == null ? BigDecimal.ZERO
                        : total.setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    /** Total de la factura para consultas externas. */
    public BigDecimal calcularTotalFactura(Long idFactura) {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            return totalFactura(c, idFactura);
        } catch (SQLException ex) {
            return BigDecimal.ZERO;
        }
    }

    /** Total pagado sobre la factura. */
    public BigDecimal calcularSaldoPendiente(Long idFactura) {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            BigDecimal total = totalFactura(c, idFactura);
            BigDecimal pagado = totalPagado(c, idFactura);
            if (total == null) {
                return BigDecimal.ZERO;
            }
            return total.subtract(pagado);
        } catch (SQLException ex) {
            return BigDecimal.ZERO;
        }
    }
}
