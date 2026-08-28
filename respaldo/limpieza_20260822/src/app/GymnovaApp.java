/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package app;

import java.awt.EventQueue;
import javax.swing.UIManager;
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
