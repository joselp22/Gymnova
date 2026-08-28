package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.MedicionCorporal;

/**
 * DAO para la tabla medicion_corporal.
 *
 * @author Usuario
 */
public class MedicionCorporalDAO {

    public boolean guardar(
            MedicionCorporal medicionCorporal
    ) throws SQLException {

        String sql = "INSERT INTO medicion_corporal (peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm, brazo_cm, muslo_cm, observaciones, id_evaluacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    medicionCorporal.getPesoKg()
            );

            sentencia.setBigDecimal(
                    2,
                    medicionCorporal.getAlturaM()
            );

            sentencia.setBigDecimal(
                    3,
                    medicionCorporal.getPorcentajeGrasa()
            );

            sentencia.setBigDecimal(
                    4,
                    medicionCorporal.getCinturaCm()
            );

            sentencia.setBigDecimal(
                    5,
                    medicionCorporal.getPechoCm()
            );

            sentencia.setBigDecimal(
                    6,
                    medicionCorporal.getBrazoCm()
            );

            sentencia.setBigDecimal(
                    7,
                    medicionCorporal.getMusloCm()
            );

            sentencia.setString(
                    8,
                    medicionCorporal.getObservaciones()
            );

            sentencia.setLong(
                    9,
                    medicionCorporal.getIdEvaluacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            MedicionCorporal medicionCorporal
    ) throws SQLException {

        String sql = "UPDATE medicion_corporal SET peso_kg = ?, altura_m = ?, porcentaje_grasa = ?, cintura_cm = ?, pecho_cm = ?, brazo_cm = ?, muslo_cm = ?, observaciones = ?, id_evaluacion = ? WHERE id_medicion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    medicionCorporal.getPesoKg()
            );

            sentencia.setBigDecimal(
                    2,
                    medicionCorporal.getAlturaM()
            );

            sentencia.setBigDecimal(
                    3,
                    medicionCorporal.getPorcentajeGrasa()
            );

            sentencia.setBigDecimal(
                    4,
                    medicionCorporal.getCinturaCm()
            );

            sentencia.setBigDecimal(
                    5,
                    medicionCorporal.getPechoCm()
            );

            sentencia.setBigDecimal(
                    6,
                    medicionCorporal.getBrazoCm()
            );

            sentencia.setBigDecimal(
                    7,
                    medicionCorporal.getMusloCm()
            );

            sentencia.setString(
                    8,
                    medicionCorporal.getObservaciones()
            );

            sentencia.setLong(
                    9,
                    medicionCorporal.getIdEvaluacion()
            );

            sentencia.setLong(
                    10,
                    medicionCorporal.getIdMedicion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public MedicionCorporal buscar(
            Long idMedicion
    ) throws SQLException {

        String sql = "SELECT id_medicion, peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm, brazo_cm, muslo_cm, observaciones, id_evaluacion FROM medicion_corporal WHERE id_medicion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMedicion);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirMedicionCorporal(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<MedicionCorporal> listar(
            String criterio
    ) throws SQLException {

        List<MedicionCorporal> lista = new ArrayList<>();
        String sql = "SELECT id_medicion, peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm, brazo_cm, muslo_cm, observaciones, id_evaluacion FROM medicion_corporal WHERE observaciones ILIKE ? ORDER BY id_medicion";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMedicionCorporal(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idMedicion
    ) throws SQLException {

        String sql = "DELETE FROM medicion_corporal WHERE id_medicion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMedicion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<MedicionCorporal> listarPorIdEvaluacion(
            Long idEvaluacion
    ) throws SQLException {

        List<MedicionCorporal> lista = new ArrayList<>();
        String sql = "SELECT id_medicion, peso_kg, altura_m, porcentaje_grasa, cintura_cm, pecho_cm, brazo_cm, muslo_cm, observaciones, id_evaluacion FROM medicion_corporal WHERE id_evaluacion = ? ORDER BY id_medicion";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEvaluacion
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMedicionCorporal(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private MedicionCorporal convertirMedicionCorporal(
            ResultSet resultado
    ) throws SQLException {

        MedicionCorporal medicionCorporal = new MedicionCorporal();

        medicionCorporal.setIdMedicion(
                resultado.getLong("id_medicion")
        );

        medicionCorporal.setPesoKg(
                resultado.getBigDecimal("peso_kg")
        );

        medicionCorporal.setAlturaM(
                resultado.getBigDecimal("altura_m")
        );

        medicionCorporal.setPorcentajeGrasa(
                resultado.getBigDecimal("porcentaje_grasa")
        );

        medicionCorporal.setCinturaCm(
                resultado.getBigDecimal("cintura_cm")
        );

        medicionCorporal.setPechoCm(
                resultado.getBigDecimal("pecho_cm")
        );

        medicionCorporal.setBrazoCm(
                resultado.getBigDecimal("brazo_cm")
        );

        medicionCorporal.setMusloCm(
                resultado.getBigDecimal("muslo_cm")
        );

        medicionCorporal.setObservaciones(
                resultado.getString("observaciones")
        );

        medicionCorporal.setIdEvaluacion(
                resultado.getLong("id_evaluacion")
        );

        return medicionCorporal;
    }
}
