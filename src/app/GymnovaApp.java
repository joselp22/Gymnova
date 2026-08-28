/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package app;

import java.awt.EventQueue;
import javax.swing.UIManager;
import utilidades.VerificadorMembresias;
import vista.FrmLogin;
/**
 *
 * @author Usuario
 */
public class GymnovaApp {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {

        aplicarApariencia();

        // Barre membresias cuya fecha_fin ya expiro y las marca VENCIDA
        // antes de mostrar el login. Corre en el hilo de arranque a
        // proposito: la BD debe estar consistente cuando abre la UI.
        VerificadorMembresias.ejecutar();

        EventQueue.invokeLater(() -> {
            FrmLogin login = new FrmLogin();
            login.setVisible(true);
        });
    }

    private static void aplicarApariencia() {

        try {
            for (UIManager.LookAndFeelInfo informacion
                    : UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(
                        informacion.getName()
                )) {
                    UIManager.setLookAndFeel(
                            informacion.getClassName()
                    );
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "No se pudo aplicar la apariencia Nimbus: "
                    + e.getMessage()
            );
        }
    }
}
